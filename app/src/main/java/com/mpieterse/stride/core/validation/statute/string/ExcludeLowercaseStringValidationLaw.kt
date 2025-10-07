package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias ExcludeLowercase = ExcludeLowercaseStringValidationLaw

class ExcludeLowercaseStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isLowerCase() }) {
            true -> ValidationError.String.EXCLUDE_LOWERCASE
            else -> null
        }
    }
}