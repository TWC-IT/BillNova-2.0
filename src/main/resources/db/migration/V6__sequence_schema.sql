CREATE TABLE sequences (
                           sequence_type VARCHAR(50) NOT NULL,
                           sequence_year INTEGER NOT NULL,
                           last_sequence BIGINT NOT NULL,

                           PRIMARY KEY (sequence_type, sequence_year)
);

CREATE TABLE change_order_sequences(
    project_id UUID NOT NULL ,
    last_sequence INTEGER NOT NULL DEFAULT 0,

    PRIMARY KEY (project_id)
)