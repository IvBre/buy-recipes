CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price_in_cents INT NOT NULL
);

CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS recipe_products (
    recipe_id BIGINT,
    product_id BIGINT,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    PRIMARY KEY (recipe_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_recipe_products_covering ON recipe_products(recipe_id) INCLUDE (product_id);

CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    total_in_cents INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT,
    product_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS cart_item_recipes (
    cart_item_entity_id BIGINT NOT NULL,
    recipe_ids BIGINT NOT NULL,
    PRIMARY KEY (cart_item_entity_id, recipe_ids),
    FOREIGN KEY (cart_item_entity_id) REFERENCES cart_items(id),
    FOREIGN KEY (recipe_ids) REFERENCES recipes(id)
);
