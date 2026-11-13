package com.ecommerce.platform.cms.service;

import com.ecommerce.platform.cms.dto.BrandRequest;
import com.ecommerce.platform.cms.dto.BrandResponse;
import com.ecommerce.platform.cms.entity.Brand;
import com.ecommerce.platform.cms.exception.CatalogConflictException;
import com.ecommerce.platform.cms.exception.CatalogNotFoundException;
import com.ecommerce.platform.cms.repository.BrandRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brands;

    public BrandService(BrandRepository brands) {
        this.brands = brands;
    }

    @Transactional(readOnly = true)
    public Page<BrandResponse> list(Pageable pageable) {
        return brands.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BrandResponse get(UUID id) {
        return toResponse(requireBrand(id));
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brands.existsBySlug(request.slug())) {
            throw new CatalogConflictException("A brand with this slug already exists.");
        }
        Brand brand = new Brand();
        apply(brand, request);
        return toResponse(brands.saveAndFlush(brand));
    }

    @Transactional
    public BrandResponse update(UUID id, BrandRequest request) {
        Brand brand = requireBrand(id);
        if (brands.existsBySlugAndIdNot(request.slug(), id)) {
            throw new CatalogConflictException("A brand with this slug already exists.");
        }
        apply(brand, request);
        return toResponse(brands.saveAndFlush(brand));
    }

    private Brand requireBrand(UUID id) {
        return brands.findById(id)
            .orElseThrow(() -> new CatalogNotFoundException("Brand " + id + " was not found."));
    }

    private void apply(Brand brand, BrandRequest request) {
        brand.setName(request.name().trim());
        brand.setSlug(request.slug());
        brand.setDescription(request.description());
        brand.setActive(request.active());
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
            brand.getId(),
            brand.getName(),
            brand.getSlug(),
            brand.getDescription(),
            brand.isActive(),
            brand.getCreatedAt(),
            brand.getUpdatedAt(),
            brand.getCreatedBy(),
            brand.getUpdatedBy()
        );
    }
}
