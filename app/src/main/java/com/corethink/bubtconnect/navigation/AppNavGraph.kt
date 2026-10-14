package com.corethink.bubtconnect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.corethink.bubtconnect.ui.auth.AnnexLoginRoute
import com.corethink.bubtconnect.ui.home.HomeScreen
import com.corethink.bubtconnect.ui.onboarding.ProfileSetupRoute
import com.corethink.bubtconnect.ui.onboarding.WelcomeScreen
import com.corethink.bubtconnect.ui.profile.GuestProfileRoute

object Routes {
    const val WELCOME = "welcome"            // 1b
    const val PROFILE_SETUP = "profile_setup" // 1c
    const val GUEST_PROFILE = "guest_profile" // 1d
    const val ANNEX_LOGIN = "annex_login"     // 1e
    const val HOME = "home"
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
            HomeScreen()
        }
    }
}

/** Home replaces onboarding entirely so Back exits the app instead of returning to it. */
private fun NavHostController.navigateToHome() {
    navigate(Routes.HOME) {
        popUpTo(Routes.WELCOME) { inclusive = true }
        launchSingleTop = true
    }
}
