package com.recipes.controller

import com.recipes.controller.dto.CartDTO
import com.recipes.controller.response.toResponseEntity
import com.recipes.domain.model.CartId
import com.recipes.domain.model.RecipeId
import com.recipes.domain.service.CartService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/carts")
class CartController(private val cartService: CartService) {
    @GetMapping("/{cartId}")
    fun getCart(@PathVariable cartId: CartId): ResponseEntity<CartDTO> =
        cartService.getCart(cartId).toResponseEntity { CartDTO.from(it) }

    @PostMapping
    fun createCart(): ResponseEntity<CartDTO> =
        cartService.createCart().toResponseEntity(HttpStatus.CREATED) { CartDTO.from(it) }

    @PostMapping("/with_recipe/{recipeId}")
    fun createCartWithRecipe(
        @PathVariable recipeId: RecipeId
    ): ResponseEntity<CartDTO> =
        cartService.createCartWithRecipe(recipeId).toResponseEntity(HttpStatus.CREATED) { CartDTO.from(it) }

    @PostMapping("/{cartId}/add_recipe/{recipeId}")
    fun addRecipeToCart(
        @PathVariable cartId: CartId,
        @PathVariable recipeId: RecipeId
    ): ResponseEntity<CartDTO> =
        cartService.addRecipeToCart(cartId, recipeId).toResponseEntity { CartDTO.from(it) }

    @DeleteMapping("/{cartId}/recipes/{recipeId}")
    fun removeRecipeFromCart(
        @PathVariable cartId: CartId,
        @PathVariable recipeId: RecipeId
    ): ResponseEntity<CartDTO> =
        cartService.removeRecipeFromCart(cartId, recipeId)
            .toResponseEntity { CartDTO.from(it) }
}