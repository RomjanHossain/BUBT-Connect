package com.corethink.bubtconnect.ui.onboarding

import com.corethink.bubtconnect.data.repository.ProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSetupViewModelTest {

    @Test
    fun continueStaysDisabledUntilRollDeptAndSemesterAreSet() {
        val viewModel = ProfileSetupViewModel(ProfileRepository())
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.onRollChange("22235103045")
        viewModel.onDepartmentChange("CSE")
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.onSemesterChange("5th")
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun nameIsOptional() {
        val viewModel = ProfileSetupViewModel(ProfileRepository())
        viewModel.onRollChange("22235103045")
        viewModel.onDepartmentChange("CSE")
        viewModel.onSemesterChange("5th")
        assertTrue(viewModel.save())
    }

    @Test
    fun rollKeepsOnlyDigitsAndCapsLength() {
        val viewModel = ProfileSetupViewModel(ProfileRepository())
        viewModel.onRollChange("22-235 10304599")
        assertEquals("22235103045", viewModel.uiState.value.roll)
    }
}
