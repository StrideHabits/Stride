package com.mpieterse.stride.ui.layout.shared.models

import kotlinx.coroutines.flow.StateFlow

/**
 * Used in composition patterns for a ViewModel servicing multiple forms.
 *
 * **Example:**
 *
 * ```
 * val signUpForm = SignUpFormViewModel()
 * val signInForm = SignInFormViewModel()
 * private val formList: List<ValidatableForm> = listOf(
 *     signUpForm,
 *     signInForm,
 * )
 *
 * val isAllFormsValid = combine(
 *     formList.map { it.isFormValid }
 * ) {
 *     it.all { isValid ->
 *         isValid
 *     }
 * }.stateIn(
 *     viewModelScope, SharingStarted.Eagerly, false
 * )
 * ```
 */
interface ValidatableForm {
    val isFormValid: StateFlow<Boolean>
    fun validateForm()
}