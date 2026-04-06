package com.example.financecompanion.feature.receipt

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ReceiptImageProvider {
    fun createTempUri(context: Context): Uri {
        val imageDir = File(context.cacheDir, "receipt_images").apply { mkdirs() }
        val tempFile = File.createTempFile("receipt_", ".jpg", imageDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
}
