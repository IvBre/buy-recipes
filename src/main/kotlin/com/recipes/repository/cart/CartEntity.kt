package com.recipes.repository.cart

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import com.recipes.repository.product.ProductEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "carts")
class CartEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    var totalInCents: Int = 0,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItemEntity> = mutableListOf(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Cart {
        val cart = Cart(totalInCents = totalInCents, createdAt = createdAt)
        cart.assignId(CartId(id))

        val cartItems = items.map { cartItemEntity ->
            cartItemEntity.toDomain()
        }

        // Add items after we have their IDs
        cartItems.forEach { cartItem ->
            cart.addItem(cartItem)
        }

        return cart
    }

    companion object {
        fun fromDomain(cart: Cart): CartEntity {
            val cartEntity = CartEntity(
                id = if (cart.hasId()) cart.id.value else 0,
                totalInCents = cart.totalInCents,
                createdAt = cart.createdAt,
            )

            val items = cart.items.map {
                CartItemEntity.fromDomain(
                    cartItem = it,
                    cart = cartEntity,
                    product = ProductEntity.fromDomain(it.product)
                )
            }

            cartEntity.items.addAll(items)

            return cartEntity
        }
    }
}
