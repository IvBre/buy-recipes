package com.recipes.repository.cart

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.ProductId
import com.recipes.domain.model.RecipeId
import com.recipes.domain.model.TestEntityFactory.createCartItemWithId
import com.recipes.domain.model.TestEntityFactory.createCartWithId
import com.recipes.domain.model.TestEntityFactory.createProductWithId
import com.recipes.repository.product.JpaProductRepository
import com.recipes.repository.product.ProductEntity
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CartRepositoryImplTest {
    private val jpaCartRepository: JpaCartRepository = mockk()
    private val jpaProductRepository: JpaProductRepository = mockk()
    private val cartRepository = CartRepositoryImpl(jpaCartRepository, jpaProductRepository)

    @Test
    fun `findById returns null when cart not found`() {
        // given
        val cartId = CartId(1)
        every { jpaCartRepository.findById(cartId.value) } returns Optional.empty()

        // when
        val result = cartRepository.findById(cartId)

        // then
        assertNull(result)
        verify { jpaCartRepository.findById(cartId.value) }
    }

    @Test
    fun `findById maps entity to domain model correctly`() {
        // given
        val cartId = CartId(1)
        val productEntity = ProductEntity(1, "Test Product", 100)
        val cartItemEntity = CartItemEntity(
            id = 1,
            cart = CartEntity(id = cartId.value, totalInCents = 100),
            product = productEntity,
            recipeIds = mutableSetOf(1L)
        )
        val cartEntity = CartEntity(
            id = cartId.value,
            totalInCents = 100,
            items = mutableListOf(cartItemEntity)
        )

        every { jpaCartRepository.findById(cartId.value) } returns Optional.of(cartEntity)

        // when
        val result = cartRepository.findById(cartId)

        // then
        assertNotNull(result)
        assertEquals(cartId, result.id)
        assertEquals(100, result.totalInCents)
        assertEquals(1, result.items.size)
        verify { jpaCartRepository.findById(cartId.value) }
    }

    @Test
    fun `save creates new cart when no id present`() {
        // given
        val cart = Cart(totalInCents = 100, createdAt = LocalDateTime.now())
        val cartEntity = CartEntity(id = 1, totalInCents = 100)

        every { jpaCartRepository.save(any()) } returns cartEntity

        // when
        val result = cartRepository.save(cart)

        // then
        assertNotNull(result.id)
        assertEquals(100, result.totalInCents)
        verify { jpaCartRepository.save(any()) }
    }

    @Test
    fun `save updates existing cart with items`() {
        // given
        val cartId = CartId(5)
        val productId = ProductId(2)
        val cart = Cart(totalInCents = 200, createdAt = LocalDateTime.now()).apply {
            assignId(cartId)
        }
        val product = createProductWithId(productId.value, "Test Product", 200)
        val cartItem = createCartItemWithId(1, cartId, product)
        cart.addItem(cartItem)

        val existingCartEntity = CartEntity(id = cartId.value, totalInCents = 100)
        val productEntity = ProductEntity.fromDomain(product)
        val updatedCartEntity = CartEntity.fromDomain(cart)

        every { jpaCartRepository.findById(cartId.value) } returns Optional.of(existingCartEntity)
        every { jpaProductRepository.findById(productId.value) } returns Optional.of(productEntity)
        every { jpaCartRepository.save(any()) } returns updatedCartEntity

        // when
        val result = cartRepository.save(cart)

        // then
        assertNotNull(result)
        assertEquals(cartId, result.id)
        assertEquals(200, result.totalInCents)
        verify {
            jpaCartRepository.findById(cartId.value)
            jpaProductRepository.findById(productId.value)
            jpaCartRepository.save(any())
        }
    }

    @Test
    fun `save throws exception when product not found`() {
        // given
        val cartId = CartId(1)
        val cart = createCartWithId(cartId, 200)
        val product = createProductWithId(1, "Test Product", 200)
        cart.createItem(product, RecipeId(1))

        val existingCartEntity = CartEntity(id = cartId.value, totalInCents = 100)

        every { jpaCartRepository.findById(cartId.value) } returns Optional.of(existingCartEntity)
        every { jpaProductRepository.findById(1) } returns Optional.empty()

        // when/then
        assertThrows<DomainException.NotFoundException> {
            cartRepository.save(cart)
        }
    }
}