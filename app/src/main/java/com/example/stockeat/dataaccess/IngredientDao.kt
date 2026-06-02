package com.example.stockeat.dataaccess

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stockeat.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients_table WHERE toBuy = 0")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients_table WHERE toBuy = 1")
    fun getShoppingList(): Flow<List<Ingredient>>

    @Query("UPDATE ingredients_table SET toBuy = 1 WHERE id = :id")
    fun addToShoppingList(id: Int)

    @Query("UPDATE ingredients_table SET toBuy = 0 WHERE id = :id")
    fun returnToInventory(id: Int)

}