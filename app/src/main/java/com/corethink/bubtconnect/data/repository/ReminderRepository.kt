package com.corethink.bubtconnect.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Which classes and exams the student wants reminders for. Lead time comes from
 * [SettingsRepository]. In-memory; scheduling real notifications is not wired yet.
 */
class ReminderRepository {
    private val _reminders = MutableStateFlow<Set<String>>(emptySet())
    val reminders: StateFlow<Set<String>> = _reminders.asStateFlow()

    fun toggle(key: String) = _reminders.update { if (key in it) it - key else it + key }

    companion object {
        val Default = ReminderRepository()

        fun classKey(dayLabel: String, courseCode: String, startMinute: Int) = "class:$dayLabel:$courseCode:$startMinute"

        fun examKey(examId: String) = "exam:$examId"
    }
}
