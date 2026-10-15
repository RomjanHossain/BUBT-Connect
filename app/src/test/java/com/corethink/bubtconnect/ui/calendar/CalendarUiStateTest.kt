package com.corethink.bubtconnect.ui.calendar

import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.SimpleDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarUiStateTest {

    private fun event(day: Int, type: CalendarEventType = CalendarEventType.Exam) =
        CalendarEvent(SimpleDate(2026, 10, day), title = "e$day", type = type, note = "")

    private val events = listOf(
        event(5, CalendarEventType.Payment),
        event(14),
        event(20, CalendarEventType.Payment)
    )

    private fun state(today: SimpleDate = SimpleDate(2026, 10, 14), filter: CalendarFilter = CalendarFilter.All) =
        CalendarUiState(2026, 10, today, filter, allEvents = events)

    @Test
    fun todayLineSitsBeforeFirstEventOnOrAfterToday() {
        assertEquals(1, state().todayInsertIndex)
    }

    @Test
    fun todayLineGoesLastWhenAllEventsArePast() {
        assertEquals(3, state(today = SimpleDate(2026, 10, 25)).todayInsertIndex)
    }

    @Test
    fun noTodayLineInOtherMonths() {
        val november = CalendarUiState(2026, 11, SimpleDate(2026, 10, 14), allEvents = events)
        assertNull(november.todayInsertIndex)
        assertEquals("November 2026", november.monthLabel)
    }

    @Test
    fun filterShowsOnlyMatchingTypes() {
        val payments = state(filter = CalendarFilter.Payment)
        assertEquals(listOf(5, 20), payments.events.map { it.date.day })
        assertEquals(1, payments.todayInsertIndex)

        assertEquals(3, state(filter = CalendarFilter.All).events.size)
        assertEquals(listOf(14), state(filter = CalendarFilter.Exam).events.map { it.date.day })
    }
}
