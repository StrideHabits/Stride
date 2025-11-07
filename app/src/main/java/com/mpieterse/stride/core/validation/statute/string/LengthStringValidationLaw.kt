package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias Length = LengthStringValidationLaw

class LengthStringValidationLaw(
    private val min: Int,
    private val max: Int,
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        check(max >= min)
        MinLength(min).checkFor(value)?.let { return it }
        MaxLength(max).checkFor(value)?.let { return it }
        return null
    }
}