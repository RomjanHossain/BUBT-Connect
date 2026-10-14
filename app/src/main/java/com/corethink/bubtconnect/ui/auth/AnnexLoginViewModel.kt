package com.corethink.bubtconnect.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.repository.AnnexAuthRepository
import com.corethink.bubtconnect.data.repository.AnnexAuthResult
import com.corethink.bubtconnect.data.repository.FakeAnnexAuthRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnnexLoginUiState(
    val studentId: String = "",
    val password: String = "",
    val keepSignedIn: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
) {
    val canSubmit: Boolean
        get() = studentId.isNotBlank() && password.isNotBlank() && !isLoading
}

/** Drives both the inline Connect card (1d) and the full Annex login (1e). */
class AnnexLoginViewModel(
    private val authRepository: AnnexAuthRepository = FakeAnnexAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnnexLoginUiState(studentId = ProfileRepository.Default.profile.value?.roll.orEmpty())
    )
    val uiState: StateFlow<AnnexLoginUiState> = _uiState.asStateFlow()

    fun onStudentIdChange(value: String) {
        _uiState.update { it.copy(studentId = value.filter(Char::isDigit), error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onKeepSignedInChange(value: Boolean) = _uiState.update { it.copy(keepSignedIn = value) }

    fun login() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.login(state.studentId, state.password, state.keepSignedIn)
            _uiState.update {
                when (result) {
                    AnnexAuthResult.Success -> it.copy(isLoading = false, isLoggedIn = true)
                    AnnexAuthResult.InvalidCredentials ->
                        it.copy(isLoading = false, error = "Invalid ID or password. Try again.")
                }
            }
        }
    }
}
