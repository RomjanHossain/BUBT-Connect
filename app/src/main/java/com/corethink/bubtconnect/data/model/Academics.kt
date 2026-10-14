package com.corethink.bubtconnect.data.model

import java.util.Calendar
import java.util.Locale

/** A calendar day without time or zone; java.time needs API 26 and minSdk is 24. */
data class SimpleDate(val year: Int, val month: Int, val day: Int) : Comparable<SimpleDate> {
    override fun compareTo(other: SimpleDate): Int =
        compareValuesBy(this, other, SimpleDate::year, SimpleDate::month, SimpleDate::day)

    private fun toCalendar(): Calendar = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }

    fun plusDays(days: Int): SimpleDate {
        val calendar = toCalendar().apply { add(Calendar.DAY_OF_MONTH, days) }
        return SimpleDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    /** "Sun", "Mon", … */
    val weekdayShort: String
        get() = toCalendar().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US).orEmpty()

    /** "Oct" */
    val monthShort: String
        get() = toCalendar().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US).orEmpty()

    /** "Oct 05" */
    val shortLabel: String
        get() = String.format(Locale.US, "%s %02d", monthShort, day)

    companion object {
        fun today(): SimpleDate {
            val now = Calendar.getInstance()
            return SimpleDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
        }
    }
}

/** BUBT teaches Saturday to Thursday. */
enum class ClassDay(val label: String, val calendarDay: Int) {
    Sat("Sat", Calendar.SATURDAY),
    Sun("Sun", Calendar.SUNDAY),
    Mon("Mon", Calendar.MONDAY),
    Tue("Tue", Calendar.TUESDAY),
    Wed("Wed", Calendar.WEDNESDAY),
    Thu("Thu", Calendar.THURSDAY);


    companion object {
        /** Today's teaching day, or Saturday on Friday. */
        fun today(): ClassDay {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return entries.firstOrNull { it.calendarDay == day } ?: Sat
        }
    }
}

// ------------------------------------------------------------- routine (1g)
sealed interface RoutineSlot {
    val startMinute: Int
    val endMinute: Int

    data class Class(
        val course: Course,
        val location: String,
        override val startMinute: Int,
        override val endMinute: Int
    ) : RoutineSlot

    data class Break(
        override val startMinute: Int,
        override val endMinute: Int
    ) : RoutineSlot
}

// ------------------------------------------------------------- exams (1h)
enum class ExamKind(val label: String) { Normal("Normal"), Supplementary("Supplementary") }

data class Exam(
    val id: String,
    val kind: ExamKind,
    val term: String,
    val course: Course,
    val date: SimpleDate,
    val time: String,
    val room: String,
    val seat: String
)

// ------------------------------------------------------------- results (1i)
/** UGC uniform grading used by BUBT. */
enum class Grade(val label: String, val point: Double) {
    APlus("A+", 4.00), A("A", 3.75), AMinus("A-", 3.50),
    BPlus("B+", 3.25), B("B", 3.00), BMinus("B-", 2.75),
    CPlus("C+", 2.50), C("C", 2.25), D("D", 2.00), F("F", 0.00)
}

data class CourseGrade(
    val course: Course,
    val credits: Double,
    val grade: Grade
)

data class SemesterResult(
    val semester: String,
    val grades: List<CourseGrade>
) {
    val credits: Double get() = grades.sumOf { it.credits }
    val sgpa: Double get() = grades.sumOf { it.credits * it.grade.point } / credits
}

/** Credit-weighted GPA across [results]. */
fun cgpaOf(results: List<SemesterResult>): Double {
    val grades = results.flatMap { it.grades }
    val credits = grades.sumOf { it.credits }
    return if (credits == 0.0) 0.0 else grades.sumOf { it.credits * it.grade.point } / credits
}

// ------------------------------------------------------------- attendance (1j)
data class AttendanceRecord(
    val course: Course,
    val attended: Int,
    val total: Int,
    val unit: String,
    val log: List<Pair<SimpleDate, Boolean>>
) {
    val percent: Int get() = if (total == 0) 100 else attended * 100 / total

    val atRisk: Boolean get() = percent < MIN_ATTENDANCE_PERCENT

    /** Consecutive classes to attend to climb back to the minimum. */
    val classesNeeded: Int
        get() {
            var extra = 0
            while ((attended + extra) * 100 < MIN_ATTENDANCE_PERCENT * (total + extra)) extra++
            return extra
        }

    companion object {
        const val MIN_ATTENDANCE_PERCENT = 75
    }
}

// ------------------------------------------------------------- calendar (1k)
enum class CalendarEventType(val label: String) { Payment("Payment"), Exam("Exam"), Holiday("Holiday") }

data class CalendarEvent(
    val date: SimpleDate,
    val endDate: SimpleDate? = null,
    val title: String,
    val type: CalendarEventType,
    val note: String
)

// ------------------------------------------------------------- courses (1l)
enum class CourseStatus { Completed, InProgress, Remaining }

data class CurriculumCourse(
    val course: Course,
    val credits: Double,
    val status: CourseStatus
)

// ------------------------------------------------------------- fees (1t)
data class FeeStatement(
    val semester: String,
    val totalDemand: Int,
    val waiverLabel: String,
    val waiver: Int,
    val paid: Int,
    val dueDate: SimpleDate
) {
    private val net: Int get() = totalDemand - waiver
    val overpaid: Int get() = (paid - net).coerceAtLeast(0)
    val currentDue: Int get() = (net - paid).coerceAtLeast(0)
}
