package com.recipes.domain.model

import java.time.LocalDateTime

object TestEntityFactory {
    fun createProductWithId(id: Long, name: String, priceInCents: Int): Product =
        Product(name, priceInCents).also {
            it.assignId(ProductId(id))
        }

    fun createCartWithId(id: CartId, totalInCents: Int = 0): Cart =
        Cart(totalInCents, LocalDateTime.now()).also {
            it.assignId(id)
        }

    fun createCartItemWithId(id: Long, cartId: CartId, product: Product): CartItem =
        CartItem(cartId, product, LocalDateTime.now()).also {
            it.assignId(CartItemId(id))
        }

    fun createRecipeWithId(id: RecipeId, name: String): Recipe =
        Recipe(name).also {
            it.assignId(id)
        }
}