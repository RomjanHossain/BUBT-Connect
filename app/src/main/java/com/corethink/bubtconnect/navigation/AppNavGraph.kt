package com.corethink.bubtconnect.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.corethink.bubtconnect.ui.auth.AnnexLoginRoute
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.onboarding.OnboardingTopBar
import com.corethink.bubtconnect.ui.onboarding.ProfileSetupRoute
import com.corethink.bubtconnect.ui.onboarding.WelcomeScreen
import com.corethink.bubtconnect.ui.profile.GuestProfileRoute

object Routes {
    const val WELCOME = "welcome"            // 1b
    const val PROFILE_SETUP = "profile_setup" // 1c
    const val GUEST_PROFILE = "guest_profile" // 1d
    const val ANNEX_LOGIN = "annex_login"     // 1e
    const val HOME = "home"                   // 1f, inside MainScaffold
    const val NOTIFICATIONS = "notifications"
    const val EDIT_PROFILE = "edit_profile"   // 1c in edit mode, from Profile (1s)
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Routes.PROFILE_SETUP) },
                onAnnexLogin = { navController.navigate(Routes.ANNEX_LOGIN) }
            )
        }
        composable(Routes.PROFILE_SETUP) {
            ProfileSetupRoute(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Routes.GUEST_PROFILE) },
                onSkip = { navController.navigateToHome() }
            )
        }
        composable(Routes.GUEST_PROFILE) {
            GuestProfileRoute(
                onEditProfile = { navController.popBackStack() },
                onConnected = { navController.navigateToHome() },
                onMaybeLater = { navController.navigateToHome() }
            )
        }
        composable(Routes.ANNEX_LOGIN) {
            AnnexLoginRoute(
                onBack = { navController.popBackStack() },
                onLoggedIn = { navController.navigateToHome() }
            )
        }
        composable(Routes.HOME) {
            MainScaffold(
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onConnectAnnex = { navController.navigate(Routes.ANNEX_LOGIN) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onLoggedOut = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.EDIT_PROFILE) {
            ProfileSetupRoute(
                onBack = { navController.popBackStack() },
                onContinue = { navController.popBackStack() },
                onSkip = { navController.popBackStack() },
                isEditing = true
            )
        }
        composable(Routes.NOTIFICATIONS) {
            Column(modifier = Modifier.statusBarsPadding().padding(horizontal = BubtSpacing.Screen)) {
                OnboardingTopBar(title = "Notifications", onBack = { navController.popBackStack() })
                ComingSoon(title = "No notifications yet")
            }
        }
    }
}

/** Home replaces everything behind it so Back exits the app instead of returning to onboarding. */
private fun NavHostController.navigateToHome() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
