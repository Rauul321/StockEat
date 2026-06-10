package com.example.stockeat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stockeat.data.Ingredient
import com.example.stockeat.data.RecipeEntity
import com.example.stockeat.dataaccess.Converters
import com.example.stockeat.dataaccess.IngredientDao
import com.example.stockeat.dataaccess.RecipeDao

@Database(entities = [Ingredient::class, RecipeEntity::class], version = 7)
@TypeConverters(Converters::class)
abstract class StockEatDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: StockEatDatabase? = null

        fun getDatabase(context: Context): StockEatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockEatDatabase::class.java,
                    "stockeat_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}