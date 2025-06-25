package com.recipes.controller

import com.recipes.controller.dto.RecipeDTO
import com.recipes.controller.response.toResponseEntity
import com.recipes.domain.model.RecipeId
import com.recipes.domain.service.RecipeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recipes")
class RecipeController(private val recipeService: RecipeService) {
    @GetMapping
    fun getAllRecipes(): ResponseEntity<List<RecipeDTO>> =
        recipeService.getAllRecipes().toResponseEntity { recipes ->
            recipes.map { RecipeDTO.from(it) }
        }

    @GetMapping("/{id}")
    fun getRecipeById(@PathVariable id: RecipeId): ResponseEntity<RecipeDTO> =
        recipeService.getRecipeById(id).toResponseEntity { RecipeDTO.from(it) }
}