package com.example.stockeat.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.dataaccess.IngredientDao
import com.example.stockeat.data.Ingredient
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the shopping list and the purchasing process.
 *
 * It handles the state for adding new items to buy and the modal state for 
 * completing a purchase (moving items back to the inventory with updated stock and expiry dates).
 *
 * @property dao The Data Access Object for ingredient-related database operations.
 */
class ShoppingListViewModel(private val dao: IngredientDao): ViewModel() {

    /**
     * Flow of ingredients that are currently marked as 'to buy' in the database.
     */
    val ingredientsToBuy = dao.getShoppingList()

    /**
     * Current input for the name of the new item to add to the shopping list.
     */
    var itemNameInput by mutableStateOf("")
    /**
     * Current input for the quantity of the new item. Default is "1".
     */
    var itemQuantityInput by mutableStateOf("1")
    /**
     * Current selected unit for the new item. Default is "ud".
     */
    var itemUnitInput by mutableStateOf("ud")


    /**
     * Controls the visibility of the purchase confirmation modal.
     */
    var showPurchaseModal by mutableStateOf(false)
    /**
     * The ingredient currently selected for purchase in the modal.
     */
    var selectedIngredient by mutableStateOf<Ingredient?>(null)
    /**
     * Current input for the actual quantity purchased.
     */
    var purchaseQuantityInput by mutableStateOf("")
    /**
     * Selected expiry date for the purchased item.
     */
    var purchaseExpiryDate by mutableStateOf<Long?>(null)
    /**
     * Controls the visibility of the date picker within the purchase modal.
     */
    var showDatePicker by mutableStateOf(false)
    /**
     * Current selected unit for the purchased item.
     */
    var purchaseUnit by mutableStateOf("ud")

    /**
     * Resets the input fields in the "Add Item" section to their default values.
     */
    fun resetAddInputs() {
        itemNameInput = ""
        itemQuantityInput = "1"
        itemUnitInput = "ud"
    }

    /**
     * Resets all states related to the purchase modal and closes it.
     */
    fun resetPurchaseModal() {
        showPurchaseModal = false
        selectedIngredient = null
        purchaseQuantityInput = ""
        purchaseExpiryDate = null
        showDatePicker = false
        purchaseUnit = "ud"
    }

    /**
     * Updates an existing ingredient in the database.
     *
     * @param ingredient The ingredient entity with updated values.
     */
    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient)
        }
    }

    /**
     * Removes an ingredient from the database.
     *
     * @param ingredient The ingredient to be deleted.
     */
    fun deleteFromShoppingList(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.deleteIngredient(ingredient)
        }
    }

    /**
     * Creates and inserts a new ingredient marked as 'toBuy = true' into the database.
     *
     * @param name The name of the item.
     * @param quantity The quantity needed.
     * @param unit The unit of measurement.
     */
    fun addNewIngredientToBuy(name: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            val newIngredient = Ingredient(
                nombre = name,
                cantidad = quantity,
                unidad = unit,
                toBuy = true
            )
            dao.insertIngredient(newIngredient)
        }
    }

    /**
     * Completes the purchase of an ingredient by marking it as 'toBuy = false'.
     * This moves the item from the shopping list back to the inventory.
     *
     * @param ingredient The ingredient being purchased.
     */
    fun completePurchase(ingredient: Ingredient) {
        viewModelScope.launch {
            // When bought, it goes back to inventory (toBuy = false)
            dao.updateIngredient(ingredient.copy(toBuy = false))
        }
    }
}