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

/**
 * Represents the different states of the recipe generation UI.
 */
sealed class RecipeUiState {
    /** Initial state before any generation request. */
    object Idle : RecipeUiState()
    /** State when a recipe generation request is in progress. */
    object Loading : RecipeUiState()
    /** State when recipes have been successfully generated. */
    data class Success(val recipes: List<Recipe>) : RecipeUiState()
    /** State when an error occurred during recipe generation. */
    data class Error(val message: String) : RecipeUiState()
}

/**
 * ViewModel responsible for managing the recipe generation state and saved recipes.
 *
 * It maintains the UI state for recipe generation, handles user input for ingredients,
 * and interacts with the local database to save or delete recipes.
 *
 * @property recipeDao The Data Access Object for recipe-related database operations.
 */
class RecipeViewModel(private val recipeDao: RecipeDao) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)

    /**
     * Observable flow representing the current state of the recipe generation process.
     */
    val uiState: StateFlow<RecipeUiState> = _uiState

    /**
     * State list of ingredients selected by the user from their inventory.
     * Persists across configuration changes.
     */
    val selectedIngredients = mutableStateListOf<String>()

    /**
     * Current manual input string for adding ingredients not present in the inventory.
     */
    var manualInput by mutableStateOf("")

    /**
     * Flow of recipes saved locally in the database.
     */
    val savedRecipes = recipeDao.getAllSavedRecipes()

    /**
     * Requests the generation of recipes based on a list of ingredient names.
     * Updates [uiState] to reflect the progress and result of the request.
     *
     * @param ingredientNames The list of ingredients to use for generation.
     */
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

    /**
     * Toggles the selection of an ingredient.
     *
     * @param nombre The name of the ingredient to toggle.
     */
    fun toggleIngredient(nombre: String) {
        if (selectedIngredients.contains(nombre)) {
            selectedIngredients.remove(nombre)
        } else {
            selectedIngredients.add(nombre)
        }
    }

    /**
     * Saves a generated recipe to the local database.
     *
     * @param recipe The recipe object to save.
     */
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

    /**
     * Deletes a previously saved recipe from the database.
     *
     * @param recipeEntity The recipe entity to delete.
     */
    fun deleteRecipe(recipeEntity: RecipeEntity) {
        viewModelScope.launch {
            recipeDao.deleteRecipe(recipeEntity)
        }
    }
}
