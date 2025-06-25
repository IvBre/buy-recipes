-- Products
INSERT INTO products (name, price_in_cents) VALUES
    ('Tomato', 99),
    ('Pasta', 199),
    ('Olive Oil', 599),
    ('Garlic', 49),
    ('Basil', 149),
    ('Ground Beef', 799),
    ('Onion', 79),
    ('Cheese', 399),
    ('Bell Pepper', 129),
    ('Mushrooms', 299);

-- Recipes
INSERT INTO recipes (name) VALUES
    ('Classic Spaghetti Bolognese'),
    ('Vegetarian Pasta'),
    ('Mushroom Pasta');

-- Recipe-Product relationships
-- Spaghetti Bolognese
INSERT INTO recipe_products (recipe_id, product_id) VALUES
    (1, 1), -- Tomato
    (1, 2), -- Pasta
    (1, 3), -- Olive Oil
    (1, 4), -- Garlic
    (1, 5), -- Basil
    (1, 6), -- Ground Beef
    (1, 7); -- Onion

-- Vegetarian Pasta
INSERT INTO recipe_products (recipe_id, product_id) VALUES
    (2, 1), -- Tomato
    (2, 2), -- Pasta
    (2, 3), -- Olive Oil
    (2, 4), -- Garlic
    (2, 5), -- Basil
    (2, 9); -- Bell Pepper

-- Mushroom Pasta
INSERT INTO recipe_products (recipe_id, product_id) VALUES
    (3, 2), -- Pasta
    (3, 3), -- Olive Oil
    (3, 4), -- Garlic
    (3, 8), -- Cheese
    (3, 10); -- Mushrooms
