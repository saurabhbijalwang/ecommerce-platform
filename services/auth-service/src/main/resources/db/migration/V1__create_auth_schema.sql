CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_auth_users_email UNIQUE (email)
);

CREATE TABLE auth_user_roles (
    user_id UUID NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT pk_auth_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT ck_auth_user_roles_role CHECK (
        role IN ('ADMIN', 'CONTENT_MANAGER', 'CUSTOMER', 'SERVICE')
    )
);

CREATE TABLE auth_refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT uk_auth_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_auth_refresh_tokens_user_id ON auth_refresh_tokens (user_id);
CREATE INDEX idx_auth_refresh_tokens_family_id ON auth_refresh_tokens (family_id);
CREATE INDEX idx_auth_refresh_tokens_expiration ON auth_refresh_tokens (expires_at);
