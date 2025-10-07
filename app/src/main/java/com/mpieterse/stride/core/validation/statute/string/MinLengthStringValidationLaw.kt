package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias MinLength = MinLengthStringValidationLaw

class MinLengthStringValidationLaw(
    private val length: Int
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        check(length >= 0)
        return when (value.length < length) {
            true -> ValidationError.String.REQUIRE_MIN_CHARS
            else -> null
        }
    }
}