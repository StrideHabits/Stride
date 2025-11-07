package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias ExcludeCharacters = ExcludeCharactersStringValidationLaw

class ExcludeCharactersStringValidationLaw(
    private vararg val characters: Char
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it in characters }) {
            true -> ValidationError.String.EXCLUDE_CUSTOM_CHARACTERS
            else -> null
        }
    }
}