package com.ecommerce.platform.cms.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditHistoryResponse(
    UUID id,
    long productVersion,
    String action,
    String actor,
    Map<String, Object> details,
    Instant createdAt
) {
}
