package com.recipes.repository.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaCartRepository : JpaRepository<CartEntity, Long>