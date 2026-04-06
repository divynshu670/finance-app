package com.example.financecompanion.views.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DateInputField(
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fieldBackground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val fieldBorder = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(fieldBackground, RoundedCornerShape(18.dp))
            .border(1.dp, fieldBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = "Select date",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
