package com.example.recipeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopScreen(vm: RecipeViewModel, navController: NavController) {
    vm.loadTop()
    vm.loadLastAdded()
    vm.loadRecentlyViewed()
    val recipes by vm.topRecipes.collectAsState(initial = emptyList())
    val lastAddedRecipes by vm.lastAdded.collectAsState(initial = emptyList())
    val recentlyViewedRecipes by vm.recentlyViewed.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1BEE7))
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Statistika recepata",
            style = MaterialTheme.typography.h4,
            color = Color(0xFF2D0F4A),
            modifier = Modifier.padding(top = 36.dp, bottom = 16.dp)
        )
        AccordionSection(
            title = "Najposjećeniji recepti",
            items = recipes,
            navController = navController,
            vm = vm,
            formatItemText = { r, index -> "${r.naziv} (${r.posjete})" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        AccordionSection(
            title = "Zadnje dodani recepti",
            items = lastAddedRecipes,
            navController = navController,
            vm = vm,
            formatItemText = { r, index -> "${r.naziv} (${r.createdAt.format()})" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        AccordionSection(
            title = "Nedavno pregledani recepti",
            items = recentlyViewedRecipes,
            navController = navController,
            vm = vm,
            formatItemText = { r, index -> "${r.naziv} (${r.lastViewedAt?.format()})" }
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun AccordionSection(
    title: String,
    items: List<Recipe>,
    navController: NavController,
    vm: RecipeViewModel,
    formatItemText: (Recipe, Int) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color(0xFFD8B4F2))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.weight(1f),
                color = Color(0xFF2D0F4A)
            )
            Icon(
                imageVector = if (expanded)
                    Icons.Default.ExpandLess
                else
                    Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text(
                    text = "Nema recepata",
                    color = Color(0xFF2D0F4A)
                )
            } else {
                items.forEachIndexed { index, r ->
                    RecipeRow(
                        title = "${index + 1}. " + formatItemText(r, index),
                        isFavorite = r.favorit,
                        onClick = { navController.navigate("detail/${r.id}") },
                        onToggleFavorite = { vm.toggleFavorite(r.id) },
                        onEdit = { navController.navigate("edit/${r.id}") },
                        onDelete = { recipeToDelete = r }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
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

fun Long.format(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}