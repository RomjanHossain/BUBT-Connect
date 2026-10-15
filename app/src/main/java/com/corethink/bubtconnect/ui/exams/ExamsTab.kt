package com.corethink.bubtconnect.ui.exams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.Exam
import com.corethink.bubtconnect.data.model.ExamKind
import com.corethink.bubtconnect.data.repository.ReminderRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.components.EmptyIllustration
import com.corethink.bubtconnect.ui.components.EmptyState
import com.corethink.bubtconnect.ui.components.showNotAvailableYet
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtOnSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtTonalButton

@Composable
fun ExamsTab(viewModel: ExamsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ExamsContent(
        state = state,
        onSelectKind = viewModel::selectKind,
        onDownloadPdf = { showNotAvailableYet(context, "Exam PDF") },
        onDownloadAll = { showNotAvailableYet(context, "All exam PDFs") },
        onToggleReminder = viewModel::toggleReminder,
        onTurnOnAlerts = { showNotAvailableYet(context, "Exam alerts") }
    )
}

/** 1i · Exam routine. Matches HTML 1i exactly. */
@Composable
fun ExamsContent(
    state: ExamsUiState,
    onSelectKind: (ExamKind) -> Unit,
    onDownloadPdf: (Exam) -> Unit,
    onDownloadAll: () -> Unit,
    onToggleReminder: (Exam) -> Unit,
    onTurnOnAlerts: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BubtSegmentedControl(
                options = ExamKind.entries,
                selected = state.kind,
                label = ExamKind::label,
                onSelect = onSelectKind
            )
        }
        if (state.kind == ExamKind.Normal && state.supplementaryCount > 0) {
            item {
                SupplementaryBanner(
                    count = state.supplementaryCount,
                    onClick = { onSelectKind(ExamKind.Supplementary) }
                )
            }
        }
        if (state.groups.isEmpty()) {
            item {
                EmptyState(
                    illustration = EmptyIllustration.NoExams,
                    title = "No upcoming exams 🎉",
                    message = "No ${state.kind.label.lowercase()} exams for you this term. We'll notify you if one is scheduled.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    action = {
                        BubtOutlinedButton(
                            text = "Turn on exam alerts",
                            onClick = onTurnOnAlerts,
                            leadingIcon = Icons.Outlined.NotificationsNone
                        )
                    }
                )
            }
        }
        state.groups.forEach { (term, exams) ->
            item(key = term) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = term,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = onDownloadAll,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            items(exams, key = { it.id }) { exam ->
                ExamCard(
                    exam = exam,
                    supplementary = exam.kind == ExamKind.Supplementary,
                    reminderSet = ReminderRepository.examKey(exam.id) in state.reminders,
                    onDownloadPdf = { onDownloadPdf(exam) },
                    onToggleReminder = { onToggleReminder(exam) }
                )
            }
        }
    }
}

@Composable
private fun SupplementaryBanner(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (count == 1) "1 supplementary exam scheduled" else "$count supplementary exams scheduled",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    supplementary: Boolean,
    reminderSet: Boolean,
    onDownloadPdf: () -> Unit,
    onToggleReminder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (supplementary) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surface,
        border = if (supplementary) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DateBadge(day = exam.date.day, month = exam.date.monthShort)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = exam.course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${exam.course.code} · ${exam.date.weekdayShort} · ${exam.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Room ${exam.room} · Seat ${exam.seat}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BubtTonalButton(
                    text = "PDF",
                    onClick = onDownloadPdf,
                    leadingIcon = Icons.Outlined.PictureAsPdf,
                    modifier = Modifier.weight(1f)
                )
                if (reminderSet) {
                    Surface(
                        onClick = onToggleReminder,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = BubtSuccessContainerLight,
                        contentColor = BubtOnSuccessContainerLight
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reminder on",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    BubtOutlinedButton(
                        text = "Set reminder",
                        onClick = onToggleReminder,
                        leadingIcon = Icons.Outlined.NotificationsNone,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(day: Int, month: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.width(48.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 22.sp
            )
            Text(
                text = month.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamsTabPreview() {
    BUBTConnectMaterial3Theme {
        ExamsContent(
            state = ExamsUiState(
                kind = ExamKind.Normal,
                groups = mapOf("Mid-term · Fall 2026" to SampleAcademicsRepository.exams()),
                supplementaryCount = 1
            ),
            onSelectKind = {},
            onDownloadPdf = {},
            onDownloadAll = {},
            onToggleReminder = {}
        )
    }
}