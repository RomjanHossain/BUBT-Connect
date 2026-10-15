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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.DashboardHighlight
import com.corethink.bubtconnect.data.repository.SampleDashboardRepository
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtChip
import com.corethink.bubtconnect.ui.design.BubtError
import com.corethink.bubtconnect.ui.design.BubtErrorContainerLight
import com.corethink.bubtconnect.ui.design.BubtOnErrorContainerLight
import com.corethink.bubtconnect.ui.design.BubtOnSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtOnWarningContainerLight
import com.corethink.bubtconnect.ui.design.BubtPagerIndicator
import com.corethink.bubtconnect.ui.design.BubtPrimaryContainerLight
import com.corethink.bubtconnect.ui.design.BubtPrimaryLight
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtWarningContainerLight
import java.util.Calendar
import java.util.Locale

enum class QuickAction(
    val label: String,
    val icon: ImageVector,
    val needsAnnex: Boolean = false,
    val containerColor: Color,
    val contentColor: Color
) {
    ScanDocument("Scan Document", Icons.Outlined.DocumentScanner, containerColor = BubtPrimaryContainerLight, contentColor = BubtPrimaryLight),
    ViewRoutine("View Routine", Icons.Outlined.ViewTimeline, containerColor = BubtSuccessContainerLight, contentColor = BubtOnSuccessContainerLight),
    CheckResult("Check Result", Icons.Outlined.School, needsAnnex = true, containerColor = BubtWarningContainerLight, contentColor = BubtOnWarningContainerLight),
    AcademicCalendar("Academic Calendar", Icons.Outlined.CalendarMonth, containerColor = BubtErrorContainerLight, contentColor = BubtOnErrorContainerLight)
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
        onQuickAction = { action ->
            if (action.needsAnnex && state.isGuest) onConnectAnnex() else onQuickAction(action)
        }
    )
}

/** 1g · Dashboard. Matches HTML 1g exactly. */
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
        Spacer(modifier = Modifier.height(14.dp))

        SectionHeader(title = "Present Courses", actionLabel = "See all", onAction = onSeeAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = BubtSpacing.Screen),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val colors = listOf(
                Color(0xFF2563EB),
                Color(0xFF10B981),
                Color(0xFFF59E0B),
                Color(0xFFA855F7),
                Color(0xFFEF4444)
            )
            items(state.courses.size) { index ->
                val course = state.courses[index]
                BubtChip(
                    text = course.code,
                    onClick = { onCourseClick(course) },
                    leadingDot = colors[index % colors.size]
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (state.highlights.isNotEmpty()) {
            HighlightsCarousel(highlights = state.highlights, onClick = onHighlightClick)
            Spacer(modifier = Modifier.height(14.dp))
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
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            Surface(
                onClick = onNotifications,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BubtError)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BubtSpacing.Screen, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(BubtShapes.Chip)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
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
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            HighlightCard(highlights[page], onClick = { onClick(highlights[page]) })
        }
        Spacer(modifier = Modifier.height(10.dp))
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
                container = colors.surface,
                content = colors.onSurface,
                eyebrow = "NEXT CLASS",
                eyebrowTrailing = "in $minutesUntil min",
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
            container = colors.surface,
            content = colors.onSurface,
            eyebrow = "EXAM ALERT",
            eyebrowTrailing = highlight.examName,
            title = "${highlight.course.code} · ${highlight.course.title}",
            lines = listOf(
                Icons.AutoMirrored.Outlined.EventNote to highlight.date,
                Icons.Outlined.Place to highlight.room
            )
        )
        is DashboardHighlight.PaymentDue -> HighlightSurface(
            onClick = onClick,
            container = colors.surface,
            content = colors.onSurface,
            eyebrow = "PAYMENT DUE",
            eyebrowTrailing = highlight.dueDate,
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
    eyebrowTrailing: String,
    title: String,
    lines: List<Pair<ImageVector, String>>,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = content,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = eyebrow,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = eyebrowTrailing,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                lines.forEach { (icon, text) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Timeline Progress bar row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Active reminder",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------- quick actions (1g horizontal rows)
@Composable
private fun QuickActionsGrid(
    isGuest: Boolean,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { action ->
                    QuickActionRowTile(
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

/** Tonal Row Tile matching `1g` Quick Actions in Student App UI.dc.html exactly (`tonal r`). */
@Composable
private fun QuickActionRowTile(
    action: QuickAction,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(action.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = action.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (locked) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Requires Annex",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
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

private fun formatClock(minuteOfDay: Int): Pair<String, String> {
    val hour24 = minuteOfDay / 60
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val period = if (hour24 < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d", hour12, minuteOfDay % 60) to period
}

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