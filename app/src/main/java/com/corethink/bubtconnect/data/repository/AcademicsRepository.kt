package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.AttendanceRecord
import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.ClassDay
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.CourseGrade
import com.corethink.bubtconnect.data.model.CourseStatus
import com.corethink.bubtconnect.data.model.CurriculumCourse
import com.corethink.bubtconnect.data.model.Exam
import com.corethink.bubtconnect.data.model.ExamKind
import com.corethink.bubtconnect.data.model.FeeStatement
import com.corethink.bubtconnect.data.model.Grade
import com.corethink.bubtconnect.data.model.RoutineSlot
import com.corethink.bubtconnect.data.model.SemesterResult
import com.corethink.bubtconnect.data.model.SimpleDate

interface AcademicsRepository {
    fun routine(day: ClassDay): List<RoutineSlot>
    fun exams(): List<Exam>
    fun results(): List<SemesterResult>
    fun attendance(): List<AttendanceRecord>
    fun calendarEvents(): List<CalendarEvent>
    fun curriculum(): List<CurriculumCourse>
    fun feeStatements(): List<FeeStatement>
}

/** Sample data until the Annex client lands in data/source/remote. */
object SampleAcademicsRepository : AcademicsRepository {

    private val mat231 = Course("MAT 231", "Calculus II")
    private val cse231 = Course("CSE 231", "Data Structures")
    private val cse232 = Course("CSE 232", "DS Lab")
    private val eee203 = Course("EEE 203", "Electronics")
    private val eng102 = Course("ENG 102", "English II")

    private fun t(hour: Int, minute: Int = 0) = hour * 60 + minute

    private val routine = mapOf(
        ClassDay.Sat to listOf(
            RoutineSlot.Class(eee203, "Rm 3-110", t(8, 30), t(10)),
            RoutineSlot.Class(cse231, "Rm 4-302", t(10, 30), t(11, 45)),
            RoutineSlot.Class(eng102, "Rm 1-210", t(12, 30), t(13, 45))
        ),
        ClassDay.Sun to listOf(
            RoutineSlot.Class(mat231, "Rm 2-405", t(8, 30), t(10)),
            RoutineSlot.Class(cse232, "Lab 3 · Sec 2", t(10, 30), t(13))
        ),
        ClassDay.Mon to listOf(
            RoutineSlot.Class(mat231, "Rm 2-405", t(8, 30), t(10)),
            RoutineSlot.Class(cse231, "Rm 4-302", t(10, 30), t(11, 45)),
            RoutineSlot.Break(t(11, 45), t(12, 30)),
            RoutineSlot.Class(cse232, "Lab 3 · Sec 2", t(12, 30), t(14, 30)),
            RoutineSlot.Class(eng102, "Rm 1-210", t(14, 30), t(15, 45))
        ),
        ClassDay.Tue to listOf(
            RoutineSlot.Class(eee203, "Rm 3-110", t(9), t(10, 30)),
            RoutineSlot.Class(mat231, "Rm 2-405", t(11), t(12, 30))
        ),
        ClassDay.Wed to listOf(
            RoutineSlot.Class(cse231, "Rm 4-302", t(8, 30), t(9, 45)),
            RoutineSlot.Break(t(9, 45), t(10, 30)),
            RoutineSlot.Class(eee203, "Rm 3-110", t(10, 30), t(12))
        ),
        ClassDay.Thu to emptyList()
    )

    override fun routine(day: ClassDay): List<RoutineSlot> = routine[day].orEmpty()

    override fun exams(): List<Exam> = listOf(
        Exam("mid-mat231", ExamKind.Normal, "Mid-term · Fall 2026", mat231, SimpleDate(2026, 10, 20), "10:00 – 12:00", "Room 2-405", "34"),
        Exam("mid-cse231", ExamKind.Normal, "Mid-term · Fall 2026", Course("CSE 231", "Data Structures"), SimpleDate(2026, 10, 22), "10:00 – 12:00", "Room 4-302", "12"),
        Exam("mid-eee203", ExamKind.Normal, "Mid-term · Fall 2026", eee203, SimpleDate(2026, 10, 26), "02:00 – 04:00 PM", "Room 3-110", "08"),
        Exam("sup-phy101", ExamKind.Supplementary, "Supplementary · Fall 2026", Course("PHY 101", "Physics I"), SimpleDate(2026, 11, 8), "10:00 – 12:00", "Room 1-104", "19")
    )

    override fun results(): List<SemesterResult> = listOf(
        SemesterResult("Spring 2025", listOf(
            CourseGrade(Course("CSE 101", "Intro to Programming"), 3.0, Grade.AMinus),
            CourseGrade(Course("MAT 101", "Calculus I"), 3.0, Grade.BPlus),
            CourseGrade(Course("PHY 101", "Physics I"), 3.0, Grade.B),
            CourseGrade(Course("ENG 101", "English I"), 3.0, Grade.A)
        )),
        SemesterResult("Summer 2025", listOf(
            CourseGrade(Course("CSE 111", "Structured Programming"), 3.0, Grade.A),
            CourseGrade(Course("CSE 112", "Structured Programming Lab"), 1.5, Grade.APlus),
            CourseGrade(Course("MAT 111", "Discrete Mathematics"), 3.0, Grade.BPlus),
            CourseGrade(Course("EEE 101", "Electrical Circuits"), 3.0, Grade.BPlus)
        )),
        SemesterResult("Fall 2025", listOf(
            CourseGrade(Course("CSE 121", "Object Oriented Programming"), 3.0, Grade.AMinus),
            CourseGrade(Course("CSE 122", "OOP Lab"), 1.5, Grade.A),
            CourseGrade(Course("MAT 121", "Linear Algebra"), 3.0, Grade.B),
            CourseGrade(Course("STA 121", "Statistics"), 3.0, Grade.AMinus)
        )),
        SemesterResult("Spring 2026", listOf(
            CourseGrade(Course("CSE 211", "Computer Architecture"), 3.0, Grade.A),
            CourseGrade(Course("CSE 213", "Numerical Methods"), 3.0, Grade.AMinus),
            CourseGrade(Course("EEE 201", "Digital Electronics"), 3.0, Grade.AMinus),
            CourseGrade(Course("ACC 201", "Accounting"), 2.0, Grade.A)
        )),
        SemesterResult("Summer 2026", listOf(
            CourseGrade(Course("CSE 221", "Algorithms"), 3.0, Grade.A),
            CourseGrade(Course("CSE 223", "Digital Logic"), 3.0, Grade.AMinus),
            CourseGrade(Course("MAT 221", "Linear Algebra II"), 3.0, Grade.BPlus),
            CourseGrade(Course("CSE 222", "Algorithms Lab"), 1.5, Grade.APlus)
        ))
    )

    override fun attendance(): List<AttendanceRecord> = listOf(
        record(cse231, 23, 25, "classes"),
        record(mat231, 17, 20, "classes"),
        record(eee203, 12, 20, "classes"),
        record(cse232, 7, 9, "labs"),
        record(eng102, 14, 14, "classes")
    )

    /** Builds a date-wise log, every third day from 3 Aug, with absences spread evenly. Newest first. */
    private fun record(course: Course, attended: Int, total: Int, unit: String): AttendanceRecord {
        val absences = total - attended
        val absentIndices = (0 until absences).map { it * total / absences + 1 }.toSet()
        val start = SimpleDate(2026, 8, 3)
        val log = (0 until total).map { index -> start.plusDays(index * 3) to (index !in absentIndices) }
        return AttendanceRecord(course, attended, total, unit, log.reversed())
    }

    override fun calendarEvents(): List<CalendarEvent> = listOf(
        CalendarEvent(SimpleDate(2026, 9, 28), title = "1st installment due", type = CalendarEventType.Payment, note = "Payment"),
        CalendarEvent(SimpleDate(2026, 10, 5), title = "2nd installment due", type = CalendarEventType.Payment, note = "Payment"),
        CalendarEvent(SimpleDate(2026, 10, 14), title = "Durga Puja", type = CalendarEventType.Holiday, note = "Holiday · campus closed"),
        CalendarEvent(SimpleDate(2026, 10, 20), SimpleDate(2026, 10, 27), "Mid-term exams", CalendarEventType.Exam, "Exam"),
        CalendarEvent(SimpleDate(2026, 10, 31), title = "Exam fee clearance", type = CalendarEventType.Payment, note = "Payment · deadline"),
        CalendarEvent(SimpleDate(2026, 11, 8), title = "Supplementary exams", type = CalendarEventType.Exam, note = "Exam"),
        CalendarEvent(SimpleDate(2026, 12, 1), title = "Final exams begin", type = CalendarEventType.Exam, note = "Exam"),
        CalendarEvent(SimpleDate(2026, 12, 16), title = "Victory Day", type = CalendarEventType.Holiday, note = "Holiday · campus closed")
    )

    override fun curriculum(): List<CurriculumCourse> = listOf(
        CurriculumCourse(Course("CSE 101", "Intro to Programming"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 111", "Structured Programming"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 121", "Object Oriented Programming"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 211", "Computer Architecture"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 213", "Numerical Methods"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 221", "Algorithms"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 223", "Digital Logic"), 3.0, CourseStatus.Completed),
        CurriculumCourse(Course("CSE 231", "Data Structures"), 3.0, CourseStatus.InProgress),
        CurriculumCourse(Course("CSE 232", "DS Lab"), 1.5, CourseStatus.InProgress),
        CurriculumCourse(Course("CSE 241", "Operating Systems"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 251", "Computer Networks"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 261", "Database Systems"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 311", "Software Engineering"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 321", "Artificial Intelligence"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 331", "Compiler Design"), 3.0, CourseStatus.Remaining),
        CurriculumCourse(Course("CSE 400", "Project / Thesis"), 4.0, CourseStatus.Remaining)
    )

    override fun feeStatements(): List<FeeStatement> = listOf(
        FeeStatement("Fall 2026", 58_500, "Merit 30%", 14_400, 30_000, SimpleDate(2026, 10, 31)),
        FeeStatement("Summer 2026", 56_000, "Merit 30%", 13_800, 42_200, SimpleDate(2026, 6, 30)),
        FeeStatement("Spring 2026", 56_000, "Merit 25%", 11_500, 45_500, SimpleDate(2026, 2, 28)),
        FeeStatement("Fall 2025", 54_000, "Sibling 10%", 4_800, 49_200, SimpleDate(2025, 10, 31))
    )
}
