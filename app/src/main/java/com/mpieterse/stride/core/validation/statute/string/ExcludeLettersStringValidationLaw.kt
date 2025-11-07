package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias ExcludeLetters = ExcludeLettersStringValidationLaw

class ExcludeLettersStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isLetter() }) {
            true -> ValidationError.String.EXCLUDE_LETTERS
            else -> null
        }
    }
}