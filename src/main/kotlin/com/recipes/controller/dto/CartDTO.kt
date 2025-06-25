package com.recipes.controller.dto

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import java.time.LocalDateTime

data class CartDTO(
    val id: CartId,
    val totalInCents: Int,
    val items: List<CartItemDTO>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(cart: Cart) = CartDTO(
            id = cart.id,
            totalInCents = cart.totalInCents,
            items = cart.items.map { CartItemDTO.from(it) },
            createdAt = cart.createdAt,
        )
    }
}