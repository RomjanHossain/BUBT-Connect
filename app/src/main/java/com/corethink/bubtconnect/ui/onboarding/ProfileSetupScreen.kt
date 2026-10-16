package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.BubtDepartments
import com.corethink.bubtconnect.data.model.BubtSemesters
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
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
    val haptic = LocalHapticFeedback.current
    ProfileSetupScreen(
        state = state,
        onBack = onBack,
        onRollChange = viewModel::onRollChange,
        onDepartmentChange = viewModel::onDepartmentChange,
        onSemesterChange = viewModel::onSemesterChange,
        onNameChange = viewModel::onNameChange,
        onContinue = {
            if (viewModel.save()) {
                onContinue()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        },
        isEditing = isEditing
    )
}

/** 1d · Basic profile setup — step 1 of 2. Matches HTML design 1d exactly. */
@Composable
fun ProfileSetupScreen(
    state: ProfileSetupUiState,
    onBack: () -> Unit,
    onRollChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onSemesterChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    isEditing: Boolean = false
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            // App Bar: Back arrow on left (-12dp offset), Step 1 of 2 on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .offset(x = (-12).dp)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isEditing) {
                    Text(
                        text = "Step 1 of 2",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar line: 4dp height, 50% fill
            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEditing) "Edit profile" else "Let's set up your profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEditing) "Update your basic details." else "No account needed. You can connect Annex later.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubtPrimaryButton(
                    text = if (isEditing) "Save" else "Continue",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileSetupScreenPreview() {
    BUBTConnectMaterial3Theme {
        ProfileSetupScreen(
            state = ProfileSetupUiState(roll = "22235103045", department = "CSE", semester = "5th"),
            onBack = {},
            onRollChange = {},
            onDepartmentChange = {},
            onSemesterChange = {},
            onNameChange = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, name = "1d Error State")
@Composable
private fun ProfileSetupScreenErrorPreview() {
    BUBTConnectMaterial3Theme {
        ProfileSetupScreen(
            state = ProfileSetupUiState(
                roll = "22235103045",
                department = "Computer Science & Eng.",
                semester = "",
                name = "Rafi Ahmed",
                showErrors = true
            ),
            onBack = {},
            onRollChange = {},
            onDepartmentChange = {},
            onSemesterChange = {},
            onNameChange = {},
            onContinue = {}
        )
    }
}