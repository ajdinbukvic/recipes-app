package com.example.recipeapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.recipeapp.data.AppDatabase
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.ui.theme.RecipeAppTheme
import com.example.recipeapp.viewmodel.RecipeImageViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: RecipeViewModel by viewModels()
    private val imageViewModel: RecipeImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //populateDbIfEmpty()
        setContent {
            RecipeAppTheme {
                MainApp(vm, imageViewModel)
            }
        }
    }
    /*private fun populateDbIfEmpty() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.recipeDao()

        CoroutineScope(Dispatchers.IO).launch {
            val count = dao.getCount()
            if (count == 0) {
                try {
                    val json = assets.open("recipes.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val recipes: List<Recipe> = Gson().fromJson(
                        json,
                        Array<Recipe>::class.java
                    ).toList()

                    dao.insertAll(recipes)
                    Log.d("RecipeApp", "Baza inicijalizovana sa ${recipes.size} recepata")
                } catch (e: Exception) {
                    Log.e("RecipeApp", "Greška pri popunjavanju baze: ${e.message}")
                }
            }
        }
    }*/
}
