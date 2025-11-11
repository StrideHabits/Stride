package com.mpieterse.stride.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Utility functions for image conversion between Bitmap and Base64.
 */

/**
 * Converts a Bitmap to a Base64 string.
 * Automatically selects format: PNG if bitmap has alpha channel, JPEG otherwise.
 * 
 * @param bitmap The bitmap to convert
 * @return Base64 encoded string, or null if conversion fails
 */
fun bitmapToBase64(bitmap: Bitmap): String? {
    return try {
        val format = if (bitmap.hasAlpha()) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        
        val quality = if (format == Bitmap.CompressFormat.PNG) {
            100 // PNG is lossless
        } else {
            85 // JPEG quality (85% is a good balance)
        }
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        outputStream.close()
        
        Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

/**
 * Converts a Base64 string to a Bitmap.
 * 
 * @param base64 The Base64 encoded image string
 * @return Bitmap, or null if conversion fails
 */
fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

