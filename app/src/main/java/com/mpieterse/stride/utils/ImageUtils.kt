package com.mpieterse.stride.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Converts a Bitmap to a Base64-encoded string.
 *
 * Automatically selects the appropriate compression format:
 * - PNG format (100% quality) for bitmaps with transparency (alpha channel)
 * - JPEG format (85% quality) for bitmaps without transparency
 *
 * @param bitmap The bitmap to encode
 * @return Base64-encoded string representation of the bitmap, or null if encoding fails
 */
fun bitmapToBase64(bitmap: Bitmap): String? = runCatching {
    val outputStream = ByteArrayOutputStream()
    val format = if (bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    val quality = if (format == Bitmap.CompressFormat.PNG) 100 else 85
    
    if (!bitmap.compress(format, quality, outputStream)) {
        Log.e("ImageUtils", "Failed to compress bitmap")
        return@runCatching null
    }
    
    Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}.getOrElse { exception ->
    Log.e("ImageUtils", "Error encoding bitmap to Base64", exception)
    null
}

/**
 * Converts a Base64-encoded string to a Bitmap.
 *
 * @param base64 The Base64-encoded string to decode
 * @return The decoded Bitmap, or null if decoding fails
 */
fun base64ToBitmap(base64: String): Bitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()


