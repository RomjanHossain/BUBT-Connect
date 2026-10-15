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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.ClassDay
import com.corethink.bubtconnect.data.model.RoutineSlot
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.formatClock
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
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

/** 1h · Class routine. 6-column week grid + timeline. Matches HTML 1h exactly. */
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
    val dayDates = remember {
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayIndex = ClassDay.entries.indexOfFirst { it.calendarDay == todayDow }
            .let { if (it == -1) 0 else it }
        ClassDay.entries.mapIndexed { index, _ ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, index - todayIndex) }
                .get(Calendar.DAY_OF_MONTH)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 6-Column Week Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClassDay.entries.forEachIndexed { index, day ->
                    val selected = day == state.selectedDay
                    Surface(
                        onClick = { onSelectDay(day) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = day.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dayDates[index]}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (state.slots.isEmpty()) {
            item {
                Text(
                    text = "No classes on ${state.selectedDay.label}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        items(state.slots.size) { index ->
            val slot = state.slots[index]
            val isNow = isToday && nowMinute in slot.startMinute until slot.endMinute
            val isPast = isToday && nowMinute >= slot.endMinute
            TimelineRow(
                startMinute = slot.startMinute,
                isNow = isNow,
                isPast = isPast
            ) {
                when (slot) {
                    is RoutineSlot.Class -> ClassCard(
                        slot = slot,
                        isNow = isNow,
                        isPast = isPast,
                        minutesLeft = (slot.endMinute - nowMinute).coerceAtLeast(0),
                        reminderOn = RoutineViewModel.reminderKey(state.selectedDay, slot) in state.reminders,
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
    isPast: Boolean,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Time column (44dp)
        Text(
            text = formatClock(startMinute),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
            color = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 14.dp)
        )

        // Line and dot column (16dp)
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline)
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(if (isNow) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isNow -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.background
                        }
                    )
                    .then(
                        if (!isNow && !isPast) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ClassCard(
    slot: RoutineSlot.Class,
    isNow: Boolean,
    isPast: Boolean,
    minutesLeft: Int,
    reminderOn: Boolean,
    onToggleReminder: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isPast) 0.6f else 1f },
        shape = RoundedCornerShape(16.dp),
        color = if (isNow) MaterialTheme.colorScheme.surface
        else if (isPast) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isNow) 4.dp else 1.dp,
        border = if (!isNow && !isPast) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${slot.course.code} · ${slot.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (reminderOn) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                        contentDescription = "Toggle reminder",
                        tint = if (reminderOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isNow) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "Now",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = "$minutesLeft min left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isPast) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakCard(minutes: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = "Break · $minutes min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineTabPreview() {
    BUBTConnectMaterial3Theme {
        RoutineContent(
            state = RoutineUiState(
                selectedDay = ClassDay.Sun,
                today = ClassDay.Sun,
                slots = SampleAcademicsRepository.routine(ClassDay.Sun)
            ),
            onSelectDay = {},
            onToggleReminder = {}
        )
    }
}