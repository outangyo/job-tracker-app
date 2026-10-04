package com.mocode.jobtracker.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val darkThemeOption: String = "System Default"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
    }

    fun setDarkThemeOption(option: String) {
        _uiState.update { it.copy(darkThemeOption = option) }
    }
}
