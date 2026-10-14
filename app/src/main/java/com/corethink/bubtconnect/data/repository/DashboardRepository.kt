package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.DashboardHighlight
import java.util.Calendar

interface DashboardRepository {
    fun presentCourses(): List<Course>
    fun highlights(): List<DashboardHighlight>
}

/**
 * Sample data until routine/exam/payment sync from Annex lands in data/source/remote.
 * The next class is always scheduled 25 minutes out so the countdown has something to show.
 */
object SampleDashboardRepository : DashboardRepository {
    private val courses = listOf(
        Course("CSE 231", "Data Structures"),
        Course("CSE 233", "Digital Logic Design"),
        Course("MAT 231", "Linear Algebra"),
        Course("EEE 203", "Electronic Devices & Circuits"),
        Course("ENG 201", "Technical Writing")
    )

    override fun presentCourses(): List<Course> = courses

    override fun highlights(): List<DashboardHighlight> {
        val now = Calendar.getInstance()
        val nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = (nowMinute + 25) % (24 * 60)
        return listOf(
            DashboardHighlight.NextClass(
                course = courses[0],
                startMinuteOfDay = start,
                endMinuteOfDay = (start + 75) % (24 * 60),
                room = "Room 4-302, Building 4"
            ),
            DashboardHighlight.ExamAlert(
                course = courses[2],
                examName = "Mid-term",
                date = "Sun, 19 Oct · 10:00 AM",
                room = "Room 2-105, Building 2"
            ),
            DashboardHighlight.PaymentDue(
                title = "Semester fee · 2nd installment",
                amountTaka = 18500,
                dueDate = "25 Oct"
            )
        )
    }
}
