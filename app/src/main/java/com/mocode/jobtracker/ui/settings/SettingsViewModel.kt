package com.mocode.jobtracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.preferences.ThemePreference
import com.mocode.jobtracker.data.preferences.UserPreferencesRepository
import com.mocode.jobtracker.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler? = null
) : ViewModel() {

    private val _isBiometricEnabled = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.notificationsEnabled,
        userPreferencesRepository.themePreference,
        _isBiometricEnabled
    ) { notificationsEnabled, themePreference, biometricEnabled ->
        SettingsUiState(
            isBiometricEnabled = biometricEnabled,
            isNotificationsEnabled = notificationsEnabled,
            themePreference = themePreference
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun toggleBiometric(enabled: Boolean) {
        _isBiometricEnabled.update { enabled }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                reminderScheduler?.rescheduleAllFutureReminders()
            } else {
                reminderScheduler?.cancelAllReminders()
            }
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.setThemePreference(theme)
        }
    }
}
