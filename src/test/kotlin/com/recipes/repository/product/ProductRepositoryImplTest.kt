package com.recipes.repository.product

import com.recipes.domain.model.Product
import com.recipes.domain.model.ProductId
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.TestEntityFactory.createProductWithId
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DataIntegrityViolationException

@ExtendWith(MockKExtension::class)
class ProductRepositoryImplTest {
    private val jpaRepository: JpaProductRepository = mockk()
    private val productRepository = ProductRepositoryImpl(jpaRepository)

    @Test
    fun `findById returns null when product not found`() {
        // given
        val productId = ProductId(1)
        every { jpaRepository.findById(productId.value) } returns Optional.empty()

        // when
        val result = productRepository.findById(productId)

        // then
        assertNull(result)
        verify { jpaRepository.findById(productId.value) }
    }

    @Test
    fun `findById returns product when found`() {
        // given
        val productId = ProductId(1)
        val productEntity = ProductEntity(id = 1, name = "Test Product", priceInCents = 100)
        every { jpaRepository.findById(productId.value) } returns Optional.of(productEntity)

        // when
        val result = productRepository.findById(productId)

        // then
        assertNotNull(result)
        assertEquals(productId, result.id)
        assertEquals("Test Product", result.name)
        assertEquals(100, result.priceInCents)
        verify { jpaRepository.findById(productId.value) }
    }

    @Test
    fun `findAll returns empty list when no products exist`() {
        // given
        every { jpaRepository.findAll() } returns emptyList()

        // when
        val result = productRepository.findAll()

        // then
        assertTrue(result.isEmpty())
        verify { jpaRepository.findAll() }
    }

    @Test
    fun `findAll returns list of products`() {
        // given
        val products = listOf(
            ProductEntity(id = 1, name = "Product 1", priceInCents = 100),
            ProductEntity(id = 2, name = "Product 2", priceInCents = 200)
        )
        every { jpaRepository.findAll() } returns products

        // when
        val result = productRepository.findAll()

        // then
        assertEquals(2, result.size)
        assertEquals("Product 1", result[0].name)
        assertEquals("Product 2", result[1].name)
        verify { jpaRepository.findAll() }
    }

    @Test
    fun `save creates new product successfully`() {
        // given
        val product = Product("New Product", 100)
        val productEntity = ProductEntity(id = 1, name = "New Product", priceInCents = 100)
        every { jpaRepository.save(any()) } returns productEntity

        // when
        val result = productRepository.save(product)

        // then
        assertEquals(ProductId(1), result.id)
        assertEquals("New Product", result.name)
        assertEquals(100, result.priceInCents)
        verify { jpaRepository.save(any()) }
    }

    @Test
    fun `save updates existing product`() {
        // given
        val productId = ProductId(1)
        val product = createProductWithId(productId.value,"Updated Product", 200)
        val productEntity = ProductEntity.fromDomain(product)
        every { jpaRepository.save(any()) } returns productEntity

        // when
        val result = productRepository.save(product)

        // then
        assertEquals(productId, result.id)
        assertEquals("Updated Product", result.name)
        assertEquals(200, result.priceInCents)
        verify { jpaRepository.save(any()) }
    }

    @Test
    fun `save throws ValidationException on data integrity violation`() {
        // given
        val product = Product("Invalid Product", 100)
        every { jpaRepository.save(any()) } throws DataIntegrityViolationException("Validation failed")

        // when/then
        val exception = assertThrows<DomainException.ValidationException> {
            productRepository.save(product)
        }
        assertEquals("Product validation failed: Validation failed", exception.message)
        verify { jpaRepository.save(any()) }
    }
}