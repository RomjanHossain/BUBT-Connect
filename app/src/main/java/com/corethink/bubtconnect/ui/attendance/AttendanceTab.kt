package com.corethink.bubtconnect.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.AttendanceRecord
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.LockedFeature
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.components.rememberSyncedLabel
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtError
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtWarning

@Composable
fun AttendanceTab(onConnectAnnex: () -> Unit, viewModel: AttendanceViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.isConnected) {
        LockedFeature(
            title = "Attendance",
            message = "Connect your Annex account to track attendance for every course.",
            onConnect = onConnectAnnex
        )
        return
    }
    AttendanceContent(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenLog = viewModel::openLog,
        onCloseLog = viewModel::closeLog
    )
}

/** 1j · Attendance. Below 75% = dashed card + "At risk" chip. Tap a card for the date-wise log. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceContent(
    state: AttendanceUiState,
    onRefresh: () -> Unit,
    onOpenLog: (AttendanceRecord) -> Unit,
    onCloseLog: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
        ) {
            item {
                Text(
                    text = "Minimum ${AttendanceRecord.MIN_ATTENDANCE_PERCENT}% needed to sit for finals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(state.records, key = { it.course.code }) { record ->
                AttendanceCard(record = record, onClick = { onOpenLog(record) })
            }
            item {
                Text(
                    text = "${rememberSyncedLabel(state.lastSyncedAt)} · pull to refresh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    state.openLog?.let { record ->
        ModalBottomSheet(onDismissRequest = onCloseLog) {
            AttendanceLog(record)
        }
    }
}

@Composable
private fun AttendanceCard(record: AttendanceRecord, onClick: () -> Unit) {
    val ringColor = when {
        record.atRisk -> BubtError
        record.percent < 80 -> BubtWarning
        else -> BubtSuccess
    }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (record.atRisk) Modifier.dashedBorder(BubtError, 16.dp) else Modifier),
        shape = BubtShapes.Card,
        color = if (record.atRisk) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        border = if (record.atRisk) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(BubtSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { record.percent / 100f },
                    modifier = Modifier.size(52.dp),
                    color = ringColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeWidth = 5.dp
                )
                Text(
                    text = "${record.percent}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(BubtSpacing.Lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.course.code,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append("${record.attended} / ${record.total} ${record.unit}")
                        if (record.atRisk) append(" · need ${record.classesNeeded} more")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (record.atRisk) {
                Surface(
                    shape = BubtShapes.Chip,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PriorityHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(text = "At risk", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "On track",
                    tint = BubtSuccess
                )
            }
        }
    }
}

@Composable
private fun AttendanceLog(record: AttendanceRecord) {
    Column(modifier = Modifier.padding(horizontal = BubtSpacing.Screen)) {
        Text(
            text = "${record.course.code} · ${record.course.title}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${record.attended} of ${record.total} ${record.unit} attended (${record.percent}%)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = BubtSpacing.Md)
        )
        LazyColumn(contentPadding = PaddingValues(bottom = BubtSpacing.Xl)) {
            items(record.log) { (date, present) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = BubtSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${date.weekdayShort}, ${date.shortLabel}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (present) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = if (present) BubtSuccess else BubtError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                    Text(
                        text = if (present) "Present" else "Absent",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(56.dp)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 750)
@Composable
private fun AttendanceContentPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendanceContent(
                state = AttendanceUiState(
                    isConnected = true,
                    records = SampleAcademicsRepository.attendance(),
                    lastSyncedAt = System.currentTimeMillis() - 5 * 60_000
                ),
                onRefresh = {},
                onOpenLog = {},
                onCloseLog = {}
            )
        }
    }
}
