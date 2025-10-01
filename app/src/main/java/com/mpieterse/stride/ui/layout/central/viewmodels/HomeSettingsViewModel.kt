package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "HomeSettingsViewModel"
    }
}