package com.example.stockeat.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.data.Ingredient
import com.example.stockeat.api_service.RetrofitClient
import com.example.stockeat.dataaccess.IngredientDao
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the user's food inventory.
 *
 * It handles adding, updating, consuming, and deleting ingredients,
 * as well as managing the UI state for various dialogs and scanner results.
 *
 * @property dao The Data Access Object for ingredient-related database operations.
 */
class InventoryViewModel(private val dao: IngredientDao) : ViewModel() {

    /**
     * Flow of all ingredients available in the local database.
     */
    val ingredients = dao.getAllIngredients()

    // --- State for persisting input fields between rotations ---

    /** Current input for the name of the ingredient being added. */
    var nameInput by mutableStateOf("")
    /** Current input for the quantity of the ingredient being added. */
    var quantityInput by mutableStateOf("")
    /** Current selected unit for the ingredient being added. */
    var selectedUnitInput by mutableStateOf("ud")
    /** Current selected expiry date for the ingredient being added. */
    var expiryDateInput by mutableStateOf<Long?>(null)
    /** Image URL obtained from a scanned product, if any. */
    var scannedImageUrlInput by mutableStateOf<String?>(null)
    /** Error message to display if the barcode scan fails. */
    var scannerErrorMessage by mutableStateOf<String?>(null)

    // --- State for dialogs and modals ---

    /** Controls the visibility of the consumption amount dialog. */
    var showConsumeToast by mutableStateOf(false)
    /** The ingredient currently selected for consumption in the dialog. */
    var ingredientToConsume by mutableStateOf<Ingredient?>(null)
    /** Current input for the amount of ingredient to consume. */
    var consumptionAmountInput by mutableStateOf("")

    /** Controls the visibility of the ingredient editing modal. */
    var showEditModal by mutableStateOf(false)
    /** The ingredient currently being edited in the modal. */
    var ingredientToEdit by mutableStateOf<Ingredient?>(null)

    /** Controls the visibility of the out-of-stock dialog. */
    var showExhaustedDialog by mutableStateOf(false)
    /** The ingredient that has just run out of stock. */
    var exhaustedIngredient by mutableStateOf<Ingredient?>(null)

    /**
     * Resets all input fields in the "Add Ingredient" section to their default values.
     */
    fun resetInputs() {
        nameInput = ""
        quantityInput = ""
        selectedUnitInput = "ud"
        expiryDateInput = null
        scannedImageUrlInput = null
    }

    /**
     * Adds a new ingredient to the database.
     *
     * @param name Name of the ingredient.
     * @param quantity Initial quantity.
     * @param unit Unit of measurement.
     * @param toBuy Whether the item should start in the shopping list.
     * @param expiryDate Optional expiration date timestamp.
     * @param imageUrl Optional image URL for the product.
     */
    fun addIngredient(name : String, quantity : Double, unit: String, toBuy: Boolean, expiryDate: Long? = null, imageUrl: String? = null) {
        val ingredient =
            Ingredient(nombre = name, cantidad = quantity, unidad = unit, toBuy = toBuy, expiryDate = expiryDate, imageUrl = imageUrl)
        viewModelScope.launch {
            dao.insertIngredient(ingredient)
        }
    }

    /**
     * Updates an existing ingredient's information in the database.
     *
     * @param ingredient The ingredient entity with updated values.
     */
    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient)
        }
    }

    /**
     * Reduces the quantity of an ingredient. If it reaches zero, [onExhausted] is triggered.
     *
     * @param ingredient The ingredient to consume.
     * @param amountToConsume The quantity to subtract.
     * @param onExhausted Callback invoked when the quantity reaches zero.
     */
    fun consumeIngredient(ingredient: Ingredient, amountToConsume: Double, onExhausted: (Ingredient) -> Unit = {}) {
        viewModelScope.launch {
            val newQuantity = (ingredient.cantidad - amountToConsume).coerceAtLeast(0.0)
            val updated = ingredient.copy(cantidad = newQuantity)
            if (newQuantity == 0.0) {
                // We notify but don't update toBuy automatically here
                onExhausted(updated)
            } else {
                dao.updateIngredient(updated)
            }
        }
    }

    /**
     * Marks an ingredient to be bought, effectively moving it to the shopping list view.
     *
     * @param ingredient The ingredient to move.
     */
    fun moveToShoppingList(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient.copy(toBuy = true))
        }
    }

    /**
     * Permanently removes an ingredient from the database.
     *
     * @param ingredient The ingredient to delete.
     */
    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.deleteIngredient(ingredient)
        }
    }

    /**
     * Looks up product information using a barcode via Open Food Facts API.
     *
     * @param barcode The product barcode string.
     * @param onResult Callback invoked with the product name and optional image URL.
     */
    fun scanProduct(barcode: String, onResult: (String, String?) -> Unit) {
        viewModelScope.launch {
            scannerErrorMessage = null
            try {
                val response = RetrofitClient.instance.getProduct(barcode)
                if (response.status == "encontrado") {
                    onResult(response.nombre ?: "", response.imageUrl)
                } else {
                    scannerErrorMessage = "Product not found ($barcode)"
                }
            } catch (e: Exception) {
                scannerErrorMessage = "Network error: ${e.localizedMessage ?: "Please check your connection"}"
            }
        }
    }

    /**
     * Clears the current scanner error message.
     */
    fun clearScannerError() {
        scannerErrorMessage = null
    }

    /**
     * Helper function to format a timestamp into a human-readable date string.
     *
     * @param timestamp The date timestamp in milliseconds.
     * @return Formatted date string (dd/MM/yyyy).
     */
    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Sin fecha"
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}