package com.ecommerce.platform.cms.dto;

import java.time.Instant;
import java.util.UUID;

public record BrandResponse(
    UUID id,
    String name,
    String slug,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {
}
