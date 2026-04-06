package com.example.financecompanion.views.screen.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.R
import com.example.financecompanion.viewmodel.GoalsViewModel
import com.example.financecompanion.views.components.common.ResponsiveScreenContainer
import com.example.financecompanion.views.components.goal.*

@Composable
fun GoalsScreen() {

    val context = LocalContext.current
    val viewModel: GoalsViewModel = viewModel(
        factory = GoalsViewModel.factory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val daysRemaining = remember(uiState.targetDateMillis) {
        calculateDaysRemaining(uiState.targetDateMillis)
    }
    val screenHorizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val screenVerticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    var showDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ResponsiveScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = screenHorizontalPadding,
                vertical = screenVerticalPadding
            ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {

            item {
                EmergencyFundCard(
                    saved = uiState.saved,
                    target = uiState.target,
                    daysRemaining = daysRemaining,
                    onEditClick = { showDialog = true },
                    onDeleteClick = if (uiState.target > 0.0) {
                        { showDeleteDialog = true }
                    } else {
                        null
                    }
                )
            }

            item {
                StreakCard(openedDays = uiState.streakActivityDays)
            }

            item {
                NoSpendChallengeCard(
                    challenges = uiState.challenges.map {
                        Challenge(
                            id = it.id,
                            name = it.name,
                            category = it.category,
                            target = it.target,
                            dateMillis = it.dateMillis,
                            completed = it.completed,
                            progress = it.progress,
                            spent = it.spent
                        )
                    },
                    onAddClick = { showChallengeDialog = true },
                    onDeleteClick = viewModel::deleteChallenge
                )
            }
        }
    }

    if (showDialog) {
        EmergencyFundDialog(
            saved = uiState.saved,
            target = uiState.target,
            targetDateMillis = uiState.targetDateMillis,
            onDismiss = { showDialog = false },
            onCreateFund = { amount, targetDate ->
                viewModel.createFund(amount, targetDate)
                showDialog = false
            },
            onAddAmount = { amount, targetDate ->
                viewModel.addAmount(
                    amount = amount,
                    current = uiState.saved,
                    target = uiState.target,
                    targetDateMillis = targetDate
                )
                showDialog = false
            }
        )
    }

    if (showChallengeDialog) {
        AddChallengeDialog(
            onDismiss = { showChallengeDialog = false },
            onCreate = {
                val created = viewModel.addChallenge(
                    com.example.financecompanion.data.local.entity.ChallengeEntity(
                        name = it.name,
                        category = it.category,
                        target = it.target,
                        dateMillis = it.dateMillis,
                        completed = false
                    )
                )

                if (created) {
                    showChallengeDialog = false
                    null
                } else {
                    "A challenge for this category and date already exists."
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Emergency Fund") },
            text = {
                Text("Are you sure you want to delete your emergency fund?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFund()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
