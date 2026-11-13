package com.ecommerce.platform.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.platform.auth.entity.AuthUser;
import com.ecommerce.platform.auth.entity.UserRole;
import com.ecommerce.platform.auth.service.JwtTokenService;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void signsAnAccessTokenWithIssuerAudienceAndRoleClaims() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        JwtKeyConfiguration keyConfiguration = new JwtKeyConfiguration();
        var rsaKey = keyConfiguration.rsaKey(
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
        );
        JwtProperties properties = new JwtProperties(
            "https://auth.example.test",
            "ecommerce-platform",
            Duration.ofMinutes(15),
            Duration.ofDays(30)
        );
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtTokenService service = new JwtTokenService(
            keyConfiguration.jwtEncoder(rsaKey),
            rsaKey,
            properties,
            Clock.fixed(issuedAt, ZoneOffset.UTC)
        );
        AuthUser user = new AuthUser();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.test");
        user.setRoles(Set.of(UserRole.ADMIN, UserRole.CONTENT_MANAGER));

        String accessToken = service.createAccessToken(user);
        var decoded = new JwtSecurityConfiguration().jwtDecoder(rsaKey, properties).decode(accessToken);

        assertThat(decoded.getIssuer()).hasToString(properties.issuer());
        assertThat(decoded.getAudience()).containsExactly(properties.audience());
        assertThat(decoded.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decoded.getClaimAsStringList("roles"))
            .containsExactly("ADMIN", "CONTENT_MANAGER");
        assertThat(decoded.getExpiresAt()).isEqualTo(issuedAt.plus(properties.accessTokenTtl()));
    }
}
