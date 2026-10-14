package com.corethink.bubtconnect.ui.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.Exam
import com.corethink.bubtconnect.data.model.ExamKind
import com.corethink.bubtconnect.data.repository.AcademicsRepository
import com.corethink.bubtconnect.data.repository.ReminderRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExamsUiState(
    val kind: ExamKind = ExamKind.Normal,
    /** Exams of [kind], grouped by term ("Mid-term · Fall 2026"), soonest first. */
    val groups: Map<String, List<Exam>> = emptyMap(),
    val reminders: Set<String> = emptySet()
)

class ExamsViewModel(
    private val repository: AcademicsRepository = SampleAcademicsRepository,
    private val reminderRepository: ReminderRepository = ReminderRepository.Default
) : ViewModel() {

    private val kind = MutableStateFlow(ExamKind.Normal)

    val uiState: StateFlow<ExamsUiState> = combine(kind, reminderRepository.reminders) { kind, reminders ->
        ExamsUiState(kind, groupsFor(kind), reminders)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExamsUiState(groups = groupsFor(ExamKind.Normal))
    )

    private fun groupsFor(kind: ExamKind): Map<String, List<Exam>> =
        repository.exams().filter { it.kind == kind }.sortedBy { it.date }.groupBy { it.term }

    fun selectKind(value: ExamKind) {
        kind.value = value
    }

    fun toggleReminder(exam: Exam) = reminderRepository.toggle(ReminderRepository.examKey(exam.id))
}
