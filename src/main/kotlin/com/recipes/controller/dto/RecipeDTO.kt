package com.recipes.controller.dto

import com.recipes.domain.model.Recipe
import com.recipes.domain.model.RecipeId

data class RecipeDTO(
    val id: RecipeId,
    val name: String,
    val products: List<ProductDTO>
) {
    companion object {
        fun from(recipe: Recipe) = RecipeDTO(
            id = recipe.id,
            name = recipe.name,
            products = recipe.products.map { ProductDTO.from(it) }
        )
    }
}

