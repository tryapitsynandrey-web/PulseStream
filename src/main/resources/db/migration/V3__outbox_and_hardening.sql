-- Create Transactional Outbox Table
CREATE TABLE outbox_events (
    id VARCHAR(50) PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    correlation_id VARCHAR(100)
);

-- Index for polling pending outbox events ordered by creation time
CREATE INDEX idx_outbox_polling ON outbox_events(status, created_at);

-- Performance index for sumRevenue analytics query: orders(status, created_at)
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

-- Performance index for sumRefunds analytics query: refunds(status, created_at)
CREATE INDEX idx_refunds_status_created ON refunds(status, created_at);
