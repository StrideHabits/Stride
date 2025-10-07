package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.models.FormField
import com.mpieterse.stride.core.validation.statute.string.*
import com.mpieterse.stride.ui.layout.shared.models.ValidatableForm
import com.mpieterse.stride.ui.layout.startup.models.SignUpFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpFormViewModel 
@Inject constructor() : ViewModel(), ValidatableForm {
    companion object {
        private const val TAG = "SignUpFormViewModel"
    }


// --- Internals


    private val schema = SignUpFormState(
        identity = FormField(
            value = "", //
            rules = listOf(
                NotEmpty(), ExcludeWhitespace(), AsEmailAddress()
            )
        ), //
        passwordDefault = FormField(
            value = "", //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
            )
        ), //
        passwordConfirm = FormField(
            value = "", //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
            )
        )
    )


    private val _formState = MutableStateFlow(schema)
    val formState = _formState.asStateFlow()


// --- Functions


    fun onIdentityChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                identity = current.identity.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordDefaultChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                passwordDefault = current.passwordDefault.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordConfirmChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                passwordConfirm = current.passwordConfirm.copy(value = value).applyValidation()
            )
        }
    }


// --- Validation


    override val isFormValid = formState.map { field ->
        listOf(
            field.identity, //
            field.passwordDefault, //
            field.passwordConfirm, //
        ).all {
            it.isValid
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )


    override fun validateForm() {
        _formState.update { current ->
            current.copy(
                identity = current.identity.applyValidation(),
                passwordDefault = current.passwordDefault.applyValidation(),
                passwordConfirm = current.passwordConfirm.applyValidation(),
            )
        }
    }
}