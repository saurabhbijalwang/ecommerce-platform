package com.ecommerce.platform.auth.service;

import com.ecommerce.platform.auth.config.JwtProperties;
import com.ecommerce.platform.auth.dto.CreateUserRequest;
import com.ecommerce.platform.auth.dto.LoginRequest;
import com.ecommerce.platform.auth.dto.RegisterRequest;
import com.ecommerce.platform.auth.dto.TokenResponse;
import com.ecommerce.platform.auth.dto.UserResponse;
import com.ecommerce.platform.auth.entity.AuthUser;
import com.ecommerce.platform.auth.entity.RefreshToken;
import com.ecommerce.platform.auth.entity.UserRole;
import com.ecommerce.platform.auth.exception.AuthConflictException;
import com.ecommerce.platform.auth.exception.AuthRequestException;
import com.ecommerce.platform.auth.exception.InvalidCredentialsException;
import com.ecommerce.platform.auth.exception.InvalidRefreshTokenException;
import com.ecommerce.platform.auth.repository.AuthUserRepository;
import com.ecommerce.platform.auth.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String dummyPasswordHash;

    public AuthService(
        AuthUserRepository users,
        RefreshTokenRepository refreshTokens,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        JwtProperties jwtProperties,
        Clock clock
    ) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        AuthUser user = createUser(request.email(), request.password(), Set.of(UserRole.CUSTOMER));
        return issueSession(user, UUID.randomUUID());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        AuthUser user = users.findByEmail(email).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            throw new InvalidCredentialsException();
        }
        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordMatches || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }
        return issueSession(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenResponse refresh(String rawRefreshToken) {
        String tokenHash = hashRefreshToken(rawRefreshToken);
        RefreshToken existing = refreshTokens.findByTokenHashForUpdate(tokenHash)
            .orElseThrow(InvalidRefreshTokenException::new);
        Instant now = clock.instant();
        if (existing.getRevokedAt() != null) {
            revokeFamily(existing.getFamilyId(), now);
            throw new InvalidRefreshTokenException();
        }
        if (!existing.getExpiresAt().isAfter(now) || !existing.getUser().isEnabled()) {
            existing.setRevokedAt(now);
            throw new InvalidRefreshTokenException();
        }

        existing.setRevokedAt(now);
        return issueSession(existing.getUser(), existing.getFamilyId());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokens.findByTokenHash(hashRefreshToken(rawRefreshToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(clock.instant());
            }
        });
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        AuthUser user = createUser(request.email(), request.password(), Set.of(request.role()));
        return toUserResponse(user);
    }

    @Transactional
    public void ensureBootstrapAdmin(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("AUTH_BOOTSTRAP_ADMIN_EMAIL must be a valid email address.");
        }
        AuthUser existing = users.findByEmail(normalizedEmail).orElse(null);
        if (existing != null) {
            if (!existing.getRoles().contains(UserRole.ADMIN)) {
                throw new IllegalStateException(
                    "The configured bootstrap account already exists without the ADMIN role."
                );
            }
            return;
        }
        createUser(normalizedEmail, password, Set.of(UserRole.ADMIN));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String subject) {
        UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCredentialsException();
        }
        AuthUser user = users.findById(userId)
            .orElseThrow(InvalidCredentialsException::new);
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }
        return toUserResponse(user);
    }

    private AuthUser createUser(String email, String rawPassword, Set<UserRole> roles) {
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);
        if (users.existsByEmail(normalizedEmail)) {
            throw new AuthConflictException("An account with this email already exists.");
        }
        Instant now = clock.instant();
        AuthUser user = new AuthUser();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(roles);
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return users.saveAndFlush(user);
    }

    private TokenResponse issueSession(AuthUser user, UUID familyId) {
        String rawRefreshToken = createRefreshTokenValue();
        Instant now = clock.instant();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashRefreshToken(rawRefreshToken));
        refreshToken.setFamilyId(familyId);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plus(jwtProperties.refreshTokenTtl()));
        refreshTokens.save(refreshToken);
        return new TokenResponse(
            "Bearer",
            jwtTokenService.createAccessToken(user),
            rawRefreshToken,
            jwtTokenService.accessTokenExpiresInSeconds(),
            toUserResponse(user)
        );
    }

    private void revokeFamily(UUID familyId, Instant now) {
        refreshTokens.findAllByFamilyIdAndRevokedAtIsNull(familyId)
            .forEach(token -> token.setRevokedAt(now));
    }

    private String createRefreshTokenValue() {
        byte[] value = new byte[32];
        secureRandom.nextBytes(value);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String hashRefreshToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for refresh token hashing.", exception);
        }
    }

    private void validatePassword(String password) {
        if (password == null
            || password.length() < 12
            || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthRequestException("Password must contain at least 12 characters and no more than 72 UTF-8 bytes.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toUserResponse(AuthUser user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            Set.copyOf(user.getRoles()),
            user.isEnabled(),
            user.getCreatedAt()
        );
    }
}
