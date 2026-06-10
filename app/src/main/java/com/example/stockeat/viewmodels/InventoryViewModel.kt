package com.example.stockeat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockeat.data.Ingredient
import com.example.stockeat.api_service.RetrofitClient
import com.example.stockeat.dataaccess.IngredientDao
import kotlinx.coroutines.launch

class InventoryViewModel(private val dao: IngredientDao) : ViewModel() {

    val ingredients = dao.getAllIngredients()

    fun addIngredient(name : String, quantity : Double, unit: String, toBuy: Boolean, expiryDate: Long? = null, imageUrl: String? = null) {
        val ingredient =
            Ingredient(nombre = name, cantidad = quantity, unidad = unit, toBuy = toBuy, expiryDate = expiryDate, imageUrl = imageUrl)
        viewModelScope.launch {
            dao.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient)
        }
    }

    fun consumeIngredient(ingredient: Ingredient, amountToConsume: Double, onExhausted: (Ingredient) -> Unit = {}) {
        viewModelScope.launch {
            val newQuantity = (ingredient.cantidad - amountToConsume).coerceAtLeast(0.0)
            val updated = ingredient.copy(cantidad = newQuantity)
            if (newQuantity == 0.0) {
                // Notificamos pero no actualizamos toBuy automáticamente
                onExhausted(updated)
            } else {
                dao.updateIngredient(updated)
            }
        }
    }

    fun moveToShoppingList(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.updateIngredient(ingredient.copy(toBuy = true))
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            dao.deleteIngredient(ingredient)
        }
    }

    fun scanProduct(barcode: String, onResult: (String, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getProduct(barcode)
                if (response.status == "encontrado") {
                    onResult(response.nombre ?: "", response.imageUrl)
                }
            } catch (e: Exception) {

            }
        }
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Sin fecha"
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}