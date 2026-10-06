package com.mocode.jobtracker.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserPreferencesRepository(
    initialNotificationsEnabled: Boolean = true,
    initialTheme: ThemePreference = ThemePreference.SYSTEM
) : UserPreferencesRepository {

    private val _notificationsEnabled = MutableStateFlow(initialNotificationsEnabled)
    override val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _themePreference = MutableStateFlow(initialTheme)
    override val themePreference: Flow<ThemePreference> = _themePreference.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    override suspend fun setThemePreference(theme: ThemePreference) {
        _themePreference.value = theme
    }

    override suspend fun getNotificationsEnabledOnce(): Boolean {
        return _notificationsEnabled.value
    }

    override suspend fun getThemePreferenceOnce(): ThemePreference {
        return _themePreference.value
    }
}
