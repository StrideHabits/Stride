package com.mpieterse.stride.core.validation.statute

import com.mpieterse.stride.core.validation.ValidationError

fun interface ValidationLaw<T> {
    fun checkFor(value: T): ValidationError?
}