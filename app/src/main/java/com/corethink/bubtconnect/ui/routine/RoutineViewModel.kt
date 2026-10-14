package com.corethink.bubtconnect.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.ClassDay
import com.corethink.bubtconnect.data.model.RoutineSlot
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.ReminderRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RoutineUiState(
    val selectedDay: ClassDay = ClassDay.today(),
    val today: ClassDay = ClassDay.today(),
    val slots: List<RoutineSlot> = emptyList(),
    val reminders: Set<String> = emptySet(),
    val reminderLeadMinutes: Int = 10
)

class RoutineViewModel(
    private val repository: AcademicsRepository = SampleAcademicsRepository,
    private val reminderRepository: ReminderRepository = ReminderRepository.Default,
    settingsRepository: SettingsRepository = SettingsRepository.Default
) : ViewModel() {

    private val selectedDay = MutableStateFlow(ClassDay.today())

    val uiState: StateFlow<RoutineUiState> = combine(
        selectedDay,
        reminderRepository.reminders,
        settingsRepository.settings
    ) { day, reminders, settings ->
        RoutineUiState(
            selectedDay = day,
            slots = repository.routine(day),
            reminders = reminders,
            reminderLeadMinutes = settings.classReminderMinutes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutineUiState(slots = repository.routine(ClassDay.today()))
    )

    fun selectDay(day: ClassDay) {
        selectedDay.value = day
    }

    fun toggleReminder(slot: RoutineSlot.Class) {
        reminderRepository.toggle(reminderKey(selectedDay.value, slot))
    }

    companion object {
        fun reminderKey(day: ClassDay, slot: RoutineSlot.Class) =
            ReminderRepository.classKey(day.label, slot.course.code, slot.startMinute)
    }
}
