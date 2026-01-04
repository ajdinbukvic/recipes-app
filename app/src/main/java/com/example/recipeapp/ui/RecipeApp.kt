package com.example.recipeapp.ui

import android.app.Application
import android.util.Log
import com.example.recipeapp.data.AppDatabase
import com.example.recipeapp.model.Category
import com.example.recipeapp.model.Recipe
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        populateDbIfEmpty()
    }

    private fun populateDbIfEmpty() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.recipeDao()
        val categoryDao = db.categoryDao()

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Seed kategorije ako je prazno
            if (categoryDao.getCount() == 0) {
                val categories = listOf(
                    Category(name = "Kolač"),
                    Category(name = "Torta"),
                    Category(name = "Rolat"),
                    Category(name = "Pita"),
                    Category(name = "Sokovi i salate"),
                    Category(name = "Ostalo")
                )
                categoryDao.insertAll(categories)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            val count = dao.getCount()
            if (count == 0) {
                try {
                    val json = assets.open("recipes.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val recipes: List<Recipe> = Gson().fromJson(json, Array<Recipe>::class.java)
                        .map { r ->
                            r.copy(
                                napomena = r.napomena ?: "",
                                sastojci = r.sastojci ?: "",
                                postupak = r.postupak ?: "",
                                createdAt = if (r.createdAt == 0L) System.currentTimeMillis() else r.createdAt,
                                lastViewedAt = r.lastViewedAt
                            )
                        }

                    dao.insertAll(recipes)
                    Log.d("RecipeApp", "Baza inicijalizovana sa ${recipes.size} recepata")
                } catch (e: Exception) {
                    Log.e("RecipeApp", "Greška pri popunjavanju baze: ${e.message}")
                }
            }
        }
    }
}
