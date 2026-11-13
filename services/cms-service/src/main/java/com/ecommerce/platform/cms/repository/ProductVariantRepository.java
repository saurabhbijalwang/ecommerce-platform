package com.ecommerce.platform.cms.repository;

import com.ecommerce.platform.cms.entity.ProductVariant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndProduct_IdNot(String sku, UUID productId);
}
