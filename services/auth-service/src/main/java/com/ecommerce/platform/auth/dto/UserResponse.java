package com.ecommerce.platform.auth.dto;

import com.ecommerce.platform.auth.entity.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Set<UserRole> roles,
    boolean enabled,
    Instant createdAt
) {
}
