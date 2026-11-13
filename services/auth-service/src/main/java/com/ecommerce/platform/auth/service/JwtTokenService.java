package com.ecommerce.platform.auth.service;

import com.ecommerce.platform.auth.config.JwtProperties;
import com.ecommerce.platform.auth.entity.AuthUser;
import com.nimbusds.jose.jwk.RSAKey;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final RSAKey rsaKey;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenService(
        JwtEncoder jwtEncoder,
        RSAKey rsaKey,
        JwtProperties properties,
        Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.rsaKey = rsaKey;
        this.properties = properties;
        this.clock = clock;
    }

    public String createAccessToken(AuthUser user) {
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(user.getId().toString())
            .audience(List.of(properties.audience()))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(properties.accessTokenTtl()))
            .id(UUID.randomUUID().toString())
            .claim("email", user.getEmail())
            .claim("roles", user.getRoles().stream().map(Enum::name).sorted().toList())
            .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
            .keyId(rsaKey.getKeyID())
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenExpiresInSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }
}
