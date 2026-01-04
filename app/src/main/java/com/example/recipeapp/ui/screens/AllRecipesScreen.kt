package com.example.recipeapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.ui.utils.SortType
import com.example.recipeapp.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun AllRecipesScreen(vm: RecipeViewModel, navController: NavController) {
    vm.loadAll()
    val recipes = vm.allRecipes.collectAsState()
    var tapCount by remember { mutableStateOf(0) }
    var showExport by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 4
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1BEE7))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Svi recepti (${recipes.value.size})",
                    style = MaterialTheme.typography.h4,
                    color = Color(0xFF2D0F4A),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            tapCount++
                            if (tapCount == 5) {
                                showExport = true
                                tapCount = 0
                            }
                        }
                )

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sortiranje")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SortType.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.value) },
                                onClick = {
                                    vm.setSort(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (showExport) {
                IconButton(onClick = {
                    vm.exportDatabaseToDownloads(context)
                    showExport = false
                }) {
                    Icon(Icons.Default.Backup, contentDescription = "Export")
                }
            }

            var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(recipes.value) { r ->
                    RecipeRow(
                        title = r.naziv,
                        isFavorite = r.favorit,
                        onClick = { navController.navigate("detail/${r.id}") },
                        onToggleFavorite = { vm.toggleFavorite(r.id) },
                        onEdit = { navController.navigate("edit/${r.id}") },
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
                        }) { Text("Da") }
                    },
                    dismissButton = {
                        TextButton(onClick = { recipeToDelete = null }) { Text("Ne") }
                    }
                )
            }
        }

        ScrollProgressIndicator(
            listState = listState,
            itemCount = recipes.value.size,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                backgroundColor = Color(0xFF7E57C2)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Na vrh", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeRow(
    title: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete()
                    true
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    true
                }
                else -> false
            }
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != DismissValue.Default) {
            dismissState.reset()
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        modifier = Modifier.padding(vertical = 4.dp),
        dismissThresholds = { direction ->
            FractionalThreshold(0.5f)
        },
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color = when (direction) {
                DismissDirection.StartToEnd -> Color.Blue
                DismissDirection.EndToStart -> Color.Red
            }
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Edit
                DismissDirection.EndToStart -> Icons.Default.Delete
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                }
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xFFEED9FF)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onToggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFF4A148C) else Color(0xFF4A148C)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ScrollProgressIndicator(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    if (itemCount == 0) return

    val progress by remember {
        derivedStateOf {
            val visibleIndex = listState.firstVisibleItemIndex
            visibleIndex.toFloat() / itemCount.toFloat()
        }
    }

    Box(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.small)
            .background(Color.Black.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(progress.coerceIn(0f, 1f))
                .background(Color(0xFF7B1FA2))
                .align(Alignment.TopCenter)
        )
    }
}