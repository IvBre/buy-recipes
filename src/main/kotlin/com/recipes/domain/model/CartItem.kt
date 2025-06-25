package com.recipes.domain.model

import java.time.LocalDateTime

data class CartItem(
    val cartId: CartId,
    val product: Product,
    val createdAt: LocalDateTime,
    private val _recipeIds: MutableSet<RecipeId> = mutableSetOf()
) : Entity<CartItemId>() {
    val recipeIds: Set<RecipeId>
        get() = _recipeIds.toSet()

    fun addRecipe(recipeId: RecipeId) {
        _recipeIds.add(recipeId)
    }

    fun removeRecipe(recipeId: RecipeId) {
        _recipeIds.remove(recipeId)
    }

    fun isUsedByOtherRecipes(recipeId: RecipeId): Boolean {
        return _recipeIds.size > 1 || (_recipeIds.size == 1 && !_recipeIds.contains(recipeId))
    }
}
