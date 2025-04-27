CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    main_visitor_name VARCHAR(255) NOT NULL,
    main_visitor_phone VARCHAR(20) NOT NULL,
    visitors_count INT NOT NULL,
    date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    table_id INT NOT NULL,
    notes TEXT
);