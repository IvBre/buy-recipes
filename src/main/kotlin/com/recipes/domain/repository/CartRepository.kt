package com.recipes.domain.repository

import com.recipes.domain.model.Cart
import com.recipes.domain.model.CartId

interface CartRepository {
    fun findById(id: CartId): Cart?
    fun save(cart: Cart): Cart
}