package com.corethink.bubtconnect.ui.academics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/** 03 · Academics hub — one screen, scrollable top tabs (1g–1l). */
@Composable
fun AcademicsScreen(
    selectedTab: AcademicsTab,
    onSelectTab: (AcademicsTab) -> Unit,
    onConnectAnnex: () -> Unit,
    onOpenFees: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Academics",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = BubtSpacing.Screen, end = BubtSpacing.Screen, top = BubtSpacing.Lg)
        )
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = BubtSpacing.Screen,
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
        when (selectedTab) {
            AcademicsTab.Routine -> RoutineTab()
            AcademicsTab.Exams -> ExamsTab()
            AcademicsTab.Results -> ResultsTab(onConnectAnnex = onConnectAnnex)
            AcademicsTab.Attendance -> AttendanceTab(onConnectAnnex = onConnectAnnex)
            AcademicsTab.Calendar -> CalendarTab(
                onSeeExamRoutine = { onSelectTab(AcademicsTab.Exams) },
                onOpenFees = onOpenFees
            )
            AcademicsTab.Courses -> CoursesTab()
        }
    }
}
