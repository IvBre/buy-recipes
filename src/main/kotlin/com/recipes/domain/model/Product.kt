package com.recipes.domain.model

data class Product(
    val name: String,
    val priceInCents: Int
) : Entity<ProductId>()
