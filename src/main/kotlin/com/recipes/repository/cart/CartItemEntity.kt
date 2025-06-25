package com.recipes.repository.cart

import com.recipes.domain.model.CartId
import com.recipes.repository.product.ProductEntity
import com.recipes.domain.model.CartItem
import com.recipes.domain.model.CartItemId
import com.recipes.domain.model.RecipeId
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "cart_items")
class CartItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: CartEntity,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductEntity,

    @ElementCollection
    @CollectionTable(name = "cart_item_recipes")
    val recipeIds: MutableSet<Long> = mutableSetOf(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): CartItem {
        val cartItem = CartItem(
            cartId = CartId(cart.id),
            product = product.toDomain(),
            createdAt = createdAt,
        )
        cartItem.assignId(CartItemId(id))
        recipeIds.forEach { recipeId ->
            cartItem.addRecipe(RecipeId(recipeId))
        }
        return cartItem
    }

    companion object {
        fun fromDomain(cartItem: CartItem, cart: CartEntity, product: ProductEntity): CartItemEntity {
            val entity = CartItemEntity(
                id = if (cartItem.hasId()) cartItem.id.value else 0,
                cart = cart,
                product = product,
                createdAt = cartItem.createdAt,
            )
            entity.recipeIds.addAll(cartItem.recipeIds.map { it.value })
            return entity
        }
    }
}