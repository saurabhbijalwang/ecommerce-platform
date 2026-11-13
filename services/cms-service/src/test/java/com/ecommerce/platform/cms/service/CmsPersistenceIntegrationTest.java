package com.ecommerce.platform.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.platform.cms.dto.BrandRequest;
import com.ecommerce.platform.cms.dto.CategoryRequest;
import com.ecommerce.platform.cms.dto.ProductRequest;
import com.ecommerce.platform.cms.exception.CatalogConflictException;
import com.ecommerce.platform.cms.repository.AuditHistoryRepository;
import com.ecommerce.platform.cms.repository.ProductVersionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class CmsPersistenceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("ecommerce_cms_db")
        .withUsername("postgres")
        .withPassword("postgres_test_password")
        .withInitScript("create-cms-test-role.sql");

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVersionRepository productVersions;

    @Autowired
    private AuditHistoryRepository auditHistory;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "cms_writer");
        registry.add("spring.datasource.password", () -> "cms_writer_test_password");
        registry.add("eureka.client.enabled", () -> false);
    }

    @Test
    void migratesAndPersistsCatalogChangesAndRejectsStaleVersions() {
        var category = categoryService.create(new CategoryRequest("Phones", "phones", null, true));
        var brand = brandService.create(new BrandRequest("Acme", "acme", null, true));
        ProductRequest createRequest = request(category.id(), brand.id(), null, "100.00");

        var created = productService.create(createRequest);
        ProductRequest updateRequest = request(category.id(), brand.id(), created.version(), "110.00");
        var updated = productService.update(created.id(), updateRequest);

        assertThat(updated.version()).isEqualTo(created.version() + 1);
        assertThat(updated.price()).isEqualByComparingTo("110.00");
        assertThat(updated.variants()).hasSize(1);
        assertThat(productVersions.findAllByProduct_IdOrderByVersionDesc(created.id())).hasSize(2);
        assertThat(auditHistory.findAllByProduct_IdOrderByCreatedAtDesc(created.id())).hasSize(2);
        assertThrows(
            CatalogConflictException.class,
            () -> productService.update(created.id(), updateRequest)
        );
    }

    private ProductRequest request(
        UUID categoryId,
        UUID brandId,
        Long expectedVersion,
        String price
    ) {
        return new ProductRequest(
            "PHONE-001",
            "Phone",
            "Catalog test product",
            "PHONE-001-SKU",
            categoryId,
            brandId,
            new BigDecimal(price),
            "USD",
            Map.of("color", "black"),
            List.of(new ProductRequest.VariantRequest(
                "PHONE-001-128",
                "128 GB",
                new BigDecimal(price),
                Map.of("storage", "128 GB")
            )),
            List.of(),
            expectedVersion
        );
    }
}
