package com.example.recipeapp.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeapp.viewmodel.RecipeViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.core.view.drawToBitmap
import com.example.recipeapp.model.RecipeImage
import com.example.recipeapp.ui.utils.saveBitmapToGallery
import com.example.recipeapp.ui.utils.saveImageToRecipeFolder
import com.example.recipeapp.viewmodel.RecipeImageViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.graphics.scale
import com.example.recipeapp.ui.utils.FullScreenImagePager
import com.example.recipeapp.ui.utils.createImageUri
import com.example.recipeapp.ui.utils.decodeBitmapWithRotation
import com.example.recipeapp.ui.utils.decodeSampledBitmapFromFile

@Composable
fun RecipeDetailScreen(vm: RecipeViewModel, id: Long, navController: NavController, imageViewModel: RecipeImageViewModel) {
    val context = LocalContext.current
    var fullScreen by remember { mutableStateOf(false) }
    var fullscreenImage by remember { mutableStateOf<RecipeImage?>(null) }
    var imageToDelete by remember { mutableStateOf<RecipeImage?>(null) }
    val images by imageViewModel.images.collectAsState()
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imageFilePath = remember { mutableStateOf<String?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageFilePath.value != null) {
                imageViewModel.addImage(
                    recipeId = id,
                    path = imageFilePath.value!!
                )
            }
        }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val path = saveImageToRecipeFolder(context, id, bitmap)
                imageViewModel.addImage(id, path)
            }
        }
    val scope = rememberCoroutineScope()
    var recipe by remember { mutableStateOf<com.example.recipeapp.model.Recipe?>(null) }
    LaunchedEffect(id) {
        vm.incrementVisitsAndRecentlyViewed(id, System.currentTimeMillis())
        val r = vm.getByIdSuspend(id)
        recipe = r
        imageViewModel.loadImages(id)
    }
    recipe?.let { r ->
        var showDeleteDialog by remember { mutableStateOf(false) }
        var isFavorite by remember { mutableStateOf(r.favorit) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFE1BEE7))
                .padding(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Nazad",
                        tint = Color(0xFF2D0F4A)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val parts = mutableListOf<String>()

                        parts.add("Naziv:\n${r.naziv}")

                        if (!r.sastojci.isNullOrBlank()) {
                            parts.add("Sastojci:\n${r.sastojci.trim()}")
                        }

                        if (!r.postupak.isNullOrBlank()) {
                            parts.add("Postupak:\n${r.postupak.trim()}")
                        }

                        val finalText = parts.joinToString("\n\n")

                        val clip = ClipData.newPlainText("recept", finalText)
                        clipboard.setPrimaryClip(clip)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Tekst kopiran u clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopiraj")
                    }

                    IconButton(onClick = {
                        try {
                            val activity = context as Activity
                            val rootView = activity.window.decorView.rootView
                            val bitmap = rootView.drawToBitmap()
                            saveBitmapToGallery(context, bitmap)
                            Toast.makeText(context, "Screenshot spremljen u galeriju!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Screenshot, contentDescription = "Screenshot")
                    }

                    IconButton(onClick = {
                        val updated = r.copy(favorit = !isFavorite)
                        vm.update(updated)
                        isFavorite = updated.favorit
                        Toast.makeText(
                            context,
                            if (isFavorite) "Dodano u favorite" else "Uklonjeno iz favorita",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (isFavorite) Color(0xFF4A148C) else Color(0xFF4A148C)
                        )
                    }
                    IconButton(onClick = {
                        val parts = mutableListOf<String>()

                        parts.add("Naziv:\n${r.naziv}")

                        if (!r.sastojci.isNullOrBlank()) {
                            parts.add("Sastojci:\n${r.sastojci.trim()}")
                        }

                        if (!r.postupak.isNullOrBlank()) {
                            parts.add("Postupak:\n${r.postupak.trim()}")
                        }

                        val finalText = parts.joinToString("\n\n")
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, finalText)
                        }

                        try {
                            context.startActivity(Intent.createChooser(intent, "Podijeli recept preko..."))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Nema aplikacija za dijeljenje", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Podijeli recept",
                            tint = Color(0xFF4A148C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = r.naziv,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { navController.navigate("edit/${r.id}") }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF4A148C))
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Potvrda") },
                    text = { Text("Da li ste sigurni da želite obrisati recept?") },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.delete(r)
                            Toast.makeText(context, "Recept obrisan", Toast.LENGTH_SHORT).show()
                            showDeleteDialog = false
                            navController.popBackStack()
                        }) { Text("Da") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Ne") }
                    }
                )
            }

            if(r.sastojci.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sastojci",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF2D0F4A),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFEED9FF))
                        .padding(12.dp)
                ) {
                    Text(text = r.sastojci, style = MaterialTheme.typography.body1, color = Color(0xFF2D0F4A))
                }

            }
            if (r.postupak.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Postupak",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF2D0F4A),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFEED9FF))
                        .padding(12.dp)
                ) {
                    Text(text = r.postupak, style = MaterialTheme.typography.body1, color = Color(0xFF2D0F4A))
                }

            }
            if (r.napomena.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Napomena",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF2D0F4A),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFEED9FF))
                        .padding(12.dp)
                ) {
                    Text(
                        text = r.napomena,
                        style = MaterialTheme.typography.body1,
                        color = Color(0xFF2D0F4A)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Slike (${images.size})",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF2D0F4A),
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        val (uri, path) = createImageUri(context, id)
                        imageUri.value = uri
                        imageFilePath.value = path
                        cameraLauncher.launch(uri)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Kamera")
                    }

                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Galerija")
                    }
                }
            }
            if (images.isNotEmpty()) {

                Spacer(Modifier.height(12.dp))
                var currentIndex by remember { mutableStateOf(0) }
                RecipeImagePager(
                    images = images,
                    onImageClick = { clickedImage ->
                        fullscreenImage = clickedImage
                        fullScreen = true
                        currentIndex = images.indexOfFirst { it.id == clickedImage.id }
                    },
                    onLongPress = { imageToDelete = it },
                    onPageChanged = { index ->
                        currentIndex = index
                    }
                )

                if (fullScreen && fullscreenImage != null) {
                   fullscreenImage?.let {
                        FullScreenImagePager(
                            images = images,
                            startIndex = images.indexOfFirst { it.id == fullscreenImage!!.id },
                            onDismiss = { fullScreen = false },
                            onSave = { image ->
                                imageViewModel.deleteImage(image)
                                fullScreen = false
                            }
                        )
                    }
               }

                imageToDelete?.let {
                    ImageOptionsDialog(
                        onDelete = {
                            imageViewModel.deleteImage(it)
                            imageToDelete = null
                        },
                        onDismiss = { imageToDelete = null }
                    )
                }
                if (images.isNotEmpty()) {
                    Text(
                        text = "Slika ${currentIndex + 1}/${images.size}",
                        color = Color(0xFF4A148C),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .background(Color(0xFFD8B4F2))
                    )
                }
            }
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "recept_screenshot_${System.currentTimeMillis()}.png"
        val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        } ?: run {
            Toast.makeText(context, "Ne mogu spremiti screenshot", Toast.LENGTH_SHORT).show()
        }
    }
    fun resizeBitmap(bitmap: Bitmap, maxSize: Int = 1080): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return bitmap.scale(newWidth, newHeight)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeImagePager(
    images: List<RecipeImage>,
    onImageClick: (RecipeImage) -> Unit,
    onLongPress: (RecipeImage) -> Unit,
    onPageChanged: ((Int) -> Unit)? = null
) {
    if (images.isEmpty()) return
    val pagerState = rememberPagerState(
        pageCount = { images.size }
    )
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged?.invoke(pagerState.currentPage)
    }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) { page ->

        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false
                )
                .background(
                    color = Color(0xFFD8B4F2),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            val image = images[page]
            val bitmap = decodeBitmapWithRotation(
                path = image.imagePath,
                reqWidth = 800,
                reqHeight = 800
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { onImageClick(image) },
                        onLongClick = { onLongPress(image) }
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun FullscreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val bitmap = decodeSampledBitmapFromFile(imagePath, reqWidth = 800, reqHeight = 800)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentScale = ContentScale.Fit
        )
    }
}

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

