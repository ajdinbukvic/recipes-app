package com.example.recipeapp.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun saveImageToRecipeFolder(
    context: Context,
    recipeId: Long,
    bitmap: Bitmap
): String {

    val dir = File(
        context.getExternalFilesDir(null),
        "recipes/recipe_$recipeId"
    )

    if (!dir.exists()) dir.mkdirs()

    val file = File(dir, "img_${System.currentTimeMillis()}.png")

    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    return file.absolutePath
}

fun decodeSampledBitmapFromFile(
    filePath: String,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(filePath, options)

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(filePath, options)
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun createImageUri(context: Context, recipeId: Long): Pair<Uri, String> {
    val dir = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "recipes/recipe_$recipeId"
    )
    if (!dir.exists()) dir.mkdirs()

    val file = File(dir, "img_${System.currentTimeMillis()}.jpg")

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    return uri to file.absolutePath
}

fun decodeBitmapWithRotation(
    path: String,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    val bitmap = decodeSampledBitmapFromFile(path, reqWidth, reqHeight)

    val exif = ExifInterface(path)
    val rotation = when (
        exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    if (rotation == 0) return bitmap

    val matrix = Matrix().apply {
        postRotate(rotation.toFloat())
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}

fun saveImageToGallery(
    context: Context,
    sourcePath: String
) {
    val sourceFile = File(sourcePath)
    if (!sourceFile.exists()) return

    val fileName = "recipe_${System.currentTimeMillis()}.jpg"

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/RecipeApp"
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ) ?: return

    resolver.openOutputStream(uri)?.use { output ->
        sourceFile.inputStream().use { input ->
            input.copyTo(output)
        }
    }

    values.clear()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    resolver.update(uri, values, null, null)

    Toast.makeText(
        context,
        "Slika spremljena u Galeriju (RecipeApp)",
        Toast.LENGTH_SHORT
    ).show()
}
