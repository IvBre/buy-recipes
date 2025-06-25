package com.recipes.repository.recipe

import com.recipes.domain.model.Recipe
import com.recipes.domain.model.RecipeId
import com.recipes.repository.product.ProductEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "recipes")
class RecipeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val name: String,

    @ManyToMany
    @JoinTable(
        name = "recipe_products",
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
    val products: MutableList<ProductEntity> = mutableListOf()
) {
    fun toDomain(): Recipe {
        val recipe = Recipe(name = name)
        recipe.assignId(RecipeId(id))
        products.forEach { recipe.addProduct(it.toDomain()) }
        return recipe
    }

    companion object {
        fun fromDomain(recipe: Recipe) = RecipeEntity(
            id = if (recipe.hasId()) recipe.id.value else 0,
            name = recipe.name,
            products = recipe.products.map { ProductEntity.fromDomain(it) }.toMutableList()
        )
    }
}
