package com.recipes.repository.product

import com.recipes.domain.model.Product
import com.recipes.domain.model.ProductId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val priceInCents: Int
) {
    fun toDomain(): Product {
        val product = Product(
            name = name,
            priceInCents = priceInCents
        )
        product.assignId(ProductId(id))
        return product
    }

    companion object {
        fun fromDomain(product: Product) = ProductEntity(
            id = if (product.hasId()) product.id.value else 0,
            name = product.name,
            priceInCents = product.priceInCents
        )
    }
}
