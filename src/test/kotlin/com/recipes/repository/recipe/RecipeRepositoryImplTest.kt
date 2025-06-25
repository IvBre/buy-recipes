package com.recipes.repository.recipe

import com.recipes.domain.model.DomainException
import com.recipes.domain.model.Recipe
import com.recipes.domain.model.RecipeId
import com.recipes.domain.model.TestEntityFactory.createProductWithId
import com.recipes.repository.product.ProductEntity
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DataIntegrityViolationException

@ExtendWith(MockKExtension::class)
class RecipeRepositoryImplTest {
    private val jpaRepository: JpaRecipeRepository = mockk()
    private val recipeRepository = RecipeRepositoryImpl(jpaRepository)

    @Test
    fun `findById returns null when recipe not found`() {
        // given
        val recipeId = RecipeId(1)
        every { jpaRepository.findById(recipeId.value) } returns Optional.empty()

        // when
        val result = recipeRepository.findById(recipeId)

        // then
        assertNull(result)
        verify { jpaRepository.findById(recipeId.value) }
    }

    @Test
    fun `findById returns recipe with products when found`() {
        // given
        val recipeId = RecipeId(1)
        val productEntity = ProductEntity(id = 1, name = "Test Product", priceInCents = 100)
        val recipeEntity = RecipeEntity(
            id = recipeId.value,
            name = "Test Recipe",
            products = mutableListOf(productEntity)
        )

        every { jpaRepository.findById(recipeId.value) } returns Optional.of(recipeEntity)

        // when
        val result = recipeRepository.findById(recipeId)

        // then
        assertNotNull(result)
        assertEquals(recipeId, result.id)
        assertEquals("Test Recipe", result.name)
        assertEquals(1, result.products.size)
        assertEquals("Test Product", result.products.first().name)
        verify { jpaRepository.findById(recipeId.value) }
    }

    @Test
    fun `findAll returns empty list when no recipes exist`() {
        // given
        every { jpaRepository.findAll() } returns emptyList()

        // when
        val result = recipeRepository.findAll()

        // then
        assertTrue(result.isEmpty())
        verify { jpaRepository.findAll() }
    }

    @Test
    fun `findAll returns list of recipes with their products`() {
        // given
        val productEntity = ProductEntity(id = 1, name = "Test Product", priceInCents = 100)
        val recipes = listOf(
            RecipeEntity(id = 1, name = "Recipe 1", products = mutableListOf(productEntity)),
            RecipeEntity(id = 2, name = "Recipe 2", products = mutableListOf(productEntity))
        )
        every { jpaRepository.findAll() } returns recipes

        // when
        val result = recipeRepository.findAll()

        // then
        assertEquals(2, result.size)
        assertEquals("Recipe 1", result[0].name)
        assertEquals("Recipe 2", result[1].name)
        result.forEach { recipe ->
            assertEquals(1, recipe.products.size)
            assertEquals("Test Product", recipe.products.first().name)
        }
        verify { jpaRepository.findAll() }
    }

    @Test
    fun `save creates new recipe successfully`() {
        // given
        val product = createProductWithId(1, "Test Product", 100)
        val recipe = Recipe("New Recipe").apply {
            addProduct(product)
        }

        val productEntity = ProductEntity.fromDomain(product)
        val savedEntity = RecipeEntity(
            id = 1,
            name = "New Recipe",
            products = mutableListOf(productEntity)
        )

        every { jpaRepository.save(any()) } returns savedEntity

        // when
        val result = recipeRepository.save(recipe)

        // then
        assertEquals(RecipeId(1), result.id)
        assertEquals("New Recipe", result.name)
        assertEquals(1, result.products.size)
        assertEquals("Test Product", result.products.first().name)
        verify { jpaRepository.save(any()) }
    }

    @Test
    fun `save updates existing recipe`() {
        // given
        val recipeId = RecipeId(1)
        val product = createProductWithId(1, "Test Product", 100)
        val recipe = Recipe("Updated Recipe").apply {
            assignId(recipeId)
            addProduct(product)
        }

        val productEntity = ProductEntity.fromDomain(product)
        val savedEntity = RecipeEntity(
            id = recipeId.value,
            name = "Updated Recipe",
            products = mutableListOf(productEntity)
        )

        every { jpaRepository.save(any()) } returns savedEntity

        // when
        val result = recipeRepository.save(recipe)

        // then
        assertEquals(recipeId, result.id)
        assertEquals("Updated Recipe", result.name)
        assertEquals(1, result.products.size)
        assertEquals("Test Product", result.products.first().name)
        verify { jpaRepository.save(any()) }
    }

    @Test
    fun `save throws ValidationException on data integrity violation`() {
        // given
        val recipe = Recipe("Invalid Recipe")
        every { jpaRepository.save(any()) } throws DataIntegrityViolationException("Validation failed")

        // when/then
        val exception = assertThrows<DomainException.ValidationException> {
            recipeRepository.save(recipe)
        }
        assertEquals("Recipe validation failed: Validation failed", exception.message)
        verify { jpaRepository.save(any()) }
    }
}