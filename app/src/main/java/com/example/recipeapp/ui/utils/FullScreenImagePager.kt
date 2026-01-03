package com.example.recipeapp.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.recipeapp.model.RecipeImage
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImagePager(
    images: List<RecipeImage>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    onSave: ((RecipeImage) -> Unit)? = null
) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        pageCount = { images.size }
    )
    LaunchedEffect(images, startIndex) {
        pagerState.scrollToPage(startIndex)
    }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD8B4F2))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val image = images[page]
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Ovdje samo za gestures unutar Box-a
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // Ako zoom > 1, dozvoli pan
                                if (zoom != 1f || scale > 1f) {
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset += pan
                                }
                            }
                        }
                ) {
                    //val bitmap = decodeSampledBitmapFromFile(image.imagePath, reqWidth = 800, reqHeight = 800)
                    val bitmap = decodeBitmapWithRotation(
                        path = image.imagePath,
                        reqWidth = 1200,
                        reqHeight = 1600
                    )
                    Image(
                        //bitmap = BitmapFactory.decodeFile(image.imagePath).asImageBitmap(),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { scale = 1f; offset = Offset.Zero },
                                    onTap = { onDismiss() }
                                )
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
            // ===== DELETE ICON (TOP RIGHT) =====
            if (onSave != null) {
                IconButton(
                    onClick = {
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Spremi sliku",
                        tint = Color.White
                    )
                }
            }
    }
        // ===== DELETE CONFIRM DIALOG =====
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Spremanje slike") },
                text = { Text("Želite li spremiti ovu sliku u galeriju?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val image = images[pagerState.currentPage]
                            saveImageToGallery(
                                context = context,
                                sourcePath = image.imagePath
                            )
                            showSaveDialog = false
                        }
                    ) {
                        Text("Spremi", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSaveDialog = false }
                    ) {
                        Text("Odustani")
                    }
                }
            )
        }
    }
}
            /*HorizontalPagerIndicator(
                pagerState = pagerStatee,
                modifier = Modifier
                    //.align(Alignment.BottomCenter)
                    .padding(16.dp),
                activeColor = Color.White,
                inactiveColor = Color.Gray,
            )
            // Delete button (ako je prosleđeno)
            if (onSave != null) {
                IconButton(
                    onClick = { onSave(images[pagerState.currentPage]) },
                    modifier = Modifier
                        //.align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Obriši sliku",
                        tint = Color.White
                    )
                }
            }*/
       //}
    //}
//

@Composable
fun ImageOptionsDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Opcije slike") },
        text = { Text("Želite li obrisati ovu sliku?") },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Obriši", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani")
            }
        }
    )
}
