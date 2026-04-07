package com.rinnsan.noteapp.utils

import android.content.Context
import android.net.Uri
import java.io.File


fun uriToFile(uri: Uri, context: Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "temp_product_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use { output ->
            inputStream.use { it.copyTo(output) }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
