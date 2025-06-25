package com.recipes

import com.recipes.controller.dto.CartDTO
import com.recipes.domain.model.Product
import com.recipes.domain.model.Recipe
import com.recipes.domain.repository.ProductRepository
import com.recipes.domain.repository.RecipeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CartEndToEndTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var recipeRepository: RecipeRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_item_recipes, cart_items, carts, recipe_products, recipes, products CASCADE")
    }

    @Test
    fun `complete cart workflow`() {
        // Create test data
        val product1 = productRepository.save(Product("Product 1", 100))
        val product2 = productRepository.save(Product("Product 2", 200))
        val product3 = productRepository.save(Product("Product 3", 400))
        
        val recipe1 = Recipe("Recipe 1").apply { 
            addProduct(product1)
            addProduct(product3)
        }.let { recipeRepository.save(it) }

        val recipe2 = Recipe("Recipe 2").apply {
            addProduct(product2)
            addProduct(product3)
        }.let { recipeRepository.save(it) }

        // Create a cart
        val cartResponse = webTestClient.post()
            .uri("/carts")
            .exchange()
            .expectStatus().isCreated
            .expectBody(CartDTO::class.java)
            .returnResult()
            .responseBody!!

        // Add the first recipe to the cart
        webTestClient.post()
            .uri("/carts/${cartResponse.id}/add_recipe/${recipe1.id.value}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].product.name").isEqualTo("Product 1")
            .jsonPath("$.totalInCents").isEqualTo(500)

        // Add the second recipe to the cart
        webTestClient.post()
            .uri("/carts/${cartResponse.id}/add_recipe/${recipe2.id.value}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
            .jsonPath("$.items.length()").isEqualTo(3)
            .jsonPath("$.totalInCents").isEqualTo(700)

        // Remove the first recipe
        webTestClient.delete()
            .uri("/carts/${cartResponse.id}/recipes/${recipe1.id.value}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].product.name").isEqualTo("Product 2")
            .jsonPath("$.totalInCents").isEqualTo(600)
    }
}