INSERT INTO users (id, name, email, password, phone, role, created_at, updated_at) VALUES
 (1, 'Gym Admin', 'admin@gym.com', '$2b$12$L3QRTxvgptqWmtVxSREGwujaq0Kk2rga2xYx2cxa4CX0KaTjuBsI2', '9990000001', 'ADMIN', now(), now()),
 (2, 'Jane Trainer', 'jane.trainer@gym.com', '$2b$12$L3QRTxvgptqWmtVxSREGwujaq0Kk2rga2xYx2cxa4CX0KaTjuBsI2', '9990000002', 'TRAINER', now(), now()),
 (3, 'John Member', 'john.member@gym.com', '$2b$12$L3QRTxvgptqWmtVxSREGwujaq0Kk2rga2xYx2cxa4CX0KaTjuBsI2', '9990000003', 'MEMBER', now(), now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));

INSERT INTO trainers (id, name, email, phone, specialization, active, created_at) VALUES
 (1, 'Jane Trainer', 'jane.trainer@gym.com', '9990000002', 'Strength Training', true, now()),
 (2, 'Mike Fit', 'mike.fit@gym.com', '9990000004', 'Cardio', true, now()),
 (3, 'Alicia Yoga', 'alicia.yoga@gym.com', '9990000005', 'Yoga', true, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('trainers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM trainers));

INSERT INTO membership_plans (id, name, duration_days, price, description, active, created_at) VALUES
 (1, 'Monthly Basic', 30, 29.99, 'Basic gym access for one month', true, now()),
 (2, 'Quarterly Pro', 90, 79.99, 'Full gym access with classes for 3 months', true, now()),
 (3, 'Annual Elite', 365, 299.99, 'Full access, classes and personal training for 1 year', true, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('membership_plans_id_seq', (SELECT COALESCE(MAX(id), 1) FROM membership_plans));

INSERT INTO members (id, name, email, phone, user_id, trainer_id, membership_start_date, membership_expiry_date, status, created_at) VALUES
 (1, 'John Member', 'john.member@gym.com', '9990000003', 3, 1, CURRENT_DATE - 10, CURRENT_DATE + 20, 'ACTIVE', now()),
 (2, 'Sara Fitness', 'sara.fitness@gym.com', '9990000006', NULL, 2, CURRENT_DATE - 40, CURRENT_DATE - 10, 'EXPIRED', now()),
 (3, 'Tom Strong', 'tom.strong@gym.com', '9990000007', NULL, 1, CURRENT_DATE - 5, CURRENT_DATE + 25, 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('members_id_seq', (SELECT COALESCE(MAX(id), 1) FROM members));

INSERT INTO subscriptions (id, member_id, plan_id, start_date, end_date, status, created_at) VALUES
 (1, 1, 1, CURRENT_DATE - 10, CURRENT_DATE + 20, 'ACTIVE', now()),
 (2, 2, 1, CURRENT_DATE - 40, CURRENT_DATE - 10, 'EXPIRED', now()),
 (3, 3, 2, CURRENT_DATE - 5, CURRENT_DATE + 25, 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('subscriptions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM subscriptions));

INSERT INTO payments (id, member_id, subscription_id, amount, status, payment_reference, payment_date) VALUES
 (1, 1, 1, 29.99, 'PAID', 'PAY-SEED0000000001', now()),
 (2, 2, 2, 29.99, 'PAID', 'PAY-SEED0000000002', now()),
 (3, 3, 3, 79.99, 'PENDING', 'PAY-SEED0000000003', now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('payments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payments));
