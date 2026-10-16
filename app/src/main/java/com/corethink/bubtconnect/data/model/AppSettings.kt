package com.corethink.bubtconnect.data.model

enum class ThemeMode(val label: String) { Light("Light"), Dark("Dark"), System("System") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val classReminderMinutes: Int = 10,
    val examReminders: Boolean = true,
    val paymentDeadlines: Boolean = true,
    /** Cover the scanner pre-selects on 1o; the student can still switch per document. */
    val defaultCoverTemplate: CoverTemplate = CoverTemplate.Assignment
)

/** Lead times offered by the "Class reminder" dropdown; 0 turns it off. */
val ClassReminderOptions = listOf(0, 5, 10, 15, 30)
