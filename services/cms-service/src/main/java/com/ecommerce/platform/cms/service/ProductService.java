package com.ecommerce.platform.cms.service;

import com.ecommerce.platform.cms.dto.AuditHistoryResponse;
import com.ecommerce.platform.cms.dto.ProductRequest;
import com.ecommerce.platform.cms.dto.ProductResponse;
import com.ecommerce.platform.cms.dto.ProductVersionResponse;
import com.ecommerce.platform.cms.entity.AuditHistory;
import com.ecommerce.platform.cms.entity.Brand;
import com.ecommerce.platform.cms.entity.Category;
import com.ecommerce.platform.cms.entity.Product;
import com.ecommerce.platform.cms.entity.ProductMedia;
import com.ecommerce.platform.cms.entity.ProductStatus;
import com.ecommerce.platform.cms.entity.ProductVariant;
import com.ecommerce.platform.cms.entity.ProductVersion;
import com.ecommerce.platform.cms.exception.CatalogConflictException;
import com.ecommerce.platform.cms.exception.CatalogNotFoundException;
import com.ecommerce.platform.cms.exception.InvalidCatalogRequestException;
import com.ecommerce.platform.cms.repository.AuditHistoryRepository;
import com.ecommerce.platform.cms.repository.BrandRepository;
import com.ecommerce.platform.cms.repository.CategoryRepository;
import com.ecommerce.platform.cms.repository.ProductRepository;
import com.ecommerce.platform.cms.repository.ProductVariantRepository;
import com.ecommerce.platform.cms.repository.ProductVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductVariantRepository variants;
    private final ProductVersionRepository versions;
    private final AuditHistoryRepository auditHistory;
    private final AuditorAware<String> auditorAware;
    private final ObjectMapper objectMapper;

    public ProductService(
        ProductRepository products,
        CategoryRepository categories,
        BrandRepository brands,
        ProductVariantRepository variants,
        ProductVersionRepository versions,
        AuditHistoryRepository auditHistory,
        AuditorAware<String> auditorAware,
        ObjectMapper objectMapper
    ) {
        this.products = products;
        this.categories = categories;
        this.brands = brands;
        this.variants = variants;
        this.versions = versions;
        this.auditHistory = auditHistory;
        this.auditorAware = auditorAware;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(
        Pageable pageable,
        String query,
        ProductStatus status,
        UUID categoryId,
        UUID brandId
    ) {
        Pageable boundedPage = boundedPage(pageable);
        Specification<Product> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String search = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), search),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productCode")), search),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), search)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return products.findAll(specification, boundedPage).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return toResponse(requireProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.expectedVersion() != null) {
            throw new InvalidCatalogRequestException("expectedVersion must not be provided when creating a product.");
        }
        if (products.existsByProductCode(request.productCode().trim())) {
            throw new CatalogConflictException("A product with this productCode already exists.");
        }
        ensureRequestedSkusAreUnique(request, null);

        Product product = new Product();
        apply(product, request);
        Product saved = products.saveAndFlush(product);
        writeHistory(saved, "CREATED", Map.of("productCode", saved.getProductCode()));
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        if (request.expectedVersion() == null) {
            throw new InvalidCatalogRequestException("expectedVersion is required when updating a product.");
        }
        Product product = requireProduct(id);
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new CatalogConflictException("Archived products cannot be updated.");
        }
        if (!Objects.equals(product.getVersion(), request.expectedVersion())) {
            throw new CatalogConflictException("The product has changed. Reload it and retry with the latest version.");
        }
        if (products.existsByProductCodeAndIdNot(request.productCode().trim(), id)) {
            throw new CatalogConflictException("A product with this productCode already exists.");
        }
        ensureRequestedSkusAreUnique(request, id);

        apply(product, request);
        Product saved = products.saveAndFlush(product);
        writeHistory(saved, "UPDATED", Map.of("expectedVersion", request.expectedVersion()));
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse changeStatus(UUID id, ProductStatus targetStatus) {
        if (targetStatus == ProductStatus.ARCHIVED) {
            throw new InvalidCatalogRequestException("Use the archive operation to archive a product.");
        }
        Product product = requireProduct(id);
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new CatalogConflictException("Archived products cannot change status.");
        }
        if (product.getStatus() == targetStatus) {
            return toResponse(product);
        }
        ProductStatus previousStatus = product.getStatus();
        product.setStatus(targetStatus);
        Product saved = products.saveAndFlush(product);
        writeHistory(saved, "STATUS_CHANGED", Map.of(
            "from", previousStatus.name(),
            "to", targetStatus.name()
        ));
        return toResponse(saved);
    }

    @Transactional
    public void archive(UUID id) {
        Product product = requireProduct(id);
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            return;
        }
        ProductStatus previousStatus = product.getStatus();
        product.setStatus(ProductStatus.ARCHIVED);
        Product saved = products.saveAndFlush(product);
        writeHistory(saved, "ARCHIVED", Map.of("from", previousStatus.name()));
    }

    @Transactional(readOnly = true)
    public List<ProductVersionResponse> versions(UUID id) {
        requireProduct(id);
        return versions.findAllByProduct_IdOrderByVersionDesc(id).stream()
            .map(version -> new ProductVersionResponse(
                version.getId(),
                version.getVersion(),
                version.getSnapshot(),
                version.getCreatedAt()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditHistoryResponse> auditHistory(UUID id) {
        requireProduct(id);
        return auditHistory.findAllByProduct_IdOrderByCreatedAtDesc(id).stream()
            .map(entry -> new AuditHistoryResponse(
                entry.getId(),
                entry.getProductVersion(),
                entry.getAction(),
                entry.getActor(),
                entry.getDetails(),
                entry.getCreatedAt()
            ))
            .toList();
    }

    private void apply(Product product, ProductRequest request) {
        product.setProductCode(request.productCode().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setSku(request.sku().trim());
        product.setCategory(requireActiveCategory(request.categoryId()));
        product.setBrand(requireActiveBrand(request.brandId()));
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        product.setAttributes(new LinkedHashMap<>(request.attributes()));
        synchronizeVariants(product, request.variants());
        synchronizeMedia(product, request.media());
    }

    private void synchronizeVariants(Product product, List<ProductRequest.VariantRequest> requests) {
        Map<String, ProductVariant> existingBySku = new HashMap<>();
        for (ProductVariant variant : product.getVariants()) {
            existingBySku.put(variant.getSku().trim(), variant);
        }
        List<ProductVariant> replacements = new ArrayList<>();
        for (ProductRequest.VariantRequest request : requests) {
            String sku = request.sku().trim();
            ProductVariant variant = existingBySku.remove(sku);
            if (variant == null) {
                variant = new ProductVariant();
            }
            variant.setSku(sku);
            variant.setName(request.name().trim());
            variant.setPrice(request.price());
            variant.setAttributes(new LinkedHashMap<>(request.attributes()));
            replacements.add(variant);
        }
        product.replaceVariants(replacements);
    }

    private void synchronizeMedia(Product product, List<ProductRequest.MediaRequest> requests) {
        Map<String, ProductMedia> existingByUrl = new HashMap<>();
        for (ProductMedia item : product.getMedia()) {
            existingByUrl.put(item.getMediaUrl(), item);
        }
        List<ProductMedia> replacements = new ArrayList<>();
        for (ProductRequest.MediaRequest request : requests) {
            ProductMedia item = existingByUrl.remove(request.mediaUrl());
            if (item == null) {
                item = new ProductMedia();
            }
            item.setMediaUrl(request.mediaUrl());
            item.setAltText(request.altText());
            item.setMediaType(request.mediaType());
            item.setSortOrder(request.sortOrder());
            replacements.add(item);
        }
        product.replaceMedia(replacements);
    }

    private void ensureRequestedSkusAreUnique(ProductRequest request, UUID productId) {
        Set<String> requestedSkus = new HashSet<>();
        String productSku = request.sku().trim();
        if (!requestedSkus.add(productSku)) {
            throw new InvalidCatalogRequestException("Product and variant SKUs must be unique.");
        }
        boolean productSkuExists = productId == null
            ? products.existsBySku(productSku)
            : products.existsBySkuAndIdNot(productSku, productId);
        if (productSkuExists || variants.existsBySku(productSku)) {
            throw new CatalogConflictException("SKU " + productSku + " is already in use.");
        }
        Set<String> mediaUrls = new HashSet<>();
        for (ProductRequest.VariantRequest variant : request.variants()) {
            String variantSku = variant.sku().trim();
            if (!requestedSkus.add(variantSku)) {
                throw new InvalidCatalogRequestException("Product and variant SKUs must be unique.");
            }
            boolean usedByAnotherProduct = productId == null
                ? products.existsBySku(variantSku)
                : products.existsBySkuAndIdNot(variantSku, productId);
            boolean usedByAnotherVariant = productId == null
                ? variants.existsBySku(variantSku)
                : variants.existsBySkuAndProduct_IdNot(variantSku, productId);
            if (usedByAnotherProduct || usedByAnotherVariant) {
                throw new CatalogConflictException("SKU " + variantSku + " is already in use.");
            }
        }
        for (ProductRequest.MediaRequest mediaRequest : request.media()) {
            if (!mediaUrls.add(mediaRequest.mediaUrl())) {
                throw new InvalidCatalogRequestException("Media URLs must be unique within a product.");
            }
        }
    }

    private Category requireActiveCategory(UUID id) {
        Category category = categories.findById(id)
            .orElseThrow(() -> new CatalogNotFoundException("Category " + id + " was not found."));
        if (!category.isActive()) {
            throw new InvalidCatalogRequestException("The selected category is inactive.");
        }
        return category;
    }

    private Brand requireActiveBrand(UUID id) {
        Brand brand = brands.findById(id)
            .orElseThrow(() -> new CatalogNotFoundException("Brand " + id + " was not found."));
        if (!brand.isActive()) {
            throw new InvalidCatalogRequestException("The selected brand is inactive.");
        }
        return brand;
    }

    private Product requireProduct(UUID id) {
        return products.findById(id)
            .orElseThrow(() -> new CatalogNotFoundException("Product " + id + " was not found."));
    }

    private void writeHistory(Product product, String action, Map<String, Object> details) {
        long productVersion = product.getVersion();
        Instant recordedAt = Instant.now();
        ProductVersion version = new ProductVersion();
        version.setProduct(product);
        version.setVersion(productVersion);
        version.setSnapshot(objectMapper.convertValue(toResponse(product), new TypeReference<>() { }));
        version.setCreatedAt(recordedAt);
        versions.save(version);

        AuditHistory entry = new AuditHistory();
        entry.setProduct(product);
        entry.setProductVersion(productVersion);
        entry.setAction(action);
        entry.setActor(auditorAware.getCurrentAuditor()
            .orElseThrow(() -> new IllegalStateException("No CMS audit actor is available.")));
        entry.setDetails(details);
        entry.setCreatedAt(recordedAt);
        auditHistory.save(entry);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getProductCode(),
            product.getName(),
            product.getDescription(),
            product.getSku(),
            product.getCategory().getId(),
            product.getCategory().getName(),
            product.getBrand().getId(),
            product.getBrand().getName(),
            product.getPrice(),
            product.getCurrency(),
            product.getAttributes(),
            product.getStatus(),
            product.getVersion(),
            product.getVariants().stream()
                .map(variant -> new ProductResponse.VariantResponse(
                    variant.getId(),
                    variant.getSku(),
                    variant.getName(),
                    variant.getPrice(),
                    variant.getAttributes()
                ))
                .toList(),
            product.getMedia().stream()
                .map(item -> new ProductResponse.MediaResponse(
                    item.getId(),
                    item.getMediaUrl(),
                    item.getAltText(),
                    item.getMediaType(),
                    item.getSortOrder()
                ))
                .toList(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getCreatedBy(),
            product.getUpdatedBy()
        );
    }

    private Pageable boundedPage(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
            throw new InvalidCatalogRequestException("Pagination page must be non-negative and size must be positive.");
        }
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort();
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
