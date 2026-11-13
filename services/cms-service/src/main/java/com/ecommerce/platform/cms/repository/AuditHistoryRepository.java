package com.ecommerce.platform.cms.repository;

import com.ecommerce.platform.cms.entity.AuditHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditHistoryRepository extends JpaRepository<AuditHistory, UUID> {

    List<AuditHistory> findAllByProduct_IdOrderByCreatedAtDesc(UUID productId);
}
