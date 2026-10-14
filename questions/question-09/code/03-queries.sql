-- Task 2: quantity counts copies, not distinct titles or order rows.
-- Use the historical purchase price, never the current catalog price.
SELECT u.username,
       SUM(i.quantity) AS total_tech_books,
       SUM(i.quantity * i.buy_price) AS total_spent
FROM users u
JOIN orders o ON o.user_id = u.user_id
JOIN order_items i ON i.order_id = o.order_id
JOIN books b ON b.book_id = i.book_id
WHERE b.category = 'Tech'
GROUP BY u.user_id, u.username
HAVING SUM(i.quantity) >= 3
ORDER BY u.username;

-- Task 3: retain every book tied for the maximum in its category.
SELECT b.category, b.title, b.price
FROM books b
JOIN (
    SELECT category, MAX(price) AS max_price
    FROM books
    GROUP BY category
) highest ON highest.category = b.category AND highest.max_price = b.price
ORDER BY b.category, b.title;
