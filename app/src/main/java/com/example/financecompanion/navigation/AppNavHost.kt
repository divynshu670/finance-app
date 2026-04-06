package com.example.financecompanion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financecompanion.views.screen.main.MainScreen
import com.example.financecompanion.views.screen.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    startDestination: String,
    onFinishOnboarding: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.ONBOARDING) {
            OnboardingScreen(
                onGetStartedClick = {
                    onFinishOnboarding()
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.MAIN) {
            MainScreen()
        }
    }
}