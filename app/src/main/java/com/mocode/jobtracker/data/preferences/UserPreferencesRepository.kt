package com.mocode.jobtracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface UserPreferencesRepository {
    val notificationsEnabled: Flow<Boolean>
    val themePreference: Flow<ThemePreference>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setThemePreference(theme: ThemePreference)
    suspend fun getNotificationsEnabledOnce(): Boolean
    suspend fun getThemePreferenceOnce(): ThemePreference
}

class DataStoreUserPreferencesRepository(
    private val context: Context
) : UserPreferencesRepository {

    private companion object {
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
        }

    override val themePreference: Flow<ThemePreference> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ThemePreference.fromString(preferences[KEY_THEME_PREFERENCE])
        }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_PREFERENCE] = theme.name
        }
    }

    override suspend fun getNotificationsEnabledOnce(): Boolean {
        return notificationsEnabled.first()
    }

    override suspend fun getThemePreferenceOnce(): ThemePreference {
        return themePreference.first()
    }
}
