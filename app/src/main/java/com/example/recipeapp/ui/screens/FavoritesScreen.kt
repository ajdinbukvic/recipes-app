package com.example.recipeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.viewmodel.RecipeViewModel

@Composable
fun FavoritesScreen(vm: RecipeViewModel, navController: NavController) {
    vm.loadFavorites()
    val recipes = vm.favRecipes.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1BEE7))
            .padding(12.dp)
    ) {

        Text(
            text = "Omiljeni recepti (${recipes.value.size})",
            style = MaterialTheme.typography.h4,
            color = Color(0xFF2D0F4A),
            modifier = Modifier.padding(top = 36.dp, bottom = 16.dp)
        )
        var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(recipes.value) { r ->
                RecipeRow(
                    title = r.naziv,
                    isFavorite = r.favorit,
                    onClick = { navController.navigate("detail/${r.id}") },
                    onToggleFavorite = { vm.toggleFavorite(r.id) },
                    onEdit = {
                        navController.navigate("edit/${r.id}")
                    },
                    onDelete = { recipeToDelete = r }
                )
            }
        }
        recipeToDelete?.let { r ->
            AlertDialog(
                onDismissRequest = { recipeToDelete = null },
                title = { Text("Brisanje recepta") },
                text = { Text("Jeste li sigurni da želite obrisati '${r.naziv}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.delete(r)
                        recipeToDelete = null
                    }) {
                        Text("Da")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recipeToDelete = null }) {
                        Text("Ne")
                    }
                }
            )
        }
    }
}
