package com.example.recipeapp.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun RecipeAppTheme(content: @Composable() () -> Unit) {
    MaterialTheme {
        content()
    }
}
