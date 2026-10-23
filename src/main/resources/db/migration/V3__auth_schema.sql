
CREATE TABLE auth_refresh_tokens (
    --Primary Key---------------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    --Entity Fields --------------------------------
    user_id UUID NOT NULL ,
    token_hash VARCHAR(64) NOT NULL ,
    expiry TIMESTAMPTZ NOT NULL ,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),
    device_info VARCHAR(500),

    --Base Entity----------------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_auth_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_hash ON auth_refresh_tokens (token_hash) WHERE is_revoked = FALSE;
CREATE INDEX idx_user_id ON auth_refresh_tokens (user_id) WHERE is_revoked = FALSE;
CREATE INDEX idx_refresh_token_expiry ON auth_refresh_tokens (expiry) WHERE is_revoked = FALSE;