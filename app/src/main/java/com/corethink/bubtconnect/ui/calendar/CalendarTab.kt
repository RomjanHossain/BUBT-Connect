package com.corethink.bubtconnect.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtWarning

@Composable
fun CalendarTab(
    onSeeExamRoutine: () -> Unit,
    onOpenFees: () -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarContent(
        state = state,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onSelectFilter = viewModel::selectFilter,
        onSeeExamRoutine = onSeeExamRoutine,
        onOpenFees = onOpenFees
    )
}

/** 1l · Academic calendar. Filter chips, colored type icons, red Today line, past events at 60%. */
@Composable
fun CalendarContent(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectFilter: (CalendarFilter) -> Unit,
    onSeeExamRoutine: () -> Unit,
    onOpenFees: () -> Unit
) {
    val todayIndex = state.todayInsertIndex
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md)
    ) {
        item {
            MonthHeader(label = state.monthLabel, onPrevious = onPreviousMonth, onNext = onNextMonth)
            Spacer(modifier = Modifier.height(BubtSpacing.Sm))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                items(CalendarFilter.entries) { filter ->
                    FilterPill(
                        text = filter.label,
                        selected = filter == state.filter,
                        onClick = { onSelectFilter(filter) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))
        }
        if (state.events.isEmpty()) {
            item {
                Text(
                    text = "Nothing scheduled this month.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (todayIndex != null) item { TodayDivider(state.today) }
        }
        state.events.forEachIndexed { index, event ->
            if (index == todayIndex) item { TodayDivider(state.today) }
            item {
                EventRow(
                    event = event,
                    isPast = event.date < state.today,
                    isFirst = index == 0,
                    isLast = index == state.events.lastIndex,
                    onSeeExamRoutine = onSeeExamRoutine,
                    onOpenFees = onOpenFees
                )
            }
        }
        if (todayIndex != null && todayIndex == state.events.size && state.events.isNotEmpty()) {
            item { TodayDivider(state.today) }
        }
    }
}

@Composable
private fun MonthHeader(label: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MonthArrow(onClick = onPrevious, next = false)
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = BubtSpacing.Md)
        )
        MonthArrow(onClick = onNext, next = true)
    }
}

@Composable
private fun MonthArrow(onClick: () -> Unit, next: Boolean) {
    Surface(
        onClick = onClick,
        shape = BubtShapes.Skeleton,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (next) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = if (next) "Next month" else "Previous month",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Payments amber, Exams blue, Holidays green. */
@Composable
private fun typeColor(type: CalendarEventType): Color = when (type) {
    CalendarEventType.Payment -> BubtWarning
    CalendarEventType.Exam -> MaterialTheme.colorScheme.primary
    CalendarEventType.Holiday -> BubtSuccess
}

private fun typeIcon(type: CalendarEventType): ImageVector = when (type) {
    CalendarEventType.Payment -> Icons.Filled.Payments
    CalendarEventType.Exam -> Icons.Outlined.EditNote
    CalendarEventType.Holiday -> Icons.Outlined.BeachAccess
}

/** "OCT 05 · PAID", "OCT 20 – 27 · EXAM", "OCT 31 · DEADLINE". */
private fun badgeLabel(event: CalendarEvent): String {
    val date = when (event.endDate) {
        null -> event.date.shortLabel
        else -> "${event.date.shortLabel} – ${event.endDate.day}"
    }
    val badge = when (event.type) {
        CalendarEventType.Payment ->
            if (event.title.contains("clearance", ignoreCase = true)) "DEADLINE" else "PAID"
        CalendarEventType.Exam -> "EXAM"
        CalendarEventType.Holiday -> "HOLIDAY"
    }
    return "${date.uppercase()} · $badge"
}

@Composable
private fun TodayDivider(today: SimpleDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BubtSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TODAY · ${today.shortLabel.uppercase()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.width(120.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .dashedBorder(MaterialTheme.colorScheme.error, 0.dp, width = 1.dp)
        )
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    isPast: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onSeeExamRoutine: () -> Unit,
    onOpenFees: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!(isFirst && isLast)) {
                Box(
                    modifier = Modifier
                        .padding(top = if (isFirst) 22.dp else 0.dp)
                        .width(1.5.dp)
                        .then(if (isLast) Modifier.height(22.dp) else Modifier.fillMaxHeight())
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            Box(modifier = Modifier.padding(top = 12.dp)) {
                EventIcon(event.type, muted = isPast)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = BubtSpacing.Sm, bottom = BubtSpacing.Md)
        ) {
            if (isPast) {
                Column(modifier = Modifier.padding(vertical = BubtSpacing.Md)) {
                    Text(
                        text = badgeLabel(event),
                        style = MaterialTheme.typography.labelMedium,
                        color = muted
                    )
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = muted
                    )
                }
            } else {
                EventCard(event = event, onSeeExamRoutine = onSeeExamRoutine, onOpenFees = onOpenFees)
            }
        }
    }
}

@Composable
private fun EventIcon(type: CalendarEventType, muted: Boolean = false) {
    val color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else typeColor(type)
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = typeIcon(type),
                contentDescription = type.label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent, onSeeExamRoutine: () -> Unit, onOpenFees: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(horizontal = BubtSpacing.CardPadding, vertical = BubtSpacing.Md)) {
            EventIcon(type = event.type)
            Spacer(modifier = Modifier.width(BubtSpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badgeLabel(event),
                    style = MaterialTheme.typography.labelMedium,
                    color = typeColor(event.type)
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.endDate?.let { "${event.note} · ${event.date.shortLabel} – ${it.shortLabel}" } ?: event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val action = when (event.type) {
                    CalendarEventType.Exam -> "See routine" to onSeeExamRoutine
                    CalendarEventType.Payment -> "View fees" to onOpenFees
                    CalendarEventType.Holiday -> null
                }
                action?.let { (label, onClick) ->
                    Spacer(modifier = Modifier.height(BubtSpacing.Sm))
                    Surface(
                        onClick = onClick,
                        shape = BubtShapes.Chip,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "$label →",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs + 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 750)
@Composable
private fun CalendarContentPreview() {
    val today = SimpleDate(2026, 10, 11)
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CalendarContent(
                state = CalendarUiState(
                    year = 2026,
                    month = 10,
                    today = today,
                    allEvents = SampleAcademicsRepository.calendarEvents().filter { it.date.month == 10 }
                ),
                onPreviousMonth = {},
                onNextMonth = {},
                onSelectFilter = {},
                onSeeExamRoutine = {},
                onOpenFees = {}
            )
        }
    }
}
