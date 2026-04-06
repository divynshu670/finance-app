package com.example.financecompanion.views.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financecompanion.R
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.views.components.common.PrimaryButton

@Composable
fun HomeBottomSheetContent(
    amount: String,
    error: String?,
    transactionType: TransactionType,
    categories: List<TransactionCategory>,
    selectedCategory: TransactionCategory?,
    date: String,
    note: String,
    primaryActionText: String,
    showReceiptScanAction: Boolean,
    onAmountChange: (String) -> Unit,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onCategorySelected: (TransactionCategory) -> Unit,
    onDateClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    onScanReceiptClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetHorizontalPadding = dimensionResource(R.dimen.sheet_horizontal_padding)
    val sheetVerticalPadding = dimensionResource(R.dimen.sheet_vertical_padding)
    val sheetMaxContentWidth = dimensionResource(R.dimen.sheet_max_content_width)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = sheetHorizontalPadding,
                vertical = sheetVerticalPadding
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = sheetMaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing + 2.dp)
        ) {
            SheetSectionTitle("Amount")

            AmountInputField(
                amount = amount,
                isError = error != null,
                onAmountChange = onAmountChange
            )

            if (!error.isNullOrEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SheetSectionTitle("Type")
            TypeToggleRow(
                selectedType = transactionType,
                onTypeSelected = onTransactionTypeChange
            )

            if (showReceiptScanAction) {
                ReceiptScanButton(
                    onClick = onScanReceiptClick
                )
            }

            SheetSectionTitle("Category")
            CategoryGrid(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )

            SheetSectionTitle("Date")
            DateInputField(
                dateText = date,
                onClick = onDateClick
            )

            SheetSectionTitle("Note (optional)")
            NoteInputField(
                note = note,
                onNoteChange = onNoteChange
            )

            PrimaryButton(
                text = primaryActionText,
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                height = 60.dp,
                cornerRadius = 20.dp,
                textSize = 20.sp
            )
        }
    }
}
