package com.recipes.repository.product

import com.recipes.domain.model.Product
import com.recipes.domain.repository.ProductRepository
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.ProductId
import kotlin.jvm.optionals.getOrElse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val jpaRepository: JpaProductRepository
) : ProductRepository {
    override fun findById(id: ProductId): Product? =
        jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .getOrElse { null }

    override fun findAll(): List<Product> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun save(product: Product): Product = try {
        val entity = ProductEntity.fromDomain(product)
        jpaRepository.save(entity).toDomain()
    } catch (e: DataIntegrityViolationException) {
        throw DomainException.ValidationException("Product validation failed: ${e.message}")
    }
}
