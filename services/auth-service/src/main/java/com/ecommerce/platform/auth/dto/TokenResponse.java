package com.ecommerce.platform.auth.dto;

public record TokenResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserResponse user
) {
}
