package com.example.financecompanion.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionCategory(
    val label: String,
    val icon: ImageVector,
    val type: TransactionType
) {
    FOOD("Food", Icons.Outlined.Fastfood, TransactionType.EXPENSE),
    TRANSPORT("Transport", Icons.Outlined.DirectionsCar, TransactionType.EXPENSE),
    SHOPPING("Shopping", Icons.Outlined.ShoppingBag, TransactionType.EXPENSE),
    BILLS("Bills", Icons.Outlined.ReceiptLong, TransactionType.EXPENSE),
    ENTERTAINMENT("Movie", Icons.Outlined.Movie, TransactionType.EXPENSE),
    HEALTH("Health", Icons.Outlined.LocalHospital, TransactionType.EXPENSE),
    EDUCATION("Education", Icons.Outlined.School, TransactionType.EXPENSE),
    OTHER_EXPENSE("Other", Icons.Outlined.EditNote, TransactionType.EXPENSE),

    SALARY("Salary", Icons.Outlined.Work, TransactionType.INCOME),
    INVESTMENT("Invested", Icons.Outlined.TrendingUp, TransactionType.INCOME),
    OTHER_INCOME("Other", Icons.Outlined.EditNote, TransactionType.INCOME);

    companion object {
        fun byType(type: TransactionType): List<TransactionCategory> {
            return entries.filter { it.type == type }
        }
    }
}