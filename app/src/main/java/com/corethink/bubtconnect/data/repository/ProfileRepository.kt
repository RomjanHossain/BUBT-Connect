package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory profile store shared across onboarding screens. Swap for DataStore later. */
class ProfileRepository {
    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    private val _annexConnected = MutableStateFlow(false)
    val annexConnected: StateFlow<Boolean> = _annexConnected.asStateFlow()

    fun saveProfile(profile: StudentProfile) {
        _profile.value = profile
    }

    fun markAnnexConnected() {
        _annexConnected.value = true
    }

    companion object {
        /** App-wide instance until a DI container is introduced. */
        val Default = ProfileRepository()
    }
}
