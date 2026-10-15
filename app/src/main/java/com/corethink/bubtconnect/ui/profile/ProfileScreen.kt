package com.corethink.bubtconnect.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.AppSettings
import com.corethink.bubtconnect.data.model.ClassReminderOptions
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.data.model.ThemeMode
import com.corethink.bubtconnect.ui.auth.AnnexLoginUiState
import com.corethink.bubtconnect.ui.auth.AnnexLoginViewModel
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.components.rememberSyncedLabel
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtCard
import com.corethink.bubtconnect.ui.design.BubtCardStyle
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTag
import com.corethink.bubtconnect.ui.design.BubtTagStyle
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.bubtErrorText

@Composable
fun ProfileRoute(
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    onOpenFees: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    annexViewModel: AnnexLoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val annexState by annexViewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        annexState = annexState,
        onEditProfile = onEditProfile,
        onOpenFees = onOpenFees,
        onSync = viewModel::sync,
        onAnnexIdChange = annexViewModel::onStudentIdChange,
        onPasswordChange = annexViewModel::onPasswordChange,
        onConnect = annexViewModel::login,
        onClassReminderChange = viewModel::setClassReminderMinutes,
        onExamRemindersChange = viewModel::setExamReminders,
        onPaymentDeadlinesChange = viewModel::setPaymentDeadlines,
        onThemeModeChange = viewModel::setThemeMode,
        onLogout = {
            viewModel.logout()
            onLoggedOut()
        }
    )
}

/** 1u · Profile. Guest version swaps the status card for the Connect CTA from 1e. */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    annexState: AnnexLoginUiState,
    onEditProfile: () -> Unit,
    onSync: () -> Unit,
    onAnnexIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    onClassReminderChange: (Int) -> Unit,
    onExamRemindersChange: (Boolean) -> Unit,
    onPaymentDeadlinesChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenFees: () -> Unit = {}
) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            BubtTextButton(text = "Edit", onClick = onEditProfile)
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))

        ProfileIdentity(
            profile = state.profile,
            isConnected = state.isConnected,
            lastSyncedAt = state.lastSyncedAt,
            isSyncing = state.isSyncing,
            onSync = onSync
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))

        if (state.isConnected) {
            FeesEntry(onClick = onOpenFees)
        } else {
            ConnectAnnexCard(
                state = annexState,
                onAnnexIdChange = onAnnexIdChange,
                onPasswordChange = onPasswordChange,
                onConnect = onConnect
            )
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))

        NotificationSettings(
            settings = state.settings,
            onClassReminderChange = onClassReminderChange,
            onExamRemindersChange = onExamRemindersChange,
            onPaymentDeadlinesChange = onPaymentDeadlinesChange
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))

        SectionLabel("APPEARANCE")
        ThemeModeSelector(selected = state.settings.themeMode, onSelect = onThemeModeChange)
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))

        BubtOutlinedButton(
            text = "Log out",
            onClick = { confirmLogout = true },
            leadingIcon = Icons.AutoMirrored.Outlined.Logout,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (confirmLogout) {
        LogoutDialog(
            isConnected = state.isConnected,
            onConfirm = {
                confirmLogout = false
                onLogout()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}

@Composable
private fun ProfileIdentity(
    profile: StudentProfile?,
    isConnected: Boolean,
    lastSyncedAt: Long?,
    isSyncing: Boolean,
    onSync: () -> Unit
) {
    val name = profile?.name ?: if (isConnected) "Student" else "Guest Student"
    BubtCard(modifier = Modifier.fillMaxWidth(), style = BubtCardStyle.Outlined) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(BubtSpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isConnected) {
                        Spacer(modifier = Modifier.width(BubtSpacing.Xs))
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Verified",
                            tint = BubtSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (profile != null) {
                    Text(
                        text = "ID ${profile.roll}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (profile != null) {
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtTag(text = profile.department, style = BubtTagStyle.Primary)
                BubtTag(text = "Intake 49", style = BubtTagStyle.Neutral)
                BubtTag(text = "Sec 2", style = BubtTagStyle.Neutral)
            }
        }
        if (isConnected) {
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    tint = BubtSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Annex connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSyncing) "Syncing…" else "Synced ${rememberSyncedLabel(lastSyncedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onSync, enabled = !isSyncing) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeesEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(BubtSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Md))
            Text(
                text = "Fees & Waivers",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationSettings(
    settings: AppSettings,
    onClassReminderChange: (Int) -> Unit,
    onExamRemindersChange: (Boolean) -> Unit,
    onPaymentDeadlinesChange: (Boolean) -> Unit
) {
    SectionLabel("NOTIFICATIONS")
    BubtCard(modifier = Modifier.fillMaxWidth(), style = BubtCardStyle.Outlined) {
        SettingRow(label = "Class reminder", icon = Icons.Outlined.Alarm) {
            ClassReminderDropdown(
                minutes = settings.classReminderMinutes,
                onSelect = onClassReminderChange
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        SettingRow(label = "Exam reminders", icon = Icons.Outlined.EditNote) {
            Switch(checked = settings.examReminders, onCheckedChange = onExamRemindersChange)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        SettingRow(label = "Payment deadlines", icon = Icons.Outlined.Payments) {
            Switch(checked = settings.paymentDeadlines, onCheckedChange = onPaymentDeadlinesChange)
        }
    }
}

@Composable
private fun SettingRow(label: String, icon: ImageVector, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        control()
    }
}

@Composable
private fun ClassReminderDropdown(minutes: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = reminderLabel(minutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ClassReminderOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(reminderLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun reminderLabel(minutes: Int): String = if (minutes == 0) "Off" else "$minutes min before"

@Composable
private fun ThemeModeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    BubtSegmentedControl(
        options = ThemeMode.entries,
        selected = selected,
        label = ThemeMode::label,
        onSelect = onSelect
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = BubtSpacing.Sm)
    )
}

@Composable
private fun LogoutDialog(isConnected: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = BubtShapes.Card,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Log out?", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = if (isConnected) {
                    "Your Annex session and saved profile will be removed from this device."
                } else {
                    "Your saved profile will be removed from this device."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Log out", color = bubtErrorText(), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ProfileScreenConnectedPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreen(
                state = ProfileUiState(
                    profile = StudentProfile("22235103045", "CSE", "5th", "Rafi Ahmed"),
                    isConnected = true,
                    lastSyncedAt = System.currentTimeMillis() - 5 * 60_000
                ),
                annexState = AnnexLoginUiState(),
                onEditProfile = {},
                onSync = {},
                onAnnexIdChange = {},
                onPasswordChange = {},
                onConnect = {},
                onClassReminderChange = {},
                onExamRemindersChange = {},
                onPaymentDeadlinesChange = {},
                onThemeModeChange = {},
                onLogout = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ProfileScreenGuestPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreen(
                state = ProfileUiState(profile = StudentProfile("22235103045", "CSE", "5th")),
                annexState = AnnexLoginUiState(studentId = "22235103045"),
                onEditProfile = {},
                onSync = {},
                onAnnexIdChange = {},
                onPasswordChange = {},
                onConnect = {},
                onClassReminderChange = {},
                onExamRemindersChange = {},
                onPaymentDeadlinesChange = {},
                onThemeModeChange = {},
                onLogout = {}
            )
        }
    }
}
