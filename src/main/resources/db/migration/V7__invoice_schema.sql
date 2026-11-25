CREATE TABLE invoices (
                          id              UUID           NOT NULL DEFAULT uuid_generate_v4(),
                          client_id       UUID           NOT NULL,
                          project_id      UUID           NOT NULL,
                          cor_id          UUID,
                          code            VARCHAR(50)   NOT NULL,
                          amount          NUMERIC(19, 2) NOT NULL,
                          amount_paid     NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
                          invoice_date    DATE           NOT NULL DEFAULT CURRENT_DATE,
                          billing_status  VARCHAR(50)    NOT NULL,
                          invoice_type    VARCHAR(50)    NOT NULL,
                          status          VARCHAR(30)    NOT NULL,
                          created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          is_deleted      BOOLEAN        NOT NULL DEFAULT FALSE,
                          deleted_at      TIMESTAMPTZ,
                          version         BIGINT         NOT NULL DEFAULT 0,

                          CONSTRAINT uq_indexes_code UNIQUE (code),
    CONSTRAINT pk_invoices_id PRIMARY KEY (id),
                          CONSTRAINT chk_invoices_status CHECK ( status IN ('DRAFT', 'ISSUED', 'PAID', 'LOST', 'PARTIALLY_PAID') )
);

CREATE INDEX idx_invoices_code       ON invoices (code) WHERE is_deleted = FALSE;
CREATE INDEX idx_invoices_status     ON invoices (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_invoices_client_id  ON invoices (client_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_invoices_project_id ON invoices (project_id) WHERE is_deleted = FALSE;

CREATE TABLE payments (
                          id                 UUID           NOT NULL DEFAULT uuid_generate_v4(),
                          code               VARCHAR(50)   NOT NULL,
                          amount             NUMERIC(19, 2) NOT NULL,
                          unallocated_amount NUMERIC(19, 2),
                          date               DATE           NOT NULL,
                          meta_data          VARCHAR(255),
                          created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          is_deleted         BOOLEAN        NOT NULL DEFAULT FALSE,
                          deleted_at         TIMESTAMPTZ,
                          version            BIGINT         NOT NULL DEFAULT 0,

                          CONSTRAINT uq_payments_code UNIQUE (code),
    CONSTRAINT pk_payments_id PRIMARY KEY (id)
);

CREATE INDEX idx_payments_code ON payments (code) WHERE is_deleted = FALSE;

CREATE TABLE payment_allocations (
                                     id               UUID           NOT NULL DEFAULT uuid_generate_v4(),
                                     payment_id       UUID           NOT NULL,
                                     invoice_id       UUID           NOT NULL,
                                     allocated_amount NUMERIC(19, 2) NOT NULL,
                                     created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                                     updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                                     is_deleted       BOOLEAN        NOT NULL DEFAULT FALSE,
                                     deleted_at       TIMESTAMPTZ,
                                     version          BIGINT         NOT NULL DEFAULT 0,

                                     CONSTRAINT fk_payment_allocations_payment
                                         FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,
                                     CONSTRAINT fk_payment_allocations_invoice
                                         FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE CASCADE,
    CONSTRAINT pk_payment_allocations_id PRIMARY KEY (id)
);

CREATE INDEX idx_payment_allocations_payment_id ON payment_allocations (payment_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_payment_allocations_invoice_id ON payment_allocations (invoice_id) WHERE is_deleted = FALSE;

