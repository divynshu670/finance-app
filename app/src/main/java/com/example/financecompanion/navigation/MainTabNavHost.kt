package com.example.financecompanion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.views.screen.home.HomeScreen
import com.example.financecompanion.views.screen.insight.InsightsScreen
import com.example.financecompanion.views.screen.goal.GoalsScreen
import com.example.financecompanion.views.screen.settings.SettingsScreen
import com.example.financecompanion.views.screen.transaction.TransactionsScreen

@Composable
fun MainTabNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onEditTransaction: (Transaction) -> Unit // ✅ ADDED
) {
    NavHost(
        navController = navController,
        startDestination = BottomTabRoutes.HOME,
        modifier = modifier
    ) {

        composable(BottomTabRoutes.HOME) {
            HomeScreen(
                onViewAllClick = {
                    navController.navigate(BottomTabRoutes.TRANSACTIONS) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(AppRoutes.SETTINGS)
                },
                onEmergencyFundClick = {
                    navController.navigate(BottomTabRoutes.GOALS) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(BottomTabRoutes.TRANSACTIONS) {
            TransactionsScreen(
                onEditTransaction = onEditTransaction // ✅ PASS DOWN
            )
        }

        composable(BottomTabRoutes.GOALS) {
            GoalsScreen()
        }

        composable(BottomTabRoutes.INSIGHTS) {
            InsightsScreen()
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
