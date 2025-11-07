package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias ExcludeUppercase = ExcludeUppercaseStringValidationLaw

class ExcludeUppercaseStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isUpperCase() }) {
            true -> ValidationError.String.EXCLUDE_UPPERCASE
            else -> null
        }
    }
}