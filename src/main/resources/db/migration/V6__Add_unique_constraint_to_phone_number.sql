-- Step 1: Create a temporary table to store unique visitors
CREATE TEMPORARY TABLE unique_visitors AS
SELECT DISTINCT ON (phone_number) id, phone_number, name, notes
FROM visitors
ORDER BY phone_number, id;

-- Step 2: Delete all rows from the original table
DELETE FROM visitors;

-- Step 3: Insert the unique visitors back into the original table
INSERT INTO visitors (id, phone_number, name, notes)
SELECT id, phone_number, name, notes FROM unique_visitors;

-- Step 4: Add the unique constraint to the phone_number column
ALTER TABLE visitors ADD CONSTRAINT unique_phone_number UNIQUE (phone_number);