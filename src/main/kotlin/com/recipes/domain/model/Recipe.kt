package com.recipes.domain.model

data class Recipe(
    val name: String,
    private val _products: MutableList<Product> = mutableListOf()
) : Entity<RecipeId>() {
    val products: List<Product>
        get() = _products.toList()

    fun addProduct(product: Product) {
        _products.add(product)
    }
}
