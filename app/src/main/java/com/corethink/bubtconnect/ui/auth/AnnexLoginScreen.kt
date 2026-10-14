package com.corethink.bubtconnect.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtFieldState
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTextField
import com.corethink.bubtconnect.ui.onboarding.BubtLogo
import com.corethink.bubtconnect.ui.onboarding.BubtPasswordField
import com.corethink.bubtconnect.ui.onboarding.OnboardingTopBar

@Composable
fun AnnexLoginRoute(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: AnnexLoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }
    AnnexLoginScreen(
        state = state,
        onBack = onBack,
        onStudentIdChange = viewModel::onStudentIdChange,
        onPasswordChange = viewModel::onPasswordChange,
        onKeepSignedInChange = viewModel::onKeepSignedInChange,
        onLogin = viewModel::login
    )
}

/** 1e · Annex login. On success → sync → Home with all tiles unlocked. */
@Composable
fun AnnexLoginScreen(
    state: AnnexLoginUiState,
    onBack: () -> Unit,
    onStudentIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onKeepSignedInChange: (Boolean) -> Unit,
    onLogin: () -> Unit
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
            OnboardingTopBar(title = "Annex Login", onBack = onBack)
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BubtLogo(size = 56.dp)
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
                Column {
                    Text(
                        text = "BUBT Annex",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use your Annex ID + password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))

            BubtTextField(
                value = state.studentId,
                onValueChange = onStudentIdChange,
                label = "Student ID",
                placeholder = "22235103045",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = BubtSpacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keep me signed in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state.keepSignedIn, onCheckedChange = onKeepSignedInChange)
            }

            BubtPrimaryButton(
                text = if (state.isLoading) "Syncing…" else "Log in & Sync",
                onClick = onLogin,
                enabled = state.canSubmit,
                loading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))

            Text(
                text = "This unlocks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
                UnlockItem("Attendance")
                UnlockItem("Results & CGPA")
                UnlockItem("Class Routine (auto)")
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Xs))
                Text(
                    text = "Credentials stored encrypted on device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))
        }
    }
}

@Composable
fun AuthErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BubtShapes.Field,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Sm))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun UnlockItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = BubtSuccess,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnexLoginScreenErrorPreview() {
    BUBTConnectMaterial3Theme {
        AnnexLoginScreen(
            state = AnnexLoginUiState(
                studentId = "22235103045",
                password = "secret",
                error = "Invalid ID or password. Try again."
            ),
            onBack = {},
            onStudentIdChange = {},
            onPasswordChange = {},
            onKeepSignedInChange = {},
            onLogin = {}
        )
    }
}
