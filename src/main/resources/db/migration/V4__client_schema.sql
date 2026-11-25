CREATE TABLE clients (
    --Primary Key -----------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    -- Base Entity Fields ------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    --Entity Fields ---------------------------------
    name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL ,
    billing_address TEXT,

    CONSTRAINT pk_clients PRIMARY KEY (id),
    CONSTRAINT uq_clients_email UNIQUE (email)
);

CREATE INDEX idx_clients_email ON clients (email) WHERE is_deleted = FALSE;

CREATE TABLE client_contacts(
    --Primary Key -----------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    -- Base Entity Fields ------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    --Entity Fields ---------------------------------
    name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL ,
    client_id UUID NOT NULL ,

    CONSTRAINT pk_client_contacts PRIMARY KEY (id),
    CONSTRAINT uq_client_contacts_email UNIQUE (email),
    CONSTRAINT fk_client_contacts_client_id FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE INDEX idx_client_contacts_email ON client_contacts (email) WHERE is_deleted = FALSE;
CREATE INDEX idx_client_contacts_client_id ON client_contacts(client_id) WHERE is_deleted = FALSE;