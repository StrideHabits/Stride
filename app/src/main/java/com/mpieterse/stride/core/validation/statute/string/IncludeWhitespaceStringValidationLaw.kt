package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias IncludeWhitespace = IncludeWhitespaceStringValidationLaw

class IncludeWhitespaceStringValidationLaw(
    private val min: Int = 1,
    private val max: Int? = null,
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        if (max != null) {
            check(max >= min)
        }

        val limit = max ?: Int.MAX_VALUE
        val count = value.count { it.isWhitespace() }
        return when (count) {
            in (min..limit) -> null
            else -> {
                ValidationError.String.INCLUDE_WHITESPACE
            }
        }
    }
}