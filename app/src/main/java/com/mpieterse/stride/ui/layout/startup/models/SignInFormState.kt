package com.mpieterse.stride.ui.layout.startup.models

import com.mpieterse.stride.core.models.FormField

data class SignInFormState(
    val identity: FormField<String>,
    val password: FormField<String>,
)