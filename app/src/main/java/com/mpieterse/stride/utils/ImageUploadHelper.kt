package com.mpieterse.stride.utils

import android.content.Context
import android.util.Base64
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.repo.concrete.UploadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared helper for image upload and URL sanitization operations.
 * 
 * This utility consolidates image handling logic that was previously duplicated
 * across HomeDatabaseViewModel and HabitViewerViewModel to ensure consistency
 * and reduce maintenance burden.
 */
@Singleton
class ImageUploadHelper @Inject constructor(
    private val uploadRepo: UploadRepository
) {
    companion object {
        private const val TAG = "ImageUploadHelper"
    }

    /**
     * Uploads a base64-encoded image to the server.
     * 
     * @param context Application context for creating temp files
     * @param base64 Base64-encoded image data
     * @param mimeType MIME type of the image (e.g., "image/png", "image/jpeg")
     * @return Local file path if upload succeeds, null otherwise
     */
    suspend fun uploadImage(
        context: Context,
        base64: String,
        mimeType: String?
    ): String? = withContext(Dispatchers.IO) {
        val extension = when (mimeType) {
            "image/png" -> ".png"
            else -> ".jpg"
        }
        var tempFile: File? = null
        return@withContext try {
            // Create temp file inside try block to handle creation errors consistently
            tempFile = File.createTempFile("habit-", extension, context.cacheDir)
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            tempFile.writeBytes(bytes)
            when (val result = uploadRepo.upload(tempFile.path)) {
                is ApiResult.Ok -> result.data.localPath
                is ApiResult.Err -> {
                    Clogger.e(TAG, "Image upload failed: ${result.message}")
                    null
                }
            }
        } catch (e: Exception) {
            // Re-throw CancellationException to respect coroutine cancellation
            if (e is CancellationException) throw e
            // Image upload failures should not block habit creation/update
            // Log the error but return null to allow operation to continue
            Clogger.e(TAG, "Image upload exception", e)
            null
        } finally {
            // Safely delete temp file if it was created
            tempFile?.delete()
        }
    }

    /**
     * Sanitizes an image URL by converting HTTP to HTTPS.
     * 
     * @param url The image URL to sanitize
     * @return Sanitized URL with HTTPS, or null if input is null
     */
    fun sanitizeImageUrl(url: String?): String? {
        return url?.let {
            if (it.startsWith("http://", ignoreCase = true)) {
                "https://${it.removePrefix("http://")}"
            } else {
                it
            }
        }
    }
}

