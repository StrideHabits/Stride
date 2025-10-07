package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias NotEmpty = NotEmptyStringValidationLaw

class NotEmptyStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return MinLength(1).checkFor(value)
    }
}