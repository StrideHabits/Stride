package com.mpieterse.stride.core.validation.statute.string

import android.util.Patterns
import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias AsEmailAddress = AsEmailAddressStringValidationLaw

class AsEmailAddressStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (Patterns.EMAIL_ADDRESS.matcher(value).matches().not()) {
            true -> ValidationError.String.INVALID_EMAIL_ADDRESS
            else -> null
        }
    }
}