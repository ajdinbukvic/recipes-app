package com.example.recipeapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.recipeapp.ui.navigation.Screen


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.All, Screen.Search, Screen.Favorites, Screen.Top, Screen.Add)
    BottomNavigation(
    modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        items.forEach { screen ->
            val icon = when(screen) {
                Screen.All -> Icons.Default.List
                Screen.Search -> Icons.Default.Search
                Screen.Favorites -> Icons.Default.Favorite
                Screen.Top -> Icons.Default.Star
                Screen.Add -> Icons.Default.Add
                else -> Icons.Default.Home
            }

            BottomNavigationItem(
                icon = { Icon(icon, contentDescription = screen.route) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false
            )
        }
    }
}
