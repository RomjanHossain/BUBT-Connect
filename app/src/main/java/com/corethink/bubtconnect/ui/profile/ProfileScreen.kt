package com.corethink.bubtconnect.ui.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.corethink.bubtconnect.ui.design.BubtOnSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtTag
import com.corethink.bubtconnect.ui.design.BubtTagStyle

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

/** 1u · Profile & settings. Matches HTML 1u exactly. */
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App bar: "Profile" title on left, Edit icon button on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditProfile, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit profile",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        ProfileIdentityCard(
            profile = state.profile,
            isConnected = state.isConnected,
            lastSyncedAt = state.lastSyncedAt,
            isSyncing = state.isSyncing,
            onSync = onSync
        )

        // 1v is opened from Profile whether or not Annex is connected; only the connect
        // hero card is guest-only (1e). Guests keep the entry instead of losing the page.
        if (!state.isConnected) {
            ConnectAnnexHeroCard(onConnect = onConnect)
        }
        FeesEntry(onClick = onOpenFees)

        SectionLabel("NOTIFICATIONS")
        NotificationSettingsCard(
            settings = state.settings,
            onClassReminderChange = onClassReminderChange,
            onExamRemindersChange = onExamRemindersChange,
            onPaymentDeadlinesChange = onPaymentDeadlinesChange
        )

        SectionLabel("APPEARANCE")
        ThemeModeSelector(selected = state.settings.themeMode, onSelect = onThemeModeChange)

        Spacer(modifier = Modifier.height(8.dp))

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
private fun ProfileIdentityCard(
    profile: StudentProfile?,
    isConnected: Boolean,
    lastSyncedAt: Long?,
    isSyncing: Boolean,
    onSync: () -> Unit
) {
    val name = profile?.name ?: if (isConnected) "Rafi Ahmed" else "Guest Student"
    val roll = profile?.roll ?: "22235103045"
    val dept = profile?.department ?: "CSE"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID $roll",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        TagPill(dept)
                        TagPill("Intake 49")
                        TagPill("Sec 2")
                    }
                }
            }

            if (isConnected) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BubtSuccessContainerLight,
                    contentColor = BubtOnSuccessContainerLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = BubtSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Annex connected",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = BubtOnSuccessContainerLight
                            )
                            Text(
                                text = if (isSyncing) "Syncing…" else "Synced ${rememberSyncedLabel(lastSyncedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BubtOnSuccessContainerLight.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = onSync, enabled = !isSyncing, modifier = Modifier.size(36.dp)) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Sync,
                                    contentDescription = "Sync",
                                    tint = BubtOnSuccessContainerLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPill(text: String) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FeesEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Fees & Waivers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
private fun NotificationSettingsCard(
    settings: AppSettings,
    onClassReminderChange: (Int) -> Unit,
    onExamRemindersChange: (Boolean) -> Unit,
    onPaymentDeadlinesChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
}

@Composable
private fun SettingRow(
    label: String,
    icon: ImageVector,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
private fun ClassReminderDropdown(minutes: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = ClassReminderOptions.firstOrNull { it.first == minutes }?.second ?: "$minutes min before"

    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ClassReminderOptions.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

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
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun LogoutDialog(
    isConnected: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log out?") },
        text = {
            Text(
                if (isConnected) "Your Annex session and profile details will be cleared from this device."
                else "Your guest profile will be cleared from this device."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Log out", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    BUBTConnectMaterial3Theme {
        ProfileScreen(
            state = ProfileUiState(
                profile = StudentProfile(roll = "22235103045", department = "CSE"),
                isConnected = true
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