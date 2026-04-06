package com.example.financecompanion.views.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.views.components.transaction.TransactionCard

@Composable
fun RecentTransactionsCard(
    transactions: List<Transaction>,
    onViewAllClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent Transactions",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "View All",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions yet. Start by adding your first income or expense.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            transactions.take(3).forEach { txn ->
                TransactionCard(
                    title = txn.category,
                    subtitle = txn.note,
                    amount = txn.amount,
                    date = txn.date,
                    isIncome = txn.type == "INCOME",
                    icon = Icons.Filled.AccountBalance,
                    onEdit = {},
                    onDelete = {},
                    showActions = false
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
