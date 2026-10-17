package com.corethink.bubtconnect.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.corethink.bubtconnect.data.model.DashboardHighlight
import com.corethink.bubtconnect.ui.academics.AcademicsScreen
import com.corethink.bubtconnect.ui.academics.AcademicsTab
import com.corethink.bubtconnect.ui.community.CommunityRoute
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.home.HomeRoute
import com.corethink.bubtconnect.ui.home.QuickAction
import com.corethink.bubtconnect.ui.profile.ProfileRoute

enum class MainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("tab_home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Academics("tab_academics", "Academics", Icons.Filled.School, Icons.Outlined.School),
    Scan("tab_scan", "Scan", Icons.Filled.DocumentScanner, Icons.Outlined.DocumentScanner),
    Community("tab_community", "Community", Icons.Filled.Groups, Icons.Outlined.Groups),
    Profile("tab_profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

/** Signed-in shell: bottom navigation around the five top-level tabs. */
@Composable
fun MainScaffold(
    onNotifications: () -> Unit,
    onConnectAnnex: () -> Unit,
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    onOpenFees: () -> Unit,
    onOpenDefaultCover: () -> Unit,
    onOpenScanner: () -> Unit,
    tabNavController: NavHostController = rememberNavController()
) {
    var academicsTab by rememberSaveable { mutableStateOf(AcademicsTab.Routine) }
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    fun selectTab(tab: MainTab) {
        // Scan is a full-screen wizard (no bottom nav), not a tab destination.
        if (tab == MainTab.Scan) return onOpenScanner()
        tabNavController.navigate(tab.route) {
            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openAcademics(tab: AcademicsTab) {
        academicsTab = tab
        selectTab(MainTab.Academics)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DockedScanNavBar(
                currentTab = MainTab.entries.firstOrNull { tab ->
                    currentDestination?.hierarchy?.any { it.route == tab.route } == true
                } ?: MainTab.Home,
                onTabSelected = { selectTab(it) },
                onScan = { selectTab(MainTab.Scan) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTab.Home.route) {
                HomeRoute(
                    onNotifications = onNotifications,
                    onCourseClick = { openAcademics(AcademicsTab.Courses) },
                    onSeeAll = { openAcademics(AcademicsTab.Courses) },
                    onQuickAction = { action ->
                        when (action) {
                            QuickAction.ScanDocument -> selectTab(MainTab.Scan)
                            QuickAction.ViewRoutine -> openAcademics(AcademicsTab.Routine)
                            QuickAction.CheckResult -> openAcademics(AcademicsTab.Results)
                            QuickAction.AcademicCalendar -> openAcademics(AcademicsTab.Calendar)
                        }
                    },
                    onHighlightClick = { highlight ->
                        when (highlight) {
                            is DashboardHighlight.NextClass -> openAcademics(AcademicsTab.Routine)
                            is DashboardHighlight.ExamAlert -> openAcademics(AcademicsTab.Exams)
                            is DashboardHighlight.PaymentDue -> onOpenFees()
                        }
                    },
                    onConnectAnnex = onConnectAnnex
                )
            }
            composable(MainTab.Academics.route) {
                AcademicsScreen(
                    selectedTab = academicsTab,
                    onSelectTab = { academicsTab = it },
                    onOpenFees = onOpenFees
                )
            }
            composable(MainTab.Community.route) { CommunityRoute(onShareNotes = onOpenScanner) }
            composable(MainTab.Profile.route) {
                ProfileRoute(
                    onEditProfile = onEditProfile,
                    onLoggedOut = onLoggedOut,
                    onOpenFees = onOpenFees,
                    onOpenDefaultCover = onOpenDefaultCover
                )
            }
        }
    }
}

private val NavIconSize = 20.dp
private val NavLabelStyle @Composable get() = MaterialTheme.typography.labelSmall
private val ScanFabSize = 56.dp

/**
 * Bottom bar from 1g but with a docked center Scan FAB: Home · Academics on the left,
 * Community · Profile on the right, and an elevated Scan button bridging the gap.
 */
@Composable
private fun DockedScanNavBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onScan: () -> Unit
) {
    // Bar + safe-area strip cast one shared shadow, so the elevation fades above the
    // bar and around the sides — nothing paints over the inset strip below it.
    Column(
        modifier = Modifier
            .shadow(8.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = BubtSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(MainTab.Home, currentTab, onTabSelected, Modifier.weight(1f))
            NavTabItem(MainTab.Academics, currentTab, onTabSelected, Modifier.weight(1f))
            ScanDock(onScan)
            NavTabItem(MainTab.Community, currentTab, onTabSelected, Modifier.weight(1f))
            NavTabItem(MainTab.Profile, currentTab, onTabSelected, Modifier.weight(1f))
        }
        // SafeArea: the system navigation inset, filled with the bar's surface color.
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun NavTabItem(
    tab: MainTab,
    current: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = tab == current
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) { onSelect(tab) }
            .padding(vertical = BubtSpacing.Sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                    shape = BubtShapes.Chip
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(NavIconSize)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = NavLabelStyle,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ScanDock(onScan: () -> Unit) {
    Box(
        modifier = Modifier.width(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onScan,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(ScanFabSize)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ),
            content = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = "Scan",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        )
    }
}

/** Stand-in for screens whose wireframes haven't been implemented yet. */
@Composable
fun ComingSoon(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
