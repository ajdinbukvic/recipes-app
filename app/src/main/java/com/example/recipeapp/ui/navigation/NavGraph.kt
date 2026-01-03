package com.example.recipeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.ui.screens.*
import com.example.recipeapp.viewmodel.RecipeImageViewModel

sealed class Screen(val route: String) {
    object All : Screen("all")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Top : Screen("top")
    object Add : Screen("add")
    object Detail : Screen("detail/{id}") {
        fun createRoute(id: Long) = "detail/$id"
    }
    object Edit: Screen("edit/{id}") {
        fun createRoute(id: Long) = "edit/$id"
    }
}

@Composable
fun MainNavGraph(vm: RecipeViewModel, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(), imageViewModel: RecipeImageViewModel) {
    NavHost(navController = navController, startDestination = Screen.All.route, modifier = modifier) {
        composable(Screen.All.route) { AllRecipesScreen(vm, navController) }
        composable(Screen.Search.route) { SearchScreen(vm, navController) }
        composable(Screen.Favorites.route) { FavoritesScreen(vm, navController) }
        composable(Screen.Top.route) { TopScreen(vm, navController) }
        composable(Screen.Add.route) { AddRecipeScreen(vm, navController) }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            RecipeDetailScreen(vm, id, navController, imageViewModel)
        }
        composable("edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            AddRecipeScreen(vm, navController, id)
        }
    }
}
