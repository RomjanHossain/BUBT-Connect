package com.corethink.bubtconnect.ui.academics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corethink.bubtconnect.ui.attendance.AttendanceTab
import com.corethink.bubtconnect.ui.calendar.CalendarTab
import com.corethink.bubtconnect.ui.courses.CoursesTab
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.exams.ExamsTab
import com.corethink.bubtconnect.ui.results.ResultsTab
import com.corethink.bubtconnect.ui.routine.RoutineTab

enum class AcademicsTab(val label: String) {
    Routine("Routine"),
    Exams("Exams"),
    Results("Results"),
    Attendance("Attendance"),
    Calendar("Calendar"),
    Courses("Courses")
}

/** 03 · Academics hub — matches HTML design 1h–1l. */
@Composable
fun AcademicsScreen(
    selectedTab: AcademicsTab,
    onSelectTab: (AcademicsTab) -> Unit,
    onOpenFees: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Academics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 20.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AcademicsTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onSelectTab(tab) },
                    text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Cross-slide in the direction of the pick so tab hops feel physical.
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val enter = slideInHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) { d -> if (forward) d / 5 else -d / 5 } + fadeIn(tween(160))
                val exit = fadeOut(tween(100)) + slideOutHorizontally(
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) { d -> if (forward) -d / 8 else d / 8 }
                enter togetherWith exit
            },
            label = "academicsTabContent"
        ) { tab ->
            when (tab) {
                AcademicsTab.Routine -> RoutineTab()
                AcademicsTab.Exams -> ExamsTab()
                AcademicsTab.Results -> ResultsTab()
                AcademicsTab.Attendance -> AttendanceTab()
                AcademicsTab.Calendar -> CalendarTab(
                    onSeeExamRoutine = { onSelectTab(AcademicsTab.Exams) },
                    onOpenFees = onOpenFees
                )
                AcademicsTab.Courses -> CoursesTab()
            }
        }
    }
}