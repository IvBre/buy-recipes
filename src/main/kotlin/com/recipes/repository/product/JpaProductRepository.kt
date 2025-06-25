package com.recipes.repository.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaProductRepository : JpaRepository<ProductEntity, Long>