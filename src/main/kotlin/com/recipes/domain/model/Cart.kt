package com.recipes.domain.model

import java.time.LocalDateTime
import org.slf4j.LoggerFactory

data class Cart(
    var totalInCents: Int = 0,
    val createdAt: LocalDateTime,
    private val _items: MutableList<CartItem> = mutableListOf()
) : Entity<CartId>() {
    private val logger = LoggerFactory.getLogger(Cart::class.java)

    val items: List<CartItem>
        get() = _items.toList()

    fun addItem(cartItem: CartItem) {
        if (!_items.any { it.product.id == cartItem.product.id }) {
            _items.add(cartItem)
        }
    }

    fun createItem(product: Product, recipeId: RecipeId): CartItem? {
        val existingItem = _items.find { it.product.id == product.id }
        
        if (existingItem != null) {
            logger.debug("Product {} already exists in cart {}, adding recipe reference {}",
                product.id, id, recipeId)

            existingItem.addRecipe(recipeId)
            return existingItem
        }

        val cartItem = CartItem(cartId = this.id, product = product, createdAt = LocalDateTime.now())
        cartItem.addRecipe(recipeId)
        _items.add(cartItem)
        totalInCents += product.priceInCents

        return cartItem
    }

    fun removeRecipeItems(recipeId: RecipeId) {
        val itemsToRemove = _items.filter { cartItem ->
            cartItem.recipeIds.contains(recipeId) && !cartItem.isUsedByOtherRecipes(recipeId)
        }

        logger.debug("Found {} items to remove completely", itemsToRemove.size)

        itemsToRemove.forEach { cartItem ->
            _items.remove(cartItem)
            totalInCents -= cartItem.product.priceInCents
        }

        // For items that remain but were part of this recipe, remove the recipe reference
        _items.forEach { cartItem ->
            if (cartItem.recipeIds.contains(recipeId)) {
                cartItem.removeRecipe(recipeId)
            }
        }
    }
}