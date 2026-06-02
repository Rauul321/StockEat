package com.example.stockeat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.api_service.RetrofitClient
import com.example.stockeat.models.Recipe
import com.example.stockeat.models.RecipeRequest
import kotlinx.coroutines.launch
import java.util.Collections

class RecipeViewModel : ViewModel() {


    var recipesList by mutableStateOf<List<Recipe>>(Collections.emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchRecipes(selectedIngredients: List<String>) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val request = RecipeRequest(ingredients = selectedIngredients)
                val response = RetrofitClient.instance.generateRecipes(request)

                if(response.status == "success") {
                    recipesList = response.recipes
                } else {
                    errorMessage = "Error de servidor al generar recetas :("
                }
            } catch(e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}