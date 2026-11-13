CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT uk_brands_slug UNIQUE (slug)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    sku VARCHAR(100) NOT NULL,
    category_id UUID NOT NULL REFERENCES categories (id),
    brand_id UUID NOT NULL REFERENCES brands (id),
    price NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}'::JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT uk_products_product_code UNIQUE (product_code),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price_positive CHECK (price > 0),
    CONSTRAINT ck_products_currency_uppercase CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_products_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED', 'ARCHIVED'))
);

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    variant_sku VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(19, 4),
    attributes JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT uk_product_variants_sku UNIQUE (variant_sku),
    CONSTRAINT ck_product_variants_price_positive CHECK (price IS NULL OR price > 0)
);

CREATE TABLE product_media (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    media_url VARCHAR(2048) NOT NULL,
    alt_text VARCHAR(500),
    media_type VARCHAR(20) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT ck_product_media_type CHECK (media_type IN ('IMAGE', 'VIDEO', 'DOCUMENT')),
    CONSTRAINT ck_product_media_sort_order CHECK (sort_order >= 0),
    CONSTRAINT uk_product_media_product_url UNIQUE (product_id, media_url)
);

CREATE TABLE product_versions (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id),
    version BIGINT NOT NULL,
    snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_product_versions_product_version UNIQUE (product_id, version)
);

CREATE TABLE audit_history (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id),
    product_version BIGINT NOT NULL,
    action VARCHAR(40) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_brand_id ON products (brand_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_product_variants_product_id ON product_variants (product_id);
CREATE INDEX idx_product_media_product_id ON product_media (product_id);
CREATE INDEX idx_product_versions_product_id ON product_versions (product_id);
CREATE INDEX idx_audit_history_product_created ON audit_history (product_id, created_at DESC);
