package com.corethink.bubtconnect.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.AttendanceRecord
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.AnnexAuthRepository
import com.corethink.bubtconnect.data.repository.FakeAnnexAuthRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isConnected: Boolean = false,
    val records: List<AttendanceRecord> = emptyList(),
    val lastSyncedAt: Long? = null,
    val isRefreshing: Boolean = false,
    val openLog: AttendanceRecord? = null
)

class AttendanceViewModel(
    repository: AcademicsRepository = SampleAcademicsRepository,
    profileRepository: ProfileRepository = ProfileRepository.Default,
    private val authRepository: AnnexAuthRepository = FakeAnnexAuthRepository
) : ViewModel() {

    private val records = repository.attendance()
    private val isRefreshing = MutableStateFlow(false)
    private val openLog = MutableStateFlow<AttendanceRecord?>(null)

    val uiState: StateFlow<AttendanceUiState> = combine(
        profileRepository.annexConnected,
        profileRepository.lastSyncedAt,
        isRefreshing,
        openLog
    ) { connected, syncedAt, refreshing, log ->
        AttendanceUiState(connected, records, syncedAt, refreshing, log)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AttendanceUiState(
            isConnected = profileRepository.annexConnected.value,
            records = records,
            lastSyncedAt = profileRepository.lastSyncedAt.value
        )
    )

    fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        viewModelScope.launch {
            try {
                authRepository.sync()
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun openLog(record: AttendanceRecord) {
        openLog.value = record
    }

    fun closeLog() {
        openLog.value = null
    }
}
