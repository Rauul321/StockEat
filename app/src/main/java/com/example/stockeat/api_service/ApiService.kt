package com.example.stockeat.api_service
import com.example.stockeat.models.FoodProductResponse
import retrofit2.http.Path
import com.example.stockeat.models.RecipeRequest
import com.example.stockeat.models.RecipeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/stockeat/generate-recipes")
    suspend fun generateRecipes(@Body request: RecipeRequest): RecipeResponse

    @GET("api/stockeat/product/{barcode}")
    suspend fun getProduct(@Path("barcode") barcode: String): FoodProductResponse

}
