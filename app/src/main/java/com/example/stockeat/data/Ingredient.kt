package com.example.stockeat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients_table")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String = "ud",
    val toBuy: Boolean = false,
    val expiryDate: Long? = null,
    val imageUrl: String? = null
)

