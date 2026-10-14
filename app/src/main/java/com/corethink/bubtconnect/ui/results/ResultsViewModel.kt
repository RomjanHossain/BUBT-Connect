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
    val selectedIndex: Int = 0,
    val expandedCourse: String? = null
) {
    val selected: SemesterResult? get() = semesters.getOrNull(selectedIndex)

    /** CGPA as of the selected semester. */
    val cgpa: Double get() = cgpaOf(semesters.take(selectedIndex + 1))
}

class ResultsViewModel(
    repository: AcademicsRepository = SampleAcademicsRepository,
    profileRepository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {

    private val semesters = repository.results()
    private val selectedIndex = MutableStateFlow(semesters.lastIndex)
    private val expandedCourse = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ResultsUiState> = combine(
        profileRepository.annexConnected,
        selectedIndex,
        expandedCourse
    ) { connected, index, expanded ->
        ResultsUiState(connected, semesters, index, expanded)
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
        expandedCourse.value = null
    }

    fun toggleCourse(code: String) {
        expandedCourse.value = if (expandedCourse.value == code) null else code
    }
}
