package com.corethink.bubtconnect.ui.calendar

import androidx.lifecycle.ViewModel
import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.Locale

enum class CalendarFilter(val label: String) {
    All("All"),
    Payment("Payments"),
    Exam("Exams"),
    Holiday("Holidays")
}

data class CalendarUiState(
    val year: Int,
    val month: Int,
    val today: SimpleDate,
    val filter: CalendarFilter = CalendarFilter.All,
    val allEvents: List<CalendarEvent> = emptyList()
) {
    val events: List<CalendarEvent>
        get() = when (filter) {
            CalendarFilter.All -> allEvents
            else -> allEvents.filter { it.type == filterType(filter) }
        }

    val monthLabel: String
        get() = Calendar.getInstance().apply { clear(); set(year, month - 1, 1) }
            .getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US).orEmpty() + " $year"

    /** Where the "Today" line goes: before the first visible event on/after today, if today is in this month. */
    val todayInsertIndex: Int?
        get() = if (today.year == year && today.month == month) events.indexOfFirst { it.date >= today }
            .let { if (it == -1) events.size else it } else null

    private fun filterType(filter: CalendarFilter): CalendarEventType = when (filter) {
        CalendarFilter.Payment -> CalendarEventType.Payment
        CalendarFilter.Exam -> CalendarEventType.Exam
        CalendarFilter.Holiday -> CalendarEventType.Holiday
        CalendarFilter.All -> CalendarEventType.Payment
    }
}

class CalendarViewModel(
    private val repository: AcademicsRepository = SampleAcademicsRepository,
    today: SimpleDate = SimpleDate.today()
) : ViewModel() {

    private val filter = MutableStateFlow(CalendarFilter.All)
    private val _uiState = MutableStateFlow(stateFor(today.year, today.month, today, CalendarFilter.All))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private fun stateFor(year: Int, month: Int, today: SimpleDate, filter: CalendarFilter) = CalendarUiState(
        year = year,
        month = month,
        today = today,
        filter = filter,
        allEvents = repository.calendarEvents()
            .filter { it.date.year == year && it.date.month == month }
            .sortedBy { it.date }
    )

    fun previousMonth() = shiftMonth(-1)

    fun nextMonth() = shiftMonth(1)

    fun selectFilter(value: CalendarFilter) = _uiState.update { it.copy(filter = value) }

    private fun shiftMonth(delta: Int) = _uiState.update {
        val index = it.year * 12 + (it.month - 1) + delta
        stateFor(index / 12, index % 12 + 1, it.today, it.filter)
    }
}
