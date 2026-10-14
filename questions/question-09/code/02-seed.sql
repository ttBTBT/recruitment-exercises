-- Run once after 01-schema.sql in a fresh exercise database.
START TRANSACTION;
INSERT INTO users (user_id, username) VALUES
    (1, 'Alice'), (2, 'Bob'), (3, 'Charlie'), (4, 'Diana');
INSERT INTO books (book_id, title, category, price, stock) VALUES
    (1, 'Java Basics', 'Tech', 80.00, 100),
    (2, 'Database Design', 'Tech', 120.00, 100),
    (3, 'Distributed Systems', 'Tech', 120.00, 100),
    (4, 'World History', 'History', 90.00, 100),
    (5, 'Ancient History', 'History', 60.00, 100),
    (6, 'Poems', 'Literature', 50.00, 100);
INSERT INTO orders (order_id, user_id, order_date, total_amount) VALUES
    (1, 1, '2026-09-01 10:00:00', 130.00),
    (2, 1, '2026-09-02 10:00:00', 200.00),
    (3, 2, '2026-09-03 10:00:00', 300.00),
    (4, 3, '2026-09-04 10:00:00', 120.00),
    (5, 4, '2026-09-05 10:00:00', 240.00);
INSERT INTO order_items (order_id, book_id, quantity, buy_price) VALUES
    (1, 1, 1, 70.00), (1, 5, 1, 60.00),
    (2, 2, 2, 100.00),
    (3, 1, 3, 70.00), (3, 4, 1, 90.00),
    (4, 5, 2, 60.00),
    (5, 3, 2, 120.00);
COMMIT;
