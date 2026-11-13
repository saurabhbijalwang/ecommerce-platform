package com.ecommerce.platform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.platform.auth.dto.RegisterRequest;
import com.ecommerce.platform.auth.entity.UserRole;
import com.ecommerce.platform.auth.exception.InvalidRefreshTokenException;
import com.ecommerce.platform.auth.repository.AuthUserRepository;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.GeneralSecurityException;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AuthPersistenceIntegrationTest {

    private static final KeyPair KEY_PAIR = generateKeyPair();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("ecommerce_auth_db")
        .withUsername("postgres")
        .withPassword("postgres_test_password")
        .withInitScript("create-auth-test-role.sql");

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthUserRepository users;

    @Autowired
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "auth_user");
        registry.add("spring.datasource.password", () -> "auth_user_test_password");
        registry.add("security.jwt.public-key-base64", () ->
            Base64.getEncoder().encodeToString(KEY_PAIR.getPublic().getEncoded()));
        registry.add("security.jwt.private-key-base64", () ->
            Base64.getEncoder().encodeToString(KEY_PAIR.getPrivate().getEncoded()));
        registry.add("eureka.client.enabled", () -> false);
    }

    @Test
    void issuesSignedTokensAndRevokesRefreshFamilyWhenTokenIsReused() {
        String password = "StrongPassphrase!2026";
        var firstSession = authService.register(new RegisterRequest("customer@example.test", password));

        assertThat(firstSession.user().roles()).contains(UserRole.CUSTOMER);
        assertThat(users.findByEmail("customer@example.test").orElseThrow().getPasswordHash())
            .isNotEqualTo(password);
        assertThat(jwtDecoder.decode(firstSession.accessToken()).getSubject())
            .isEqualTo(firstSession.user().id().toString());

        var rotatedSession = authService.refresh(firstSession.refreshToken());
        assertThat(rotatedSession.refreshToken()).isNotEqualTo(firstSession.refreshToken());

        assertThrows(
            InvalidRefreshTokenException.class,
            () -> authService.refresh(firstSession.refreshToken())
        );
        assertThrows(
            InvalidRefreshTokenException.class,
            () -> authService.refresh(rotatedSession.refreshToken())
        );
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
