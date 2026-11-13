package com.ecommerce.platform.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.GeneralSecurityException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtKeyConfigurationTest {

    @Test
    void acceptsMatchingRsaKeyPairAndRejectsMismatchedKeys() throws Exception {
        KeyPair signingPair = generateKeyPair();
        KeyPair unrelatedPair = generateKeyPair();
        JwtKeyConfiguration configuration = new JwtKeyConfiguration();

        var rsaKey = configuration.rsaKey(
            encode(signingPair.getPublic().getEncoded()),
            encode(signingPair.getPrivate().getEncoded())
        );

        assertThat(rsaKey.getKeyID()).isNotBlank();
        assertThrows(
            IllegalStateException.class,
            () -> configuration.rsaKey(
                encode(signingPair.getPublic().getEncoded()),
                encode(unrelatedPair.getPrivate().getEncoded())
            )
        );
    }

    private String encode(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    private KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
}
