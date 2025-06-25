package com.recipes.controller

import com.recipes.TestConfig
import com.recipes.domain.model.Cart
import com.recipes.domain.model.Product
import com.recipes.domain.model.Recipe
import com.recipes.domain.repository.CartRepository
import com.recipes.domain.repository.ProductRepository
import com.recipes.domain.repository.RecipeRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig::class)
class CartControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var recipeRepository: RecipeRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_item_recipes, cart_items, carts, recipe_products, recipes, products CASCADE")
    }

    @Nested
    inner class CreateCart {
        @Test
        fun `returns 201 and empty cart when successful`() {
            mockMvc.post("/carts").andExpect {
                status { isCreated() }
                jsonPath("$.items") { isEmpty() }
                jsonPath("$.totalInCents") { value(0) }
            }
        }
    }

    @Nested
    inner class CreateCartWithRecipe {
        @Test
        fun `returns 201 and cart with recipe items when successful`() {
            // given
            val product1 = productRepository.save(Product("Product 1", 100))
            val product2 = productRepository.save(Product("Product 2", 200))

            val recipe = Recipe("Test Recipe").apply {
                addProduct(product1)
                addProduct(product2)
            }.let { recipeRepository.save(it) }

            // when/then
            mockMvc.post("/carts/with_recipe/${recipe.id.value}").andExpect {
                status { isCreated() }
                jsonPath("$.items.length()") { value(2) }
                jsonPath("$.items[0].product.name") { value("Product 1") }
                jsonPath("$.items[1].product.name") { value("Product 2") }
                jsonPath("$.totalInCents") { value(300) }
            }
        }

        @Test
        fun `returns 404 when recipe not found`() {
            mockMvc.post("/carts/with_recipe/999").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Recipe not found for id: 999.") }
            }
        }
    }

    @Nested
    inner class GetCart {
        @Test
        fun `returns 200 and cart when found`() {
            // given
            val product = productRepository.save(Product("Test Product", 100))

            val recipe = Recipe("Test Recipe").apply {
                addProduct(product)
            }.let { recipeRepository.save(it) }

            val cart = cartRepository.save(Cart(createdAt = LocalDateTime.now())).apply {
                createItem(product, recipe.id)
            }.let { cartRepository.save(it) }

            // when/then
            mockMvc.get("/carts/${cart.id.value}").andExpect {
                status { isOk() }
                jsonPath("$.items.length()") { value(1) }
                jsonPath("$.items[0].product.name") { value("Test Product") }
                jsonPath("$.totalInCents") { value(100) }
            }
        }

        @Test
        fun `returns 404 when cart not found`() {
            mockMvc.get("/carts/999").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Cart entity not found for id: 999.") }
            }
        }
    }

    @Nested
    inner class AddRecipeToCart {
        @Test
        fun `returns 200 and updated cart when successful`() {
            // given
            val product1 = productRepository.save(Product("Product 1", 100))
            val product2 = productRepository.save(Product("Product 2", 200))

            val recipe = Recipe("Test Recipe").apply {
                addProduct(product1)
                addProduct(product2)
            }.let { recipeRepository.save(it) }

            val cart = cartRepository.save(Cart(createdAt = LocalDateTime.now()))

            // when/then
            mockMvc.post("/carts/${cart.id.value}/add_recipe/${recipe.id.value}").andExpect {
                status { isOk() }
                jsonPath("$.items.length()") { value(2) }
                jsonPath("$.items[0].product.name") { value("Product 1") }
                jsonPath("$.items[1].product.name") { value("Product 2") }
                jsonPath("$.totalInCents") { value(300) }
            }
        }

        @Test
        fun `returns 404 when cart not found`() {
            // given
            val product = productRepository.save(Product("Test Product", 100))

            val recipe = Recipe("Test Recipe").apply {
                addProduct(product)
            }.let { recipeRepository.save(it) }

            // when/then
            mockMvc.post("/carts/999/add_recipe/${recipe.id.value}").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Cart entity not found for id: 999.") }
            }
        }

        @Test
        fun `returns 404 when recipe not found`() {
            // given
            val cart = cartRepository.save(Cart(createdAt = LocalDateTime.now()))

            // when/then
            mockMvc.post("/carts/${cart.id.value}/add_recipe/999").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Recipe not found for id: 999.") }
            }
        }
    }

    @Nested
    inner class RemoveRecipeFromCart {
        @Test
        fun `returns 200 and updated cart when successful`() {
            // given
            val product = productRepository.save(Product("Test Product", 100))

            val recipe = Recipe("Test Recipe").apply {
                addProduct(product)
            }.let { recipeRepository.save(it) }

            val cart = cartRepository.save(Cart(createdAt = LocalDateTime.now())).apply {
                createItem(product, recipe.id)
            }.let { cartRepository.save(it) }

            // when/then
            mockMvc.delete("/carts/${cart.id.value}/recipes/${recipe.id.value}").andExpect {
                status { isOk() }
                jsonPath("$.items") { isEmpty() }
                jsonPath("$.totalInCents") { value(0) }
            }
        }

        @Test
        fun `returns 404 when cart not found`() {
            // given
            val product = productRepository.save(Product("Test Product", 100))
            val recipe = Recipe("Test Recipe").apply {
                addProduct(product)
            }.let { recipeRepository.save(it) }

            // when/then
            mockMvc.delete("/carts/999/recipes/${recipe.id.value}").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Cart entity not found for id: 999.") }
            }
        }

        @Test
        fun `returns 404 when recipe not found`() {
            // given
            val cart = cartRepository.save(Cart(createdAt = LocalDateTime.now()))

            // when/then
            mockMvc.delete("/carts/${cart.id.value}/recipes/999").andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Recipe not found for id: 999.") }
            }
        }
    }
}