-- Change table_id column from INT to VARCHAR
ALTER TABLE bookings 
    ALTER COLUMN table_id TYPE VARCHAR(50) USING table_id::VARCHAR;