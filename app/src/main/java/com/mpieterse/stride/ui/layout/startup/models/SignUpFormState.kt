package com.mpieterse.stride.ui.layout.startup.models

import com.mpieterse.stride.core.models.FormField

data class SignUpFormState(
    val identity: FormField<String>,
    val passwordDefault: FormField<String>,
    val passwordConfirm: FormField<String>,
)