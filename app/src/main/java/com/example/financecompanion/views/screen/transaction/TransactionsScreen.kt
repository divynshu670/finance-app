package com.example.financecompanion.views.screen.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.R
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.repository.TransactionRepositoryImpl
import com.example.financecompanion.viewmodel.TransactionsViewModel
import com.example.financecompanion.views.components.common.ResponsiveScreenContainer
import com.example.financecompanion.views.components.transaction.TransactionCard
import com.example.financecompanion.views.components.transaction.TransactionFilterChips
import com.example.financecompanion.views.components.transaction.TransactionSearchBar

@Composable
fun TransactionsScreen(
    onEditTransaction: (Transaction) -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        TransactionRepositoryImpl(
            DatabaseProvider.getDatabase(context).transactionDao()
        )
    }

    val viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.factory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    val screenHorizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val screenVerticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    ResponsiveScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = screenHorizontalPadding,
                    vertical = screenVerticalPadding
                ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                TransactionSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                TransactionFilterChips(
                    selected = uiState.filter,
                    onSelected = viewModel::onFilterChange
                )
            }

            if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.hasAnyTransactions) {
                            "No transactions found."
                        } else {
                            "No transactions yet. Start by adding your first income or expense."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { txn ->
                        TransactionCard(
                            title = txn.category,
                            subtitle = txn.note,
                            amount = txn.amount,
                            date = txn.date,
                            isIncome = txn.type == "INCOME",
                            icon = TransactionCategory
                                .entries
                                .find { it.label == txn.category }
                                ?.icon
                                ?: TransactionCategory.OTHER_EXPENSE.icon,
                            onEdit = { onEditTransaction(txn) },
                            onDelete = { transactionToDelete = txn }
                        )
                    }
                }
            }
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text("Delete Transaction")
            },
            text = {
                Text("Are you sure you want to delete this transaction?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { transactionToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
