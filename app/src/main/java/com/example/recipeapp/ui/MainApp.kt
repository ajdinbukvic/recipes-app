package com.example.recipeapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.ui.navigation.MainNavGraph
import com.example.recipeapp.viewmodel.RecipeImageViewModel
import com.example.recipeapp.viewmodel.RecipeViewModel

@Composable
fun MainApp(vm: RecipeViewModel, imageViewModel: RecipeImageViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        MainNavGraph(vm = vm, modifier = Modifier.padding(padding), navController = navController, imageViewModel = imageViewModel)
    }
}
