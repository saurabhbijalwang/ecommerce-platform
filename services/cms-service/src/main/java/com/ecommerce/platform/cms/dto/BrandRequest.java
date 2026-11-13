package com.ecommerce.platform.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BrandRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 180)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
    @Size(max = 5000) String description,
    @NotNull Boolean active
) {
}
