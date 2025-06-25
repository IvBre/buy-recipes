# Get all recipes
curl http://localhost:8080/recipes

# Get specific recipe
curl http://localhost:8080/recipes/1

# Create new cart
curl -X POST http://localhost:8080/cart \
  -H "Content-Type: application/json" \
  -d "{}"

# Add product to cart
curl -X POST http://localhost:8080/cart/1/products/1
