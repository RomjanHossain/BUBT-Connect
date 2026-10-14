package com.corethink.bubtconnect.ui.profile

import androidx.lifecycle.ViewModel
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.data.repository.ProfileRepository
import kotlinx.coroutines.flow.StateFlow

class GuestProfileViewModel(
    repository: ProfileRepository = ProfileRepository.Default
) : ViewModel() {
    val profile: StateFlow<StudentProfile?> = repository.profile
}
