package com.ecommerce.platform.cms.repository;

import com.ecommerce.platform.cms.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, UUID id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);
}
