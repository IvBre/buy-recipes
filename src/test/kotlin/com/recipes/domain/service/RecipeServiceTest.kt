package com.recipes.domain.service

import com.recipes.domain.model.DomainError
import com.recipes.domain.model.DomainResult
import com.recipes.domain.model.Recipe
import com.recipes.domain.model.RecipeId
import com.recipes.domain.model.TestEntityFactory.createProductWithId
import com.recipes.domain.model.TestEntityFactory.createRecipeWithId
import com.recipes.domain.repository.RecipeRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class RecipeServiceTest {
    private val recipeRepository: RecipeRepository = mockk()
    private val recipeService = RecipeService(recipeRepository)

    @Test
    fun `returns success with all recipes when repository has recipes`() {
        // given
        val product1 = createProductWithId(1, "Product 1", 100)
        val product2 = createProductWithId(2, "Product 2", 200)

        val recipe1 = createRecipeWithId(RecipeId(1), "Recipe 1").apply {
            addProduct(product1)
        }

        val recipe2 = createRecipeWithId(RecipeId(2), "Recipe 2").apply {
            addProduct(product2)
        }

        val recipes = listOf(recipe1, recipe2)
        every { recipeRepository.findAll() } returns recipes

        // when
        val result = recipeService.getAllRecipes()

        // then
        assertIs<DomainResult.Success<List<Recipe>>>(result)
        assertEquals(2, result.data.size)
        assertEquals(recipes, result.data)
        verify(exactly = 1) { recipeRepository.findAll() }
    }

    @Test
    fun `returns empty list when repository is empty`() {
        // given
        every { recipeRepository.findAll() } returns emptyList()

        // when
        val result = recipeService.getAllRecipes()

        // then
        assertIs<DomainResult.Success<List<Recipe>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `returns success with recipe when found`() {
        // given
        val recipeId = RecipeId(1)
        val product = createProductWithId(1, "Product 1", 100)
        val recipe = createRecipeWithId(recipeId, "Test Recipe").apply {
            addProduct(product)
        }
        every { recipeRepository.findById(recipeId) } returns recipe

        // when
        val result = recipeService.getRecipeById(recipeId)

        // then
        assertIs<DomainResult.Success<Recipe>>(result)
        assertEquals(recipe, result.data)
        verify(exactly = 1) { recipeRepository.findById(recipeId) }
    }

    @Test
    fun `returns error when recipe not found`() {
        // given
        val recipeId = RecipeId(1)
        every { recipeRepository.findById(recipeId) } returns null

        // when
        val result = recipeService.getRecipeById(recipeId)

        // then
        assertIs<DomainResult.Error>(result)
        assertTrue(result.error is DomainError.NotFound)
        assertEquals("Recipe not found for id: $recipeId.", result.error.message)
    }
}