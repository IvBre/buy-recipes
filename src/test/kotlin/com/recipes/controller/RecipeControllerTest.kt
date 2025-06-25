package com.recipes.controller

import com.recipes.TestConfig
import com.recipes.domain.model.Recipe
import com.recipes.domain.model.Product
import com.recipes.domain.repository.ProductRepository
import com.recipes.domain.repository.RecipeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig::class)
class RecipeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var recipeRepository: RecipeRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_item_recipes, cart_items, carts, recipe_products, recipes, products CASCADE")

        testProduct1 = productRepository.save(Product("Product 1", 100))
        testProduct2 = productRepository.save(Product("Product 2", 200))
    }

    private lateinit var testProduct1: Product
    private lateinit var testProduct2: Product

    @Test
    fun `returns 200 and list of recipes when recipes exist`() {
        // given
        recipeRepository.save(Recipe("Recipe 1")).apply {
            addProduct(testProduct1)
        }.let { recipeRepository.save(it) }

        recipeRepository.save(Recipe("Recipe 2")).apply {
            addProduct(testProduct2)
        }.let { recipeRepository.save(it) }

        // when/then
        mockMvc.get("/recipes").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].name") { value("Recipe 1") }
            jsonPath("$[0].products[0].name") { value("Product 1") }
            jsonPath("$[0].products[0].priceInCents") { value(100) }
            jsonPath("$[1].name") { value("Recipe 2") }
        }
    }

    @Test
    fun `returns 200 and empty list when no recipes exist`() {
        mockMvc.get("/recipes").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `returns 200 and recipe when found`() {
        // given
        val recipe = recipeRepository.save(Recipe("Test Recipe")).apply {
            addProduct(testProduct1)
        }.let { recipeRepository.save(it) }

        // when/then
        mockMvc.get("/recipes/${recipe.id.value}").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Test Recipe") }
            jsonPath("$.products[0].name") { value("Product 1") }
            jsonPath("$.products[0].priceInCents") { value(100) }
        }
    }

    @Test
    fun `returns 404 when recipe not found`() {
        mockMvc.get("/recipes/1").andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("Recipe not found for id: 1.") }
            jsonPath("$.path") { value("/recipes/1") }
        }
    }
}