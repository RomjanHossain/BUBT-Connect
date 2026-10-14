package com.corethink.bubtconnect.ui.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.FeeStatement
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FeesUiState(
    val isConnected: Boolean = false,
    val statements: List<FeeStatement> = emptyList(),
    val selectedIndex: Int = 0,
    val lastSyncedAt: Long? = null,
    val showInfo: Boolean = false
) {
    val selected: FeeStatement? get() = statements.getOrNull(selectedIndex)
}

class FeesViewModel(
    repository: AcademicsRepository = SampleAcademicsRepository,
    profileRepository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {

    private val statements = repository.feeStatements()
    private val selectedIndex = MutableStateFlow(0)
    private val showInfo = MutableStateFlow(false)

    val uiState: StateFlow<FeesUiState> = combine(
        profileRepository.annexConnected,
        profileRepository.lastSyncedAt,
        selectedIndex,
        showInfo
    ) { connected, syncedAt, index, info ->
        FeesUiState(connected, statements, index, syncedAt, info)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeesUiState(
            isConnected = profileRepository.annexConnected.value,
            statements = statements,
            lastSyncedAt = profileRepository.lastSyncedAt.value
        )
    )

    fun selectSemester(index: Int) {
        selectedIndex.value = index
    }

    fun setInfoVisible(visible: Boolean) {
        showInfo.value = visible
    }
}
