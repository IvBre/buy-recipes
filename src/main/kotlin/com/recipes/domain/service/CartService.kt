package com.recipes.domain.service

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId
import com.recipes.domain.model.DomainError
import com.recipes.domain.model.DomainResult
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.RecipeId
import com.recipes.domain.repository.CartRepository
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val recipeService: RecipeService
) {
    private val logger = LoggerFactory.getLogger(CartService::class.java)

    fun getCart(id: CartId): DomainResult<Cart> =
        cartRepository.findById(id)?.let {
            DomainResult.Success(it)
        } ?: DomainResult.Error(DomainError.NotFound("Cart entity not found for id: $id."))


    fun createCart(): DomainResult<Cart> {
        val cart = Cart(totalInCents = 0, createdAt = LocalDateTime.now())
        val savedCart = cartRepository.save(cart)
        return DomainResult.Success(savedCart)
    }

    fun createCartWithRecipe(recipeId: RecipeId): DomainResult<Cart> {
        val recipeResult = recipeService.getRecipeById(recipeId)

        return when (recipeResult) {
            is DomainResult.Error -> DomainResult.Error(recipeResult.error)
            is DomainResult.Success -> {
                val cart = cartRepository.save(Cart(totalInCents = 0, createdAt = LocalDateTime.now()))

                recipeResult.data.products.forEach { product ->
                    cart.createItem(product, recipeId)
                }
                val savedCart = cartRepository.save(cart)
                logger.debug("Created cart {} with recipe {}, total items: {}",
                    cart.id, recipeId, cart.items.size)
                DomainResult.Success(savedCart)
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun addRecipeToCart(cartId: CartId, recipeId: RecipeId): DomainResult<Cart> {
        val cartResult = getCart(cartId)
        val recipeResult = recipeService.getRecipeById(recipeId)

        return when {
            cartResult is DomainResult.Error -> cartResult
            recipeResult is DomainResult.Error -> DomainResult.Error(recipeResult.error)
            else -> try {
                val cart = (cartResult as DomainResult.Success).data
                val recipe = (recipeResult as DomainResult.Success).data

                recipe.products.forEach { product ->
                    cart.createItem(product, recipeId)
                }

                val savedCart = cartRepository.save(cart)
                logger.debug("Added recipe {} to cart {}, new total items: {}",
                    recipeId, cartId, cart.items.size)
                DomainResult.Success(savedCart)
            } catch (e: DomainException) {
                when(e) {
                    is DomainException.ValidationException ->
                        DomainResult.Error(DomainError.ValidationError(e.message))
                    else -> DomainResult.Error(DomainError.BusinessError(e.message))
                }
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun removeRecipeFromCart(cartId: CartId, recipeId: RecipeId): DomainResult<Cart> {
        val cartResult = getCart(cartId)
        val recipeResult = recipeService.getRecipeById(recipeId)

        return when {
            cartResult is DomainResult.Error -> cartResult
            recipeResult is DomainResult.Error -> recipeResult
            else -> try {
                val cart = (cartResult as DomainResult.Success).data
                val itemsBeforeRemoval = cart.items.size
                cart.removeRecipeItems(recipeId)
                val savedCart = cartRepository.save(cart)
                logger.info("Removed recipe {} from cart {}, items removed: {}",
                    recipeId, cartId, itemsBeforeRemoval - cart.items.size)
                DomainResult.Success(savedCart)
            } catch (e: DomainException) {
                when(e) {
                    is DomainException.ValidationException ->
                        DomainResult.Error(DomainError.ValidationError(e.message))
                    else -> DomainResult.Error(DomainError.BusinessError(e.message))
                }
            }
        }
    }
}