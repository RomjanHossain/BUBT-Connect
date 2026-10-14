package com.corethink.bubtconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.DashboardHighlight
import com.corethink.bubtconnect.data.repository.DashboardRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleDashboardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val greeting: String = "",
    val displayName: String = "",
    val isGuest: Boolean = true,
    val courses: List<Course> = emptyList(),
    val highlights: List<DashboardHighlight> = emptyList()
)

class HomeViewModel(
    profileRepository: ProfileRepository = ProfileRepository.Default,
    dashboardRepository: DashboardRepository = SampleDashboardRepository,
    currentHour: () -> Int = { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
) : ViewModel() {

    private val courses = dashboardRepository.presentCourses()
    private val highlights = dashboardRepository.highlights()

    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile,
        profileRepository.annexConnected
    ) { profile, connected ->
        HomeUiState(
            greeting = greetingFor(currentHour()),
            displayName = profile?.name ?: if (connected) "Student" else "Guest Student",
            isGuest = !connected,
            courses = courses,
            highlights = highlights
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            greeting = greetingFor(currentHour()),
            courses = courses,
            highlights = highlights
        )
    )

    companion object {
        fun greetingFor(hour: Int): String = when (hour) {
            in 5..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            in 17..20 -> "Good evening,"
            else -> "Hello,"
        }
    }
}
