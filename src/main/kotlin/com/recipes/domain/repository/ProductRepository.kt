package com.recipes.domain.repository

import com.recipes.domain.model.Product
import com.recipes.domain.model.ProductId

interface ProductRepository {
    fun findById(id: ProductId): Product?
    fun findAll(): List<Product>
    fun save(product: Product): Product
}