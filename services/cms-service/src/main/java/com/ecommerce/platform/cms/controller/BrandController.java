package com.ecommerce.platform.cms.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.common.api.PagedResponse;
import com.ecommerce.platform.cms.dto.BrandRequest;
import com.ecommerce.platform.cms.dto.BrandResponse;
import com.ecommerce.platform.cms.service.BrandService;
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
@RequestMapping("/api/v1/admin/catalog/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<BrandResponse>> list(
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.success(PageResponses.from(brandService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(brandService.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/catalog/brands/" + response.id()))
            .body(ApiResponse.success("Brand created", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody BrandRequest request
    ) {
        return ApiResponse.success("Brand updated", brandService.update(id, request));
    }
}
