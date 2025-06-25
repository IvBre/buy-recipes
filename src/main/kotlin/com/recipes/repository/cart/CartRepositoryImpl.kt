package com.recipes.repository.cart

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import com.recipes.domain.repository.CartRepository
import com.recipes.domain.model.DomainException
import com.recipes.repository.product.JpaProductRepository
import kotlin.jvm.optionals.getOrElse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class CartRepositoryImpl(
    private val jpaRepository: JpaCartRepository,
    private val jpaProductRepository: JpaProductRepository
) : CartRepository {
    override fun findById(id: CartId): Cart? =
        jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .getOrElse { null }

    override fun save(cart: Cart): Cart = try {
        val cartEntity = if (cart.hasId()) {
            val existingCart = jpaRepository.findById(cart.id.value)
                .orElseThrow { DomainException.NotFoundException("Cart not found: ${cart.id}") }
            update(existingCart, cart)
        } else {
            CartEntity.fromDomain(cart)
        }

        // Save the cart and convert back to domain
        jpaRepository.save(cartEntity).toDomain()
    } catch (e: DataIntegrityViolationException) {
        throw DomainException.ValidationException("Cart validation failed: ${e.message}")
    }

    private fun update(cartEntity: CartEntity, updatedCart: Cart): CartEntity {
        cartEntity.totalInCents = updatedCart.totalInCents

        cartEntity.items.clear()

        updatedCart.items.forEach { cartItem ->
            val productEntity = jpaProductRepository.findById(cartItem.product.id.value)
                .orElseThrow { throw DomainException.NotFoundException("Product not found: ${cartItem.product.id}") }

            val cartItemEntity = CartItemEntity.fromDomain(
                cartItem = cartItem,
                cart = cartEntity,
                product = productEntity
            )
            cartEntity.items.add(cartItemEntity)
        }

        return cartEntity
    }
}
