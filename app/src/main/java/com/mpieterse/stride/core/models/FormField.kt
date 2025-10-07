package com.mpieterse.stride.core.models

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

/**
 * Domain-layer abstraction for a form field with validation rules.
 *
 * @param value The value of the field; should have an initial state set.
 * @param rules The order in which you provide the [ValidationLaw] collection is
 *              important as this is the order in which any violations will send
 *              back to the receiver. Ensure that a logical order is followed.
 * @param error The error state of the field; should be mapped to the UI.
 */
data class FormField<T>(
    val value: T,
    val rules: List<ValidationLaw<T>> = emptyList(),
    val error: ValidationError? = null
) {
    val isValid: Boolean get() = (error == null)


    fun applyValidation(): FormField<T> {
        val firstValidationError = rules.firstNotNullOfOrNull { validationLaw ->
            validationLaw.checkFor(value)
        }

        return this.copy(error = firstValidationError)
    }
}