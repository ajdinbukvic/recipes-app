package com.example.recipeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.ui.theme.RecipeAppTheme
import com.example.recipeapp.viewmodel.RecipeImageViewModel

class MainActivity : ComponentActivity() {
    private val vm: RecipeViewModel by viewModels()
    private val imageViewModel: RecipeImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeAppTheme {
                MainApp(vm, imageViewModel)
            }
        }
    }
}
