package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.BubtDepartments
import com.corethink.bubtconnect.data.model.BubtSemesters
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtTextField

@Composable
fun ProfileSetupRoute(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    isEditing: Boolean = false,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileSetupScreen(
        state = state,
        onBack = onBack,
        onRollChange = viewModel::onRollChange,
        onDepartmentChange = viewModel::onDepartmentChange,
        onSemesterChange = viewModel::onSemesterChange,
        onNameChange = viewModel::onNameChange,
        onContinue = { if (viewModel.save()) onContinue() },
        onSkip = onSkip,
        isEditing = isEditing
    )
}

/** 1c · Basic profile setup — step 1 of 2, no login needed. [isEditing] reuses it from Profile (1s). */
@Composable
fun ProfileSetupScreen(
    state: ProfileSetupUiState,
    onBack: () -> Unit,
    onRollChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onSemesterChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    isEditing: Boolean = false
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = BubtSpacing.Screen)
        ) {
            OnboardingTopBar(
                title = if (isEditing) "Edit profile" else "",
                onBack = onBack,
                trailing = {
                    if (!isEditing) {
                        Text(
                            text = "Step 1 of 2",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            if (!isEditing) {
                Spacer(modifier = Modifier.height(BubtSpacing.Sm))
                StepProgress(totalSteps = 2, completedSteps = 1)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = BubtSpacing.Xl),
                verticalArrangement = Arrangement.spacedBy(BubtSpacing.Lg)
            ) {
                Text(
                    text = if (isEditing) "Update your basic details." else "Let's set up your profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEditing) "Update your basic details." else "No account needed. You can connect Annex later.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BubtTextField(
                    value = state.roll,
                    onValueChange = onRollChange,
                    label = "Roll number",
                    placeholder = "e.g. 22235103045",
                    error = state.rollError,
                    keyboardType = KeyboardType.Number
                )
                BubtDropdownField(
                    value = state.department,
                    label = "Department",
                    placeholder = "Select department",
                    options = BubtDepartments,
                    onSelect = onDepartmentChange,
                    error = state.departmentError
                )
                BubtDropdownField(
                    value = state.semester,
                    label = "Current semester",
                    placeholder = "Select semester",
                    options = BubtSemesters,
                    onSelect = onSemesterChange,
                    error = state.semesterError
                )
                BubtTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = "Name (optional)",
                    placeholder = "e.g. Rafi Ahmed"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = BubtSpacing.Lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubtPrimaryButton(
                    text = if (isEditing) "Save" else "Continue",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isEditing) {
                    Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                    BubtTextButton(text = "Skip for now", onClick = onSkip)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileSetupScreenPreview() {
    BUBTConnectMaterial3Theme {
        ProfileSetupScreen(
            state = ProfileSetupUiState(roll = "22235103045", department = "CSE"),
            onBack = {},
            onRollChange = {},
            onDepartmentChange = {},
            onSemesterChange = {},
            onNameChange = {},
            onContinue = {},
            onSkip = {}
        )
    }
}
