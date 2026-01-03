package com.example.recipeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.recipeapp.model.RecipeImage
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeImageDao {

    @Insert
    suspend fun insert(image: RecipeImage)

    @Query("SELECT * FROM recipe_images WHERE recipeId = :recipeId")
    fun getImagesForRecipe(recipeId: Long): Flow<List<RecipeImage>>

    @Delete
    suspend fun delete(image: RecipeImage)
}