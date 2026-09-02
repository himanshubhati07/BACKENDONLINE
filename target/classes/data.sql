INSERT IGNORE INTO users (first_name, last_name, email, phone, status, deleted, created_at, updated_at) VALUES
('Alice', 'Smith', 'alice.smith@example.com', '9999990001', 'ACTIVE', FALSE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Bob', 'Jones', 'bob.jones@example.com', '9999990002', 'INACTIVE', FALSE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Carol', 'Lee', 'carol.lee@example.com', '9999990003', 'BLOCKED', FALSE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
