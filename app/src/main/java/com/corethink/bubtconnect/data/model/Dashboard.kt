package com.corethink.bubtconnect.data.model

data class Course(
    val code: String,
    val title: String
)

/** Swipeable cards at the top of Home (1f): next class, exam alert, payment due. */
sealed interface DashboardHighlight {
    data class NextClass(
        val course: Course,
        val startMinuteOfDay: Int,
        val endMinuteOfDay: Int,
        val room: String,
        val section: String? = null,
        val instructor: String? = null
    ) : DashboardHighlight

    data class ExamAlert(
        val course: Course,
        val examName: String,
        val date: String,
        val room: String
    ) : DashboardHighlight

    data class PaymentDue(
        val title: String,
        val amountTaka: Int,
        val dueDate: String
    ) : DashboardHighlight
}
