package com.mpieterse.stride.data.repo.concrete

import android.content.Context
import com.mpieterse.stride.core.net.safeCall
import com.mpieterse.stride.data.dto.uploads.LocalUploadResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class UploadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context // removes annotation warning
) {

    /**
     * Save an image file locally inside the app's private "images" directory,
     * then wrap the result in a Retrofit Response so existing call sites still work.
     */
    suspend fun upload(sourcePath: String) = safeCall<LocalUploadResponse> {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            // Let safeCall convert this into ApiResult.Err
            throw IllegalArgumentException("Source file does not exist: $sourcePath")
        }

        val imagesDir = File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }

        val destFile = File(
            imagesDir,
            "${sourceFile.nameWithoutExtension}_${System.currentTimeMillis()}.${sourceFile.extension}"
        )

        sourceFile.copyTo(destFile, overwrite = true)

        val body = LocalUploadResponse(localPath = destFile.absolutePath)
        Response.success(body)
    }

    /**
     * Delete a previously-saved local image.
     */
    suspend fun delete(localPath: String) = safeCall<Boolean> {
        val file = File(localPath)
        val deleted = file.exists() && file.delete()
        Response.success(deleted)
    }
}
