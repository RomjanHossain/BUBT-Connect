package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.AppSettings
import com.corethink.bubtconnect.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory app preferences. Swap for DataStore later, alongside [ProfileRepository]. */
class SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = _settings.update { it.copy(themeMode = mode) }

    fun setClassReminderMinutes(minutes: Int) = _settings.update { it.copy(classReminderMinutes = minutes) }

    fun setExamReminders(enabled: Boolean) = _settings.update { it.copy(examReminders = enabled) }

    fun setPaymentDeadlines(enabled: Boolean) = _settings.update { it.copy(paymentDeadlines = enabled) }

    companion object {
        val Default = SettingsRepository()
    }
}
