# Get all recipes
curl http://localhost:8080/recipes

# Get specific recipe
curl http://localhost:8080/recipes/1

# Create a new cart
curl -X POST http://localhost:8080/carts

# Create a new cart with a recipe
curl -X POST http://localhost:8080/carts/with_recipe/1

# Add recipe products to the cart
curl -X POST http://localhost:8080/carts/1/add_recipe/1

# Remove recipe products from the cart
curl -X DELETE http://localhost:8080/carts/1/recipes/1
