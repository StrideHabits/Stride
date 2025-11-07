package com.mpieterse.stride.core.validation.statute.string

import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.core.validation.statute.ValidationLaw

typealias AsFirebaseCredential = AsFirebaseCredentialStringValidationLaw

/**
 * [ValidationLaw] to validate a user's password against the server-side ruleset
 * enforced by the Firebase Authentication console. This law should not be used
 * in conjunction with any other law than [NotEmpty] to ensure integrity.
 *
 * **Errors:**
 *
 * - [ValidationError.String.REQUIRE_MIN_CHARS] (Always)
 * - [ValidationError.String.REQUIRE_MAX_CHARS] (Always)
 * - [ValidationError.String.INCLUDE_LOWERCASE]
 * - [ValidationError.String.INCLUDE_UPPERCASE]
 * - [ValidationError.String.INCLUDE_NUMBERS]
 * - [ValidationError.String.INCLUDE_SYMBOLS]
 */
class AsFirebaseCredentialStringValidationLaw : ValidationLaw<String> {
    companion object {
        const val FIREBASE_MIN_LENGTH = 6
        const val FIREBASE_MAX_LENGTH = 4096
        const val FIREBASE_REQUIRE_LOWERCASE = true
        const val FIREBASE_REQUIRE_UPPERCASE = true
        const val FIREBASE_REQUIRE_NUMERICAL = true
        const val FIREBASE_REQUIRE_SYMBOL = true
    }


// --- Law


    override fun checkFor(value: String): ValidationError? {
        MinLength(FIREBASE_MIN_LENGTH).checkFor(value)?.let { return it }
        MaxLength(FIREBASE_MAX_LENGTH).checkFor(value)?.let { return it }
        ExcludeWhitespace().checkFor(value)?.let { return it }
        if (FIREBASE_REQUIRE_LOWERCASE)
            IncludeLowercase().checkFor(value)?.let { return it }
        if (FIREBASE_REQUIRE_UPPERCASE)
            IncludeUppercase().checkFor(value)?.let { return it }
        if (FIREBASE_REQUIRE_NUMERICAL)
            IncludeNumbers().checkFor(value)?.let { return it }
        if (FIREBASE_REQUIRE_SYMBOL)
            IncludeSymbols().checkFor(value)?.let { return it }

        return null
    }
}