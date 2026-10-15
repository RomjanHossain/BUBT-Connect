package com.corethink.bubtconnect.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ViewTimeline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.DashboardHighlight
import com.corethink.bubtconnect.data.repository.SampleDashboardRepository
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtChip
import com.corethink.bubtconnect.ui.design.BubtPagerIndicator
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import java.util.Calendar
import java.util.Locale

enum class QuickAction(val label: String, val icon: ImageVector, val needsAnnex: Boolean = false) {
    ScanDocument("Scan Document", Icons.Outlined.DocumentScanner),
    ViewRoutine("View Routine", Icons.Outlined.ViewTimeline),
    CheckResult("Check Result", Icons.Outlined.School, needsAnnex = true),
    AcademicCalendar("Academic Calendar", Icons.Outlined.CalendarMonth)
}

@Composable
fun HomeRoute(
    onNotifications: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onSeeAll: () -> Unit = {},
    onQuickAction: (QuickAction) -> Unit,
    onHighlightClick: (DashboardHighlight) -> Unit,
    onConnectAnnex: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onNotifications = onNotifications,
        onCourseClick = onCourseClick,
        onSeeAll = onSeeAll,
        onHighlightClick = onHighlightClick,
        // Guest mode: locked actions open Annex login (1e) instead.
        onQuickAction = { action ->
            if (action.needsAnnex && state.isGuest) onConnectAnnex() else onQuickAction(action)
        }
    )
}

/** 1g · Dashboard. Bell opens notifications. */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onNotifications: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: () -> Unit = {},
    onHighlightClick: (DashboardHighlight) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = BubtSpacing.Lg)
    ) {
        HomeHeader(
            bengaliLine = state.bengaliLine,
            greeting = state.greeting,
            name = state.displayName,
            onNotifications = onNotifications,
            modifier = Modifier.padding(horizontal = BubtSpacing.Screen)
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))

        SectionHeader(title = "Present Courses", actionLabel = "See all", onAction = onSeeAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = BubtSpacing.Screen),
            horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
        ) {
            items(state.courses, key = { it.code }) { course ->
                BubtChip(text = course.code, onClick = { onCourseClick(course) })
            }
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))

        if (state.highlights.isNotEmpty()) {
            HighlightsCarousel(highlights = state.highlights, onClick = onHighlightClick)
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))
        }

        SectionHeader(title = "Quick actions")
        QuickActionsGrid(
            isGuest = state.isGuest,
            onQuickAction = onQuickAction,
            modifier = Modifier.padding(horizontal = BubtSpacing.Screen)
        )
    }
}

@Composable
private fun HomeHeader(
    bengaliLine: String,
    greeting: String,
    name: String,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            if (bengaliLine.isNotBlank()) {
                Text(
                    text = bengaliLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${greeting.removeSuffix(",")} $name",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            onClick = onNotifications,
            shape = BubtShapes.Field,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(BubtShapes.Chip)
                    .clickable(onClick = onAction)
                    .padding(horizontal = BubtSpacing.Xs, vertical = BubtSpacing.Xs)
            )
        }
    }
}

// ------------------------------------------------------------- highlights
@Composable
private fun HighlightsCarousel(highlights: List<DashboardHighlight>, onClick: (DashboardHighlight) -> Unit) {
    val pagerState = rememberPagerState { highlights.size }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = BubtSpacing.Screen),
            pageSpacing = BubtSpacing.Md,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            HighlightCard(highlights[page], onClick = { onClick(highlights[page]) })
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        BubtPagerIndicator(count = highlights.size, selected = pagerState.currentPage)
    }
}

@Composable
private fun HighlightCard(highlight: DashboardHighlight, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    when (highlight) {
        is DashboardHighlight.NextClass -> {
            val minutesUntil = remember(highlight) { minutesUntil(highlight.startMinuteOfDay) }
            val subtitle = listOfNotNull(
                highlight.course.code,
                highlight.section,
                highlight.instructor
            ).joinToString(" · ")
            HighlightSurface(
                onClick = onClick,
                container = colors.primaryContainer,
                content = colors.onPrimaryContainer,
                eyebrow = "Next class · in ${formatDuration(minutesUntil)}",
                title = highlight.course.title,
                subtitle = subtitle,
                trailingIcon = Icons.Outlined.NotificationsActive,
                lines = listOf(
                    Icons.Outlined.AccessTime to formatTimeRange(highlight.startMinuteOfDay, highlight.endMinuteOfDay),
                    Icons.Outlined.Place to highlight.room
                )
            )
        }
        is DashboardHighlight.ExamAlert -> HighlightSurface(
            onClick = onClick,
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
            eyebrow = "Exam alert · ${highlight.examName}",
            title = "${highlight.course.code} · ${highlight.course.title}",
            lines = listOf(
                Icons.AutoMirrored.Outlined.EventNote to highlight.date,
                Icons.Outlined.Place to highlight.room
            )
        )
        is DashboardHighlight.PaymentDue -> HighlightSurface(
            onClick = onClick,
            container = colors.errorContainer,
            content = colors.onErrorContainer,
            eyebrow = "Payment due · ${highlight.dueDate}",
            title = highlight.title,
            lines = listOf(
                Icons.Outlined.Payments to "৳ ${String.format(Locale.US, "%,d", highlight.amountTaka)}"
            )
        )
    }
}

@Composable
private fun HighlightSurface(
    onClick: () -> Unit,
    container: Color,
    content: Color,
    eyebrow: String,
    title: String,
    lines: List<Pair<ImageVector, String>>,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        shape = BubtShapes.Card,
        color = container,
        contentColor = content
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
                if (trailingIcon != null) {
                    Icon(imageVector = trailingIcon, contentDescription = "Reminder on", modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Xs))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            lines.forEach { (icon, text) ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ------------------------------------------------------------- quick actions
@Composable
private fun QuickActionsGrid(
    isGuest: Boolean,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
        QuickAction.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
                row.forEach { action ->
                    QuickActionTile(
                        action = action,
                        locked = action.needsAnnex && isGuest,
                        onClick = { onQuickAction(action) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickAction,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(BubtShapes.Field)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (locked) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Requires Annex login",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Lg))
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------- formatting
private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun minutesUntil(startMinuteOfDay: Int): Int {
    val now = Calendar.getInstance()
    val nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    return (startMinuteOfDay - nowMinute + 24 * 60) % (24 * 60)
}

private fun formatDuration(minutes: Int): String =
    if (minutes < 60) "$minutes min" else "${minutes / 60} h ${minutes % 60} min"

private fun formatClock(minuteOfDay: Int): Pair<String, String> {
    val hour24 = minuteOfDay / 60
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val period = if (hour24 < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d", hour12, minuteOfDay % 60) to period
}

/** "10:30 – 11:45 AM", or "11:30 AM – 12:45 PM" when the period changes. */
private fun formatTimeRange(start: Int, end: Int): String {
    val (startTime, startPeriod) = formatClock(start)
    val (endTime, endPeriod) = formatClock(end)
    return if (startPeriod == endPeriod) "$startTime – $endTime $endPeriod"
    else "$startTime $startPeriod – $endTime $endPeriod"
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                state = HomeUiState(
                    greeting = "Good morning,",
                    displayName = "Rafi Ahmed",
                    isGuest = true,
                    courses = SampleDashboardRepository.presentCourses(),
                    highlights = SampleDashboardRepository.highlights()
                ),
                onNotifications = {},
                onCourseClick = {},
                onQuickAction = {}
            )
        }
    }
}
