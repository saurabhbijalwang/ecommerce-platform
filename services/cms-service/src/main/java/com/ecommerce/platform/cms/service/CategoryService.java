package com.ecommerce.platform.cms.service;

import com.ecommerce.platform.cms.dto.CategoryRequest;
import com.ecommerce.platform.cms.dto.CategoryResponse;
import com.ecommerce.platform.cms.entity.Category;
import com.ecommerce.platform.cms.exception.CatalogConflictException;
import com.ecommerce.platform.cms.exception.CatalogNotFoundException;
import com.ecommerce.platform.cms.repository.CategoryRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(Pageable pageable) {
        return categories.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return toResponse(requireCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categories.existsBySlug(request.slug())) {
            throw new CatalogConflictException("A category with this slug already exists.");
        }
        Category category = new Category();
        apply(category, request);
        return toResponse(categories.saveAndFlush(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = requireCategory(id);
        if (categories.existsBySlugAndIdNot(request.slug(), id)) {
            throw new CatalogConflictException("A category with this slug already exists.");
        }
        apply(category, request);
        return toResponse(categories.saveAndFlush(category));
    }

    private Category requireCategory(UUID id) {
        return categories.findById(id)
            .orElseThrow(() -> new CatalogNotFoundException("Category " + id + " was not found."));
    }

    private void apply(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setActive(request.active());
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.getCreatedBy(),
            category.getUpdatedBy()
        );
    }
}
