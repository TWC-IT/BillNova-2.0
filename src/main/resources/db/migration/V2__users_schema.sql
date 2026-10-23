
CREATE TABLE users (
    --Primary key ---------------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    --Entity Fields ---------------------------------
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked_until TIMESTAMPTZ,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMPTZ,

    --Base Entity----------------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    --Constraints ---------------------------------
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK ( role IN (
        'ADMIN', 'USER'
        ) )
);

CREATE INDEX idx_users_username ON users (username) WHERE is_deleted = FALSE;

INSERT INTO users(
                  id,
                  first_name,
                  last_name,
                  username,
                  password_hash,
                  role
) VALUES (
          uuid_generate_v4(),
          'System',
          'Admin',
          'TWC-Admin',
          --Hash for Admin@123
          '$2a$10$3eEezsAtJHbbJLgynj4F2OY0EQHuR/yviq8yGCMa3EGSRva7F/zrq',
          'ADMIN'
         );