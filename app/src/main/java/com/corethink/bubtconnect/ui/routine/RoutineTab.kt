package com.corethink.bubtconnect.ui.routine

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.ClassDay
import com.corethink.bubtconnect.data.model.RoutineSlot
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.components.formatClock
import com.corethink.bubtconnect.ui.components.periodOf
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import java.util.Calendar

@Composable
fun RoutineTab(viewModel: RoutineViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RoutineContent(
        state = state,
        onSelectDay = viewModel::selectDay,
        onToggleReminder = viewModel::toggleReminder
    )
}

/** 1g · Class routine. Bell = per-class reminder (filled = on); lead time comes from Profile. */
@Composable
fun RoutineContent(
    state: RoutineUiState,
    onSelectDay: (ClassDay) -> Unit,
    onToggleReminder: (RoutineSlot.Class) -> Unit
) {
    val nowMinute = remember(state.selectedDay) {
        Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
    }
    val isToday = state.selectedDay == state.today

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = BubtSpacing.Xl)
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
                horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
            ) {
                items(ClassDay.entries) { day ->
                    FilterPill(
                        text = day.label,
                        selected = day == state.selectedDay,
                        onClick = { onSelectDay(day) }
                    )
                }
            }
        }
        if (state.slots.isEmpty()) {
            item {
                Text(
                    text = "No classes on ${state.selectedDay.label}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(BubtSpacing.Screen)
                )
            }
        }
        items(state.slots.size) { index ->
            val slot = state.slots[index]
            val isNow = isToday && nowMinute in slot.startMinute until slot.endMinute
            TimelineRow(
                startMinute = slot.startMinute,
                isNow = isNow,
                isFirst = index == 0,
                isLast = index == state.slots.lastIndex
            ) {
                when (slot) {
                    is RoutineSlot.Class -> ClassCard(
                        slot = slot,
                        isNow = isNow,
                        reminderOn = RoutineViewModel.reminderKey(state.selectedDay, slot) in state.reminders,
                        leadMinutes = state.reminderLeadMinutes,
                        onToggleReminder = { onToggleReminder(slot) }
                    )
                    is RoutineSlot.Break -> BreakCard(minutes = slot.endMinute - slot.startMinute)
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    startMinute: Int,
    isNow: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = BubtSpacing.Screen)
    ) {
        Column(modifier = Modifier.width(48.dp).padding(top = BubtSpacing.Lg)) {
            Text(
                text = formatClock(startMinute),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = periodOf(startMinute),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // The rail runs through every row; trimmed above the first marker and below the last.
            if (!(isFirst && isLast)) {
                Box(
                    modifier = Modifier
                        .padding(top = if (isFirst) 22.dp else 0.dp)
                        .width(1.5.dp)
                        .then(if (isLast) Modifier.height(22.dp) else Modifier.fillMaxHeight())
                        .background(lineColor)
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = BubtSpacing.Sm, bottom = BubtSpacing.Md)
        ) {
            content()
        }
    }
}

@Composable
private fun ClassCard(
    slot: RoutineSlot.Class,
    isNow: Boolean,
    reminderOn: Boolean,
    leadMinutes: Int,
    onToggleReminder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = if (isNow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (isNow) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = if (isNow) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BubtSpacing.CardPadding, vertical = BubtSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = slot.course.code, style = MaterialTheme.typography.titleMedium)
                    if (isNow) {
                        Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                        Surface(
                            shape = BubtShapes.Chip,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = BubtSpacing.Sm, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${slot.course.title} · ${slot.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isNow) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ReminderToggle(on = reminderOn, leadMinutes = leadMinutes, onClick = onToggleReminder)
        }
    }
}

@Composable
private fun ReminderToggle(on: Boolean, leadMinutes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = BubtShapes.Skeleton,
        color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (on) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (on) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                contentDescription = if (on) "Reminder on, $leadMinutes min before" else "Set reminder",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BreakCard(minutes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 16.dp)
            .padding(horizontal = BubtSpacing.CardPadding, vertical = BubtSpacing.Md)
    ) {
        Text(
            text = "Break · $minutes min",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun RoutineContentPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RoutineContent(
                state = RoutineUiState(
                    selectedDay = ClassDay.Mon,
                    slots = SampleAcademicsRepository.routine(ClassDay.Mon),
                    reminders = setOf("class:Mon:CSE 231:630")
                ),
                onSelectDay = {},
                onToggleReminder = {}
            )
        }
    }
}
