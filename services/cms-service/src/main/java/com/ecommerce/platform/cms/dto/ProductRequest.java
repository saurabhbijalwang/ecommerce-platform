package com.ecommerce.platform.cms.dto;

import com.ecommerce.platform.cms.entity.ProductMediaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductRequest(
    @NotBlank @Size(max = 80) String productCode,
    @NotBlank @Size(max = 200) String name,
    @Size(max = 10000) String description,
    @NotBlank @Size(max = 100) String sku,
    @NotNull UUID categoryId,
    @NotNull UUID brandId,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 15, fraction = 4)
    BigDecimal price,
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
    Map<String, Object> attributes,
    List<@NotNull @Valid VariantRequest> variants,
    List<@NotNull @Valid MediaRequest> media,
    @PositiveOrZero Long expectedVersion
) {

    public ProductRequest {
        attributes = attributes == null ? Map.of() : attributes;
        variants = variants == null ? List.of() : variants;
        media = media == null ? List.of() : media;
    }

    public record VariantRequest(
        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 150) String name,
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 15, fraction = 4)
        BigDecimal price,
        Map<String, Object> attributes
    ) {
        public VariantRequest {
            attributes = attributes == null ? Map.of() : attributes;
        }
    }

    public record MediaRequest(
        @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$") String mediaUrl,
        @Size(max = 500) String altText,
        @NotNull ProductMediaType mediaType,
        @PositiveOrZero int sortOrder
    ) {
    }
}
