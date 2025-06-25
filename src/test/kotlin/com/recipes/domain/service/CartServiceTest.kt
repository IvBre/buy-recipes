package com.recipes.domain.service

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import com.recipes.domain.model.DomainError
import com.recipes.domain.model.DomainResult
import com.recipes.domain.model.RecipeId
import com.recipes.domain.model.TestEntityFactory.createCartWithId
import com.recipes.domain.model.TestEntityFactory.createProductWithId
import com.recipes.domain.model.TestEntityFactory.createRecipeWithId
import com.recipes.domain.repository.CartRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CartServiceTest {
    private val cartRepository: CartRepository = mockk()
    private val recipeService: RecipeService = mockk()
    private val cartService = CartService(cartRepository, recipeService)

    @Test
    fun `getCart returns success when cart exists`() {
        // given
        val cartId = CartId(1)
        val cart = createCartWithId(cartId)
        every { cartRepository.findById(cartId) } returns cart

        // when
        val result = cartService.getCart(cartId)

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(cart, result.data)
        verify { cartRepository.findById(cartId) }
    }

    @Test
    fun `getCart returns error when cart does not exist`() {
        // given
        val cartId = CartId(1)
        every { cartRepository.findById(cartId) } returns null

        // when
        val result = cartService.getCart(cartId)

        // then
        assertIs<DomainResult.Error>(result)
        assertEquals("Cart entity not found for id: $cartId.", result.error.message)
        verify { cartRepository.findById(cartId) }
    }

    @Test
    fun `createCart creates empty cart successfully`() {
        // given
        every { cartRepository.save(any()) } answers { firstArg() }

        // when
        val result = cartService.createCart()

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(0, result.data.totalInCents)
        assertEquals(0, result.data.items.size)
        verify { cartRepository.save(any()) }
    }

    @Test
    fun `createCart sets createdAt field`() {
        // given
        val now = LocalDateTime.now()

        every { cartRepository.save(any()) } answers { firstArg() }

        // when
        val result = cartService.createCart()

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertTrue(result.data.createdAt.isAfter(now))
    }

    @Test
    fun `createCartWithRecipe creates cart with recipe products when recipe exists`() {
        // given
        val recipeId = RecipeId(1)
        val product1 = createProductWithId(1, "Product 1", 100)
        val product2 = createProductWithId(2, "Product 2", 200)
        val recipe = createRecipeWithId(recipeId, "Test Recipe").apply {
            addProduct(product1)
            addProduct(product2)
        }

        // Mock save to simulate DB behavior of assigning ID
        every { cartRepository.save(any()) } answers {
            (firstArg() as Cart).let { cart ->
                if (!cart.hasId()) {
                    cart.assignId(CartId(1))
                }
                cart
            }
        }
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(recipe)

        // when
        val result = cartService.createCartWithRecipe(recipeId)

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(300, result.data.totalInCents)
        assertEquals(2, result.data.items.size)
        verify {
            recipeService.getRecipeById(recipeId)
            cartRepository.save(any())
        }
    }

    @Test
    fun `createCartWithRecipe returns error when recipe does not exist`() {
        // given
        val recipeId = RecipeId(1)
        val error = DomainError.NotFound("Recipe not found")
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Error(error)

        // when
        val result = cartService.createCartWithRecipe(recipeId)

        // then
        assertIs<DomainResult.Error>(result)
        assertEquals(error, result.error)
        verify { recipeService.getRecipeById(recipeId) }
        verify(exactly = 0) { cartRepository.save(any()) }
    }

    @Test
    fun `addRecipeToCart adds all recipe products to cart`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)

        val product1 = createProductWithId(1, "Product 1", 100)
        val product2 = createProductWithId(2, "Product 2", 200)
        val cart = createCartWithId(cartId)
        val recipe = createRecipeWithId(recipeId, "Test Recipe").apply {
            addProduct(product1)
            addProduct(product2)
        }

        every { cartRepository.findById(cartId) } returns cart
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(recipe)
        every { cartRepository.save(any()) } returnsArgument 0

        // when
        val result = cartService.addRecipeToCart(cartId, recipeId)

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(300, result.data.totalInCents)
        assertEquals(2, result.data.items.size)
        verify {
            cartRepository.findById(cartId)
            recipeService.getRecipeById(recipeId)
            cartRepository.save(any())
        }
    }

    @Test
    fun `addRecipeToCart returns error when cart does not exist`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)
        val error = DomainError.NotFound("Cart entity not found for id: $cartId.")

        every { cartRepository.findById(cartId) } returns null
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(
            createRecipeWithId(recipeId, "Test Recipe")
        )

        // when
        val result = cartService.addRecipeToCart(cartId, recipeId)

        // then
        assertIs<DomainResult.Error>(result)
        assertEquals(error.message, result.error.message)
        verify { cartRepository.findById(cartId) }
        verify { recipeService.getRecipeById(recipeId) }
        verify(exactly = 0) { cartRepository.save(any()) }
    }

    @Test
    fun `addRecipeToCart returns error when recipe does not exist`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)
        val cart = createCartWithId(cartId)
        val error = DomainError.NotFound("Recipe not found")

        every { cartRepository.findById(cartId) } returns cart
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Error(error)

        // when
        val result = cartService.addRecipeToCart(cartId, recipeId)

        // then
        assertIs<DomainResult.Error>(result)
        assertEquals(error, result.error)
        verify { cartRepository.findById(cartId) }
        verify { recipeService.getRecipeById(recipeId) }
        verify(exactly = 0) { cartRepository.save(any()) }
    }

    @Test
    fun `addRecipeToCart adds items only once when adding same recipe twice`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)
        val cart = createCartWithId(cartId)
        val product1 = createProductWithId(1, "Product 1", 100)
        val product2 = createProductWithId(2, "Product 2", 200)
        val recipe = createRecipeWithId(recipeId, "Test Recipe").apply {
            addProduct(product1)
            addProduct(product2)
        }

        every { cartRepository.findById(cartId) } returns cart
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(recipe)
        every { cartRepository.save(any()) } returnsArgument 0

        // when
        cartService.addRecipeToCart(cartId, recipeId) // First add
        val result = cartService.addRecipeToCart(cartId, recipeId) // Second add

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(300, result.data.totalInCents)
        assertEquals(2, result.data.items.size)
        result.data.items.forEach { item ->
            assertTrue(item.recipeIds.contains(recipeId))
        }
        verify(exactly = 2) { cartRepository.findById(cartId) }
        verify(exactly = 2) { recipeService.getRecipeById(recipeId) }
        verify(exactly = 2) { cartRepository.save(any()) }
    }

    @Test
    fun `removeRecipeFromCart removes all recipe items from cart`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)
        val cart = createCartWithId(cartId)
        val recipe = createRecipeWithId(recipeId, "Test Recipe")

        val product1 = createProductWithId(1, "Product 1", 100)
        val product2 = createProductWithId(2, "Product 2", 200)
        cart.createItem(product1, recipeId)
        cart.createItem(product2, recipeId)

        every { cartRepository.findById(cartId) } returns cart
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(recipe)
        every { cartRepository.save(any()) } returnsArgument 0

        // when
        val result = cartService.removeRecipeFromCart(cartId, recipeId)

        // then
        assertIs<DomainResult.Success<Cart>>(result)
        assertEquals(0, result.data.totalInCents)
        assertEquals(0, result.data.items.size)
        verify {
            cartRepository.findById(cartId)
            recipeService.getRecipeById(recipeId)
            cartRepository.save(any())
        }
    }

    @Test
    fun `removeRecipeFromCart returns error when cart does not exist`() {
        // given
        val cartId = CartId(1)
        val recipeId = RecipeId(1)
        val error = DomainError.NotFound("Cart entity not found for id: $cartId.")

        every { cartRepository.findById(cartId) } returns null
        every { recipeService.getRecipeById(recipeId) } returns DomainResult.Success(
            createRecipeWithId(recipeId, "Test Recipe")
        )

        // when
        val result = cartService.removeRecipeFromCart(cartId, recipeId)

        // then
        assertIs<DomainResult.Error>(result)
        assertEquals(error.message, result.error.message)
        verify { cartRepository.findById(cartId) }
        verify(exactly = 0) { cartRepository.save(any()) }
    }

    @Test
    fun `removeRecipeFromCart keeps shared products when used by other recipe`() {
        // given
        val cartId = CartId(1)
        val recipe1Id = RecipeId(1)
        val recipe2Id = RecipeId(2)
        val cart = createCartWithId(cartId)

        // Common products between recipes
        val pasta = createProductWithId(1, "Pasta", 200)
        val oil = createProductWithId(2, "Olive Oil", 300)

        // Unique products for each recipe
        val beef = createProductWithId(3, "Ground Beef", 800)
        val tomato = createProductWithId(4, "Tomato", 100)

        // Create recipes with shared and unique products
        val recipe1 = createRecipeWithId(recipe1Id, "Bolognese").apply {
            addProduct(pasta)
            addProduct(oil)
            addProduct(beef)
        }

        val recipe2 = createRecipeWithId(recipe2Id, "Marinara").apply {
            addProduct(pasta)
            addProduct(oil)
            addProduct(tomato)
        }

        // Add both recipes to the cart
        every { cartRepository.findById(cartId) } returns cart
        every { recipeService.getRecipeById(recipe1Id) } returns DomainResult.Success(recipe1)
        every { recipeService.getRecipeById(recipe2Id) } returns DomainResult.Success(recipe2)
        every { cartRepository.save(any()) } returnsArgument 0

        cartService.addRecipeToCart(cartId, recipe1Id)
        cartService.addRecipeToCart(cartId, recipe2Id)

        // Verify initial state
        assertEquals(4, cart.items.size) // pasta, oil, beef, tomato
        assertEquals(1400, cart.totalInCents) // 200 + 300 + 800 + 100

        // when - remove recipe1
        val result = cartService.removeRecipeFromCart(cartId, recipe1Id)

        // then
        assertIs<DomainResult.Success<Cart>>(result)

        // Shared products should remain
        assertEquals(3, result.data.items.size) // pasta, oil, tomato remain
        assertEquals(600, result.data.totalInCents) // 200 + 300 + 100

        // Check that shared items still reference recipe2
        val sharedItems = result.data.items.filter { it.product.id in setOf(pasta.id, oil.id) }
        sharedItems.forEach { item ->
            assertEquals(1, item.recipeIds.size)
            assertTrue(item.recipeIds.contains(recipe2Id))
        }

        // Check that beef is removed and tomato remains
        assertNull(result.data.items.find { it.product.id == beef.id })
        assertNotNull(result.data.items.find { it.product.id == tomato.id })

        verify(exactly = 3) { cartRepository.findById(cartId) } // Initial + 2 adds + remove
        verify(exactly = 2) { recipeService.getRecipeById(recipe1Id) } // Initial add + remove
        verify { recipeService.getRecipeById(recipe2Id) } // Second add
        verify(exactly = 3) { cartRepository.save(any()) } // 2 adds + remove
    }
}
