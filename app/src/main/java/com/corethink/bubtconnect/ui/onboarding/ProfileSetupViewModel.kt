package com.corethink.bubtconnect.ui.onboarding

import androidx.lifecycle.ViewModel
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileSetupUiState(
    val roll: String = "",
    val department: String = "",
    val semester: String = "",
    val name: String = "",
    /** Set when Continue is tapped with invalid fields (spec 1d error state). */
    val showErrors: Boolean = false
) {
    val canContinue: Boolean
        get() = roll.isNotBlank() && department.isNotBlank() && semester.isNotBlank()

    val rollError: String?
        get() = if (showErrors && roll.length != ROLL_LENGTH) "Must be $ROLL_LENGTH digits" else null

    val departmentError: String?
        get() = if (showErrors && department.isBlank()) "Please select your department" else null

    val semesterError: String?
        get() = if (showErrors && semester.isBlank()) "Please select your current semester" else null

    private companion object {
        const val ROLL_LENGTH = 11
    }
}

class ProfileSetupViewModel(
    private val repository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        repository.profile.value?.let {
            ProfileSetupUiState(it.roll, it.department, it.semester, it.name.orEmpty())
        } ?: ProfileSetupUiState()
    )
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onRollChange(value: String) {
        _uiState.update { it.copy(roll = value.filter(Char::isDigit).take(MAX_ROLL_LENGTH), showErrors = false) }
    }

    fun onDepartmentChange(value: String) = _uiState.update { it.copy(department = value, showErrors = false) }

    fun onSemesterChange(value: String) = _uiState.update { it.copy(semester = value, showErrors = false) }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    /** Persists the profile; returns false if required fields are missing. */
    fun save(): Boolean {
        val state = _uiState.value
        if (!state.canContinue) {
            _uiState.update { it.copy(showErrors = true) }
            return false
        }
        repository.saveProfile(
            StudentProfile(
                roll = state.roll,
                department = state.department,
                semester = state.semester,
                name = state.name.trim().ifBlank { null }
            )
        )
        return true
    }

    private companion object {
        const val MAX_ROLL_LENGTH = 11
    }
}
