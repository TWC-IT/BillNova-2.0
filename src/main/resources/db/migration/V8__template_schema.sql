CREATE TABLE templates (
                           id         UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name       VARCHAR(255),
                           content    TEXT         NOT NULL,
                           is_default BOOLEAN      NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
                           deleted_at TIMESTAMPTZ,
                           version    BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_templates_is_deleted ON templates (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_templates_name       ON templates (name) WHERE is_deleted = FALSE;