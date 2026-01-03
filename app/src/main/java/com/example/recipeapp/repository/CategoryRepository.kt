package com.example.recipeapp.repository

import com.example.recipeapp.data.CategoryDao
import com.example.recipeapp.data.CategoryWithCount
import com.example.recipeapp.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {

    val categories = dao.getAll()

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.insertAll(
                listOf(
                    Category(name = "Kolač"),
                    Category(name = "Torta"),
                    Category(name = "Rolat"),
                    Category(name = "Pita"),
                    Category(name = "Sokovi i salate"),
                    Category(name = "Ostalo")
                )
            )
        }
    }

    val categoriesWithCount: Flow<List<CategoryWithCount>> =
        dao.getCategoriesWithCount()
}
