package com.example.stockeat

@androidx.room.Entity(tableName = "ingredients_table")
data class Ingredient(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String = "ud",
    val toBuy: Boolean = false,
    val expiryDate: Long? = null,
    val imageUrl: String? = null
)

