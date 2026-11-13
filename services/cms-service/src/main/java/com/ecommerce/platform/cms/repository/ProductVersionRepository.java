package com.ecommerce.platform.cms.repository;

import com.ecommerce.platform.cms.entity.ProductVersion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVersionRepository extends JpaRepository<ProductVersion, UUID> {

    List<ProductVersion> findAllByProduct_IdOrderByVersionDesc(UUID productId);
}
