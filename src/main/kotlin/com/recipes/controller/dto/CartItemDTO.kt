package com.recipes.controller.dto

import com.recipes.domain.model.CartItem
import com.recipes.domain.model.CartItemId
import java.time.LocalDateTime

data class CartItemDTO(
    val id: CartItemId,
    val product: ProductDTO,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(cartItem: CartItem) = CartItemDTO(
            id = cartItem.id,
            product = ProductDTO.from(cartItem.product),
            createdAt = cartItem.createdAt,
        )
    }
}