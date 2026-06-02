package com.example.stockeat.models


import com.squareup.moshi.Json

data class FoodProductResponse(
    @Json(name = "status") val status: String,
    @Json(name = "nombre") val nombre: String?,
    @Json(name = "marca") val marca: String?,
    @Json(name = "categorias") val categorias: String?,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "mensaje") val mensaje: String? = null
)
