package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias IncludeCharacters = IncludeCharactersStringValidationLaw

class IncludeCharactersStringValidationLaw(
    private vararg val characters: Char
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        characters.forEach { char ->
            if (!value.contains(char)) {
                return ValidationError.String.INCLUDE_CUSTOM_CHARACTERS
            }
        }

        return null
    }
}