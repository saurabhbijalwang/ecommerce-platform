package com.ecommerce.platform.cms.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductVersionResponse(
    UUID id,
    long version,
    Map<String, Object> snapshot,
    Instant createdAt
) {
}
