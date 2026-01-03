package com.example.recipeapp.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.withContext
import java.io.OutputStream

fun captureViewBitmap(view: View): Bitmap {
    val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(widthSpec, heightSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

    val bitmap = createBitmap(view.measuredWidth, view.measuredHeight)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val filename = "recept_screenshot_" + System.currentTimeMillis() + ".png"
    val file = File(context.cacheDir, filename)
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    fos.flush()
    fos.close()
    return file.toUri()
}

fun shareImage(context: Context, uri: Uri) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Podijeli screenshot"))
}

suspend fun captureLongScreenshot(
    context: Context,
    content: @Composable () -> Unit,
    widthPx: Int = 1080
) {
    withContext(Dispatchers.Main) {
        // Napravi offscreen ComposeView
        val composeView = ComposeView(context).apply {
            setContent {
                content()
            }
        }

        // Measure & layout bez vertikalnog ograničenja
        composeView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(widthPx, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.UNSPECIFIED
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        // Renderuj u bitmap dužine sadržaja
        val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)
        val canvas = android.graphics.Canvas(bitmap)
        composeView.draw(canvas)

        // Spremi u galeriju
        val success = saveBitmapToGallery(context, bitmap)
        Toast.makeText(
            context,
            if (success) "Screenshot spremljen u galeriju!" else "Greška pri spremanju screenshot-a",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "recept_${System.currentTimeMillis()}.png"
        val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/RecipeApp")
            }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM)
            val file = java.io.File(imagesDir, filename)
            java.io.FileOutputStream(file)
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }  // Spremi bitmap
        true  // Ako nije bacio exception, vraća true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Funkcija za spremanje u galeriju
/*fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "recept_${System.currentTimeMillis()}.png"
        val fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )!!
            context.contentResolver.openOutputStream(uri)
        } else {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename)
            FileOutputStream(file)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}*/

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
