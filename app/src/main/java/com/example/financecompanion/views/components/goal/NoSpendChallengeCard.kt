package com.example.financecompanion.views.components.goal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financecompanion.utils.formatCurrencyDisplay
import com.example.financecompanion.utils.formatMillisToDate

data class Challenge(
    val id: Int = 0,
    val name: String,
    val category: String,
    val target: Double,
    val dateMillis: Long,
    val completed: Boolean,
    val progress: Float = 0f,
    val spent: Double = 0.0
)

@Composable
fun NoSpendChallengeCard(
    challenges: List<Challenge>,
    onAddClick: () -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text("No-Spend Challenge", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        if (challenges.isEmpty()) {
            Text(
                text = "Create a challenge to stay intentional with your weekly spending.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            challenges.forEach { challenge ->
                ChallengeItem(
                    challenge = challenge,
                    onDeleteClick = { onDeleteClick(challenge.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddClick,
            enabled = challenges.size < 30,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Create New Challenge")
        }
    }
}

@Composable
private fun ChallengeItem(
    challenge: Challenge,
    onDeleteClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = challenge.progress.coerceIn(0f, 1f),
        label = "challengeProgress"
    )
    val status = if (challenge.completed) {
        "Completed"
    } else {
        "In Progress"
    }
    val statusColor = if (challenge.completed) {
        Color(0xFF08B57A)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${challenge.category} • Due ${formatMillisToDate(challenge.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete challenge",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onDeleteClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spent ${formatCurrencyDisplay(challenge.spent)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Limit ${formatCurrencyDisplay(challenge.target)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
