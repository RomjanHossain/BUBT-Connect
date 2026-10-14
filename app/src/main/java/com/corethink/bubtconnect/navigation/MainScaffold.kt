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
    tabNavController: NavHostController = rememberNavController()
) {
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    fun selectTab(tab: MainTab) {
        tabNavController.navigate(tab.route) {
            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
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
                    onCourseClick = { selectTab(MainTab.Academics) },
                    onQuickAction = { action ->
                        when (action) {
                            QuickAction.ScanDocument -> selectTab(MainTab.Scan)
                            QuickAction.ViewRoutine,
                            QuickAction.CheckResult,
                            QuickAction.AcademicCalendar -> selectTab(MainTab.Academics)
                        }
                    },
                    onConnectAnnex = onConnectAnnex
                )
            }
            composable(MainTab.Academics.route) { ComingSoon(MainTab.Academics.label) }
            composable(MainTab.Scan.route) { ComingSoon(MainTab.Scan.label) }
            composable(MainTab.Community.route) { ComingSoon(MainTab.Community.label) }
            composable(MainTab.Profile.route) { ComingSoon(MainTab.Profile.label) }
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
