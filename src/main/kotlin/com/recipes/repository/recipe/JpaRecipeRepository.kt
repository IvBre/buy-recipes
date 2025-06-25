package com.recipes.repository.recipe

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaRecipeRepository : JpaRepository<RecipeEntity, Long>