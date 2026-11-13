package com.ecommerce.common.constants;

public final class PlatformConstants {

    private PlatformConstants() {
    }

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String CATALOG_EVENT_TOPIC = "product.catalog.events";
    public static final String PRODUCT_CREATED_EVENT = "PRODUCT_CREATED";
    public static final String PRODUCT_UPDATED_EVENT = "PRODUCT_UPDATED";
    public static final String PRODUCT_DELETED_EVENT = "PRODUCT_DELETED";
    public static final String PRODUCT_UNPUBLISHED_EVENT = "PRODUCT_UNPUBLISHED";
}
