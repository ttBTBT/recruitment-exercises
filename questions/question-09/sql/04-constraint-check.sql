-- Execute this file separately. The INSERT MUST fail (CHECK violation).
-- Do not use --force: a failure is the expected result, not a passed seed run.
INSERT INTO order_items (order_id, book_id, quantity, buy_price)
VALUES (1, 1, 0, 70.00);
