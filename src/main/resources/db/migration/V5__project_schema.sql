
CREATE TABLE projects (
    --Primary Key -----------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    --Entity Fields ---------------------------------
    code VARCHAR(50) NOT NULL ,
    name VARCHAR(255) NOT NULL ,
    status VARCHAR(30) NOT NULL ,
    location TEXT,
    client_id UUID NOT NULL ,
    client_contact_id UUID NOT NULL ,
    contract_amount DECIMAL(19, 2) NOT NULL ,
    invoiced_amount DECIMAL(19, 2) NOT NULL ,
    total_amount DECIMAL(19, 2) NOT NULL ,
    amount_received DECIMAL(19, 2) NOT NULL ,

    -- Base Entity Fields ------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT uq_projects_code UNIQUE (code),
    CONSTRAINT fk_projects_client_id FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_projects_client_contact_id FOREIGN KEY (client_contact_id) REFERENCES client_contacts(id),
    CONSTRAINT chk_projects_status CHECK ( status IN ('DRAFT', 'ONGOING', 'COMPLETED', 'LOST') )
);

CREATE INDEX idx_projects_code ON projects(code) WHERE is_deleted = FALSE;
CREATE INDEX idx_projects_client_id ON projects(client_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_projects_client_contact_id ON projects(client_contact_id) WHERE is_deleted = FALSE;


CREATE TABLE change_orders (
    --Primary Key -----------------------------
    id UUID NOT NULL DEFAULT uuid_generate_v4(),

    --Entity Fields ---------------------------------
    code VARCHAR(50) NOT NULL ,
    status VARCHAR(30) NOT NULL ,
    project_id UUID NOT NULL ,
    amount DECIMAL(19, 2) NOT NULL ,
    invoiced_amount DECIMAL(19, 2) NOT NULL ,
    received_amount DECIMAL(19, 2) NOT NULL ,

    -- Base Entity Fields ------------------------
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_change_orders PRIMARY KEY (id),
    CONSTRAINT uq_change_orders_code UNIQUE (code),
    CONSTRAINT fk_change_orders_project_id FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT chk_change_orders_status CHECK ( status IN ('DRAFT', 'ISSUED', 'PAID', 'LOST'))
);

CREATE INDEX idx_change_orders_code ON change_orders(code) WHERE is_deleted = FALSE;
CREATE INDEX idx_change_orders_project_id ON change_orders(project_id) WHERE is_deleted = FALSE;
