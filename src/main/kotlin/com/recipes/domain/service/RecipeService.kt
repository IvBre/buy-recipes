package com.recipes.domain.service

import com.recipes.domain.model.DomainError
import com.recipes.domain.model.DomainResult
import com.recipes.domain.model.Recipe
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.RecipeId
import com.recipes.domain.repository.RecipeRepository
import org.springframework.stereotype.Service

@Service
class RecipeService(private val repository: RecipeRepository) {
    fun getAllRecipes(): DomainResult<List<Recipe>> =
        DomainResult.Success(repository.findAll())

    fun getRecipeById(id: RecipeId): DomainResult<Recipe> = try {
        repository.findById(id)?.let {
            DomainResult.Success(it)
        } ?: DomainResult.Error(DomainError.NotFound("Recipe not found for id: $id."))
    } catch (e: DomainException) {
        when(e) {
            is DomainException.ValidationException ->
                DomainResult.Error(DomainError.ValidationError(e.message))
            else ->
                DomainResult.Error(DomainError.BusinessError(e.message))
        }
    }
}