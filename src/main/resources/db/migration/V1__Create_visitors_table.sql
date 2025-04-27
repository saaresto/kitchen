CREATE TABLE visitors (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    notes VARCHAR(1000)
);