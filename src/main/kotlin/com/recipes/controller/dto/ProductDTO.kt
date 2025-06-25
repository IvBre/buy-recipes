package com.recipes.controller.dto

import com.recipes.domain.model.Product
import com.recipes.domain.model.ProductId

data class ProductDTO(
    val id: ProductId,
    val name: String,
    val priceInCents: Int
) {
    companion object {
        fun from(product: Product) = ProductDTO(
            id = product.id,
            name = product.name,
            priceInCents = product.priceInCents
        )
    }
}

