package com.mpieterse.stride.core.services

import com.mpieterse.stride.core.models.results.GenericError

enum class AuthenticationError : GenericError {
    UNKNOWN,
    TIMEOUT,
    NETWORK,
    INVALID_USER_STATUS,
    INVALID_CREDENTIALS,
}