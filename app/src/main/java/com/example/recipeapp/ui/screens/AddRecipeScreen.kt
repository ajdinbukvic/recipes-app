package com.example.recipeapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recipeapp.model.Category
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.viewmodel.RecipeViewModel

@Composable
fun AddRecipeScreen(vm: RecipeViewModel, navController: NavController, editId: Long = 0L) {
    var naziv by remember { mutableStateOf("") }
    var sastojci by remember { mutableStateOf("") }
    var postupak by remember { mutableStateOf("") }
    var napomena by remember { mutableStateOf("") }
    val categories by vm.categories.collectAsState()
    var recipeCategoryId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val context = LocalContext.current  // Potrebno za Toast

    /*Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(value = naziv, onValueChange = { naziv = it }, label = { Text("Naziv") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = sastojci, onValueChange = { sastojci = it }, label = { Text("Sastojci") }, modifier = Modifier.fillMaxWidth(), maxLines = 6)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = postupak, onValueChange = { postupak = it }, label = { Text("Postupak") }, modifier = Modifier.fillMaxWidth(), maxLines = 10)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            val r = Recipe(naziv = naziv, sastojci = sastojci, postupak = postupak)
            vm.insert(r) { id -> navController.navigate("detail/$id") }
        }) { Text("Spremi") }
    }*/

    // Ako je editId > 0, učitaj recept
    LaunchedEffect(editId) {
        if (editId > 0) {
            val r = vm.getByIdSuspend(editId)
            r?.let {
                naziv = it.naziv
                sastojci = it.sastojci
                postupak = it.postupak
                napomena = it.napomena
                recipeCategoryId = it.categoryId
            }
        }
    }

    /*LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory == null) {
            selectedCategory = categories.first() // ili find { it.name == "Kolač" }
        }
    }*/

    LaunchedEffect(categories, recipeCategoryId) {
        if (categories.isNotEmpty()) {

            selectedCategory = when {
                // EDIT MODE → pronađi kategoriju recepta
                recipeCategoryId != null ->
                    categories.find { it.id == recipeCategoryId }

                // ADD MODE → nema recepta, nema kategorije
                else -> categories.first()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1BEE7))
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // Naslov
        Text(
            text = if (editId > 0) "Uredi recept" else "Dodaj novi recept",
            style = MaterialTheme.typography.h4,
            color = Color(0xFF2D0F4A),
            modifier = Modifier.padding(top = 36.dp, bottom = 16.dp)
        )
        // Naziv
        OutlinedTextField(
            value = naziv,
            onValueChange = { naziv = it },
            label = { Text("Naziv") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF311B92),
                backgroundColor = Color(0xFFF8EAF6),
                focusedBorderColor = Color(0xFF311B92),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF311B92)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sastojci (veći text field)
        OutlinedTextField(
            value = sastojci,
            onValueChange = { sastojci = it },
            label = { Text("Sastojci") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp), // veći prostor za unos
            maxLines = 10,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF311B92),
                backgroundColor = Color(0xFFF8EAF6),
                focusedBorderColor = Color(0xFF311B92),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF311B92)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Postupak (veći text field)
        OutlinedTextField(
            value = postupak,
            onValueChange = { postupak = it },
            label = { Text("Postupak") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp), // još veći prostor
            maxLines = 15,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF311B92),
                backgroundColor = Color(0xFFF8EAF6),
                focusedBorderColor = Color(0xFF311B92),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF311B92)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Napomena (malo veći text field)
        OutlinedTextField(
            value = napomena,
            onValueChange = { napomena = it },
            label = { Text("Napomena") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // malo veći prostor
            maxLines = 15,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF311B92),
                backgroundColor = Color(0xFFF8EAF6),
                focusedBorderColor = Color(0xFF311B92),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF311B92)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategorija") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color(0xFF311B92),
                    backgroundColor = Color(0xFFF8EAF6),
                    focusedBorderColor = Color(0xFF311B92),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF311B92)
                ),
                modifier = Modifier
                    .clickable { expanded = true } // klik samo na TextField
                    .fillMaxWidth(), // fill samo unutar weight-a
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(onClick = {
                        selectedCategory = cat
                        expanded = false
                    }) {
                        Text(cat.name)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Dugme centrirano i prošireno horizontalno
        Button(
            onClick = {
                if (selectedCategory == null) {
                    Toast.makeText(context, "Odaberite kategoriju", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (naziv.isBlank()) {
                    Toast.makeText(
                        context,
                        "Naziv recepta je obavezan",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                val r = Recipe(
                    id = if (editId > 0) editId else 0L,
                    naziv = naziv,
                    sastojci = sastojci,
                    postupak = postupak,
                    napomena = napomena,
                    categoryId = selectedCategory!!.id
                )
                if (editId > 0) {
                    vm.update(r)
                } else {
                    vm.insert(r)
                }
                Toast.makeText(context, "Recept spremljen", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth() // dugme preko cijele širine
        ) {
            Text(if (editId > 0) "Spremi promjene" else "Spremi novi recept")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
