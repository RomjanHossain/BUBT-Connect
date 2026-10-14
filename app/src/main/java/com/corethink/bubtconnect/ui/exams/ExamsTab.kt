package com.corethink.bubtconnect.ui.exams

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.Exam
import com.corethink.bubtconnect.data.model.ExamKind
import com.corethink.bubtconnect.data.repository.ReminderRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.components.showNotAvailableYet
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing

@Composable
fun ExamsTab(viewModel: ExamsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ExamsContent(
        state = state,
        onSelectKind = viewModel::selectKind,
        onDownloadPdf = { showNotAvailableYet(context, "Exam PDF") },
        onToggleReminder = viewModel::toggleReminder
    )
}

/** 1h · Exam routine. Reminder button flips to the filled "Reminder set" state. */
@Composable
fun ExamsContent(
    state: ExamsUiState,
    onSelectKind: (ExamKind) -> Unit,
    onDownloadPdf: (Exam) -> Unit,
    onToggleReminder: (Exam) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        item {
            BubtSegmentedControl(
                options = ExamKind.entries,
                selected = state.kind,
                label = ExamKind::label,
                onSelect = onSelectKind
            )
        }
        if (state.groups.isEmpty()) {
            item {
                Text(
                    text = "No ${state.kind.label.lowercase()} exams scheduled.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = BubtSpacing.Lg)
                )
            }
        }
        state.groups.forEach { (term, exams) ->
            item(key = term) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = term,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${exams.size} exam${if (exams.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(exams, key = { it.id }) { exam ->
                ExamCard(
                    exam = exam,
                    reminderSet = ReminderRepository.examKey(exam.id) in state.reminders,
                    onDownloadPdf = { onDownloadPdf(exam) },
                    onToggleReminder = { onToggleReminder(exam) }
                )
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    reminderSet: Boolean,
    onDownloadPdf: () -> Unit,
    onToggleReminder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.Top) {
                DateBadge(day = exam.date.day, month = exam.date.monthShort)
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${exam.course.code} · ${exam.course.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${exam.date.weekdayShort} · ${exam.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${exam.room} · Seat ${exam.seat}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtOutlinedButton(
                    text = "PDF",
                    onClick = onDownloadPdf,
                    leadingIcon = Icons.Outlined.Download,
                    modifier = Modifier.weight(1f)
                )
                if (reminderSet) {
                    BubtPrimaryButton(
                        text = "Reminder set",
                        onClick = onToggleReminder,
                        leadingIcon = Icons.Filled.Check,
                        modifier = Modifier.weight(1.4f)
                    )
                } else {
                    BubtOutlinedButton(
                        text = "Set reminder",
                        onClick = onToggleReminder,
                        leadingIcon = Icons.Outlined.NotificationsNone,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(day: Int, month: String) {
    Surface(
        shape = BubtShapes.Field,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(width = 48.dp, height = 52.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = day.toString(), style = MaterialTheme.typography.titleLarge)
            Text(text = month.uppercase(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ExamsContentPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ExamsContent(
                state = ExamsUiState(
                    groups = SampleAcademicsRepository.exams().filter { it.kind == ExamKind.Normal }.groupBy { it.term },
                    reminders = setOf("exam:mid-cse231")
                ),
                onSelectKind = {},
                onDownloadPdf = {},
                onToggleReminder = {}
            )
        }
    }
}
