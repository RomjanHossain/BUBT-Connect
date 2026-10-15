package com.corethink.bubtconnect.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicsModelTest {

    private val course = Course("EEE 203", "Electronics")

    private fun attendance(attended: Int, total: Int) =
        AttendanceRecord(course, attended, total, "classes", emptyList())

    @Test
    fun attendanceBelowSeventyFivePercentIsAtRisk() {
        assertTrue(attendance(12, 20).atRisk)
        assertFalse(attendance(15, 20).atRisk)
        assertFalse(attendance(14, 14).atRisk)
    }

    @Test
    fun classesNeededCountsFutureClassesUntilBackAtMinimum() {
        // (12 + x) / (20 + x) >= 0.75 first holds at x = 12.
        assertEquals(12, attendance(12, 20).classesNeeded)
        assertEquals(0, attendance(15, 20).classesNeeded)
    }

    @Test
    fun sgpaAndCgpaAreCreditWeighted() {
        val first = SemesterResult("S1", listOf(
            CourseGrade(Course("A", "a"), 3.0, Grade.APlus),
            CourseGrade(Course("B", "b"), 1.0, Grade.B)
        ))
        val second = SemesterResult("S2", listOf(CourseGrade(Course("C", "c"), 4.0, Grade.A)))

        assertEquals(3.75, first.sgpa, 1e-9)            // (3×4.00 + 1×3.00) / 4
        assertEquals(3.75, cgpaOf(listOf(first, second)), 1e-9)
        assertEquals(0.0, cgpaOf(emptyList()), 1e-9)
    }

    @Test
    fun feeStatementSplitsDueAndOverpaid() {
        val due = FeeStatement("Fall 2026", 58_500, "Merit 30%", 14_400, 30_000, SimpleDate(2026, 10, 31))
        assertEquals(14_100, due.currentDue)
        assertEquals(0, due.overpaid)
        assertEquals(68, due.percentCleared)

        val credit = FeeStatement("Spring 2026", 56_000, "Merit 25%", 11_500, 45_500, SimpleDate(2026, 2, 28))
        assertEquals(0, credit.currentDue)
        assertEquals(1_000, credit.overpaid)
        assertEquals(100, credit.percentCleared)
    }

    @Test
    fun simpleDateDaysUntilCountsWholeDays() {
        assertEquals(16, SimpleDate(2026, 10, 31).daysUntil(SimpleDate(2026, 10, 15)))
        assertEquals(0, SimpleDate(2026, 10, 15).daysUntil(SimpleDate(2026, 10, 15)))
        assertEquals(-4, SimpleDate(2026, 10, 11).daysUntil(SimpleDate(2026, 10, 15)))
    }

    @Test
    fun simpleDateArithmeticAndLabels() {
        assertEquals(SimpleDate(2026, 9, 2), SimpleDate(2026, 8, 30).plusDays(3))
        assertEquals("Wed", SimpleDate(2026, 10, 14).weekdayShort)
        assertEquals("Oct 05", SimpleDate(2026, 10, 5).shortLabel)
        assertTrue(SimpleDate(2026, 10, 5) < SimpleDate(2026, 11, 1))
    }
}
