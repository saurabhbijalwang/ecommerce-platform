package com.ecommerce.platform.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyFactory;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtKeyConfiguration {

    @Bean
    RSAKey rsaKey(
        @Value("${security.jwt.public-key-base64}") String publicKeyBase64,
        @Value("${security.jwt.private-key-base64}") String privateKeyBase64
    ) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64))
            );
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
            );
            if (publicKey.getModulus().bitLength() < 2048 || !keyPairMatches(publicKey, privateKey)) {
                throw new IllegalArgumentException("The JWT RSA key pair is too short or does not match.");
            }
            return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId(publicKey.getEncoded()))
                .build();
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException(
                "JWT RSA keys are missing or invalid; provide Base64-encoded X.509 public and PKCS#8 private keys.",
                exception
            );
        }
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> jwkSource = (selector, context) ->
            selector.select(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    private String keyId(byte[] publicKey) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(publicKey);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private boolean keyPairMatches(RSAPublicKey publicKey, RSAPrivateKey privateKey)
        throws GeneralSecurityException {
        byte[] challenge = "ecommerce-platform-jwt-key-check".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(challenge);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(challenge);
        return verifier.verify(signature);
    }
}
