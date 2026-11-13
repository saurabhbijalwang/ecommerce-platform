package com.ecommerce.platform.cms.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.common.api.PagedResponse;
import com.ecommerce.platform.cms.dto.AuditHistoryResponse;
import com.ecommerce.platform.cms.dto.ProductRequest;
import com.ecommerce.platform.cms.dto.ProductResponse;
import com.ecommerce.platform.cms.dto.ProductStatusRequest;
import com.ecommerce.platform.cms.dto.ProductVersionResponse;
import com.ecommerce.platform.cms.entity.ProductStatus;
import com.ecommerce.platform.cms.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/catalog/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<ProductResponse>> list(
        @PageableDefault(size = 20, sort = "name") Pageable pageable,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) ProductStatus status,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) UUID brandId
    ) {
        return ApiResponse.success(PageResponses.from(
            productService.list(pageable, query, status, categoryId, brandId)
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(productService.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/catalog/products/" + response.id()))
            .body(ApiResponse.success("Product created", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.success("Product updated", productService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProductResponse> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody ProductStatusRequest request
    ) {
        return ApiResponse.success(
            "Product status updated",
            productService.changeStatus(id, request.status())
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> archive(@PathVariable UUID id) {
        productService.archive(id);
        return ApiResponse.success("Product archived", null);
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<ProductVersionResponse>> versions(@PathVariable UUID id) {
        return ApiResponse.success(productService.versions(id));
    }

    @GetMapping("/{id}/audit")
    public ApiResponse<List<AuditHistoryResponse>> auditHistory(@PathVariable UUID id) {
        return ApiResponse.success(productService.auditHistory(id));
    }
}
