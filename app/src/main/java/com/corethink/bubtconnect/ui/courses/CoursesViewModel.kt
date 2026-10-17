package com.corethink.bubtconnect.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.CourseStatus
import com.corethink.bubtconnect.data.model.CurriculumCourse
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CourseFilter(val label: String) { All("All"), Completed("Completed"), Remaining("Remaining") }

data class CoursesUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val filter: CourseFilter = CourseFilter.All,
    val department: String = "CSE",
    val courses: List<CurriculumCourse> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0
) {
    val progressPercent: Int
        get() = if (totalCount == 0) 0 else completedCount * 100 / totalCount
}

class CoursesViewModel(
    repository: AcademicsRepository = SampleAcademicsRepository,
    profileRepository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {

    private val curriculum = repository.curriculum()
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(CourseFilter.All)
    /** false until the curriculum has been fetched — drives the 1n shimmer skeleton. */
    private val loaded = MutableStateFlow(false)

    init {
        // The curriculum loads off the main snapshot read; keep the skeleton up meanwhile.
        viewModelScope.launch {
            delay(SKELETON_DURATION_MILLIS)
            loaded.value = true
        }
    }

    val uiState: StateFlow<CoursesUiState> = combine(
        query,
        filter,
        profileRepository.profile,
        loaded
    ) { query, filter, profile, ready ->
        CoursesUiState(
            isLoading = !ready,
            query = query,
            filter = filter,
            department = profile?.department ?: "CSE",
            courses = filterCourses(curriculum, query, filter),
            completedCount = curriculum.count { it.status == CourseStatus.Completed },
            totalCount = curriculum.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CoursesUiState(isLoading = true)
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun selectFilter(value: CourseFilter) {
        filter.value = value
    }

    companion object {
        /** One full shimmer sweep cycle, matching the 1n note ("sweeps every 1.4s" ×2 visible). */
        const val SKELETON_DURATION_MILLIS = 1400L

        fun filterCourses(
            courses: List<CurriculumCourse>,
            query: String,
            filter: CourseFilter
        ): List<CurriculumCourse> {
            val needle = query.trim()
            return courses.filter { item ->
                val matchesQuery = needle.isEmpty() ||
                    item.course.code.contains(needle, ignoreCase = true) ||
                    item.course.code.replace(" ", "").contains(needle.replace(" ", ""), ignoreCase = true) ||
                    item.course.title.contains(needle, ignoreCase = true)
                val matchesFilter = when (filter) {
                    CourseFilter.All -> true
                    CourseFilter.Completed -> item.status == CourseStatus.Completed
                    CourseFilter.Remaining -> item.status != CourseStatus.Completed
                }
                matchesQuery && matchesFilter
            }
        }
    }
}
