CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE check_result (
                              id               BIGINT GENERATED ALWAYS AS IDENTITY,
                              endpoint_id      BIGINT      NOT NULL,
                              status_code      INT,
                              response_time_ms BIGINT,
                              success          BOOLEAN     NOT NULL,
                              error            TEXT,
                              checked_at       TIMESTAMPTZ NOT NULL,
                              PRIMARY KEY (id, checked_at)          -- must include the time column
);

SELECT create_hypertable('check_result', 'checked_at',
                         chunk_time_interval => INTERVAL '1 day');

CREATE INDEX idx_check_result_endpoint_time
    ON check_result (endpoint_id, checked_at DESC);