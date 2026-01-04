package com.example.recipeapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.AppDatabase
import com.example.recipeapp.model.RecipeImage
import com.example.recipeapp.repository.RecipeImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecipeImageRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RecipeImageRepository(db.recipeImageDao())
    }

    private val _images = MutableStateFlow<List<RecipeImage>>(emptyList())
    val images: StateFlow<List<RecipeImage>> = _images.asStateFlow()

    fun loadImages(recipeId: Long) {
        viewModelScope.launch {
            repository.getImagesForRecipe(recipeId)
                .collect { list ->
                    _images.value = list
                }
        }
    }

    fun addImage(recipeId: Long, path: String) {
        viewModelScope.launch {
            repository.insert(
                RecipeImage(
                    recipeId = recipeId,
                    imagePath = path
                )
            )
        }
    }
    fun deleteImage(image: RecipeImage) {
        viewModelScope.launch {
            repository.delete(image)
        }
    }
}