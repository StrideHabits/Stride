package com.mpieterse.stride.core.validation

import com.mpieterse.stride.core.models.results.GenericError

/**
 * Superclass for all validation errors.
 */
sealed interface ValidationError : GenericError {

    /**
     * Independent error type for `String` validations.
     */
    enum class String : ValidationError {
        REQUIRE_MIN_CHARS,
        REQUIRE_MAX_CHARS,
        INCLUDE_LOWERCASE,
        EXCLUDE_LOWERCASE,
        INCLUDE_UPPERCASE,
        EXCLUDE_UPPERCASE,
        INCLUDE_WHITESPACE,
        EXCLUDE_WHITESPACE,
        INCLUDE_NUMBERS,
        EXCLUDE_NUMBERS,
        INCLUDE_LETTERS,
        EXCLUDE_LETTERS,
        INCLUDE_SYMBOLS,
        EXCLUDE_SYMBOLS,
        INCLUDE_CUSTOM_CHARACTERS,
        EXCLUDE_CUSTOM_CHARACTERS,
        INVALID_EMAIL_ADDRESS,
    }


    /**
     * Independent error type for `Number` validations.
     */
    enum class Number : ValidationError {
        UNKNOWN,
        OUT_OF_MIN_RANGE,
        OUT_OF_MAX_RANGE,
        REQUIRE_POSITIVE,
        REQUIRE_NEGATIVE,
    }
}