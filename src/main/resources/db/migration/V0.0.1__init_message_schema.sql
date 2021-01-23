CREATE TABLE IF NOT EXISTS messages
(
    id             BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL,
    message        VARCHAR(255),
    error          BOOLEAN
);
