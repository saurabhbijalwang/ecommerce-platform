package com.ecommerce.platform.cms.controller;

import com.ecommerce.common.api.PagedResponse;
import org.springframework.data.domain.Page;

final class PageResponses {

    private PageResponses() {
    }

    static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
