package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.remote.SummitApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UploadRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun upload(path: String) = safeCall {
        val f = File(path)
        val body = f.asRequestBody("application/octet-stream".toMediaType())
        val part = MultipartBody.Part.createFormData("file", f.name, body)
        api.upload(part)
    }
}
