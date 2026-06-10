package com.example.stockeat.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.data.RecipeEntity
import com.example.stockeat.api_service.RetrofitClient
import com.example.stockeat.dataaccess.RecipeDao
import com.example.stockeat.models.Recipe
import com.example.stockeat.models.RecipeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecipeUiState {
    object Idle : RecipeUiState()
    object Loading : RecipeUiState()
    data class Success(val recipes: List<Recipe>) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

class RecipeViewModel(private val recipeDao: RecipeDao) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState

    val selectedIngredients = mutableStateListOf<String>()
    var manualInput by mutableStateOf("")

    // Flow de recetas guardadas localmente
    val savedRecipes = recipeDao.getAllSavedRecipes()

    fun generateRecipes(ingredientNames: List<String>) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                val response = RetrofitClient.instance.generateRecipes(RecipeRequest(ingredientNames))
                _uiState.value = RecipeUiState.Success(response.recipes)
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleIngredient(nombre: String) {
        if (selectedIngredients.contains(nombre)) {
            selectedIngredients.remove(nombre)
        } else {
            selectedIngredients.add(nombre)
        }
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val entity = RecipeEntity(
                name = recipe.name,
                ingredients = recipe.ingredients,
                preparation = recipe.preparation
            )
            recipeDao.saveRecipe(entity)
        }
    }

    fun deleteRecipe(recipeEntity: RecipeEntity) {
        viewModelScope.launch {
            recipeDao.deleteRecipe(recipeEntity)
        }
    }
}
