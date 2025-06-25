package com.recipes.domain.repository

import com.recipes.domain.model.Recipe
import com.recipes.domain.model.RecipeId

interface RecipeRepository {
    fun findById(id: RecipeId): Recipe?
    fun findAll(): List<Recipe>
    fun save(recipe: Recipe): Recipe
}