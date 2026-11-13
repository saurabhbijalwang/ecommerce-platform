package com.ecommerce.platform.cms.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.common.api.PagedResponse;
import com.ecommerce.platform.cms.dto.CategoryRequest;
import com.ecommerce.platform.cms.dto.CategoryResponse;
import com.ecommerce.platform.cms.service.CategoryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/catalog/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<CategoryResponse>> list(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.success(PageResponses.from(categoryService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/catalog/categories/" + response.id()))
            .body(ApiResponse.success("Category created", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody CategoryRequest request
    ) {
        return ApiResponse.success("Category updated", categoryService.update(id, request));
    }
}
