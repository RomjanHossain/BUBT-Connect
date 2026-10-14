package com.corethink.bubtconnect.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CalendarEvent
import com.corethink.bubtconnect.data.model.CalendarEventType
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing

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
        onSeeExamRoutine = onSeeExamRoutine,
        onOpenFees = onOpenFees
    )
}

/** 1k · Academic calendar. Marker shape encodes type (square / circle / diamond) so it reads in grayscale. */
@Composable
fun CalendarContent(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
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
            Legend()
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
            if (todayIndex != null) item { TodayDivider() }
        }
        state.events.forEachIndexed { index, event ->
            if (index == todayIndex) item { TodayDivider() }
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
            item { TodayDivider() }
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

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Lg)) {
        CalendarEventType.entries.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                EventMarker(type = type, color = markerColor(type))
                Spacer(modifier = Modifier.width(BubtSpacing.Xs + 2.dp))
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun markerColor(type: CalendarEventType): Color = when (type) {
    CalendarEventType.Payment -> MaterialTheme.colorScheme.error
    CalendarEventType.Exam -> MaterialTheme.colorScheme.primary
    CalendarEventType.Holiday -> MaterialTheme.colorScheme.tertiary
}

/** Square = payment, circle = exam, diamond = holiday. */
@Composable
private fun EventMarker(type: CalendarEventType, color: Color, size: Dp = 12.dp) {
    val background = MaterialTheme.colorScheme.surface
    when (type) {
        CalendarEventType.Payment -> Box(
            Modifier
                .size(size)
                .background(background, RoundedCornerShape(2.dp))
                .border(1.5.dp, color, RoundedCornerShape(2.dp))
        )
        CalendarEventType.Exam -> Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(background)
                .border(1.5.dp, color, CircleShape)
        )
        CalendarEventType.Holiday -> Box(
            Modifier
                .size(size * 0.8f)
                .graphicsLayer { rotationZ = 45f }
                .background(background)
                .border(1.5.dp, color)
        )
    }
}

@Composable
private fun TodayDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BubtSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(56.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .dashedBorder(MaterialTheme.colorScheme.primary, 0.dp, width = 1.dp)
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
        Text(
            text = event.date.shortLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (isPast) muted else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(56.dp)
                .padding(top = BubtSpacing.Lg)
        )
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
            Box(modifier = Modifier.padding(top = 16.dp)) {
                EventMarker(type = event.type, color = if (isPast) muted else markerColor(event.type))
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = BubtSpacing.Sm, bottom = BubtSpacing.Md)
        ) {
            if (isPast) {
                Column(modifier = Modifier.padding(vertical = BubtSpacing.Md)) {
                    Text(text = event.title, style = MaterialTheme.typography.titleSmall, color = muted)
                    Text(text = "${event.note} · passed", style = MaterialTheme.typography.bodySmall, color = muted)
                }
            } else {
                EventCard(event = event, onSeeExamRoutine = onSeeExamRoutine, onOpenFees = onOpenFees)
            }
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
        Column(modifier = Modifier.padding(horizontal = BubtSpacing.CardPadding, vertical = BubtSpacing.Md)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = event.endDate?.let { "${event.note} · ${event.date.shortLabel} – ${it.day}" } ?: event.note,
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
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs + 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 750)
@Composable
private fun CalendarContentPreview() {
    val today = SimpleDate(2026, 10, 14)
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CalendarContent(
                state = CalendarUiState(
                    year = 2026,
                    month = 10,
                    today = today,
                    events = SampleAcademicsRepository.calendarEvents().filter { it.date.month == 10 }
                ),
                onPreviousMonth = {},
                onNextMonth = {},
                onSeeExamRoutine = {},
                onOpenFees = {}
            )
        }
    }
}
