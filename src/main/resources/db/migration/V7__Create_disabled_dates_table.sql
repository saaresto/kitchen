CREATE TABLE disabled_dates (
    id UUID PRIMARY KEY,
    date DATE NOT NULL,
    start_time TIME WITHOUT TIME ZONE,
    end_time TIME WITHOUT TIME ZONE,
    description TEXT,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATE NOT NULL
);