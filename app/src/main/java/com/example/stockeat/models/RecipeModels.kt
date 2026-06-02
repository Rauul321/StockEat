package com.example.stockeat.models

data class RecipeRequest(
    val ingredients: List<String>
)

data class RecipeResponse(
    val status: String,
    val recipes: List<Recipe>
)

data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val preparation: List<String>
)