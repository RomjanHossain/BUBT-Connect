package com.corethink.bubtconnect.ui.calendar

import androidx.lifecycle.ViewModel
import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.Locale

data class CalendarUiState(
    val year: Int,
    val month: Int,
    val today: SimpleDate,
    val events: List<CalendarEvent>
) {
    val monthLabel: String
        get() = Calendar.getInstance().apply { clear(); set(year, month - 1, 1) }
            .getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US).orEmpty() + " $year"

    /** Where the dashed "Today" line goes: before the first event on/after today, if today is in this month. */
    val todayInsertIndex: Int?
        get() = if (today.year == year && today.month == month) events.indexOfFirst { it.date >= today }
            .let { if (it == -1) events.size else it } else null
}

class CalendarViewModel(
    private val repository: AcademicsRepository = SampleAcademicsRepository,
    today: SimpleDate = SimpleDate.today()
) : ViewModel() {

    private val _uiState = MutableStateFlow(stateFor(today.year, today.month, today))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private fun stateFor(year: Int, month: Int, today: SimpleDate) = CalendarUiState(
        year = year,
        month = month,
        today = today,
        events = repository.calendarEvents()
            .filter { it.date.year == year && it.date.month == month }
            .sortedBy { it.date }
    )

    fun previousMonth() = shiftMonth(-1)

    fun nextMonth() = shiftMonth(1)

    private fun shiftMonth(delta: Int) = _uiState.update {
        val index = it.year * 12 + (it.month - 1) + delta
        stateFor(index / 12, index % 12 + 1, it.today)
    }
}
