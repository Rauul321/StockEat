package com.example.stockeat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.dataaccess.IngredientDao
import com.example.stockeat.Ingredient
import kotlinx.coroutines.launch

class ShoppingListViewModel(private val dao: IngredientDao): ViewModel() {

    val ingredientsToBuy = dao.getShoppingList()

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient)
        }
    }

    fun deleteFromShoppingList(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.deleteIngredient(ingredient)
        }
    }

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

    fun completePurchase(ingredient: Ingredient) {
        viewModelScope.launch {
            // Al comprar, vuelve al inventario (toBuy = false) 
            // y podríamos resetear la cantidad si estaba en 0 o dejar que el usuario la edite
            dao.updateIngredient(ingredient.copy(toBuy = false))
        }
    }
}