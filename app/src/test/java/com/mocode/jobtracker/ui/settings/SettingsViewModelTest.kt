package com.mocode.jobtracker.ui.settings

import com.mocode.jobtracker.data.preferences.FakeUserPreferencesRepository
import com.mocode.jobtracker.data.preferences.ThemePreference
import com.mocode.jobtracker.notification.FakeReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakeUserPreferencesRepository
    private lateinit var reminderScheduler: FakeReminderScheduler

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = FakeUserPreferencesRepository()
        reminderScheduler = FakeReminderScheduler()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun settings_initialState_loadsFromPreferences() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(preferencesRepository, reminderScheduler)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isNotificationsEnabled)
        assertEquals(ThemePreference.SYSTEM, state.themePreference)
        assertFalse(state.isBiometricEnabled)
    }

    @Test
    fun settings_toggleNotifications_persistsAndInvokesScheduler() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(preferencesRepository, reminderScheduler)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // Toggle OFF
        viewModel.toggleNotifications(false)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isNotificationsEnabled)
        assertFalse(preferencesRepository.getNotificationsEnabledOnce())
        assertTrue(reminderScheduler.allRemindersCancelled)

        // Toggle ON
        viewModel.toggleNotifications(true)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNotificationsEnabled)
        assertTrue(preferencesRepository.getNotificationsEnabledOnce())
        assertTrue(reminderScheduler.allRemindersRescheduled)
    }

    @Test
    fun settings_setThemePreference_persistsSelection() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(preferencesRepository, reminderScheduler)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.setThemePreference(ThemePreference.DARK)
        testScheduler.advanceUntilIdle()

        assertEquals(ThemePreference.DARK, viewModel.uiState.value.themePreference)
        assertEquals(ThemePreference.DARK, preferencesRepository.getThemePreferenceOnce())

        viewModel.setThemePreference(ThemePreference.LIGHT)
        testScheduler.advanceUntilIdle()

        assertEquals(ThemePreference.LIGHT, viewModel.uiState.value.themePreference)
        assertEquals(ThemePreference.LIGHT, preferencesRepository.getThemePreferenceOnce())
    }

    @Test
    fun settings_toggleBiometric_updatesUiState() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(preferencesRepository, reminderScheduler)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isBiometricEnabled)

        viewModel.toggleBiometric(true)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBiometricEnabled)
    }
}
