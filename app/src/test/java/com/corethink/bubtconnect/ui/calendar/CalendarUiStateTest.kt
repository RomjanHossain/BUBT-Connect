package com.corethink.bubtconnect.ui.calendar

import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.SimpleDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarUiStateTest {

    private fun event(day: Int) =
        CalendarEvent(SimpleDate(2026, 10, day), title = "e$day", type = CalendarEventType.Exam, note = "")

    private val events = listOf(event(5), event(14), event(20))

    @Test
    fun todayLineSitsBeforeFirstEventOnOrAfterToday() {
        val state = CalendarUiState(2026, 10, SimpleDate(2026, 10, 14), events)
        assertEquals(1, state.todayInsertIndex)
    }

    @Test
    fun todayLineGoesLastWhenAllEventsArePast() {
        val state = CalendarUiState(2026, 10, SimpleDate(2026, 10, 25), events)
        assertEquals(3, state.todayInsertIndex)
    }

    @Test
    fun noTodayLineInOtherMonths() {
        val state = CalendarUiState(2026, 11, SimpleDate(2026, 10, 14), events)
        assertNull(state.todayInsertIndex)
        assertEquals("November 2026", state.monthLabel)
    }
}
