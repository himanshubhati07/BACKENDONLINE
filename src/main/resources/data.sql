INSERT INTO product (name, price, available_quantity) VALUES
('Seed Keyboard', 49.99, 25),
('Seed Mouse', 19.99, 40),
('Seed Monitor', 199.99, 10)
ON CONFLICT DO NOTHING;
