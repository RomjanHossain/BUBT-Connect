package com.corethink.bubtconnect.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.ui.auth.AnnexLoginUiState
import com.corethink.bubtconnect.ui.auth.AnnexLoginViewModel
import com.corethink.bubtconnect.ui.auth.AuthErrorBanner
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtCard
import com.corethink.bubtconnect.ui.design.BubtCardStyle
import com.corethink.bubtconnect.ui.design.BubtChip
import com.corethink.bubtconnect.ui.design.BubtChipStyle
import com.corethink.bubtconnect.ui.design.BubtFieldState
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtTextField
import com.corethink.bubtconnect.ui.onboarding.BubtPasswordField
import com.corethink.bubtconnect.ui.onboarding.OnboardingTopBar
import com.corethink.bubtconnect.ui.onboarding.StepProgress

@Composable
fun GuestProfileRoute(
    onEditProfile: () -> Unit,
    onConnected: () -> Unit,
    onMaybeLater: () -> Unit,
    profileViewModel: GuestProfileViewModel = viewModel(),
    annexViewModel: AnnexLoginViewModel = viewModel()
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val annexState by annexViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(annexState.isLoggedIn) {
        if (annexState.isLoggedIn) onConnected()
    }
    GuestProfileScreen(
        profile = profile,
        annexState = annexState,
        onEditProfile = onEditProfile,
        onAnnexIdChange = annexViewModel::onStudentIdChange,
        onPasswordChange = annexViewModel::onPasswordChange,
        onConnect = annexViewModel::login,
        onMaybeLater = onMaybeLater
    )
}

/** 1d · Guest profile + Connect Annex CTA. Connect runs the auth flow inline. */
@Composable
fun GuestProfileScreen(
    profile: StudentProfile?,
    annexState: AnnexLoginUiState,
    onEditProfile: () -> Unit,
    onAnnexIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    onMaybeLater: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = BubtSpacing.Screen)
                .verticalScroll(rememberScrollState())
        ) {
            OnboardingTopBar(
                title = "Profile",
                trailing = { BubtChip(text = "Guest mode", style = BubtChipStyle.Normal) }
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Sm))
            StepProgress(totalSteps = 2, completedSteps = 2)
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))

            ProfileHeader(name = profile?.name ?: "Guest Student")
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))

            if (profile != null) {
                ProfileSummaryCard(profile = profile, onEditProfile = onEditProfile)
                Spacer(modifier = Modifier.height(BubtSpacing.Lg))
            }

            ConnectAnnexCard(
                state = annexState,
                onAnnexIdChange = onAnnexIdChange,
                onPasswordChange = onPasswordChange,
                onConnect = onConnect
            )

            BubtTextButton(
                text = "Maybe later",
                onClick = onMaybeLater,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = BubtSpacing.Md)
            )
        }
    }
}

@Composable
private fun ProfileHeader(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Guest · data stored on this device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(profile: StudentProfile, onEditProfile: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(BubtSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
        ) {
            SummaryRow(label = "Roll", value = profile.roll)
            SummaryRow(label = "Department", value = profile.department)
            SummaryRow(label = "Semester", value = profile.semester)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Xs))
                Text(
                    text = "Read-only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Edit in Profile",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(BubtShapes.Chip)
                        .clickable(onClick = onEditProfile)
                        .padding(horizontal = BubtSpacing.Xs, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(BubtSpacing.Sm))
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = "Locked",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

/** Inline Annex auth card — shared by 1d and the guest Profile (1s). The only filled-primary surface on the screen. */
@Composable
fun ConnectAnnexCard(
    state: AnnexLoginUiState,
    onAnnexIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    BubtCard(modifier = modifier, style = BubtCardStyle.Filled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Sm))
            Text(
                text = "Connect your Annex account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Xs))
        Text(
            text = "Use your BUBT Annex ID and password to unlock:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            BubtChip(text = "Attendance", style = BubtChipStyle.Normal)
            BubtChip(text = "Results", style = BubtChipStyle.Normal)
            BubtChip(text = "Class routine", style = BubtChipStyle.Normal)
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))
        BubtTextField(
            value = state.studentId,
            onValueChange = onAnnexIdChange,
            label = "Annex ID",
            keyboardType = KeyboardType.Number,
            stateOverride = if (state.error != null) BubtFieldState.Error else null
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        BubtPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            isError = state.error != null
        )
        AnimatedVisibility(visible = state.error != null) {
            AuthErrorBanner(
                message = state.error.orEmpty(),
                modifier = Modifier.padding(top = BubtSpacing.Md)
            )
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))
        BubtPrimaryButton(
            text = if (state.isLoading) "Connecting…" else "Connect Annex",
            onClick = onConnect,
            enabled = state.canSubmit,
            loading = state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestProfileScreenPreview() {
    BUBTConnectMaterial3Theme {
        GuestProfileScreen(
            profile = StudentProfile(roll = "22235103045", department = "CSE", semester = "5th"),
            annexState = AnnexLoginUiState(studentId = "22235103045"),
            onEditProfile = {},
            onAnnexIdChange = {},
            onPasswordChange = {},
            onConnect = {},
            onMaybeLater = {}
        )
    }
}
