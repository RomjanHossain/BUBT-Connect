package com.corethink.bubtconnect.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.SemesterResult
import com.corethink.bubtconnect.data.model.cgpaOf
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ResultsUiState(
    val isConnected: Boolean = false,
    val semesters: List<SemesterResult> = emptyList(),
    val selectedIndex: Int = 0
) {
    val selected: SemesterResult? get() = semesters.getOrNull(selectedIndex)

    /** CGPA as of the selected semester. */
    val cgpa: Double get() = cgpaOf(semesters.take(selectedIndex + 1))

    /** SGPA change versus the previous semester, or null in the first semester. */
    val sgpaDelta: Double?
        get() = if (selectedIndex == 0) null
        else selected?.let { it.sgpa - semesters[selectedIndex - 1].sgpa }

    /** Credits earned as of the selected semester. */
    val totalCredits: Double get() = semesters.take(selectedIndex + 1).sumOf { it.credits }
}

class ResultsViewModel(
    repository: AcademicsRepository = SampleAcademicsRepository,
    profileRepository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {

    private val semesters = repository.results()
    private val selectedIndex = MutableStateFlow(semesters.lastIndex)

    val uiState: StateFlow<ResultsUiState> = combine(
        profileRepository.annexConnected,
        selectedIndex
    ) { connected, index ->
        ResultsUiState(connected, semesters, index)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ResultsUiState(
            isConnected = profileRepository.annexConnected.value,
            semesters = semesters,
            selectedIndex = semesters.lastIndex
        )
    )

    fun selectSemester(index: Int) {
        selectedIndex.value = index
    }
}
