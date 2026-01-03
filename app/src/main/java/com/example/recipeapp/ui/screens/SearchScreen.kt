package com.example.recipeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeapp.model.Category
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.viewmodel.RecipeViewModel

@Composable
fun SearchScreen(vm: RecipeViewModel, navController: NavController) {
    var query by remember { mutableStateOf("") }
    val recipes = vm.searchRecipes.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var hasSearched by remember { mutableStateOf(false) } // prati da li je pretraga izvršena
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isFilterActive by remember { mutableStateOf(false) }
    //val categories by vm.categories.collectAsState() // lista svih kategorija
    val categories = vm.categoriesWithCount.collectAsState()
    /*Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEDE7F6)).padding(top = 48.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)) {
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Pretraga") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { vm.search(query) }) { Text("Traži") }
        Spacer(modifier = Modifier.height(12.dp))
        val recipes = vm.searchRecipes.collectAsState()
        Column {
            recipes.value.forEach { r ->
                RecipeRow(r.naziv, onClick = { navController.navigate("detail/${r.id}") })
            }
        }
    }*/
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1BEE7))
            .padding(12.dp)
    ) {
        // Naslov
        Text(
            text = "Pretraga recepata (${recipes.value.size})",
            style = MaterialTheme.typography.h4,
            color = Color(0xFF2D0F4A),
            modifier = Modifier.padding(top = 36.dp, bottom = 16.dp)
        )

        // Row za TextField + X dugme
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Pretraga") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF311B92),
                    backgroundColor = Color(0xFFF8EAF6),
                    focusedBorderColor = Color(0xFF311B92),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF311B92)
                )
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        query = ""
                        vm.clearSearchResults()
                        hasSearched = false
                        focusManager.clearFocus()
                    },
                    //modifier = Modifier.align(Alignment.CenterEnd as Alignment.Vertical) // <--- Ovdje
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Očisti pretragu",
                        tint = Color(0xFF311B92)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dugme Traži
        Button(
            onClick = {
                if (query.isBlank()) {
                    focusManager.clearFocus()
                    return@Button // ⬅ ništa se ne dešava
                }

                vm.search(query)
                hasSearched = true
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Traži")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row za dropdown kategorija + dugme filtriraj/resetuj
        if (categories.value.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Odaberi kategoriju",
                        onValueChange = {},
                        modifier = Modifier
                            .clickable { expanded = true } // klik samo na TextField
                            .fillMaxWidth(), // fill samo unutar weight-a
                        enabled = false,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color(0xFF311B92),
                            backgroundColor = Color(0xFFF8EAF6),
                            disabledTextColor = Color(0xFF311B92),
                            disabledBorderColor = Color.Gray
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth() // dropdown može ići preko cijelog ekrana
                    ) {
                        categories.value.forEach { cat ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategory = Category(
                                        id = cat.id,
                                        name = cat.name
                                    )
                                    expanded = false
                                }
                            ) {
                                Text("${cat.name} (${cat.recipeCount})")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(onClick = {
                    focusManager.clearFocus()
                    if (isFilterActive) {
                        // Resetuj filter
                        selectedCategory = null
                        isFilterActive = false

                        if (query.isNotBlank()) {
                            vm.search(query)
                        } else {
                            vm.clearSearchResults()
                        }
                    } else {
                        // Primijeni filter samo ako je odabrana kategorija
                        selectedCategory?.let {
                            vm.filterByCategory(it.id)
                            isFilterActive = true
                        }
                    }
                }) {
                    Text(if (isFilterActive) "Resetuj" else "Filtriraj")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp)) // ovdje biraš visinu
        val recipes = vm.searchRecipes.collectAsState()
        var recipeToDelete by remember { mutableStateOf<Recipe?>(null) } // state za dijalog
        // Prikaz rezultata ili poruke "Nema rezultata"
        if (recipes.value.isEmpty()) {
            if (hasSearched || isFilterActive) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nema rezultata pretrage",
                        style = MaterialTheme.typography.h6,
                        color = Color(0xFF2D0F4A),
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recipes.value) { r ->
                    RecipeRow(
                        title = r.naziv,
                        isFavorite = r.favorit,
                        onClick = { navController.navigate("detail/${r.id}") },
                        onToggleFavorite = { vm.toggleFavorite(r.id) },
                        onEdit = {
                            // navigacija na edit stranicu
                            navController.navigate("edit/${r.id}")
                        },
                        onDelete = { recipeToDelete = r }
                    )
                }
            }
        }
        // Dijalog se prikazuje **izvan RecipeRow**, direktno u Composable kontekstu
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
