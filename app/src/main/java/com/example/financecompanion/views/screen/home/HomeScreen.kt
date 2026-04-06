package com.example.financecompanion.views.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.R
import com.example.financecompanion.viewmodel.HomeViewModel
import com.example.financecompanion.views.components.common.ResponsiveScreenContainer
import com.example.financecompanion.views.components.home.CategoryCard
import com.example.financecompanion.views.components.home.EmergencyFundCard
import com.example.financecompanion.views.components.home.HomeBalanceCard
import com.example.financecompanion.views.components.home.RecentTransactionsCard
import com.example.financecompanion.views.components.home.WeeklySpendingCard

@Composable
fun HomeScreen(
    onViewAllClick: () -> Unit = {},
    onEmergencyFundClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {

    val context = LocalContext.current

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val screenHorizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val screenVerticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    ResponsiveScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = screenHorizontalPadding,
                vertical = screenVerticalPadding
            ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {

            item {
                HomeBalanceCard(
                    balance = uiState.balance,
                    income = uiState.income,
                    expense = uiState.expense,
                    onSettingsClick = onSettingsClick
                )
            }

            item {
                if (uiState.categoryData.isNotEmpty()) {
                    CategoryCard(
                        categoryData = uiState.categoryData
                    )
                } else {
                    HomeMessageCard(
                        message = "No expense data available to display."
                    )
                }
            }

            if (uiState.hasWeeklyData) {
                item {
                    WeeklySpendingCard(
                        weeklyData = uiState.weeklyData
                    )
                }
            }

            if (uiState.hasEmergencyFund) {
                item {
                    EmergencyFundCard(
                        saved = uiState.emergencySaved,
                        goal = uiState.emergencyGoal,
                        onClick = onEmergencyFundClick
                    )
                }
            }

            item {
                RecentTransactionsCard(
                    transactions = uiState.recentTransactions,
                    onViewAllClick = onViewAllClick
                )
            }
        }
    }
}

@Composable
private fun HomeMessageCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
