package com.recipes.repository.recipe

import com.recipes.domain.model.Recipe
import com.recipes.domain.repository.RecipeRepository
import com.recipes.domain.model.DomainException
import com.recipes.domain.model.RecipeId
import kotlin.jvm.optionals.getOrElse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class RecipeRepositoryImpl(
    private val jpaRepository: JpaRecipeRepository
) : RecipeRepository {
    override fun findById(id: RecipeId): Recipe? =
        jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .getOrElse { null }

    override fun findAll(): List<Recipe> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun save(recipe: Recipe): Recipe = try {
        val entity = RecipeEntity.fromDomain(recipe)
        jpaRepository.save(entity).toDomain()
    } catch (e: DataIntegrityViolationException) {
        throw DomainException.ValidationException("Recipe validation failed: ${e.message}")
    }
}
