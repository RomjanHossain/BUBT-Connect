package com.corethink.bubtconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.AppSettings
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.data.model.ThemeMode
import com.corethink.bubtconnect.data.repository.AnnexAuthRepository
import com.corethink.bubtconnect.data.repository.FakeAnnexAuthRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: StudentProfile? = null,
    val isConnected: Boolean = false,
    val lastSyncedAt: Long? = null,
    val isSyncing: Boolean = false,
    val settings: AppSettings = AppSettings()
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository.Default,
    private val settingsRepository: SettingsRepository = SettingsRepository.Default,
    private val authRepository: AnnexAuthRepository = FakeAnnexAuthRepository
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.profile,
        profileRepository.annexConnected,
        profileRepository.lastSyncedAt,
        isSyncing,
        settingsRepository.settings
    ) { profile, connected, syncedAt, syncing, settings ->
        ProfileUiState(profile, connected, syncedAt, syncing, settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(
            profile = profileRepository.profile.value,
            isConnected = profileRepository.annexConnected.value,
            lastSyncedAt = profileRepository.lastSyncedAt.value,
            settings = settingsRepository.settings.value
        )
    )

    fun sync() {
        if (isSyncing.value) return
        isSyncing.value = true
        viewModelScope.launch {
            try {
                authRepository.sync()
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun setClassReminderMinutes(minutes: Int) = settingsRepository.setClassReminderMinutes(minutes)

    fun setExamReminders(enabled: Boolean) = settingsRepository.setExamReminders(enabled)

    fun setPaymentDeadlines(enabled: Boolean) = settingsRepository.setPaymentDeadlines(enabled)

    fun setThemeMode(mode: ThemeMode) = settingsRepository.setThemeMode(mode)

    fun logout() = profileRepository.logout()
}
