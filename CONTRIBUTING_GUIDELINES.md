# Buy Recipes API

A Spring Boot application that manages recipes and shopping carts. Users can browse recipes, create shopping carts, and add recipe ingredients to their carts.

## Features

- Browse available recipes
- Create shopping carts
- Add recipe ingredients to carts
- Remove recipes from carts
- See total cart price

## Prerequisites

- Java 24
- Docker (for running the database)
- Gradle 8.x

## Getting Started

1. Clone the repository:
    ```bash
    $ git clone 
    $ cd buy-recipes
    ```

2. Start the database:
    ```bash
    $ docker compose up -d
    ```

3. Run the application:
    ```bash
    $ ./gradlew bootRun --args='--spring.profiles.active=local'
    ```

**Note**: You can also start everything with docker by running:
```bash
$ docker compose -f docker-compose-all.yml up
```

The API will be available at `http://localhost:8080`

## Testing

### Automated Tests

The project includes comprehensive test coverage:

- Unit tests for the service layer
- Integration tests for repositories
- End-to-end tests for controllers

Run the tests with:
```bash
$ ./gradlew test
```

### Manual Testing

#### Postman Collection

A Postman collection is available in `dev-tools/postman/RecipesAPICollection.json`. Import this into Postman to test all available endpoints.

#### Curl Scripts

For command-line testing, use the bash script in `dev-tools/bash/`:
```bash
$ ./dev-tools/bash/runEndpoints.sh
```

## License

This project is licensed under the MIT License.
