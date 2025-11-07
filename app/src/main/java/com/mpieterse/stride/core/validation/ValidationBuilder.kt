package com.mpieterse.stride.core.validation

import com.mpieterse.stride.core.models.results.Final
import com.mpieterse.stride.core.validation.statute.ValidationLaw

class ValidationBuilder<T> private constructor(
    private val valueToValidate: T
) {
    companion object {
        private const val TAG = "ValidationBuilder"

        // --- Extensions

        fun forString(value: String) = ValidationBuilder(value)
        fun forNumber(value: Number) = ValidationBuilder(value)
    }


// --- Validation


    val laws = mutableListOf<ValidationLaw<T>>()


    fun register(law: ValidationLaw<T>) = apply {
        laws.add(law)
    }


    fun validate(): Final<Unit, ValidationError> {
        for (rule in laws) {
            val violation = rule.checkFor(valueToValidate)
            violation?.let { error ->
                return Final.Failure(error)
            }
        }

        return Final.Success(Unit)
    }
}