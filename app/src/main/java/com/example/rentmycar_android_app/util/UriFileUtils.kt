package com.example.rentmycar_android_app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToTempFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Kan afbeelding niet openen")

    val file = File.createTempFile("upload_", ".jpg", context.cacheDir)

    FileOutputStream(file).use { out ->
        input.use { it.copyTo(out) }
    }

    return file
}