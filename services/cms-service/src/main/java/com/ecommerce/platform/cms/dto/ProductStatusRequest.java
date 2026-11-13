package com.ecommerce.platform.cms.dto;

import com.ecommerce.platform.cms.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(@NotNull ProductStatus status) {
}
