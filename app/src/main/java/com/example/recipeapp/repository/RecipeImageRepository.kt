package com.example.recipeapp.repository

import com.example.recipeapp.data.RecipeImageDao
import com.example.recipeapp.model.RecipeImage
import kotlinx.coroutines.flow.Flow

class RecipeImageRepository(
    private val dao: RecipeImageDao
) {
    fun getImagesForRecipe(recipeId: Long): Flow<List<RecipeImage>> {
        return dao.getImagesForRecipe(recipeId)
    }

    suspend fun insert(image: RecipeImage) {
        dao.insert(image)
    }

    suspend fun delete(image: RecipeImage) {
        dao.delete(image)
    }
}