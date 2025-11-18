package com.mpieterse.stride.core.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultExtensionsTest {

    @Test
    fun `auth errors are detected`() {
        val err = ApiResult.Err(code = 401, message = "Unauthorized")

        assertTrue(err.isAuthError())
        assertFalse(err.isNetworkError())
        assertFalse(err.isServerError())
        assertEquals("Unauthorized", err.getUserMessage())
    }

    @Test
    fun `network errors fall back to friendly copy`() {
        val err = ApiResult.Err(code = null, message = "Timeout while connecting to SummitAPI")

        assertTrue(err.isNetworkError())
        assertEquals(
            "Network error. Please check your internet connection and try again.",
            err.getUserMessage()
        )
    }

    @Test
    fun `server errors use server copy`() {
        val err = ApiResult.Err(code = 500, message = "Server exploded")

        assertTrue(err.isServerError())
        assertEquals("Server error. Please try again later.", err.getUserMessage())
    }
}

