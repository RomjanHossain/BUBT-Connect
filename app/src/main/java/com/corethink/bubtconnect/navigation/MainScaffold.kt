package com.corethink.bubtconnect.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.corethink.bubtconnect.data.model.DashboardHighlight
import com.corethink.bubtconnect.ui.academics.AcademicsScreen
import com.corethink.bubtconnect.ui.academics.AcademicsTab
import com.corethink.bubtconnect.ui.community.CommunityRoute
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                MainTab.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
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
            composable(MainTab.Community.route) { CommunityRoute() }
            composable(MainTab.Profile.route) {
                ProfileRoute(onEditProfile = onEditProfile, onLoggedOut = onLoggedOut, onOpenFees = onOpenFees)
            }
        }
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
