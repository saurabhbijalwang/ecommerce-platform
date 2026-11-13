package com.ecommerce.platform.cms.dto;

import com.ecommerce.platform.cms.entity.ProductMediaType;
import com.ecommerce.platform.cms.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String productCode,
    String name,
    String description,
    String sku,
    UUID categoryId,
    String categoryName,
    UUID brandId,
    String brandName,
    BigDecimal price,
    String currency,
    Map<String, Object> attributes,
    ProductStatus status,
    long version,
    List<VariantResponse> variants,
    List<MediaResponse> media,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {

    public record VariantResponse(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        Map<String, Object> attributes
    ) {
    }

    public record MediaResponse(
        UUID id,
        String mediaUrl,
        String altText,
        ProductMediaType mediaType,
        int sortOrder
    ) {
    }
}
