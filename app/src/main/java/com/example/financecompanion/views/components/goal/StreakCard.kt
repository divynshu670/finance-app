package com.example.financecompanion.views.components.goal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StreakCard(
    openedDays: List<Long>
) {
    val streak = calculateStreak(openedDays)
    val animatedProgress = animateFloatAsState(
        targetValue = streak / 7f,
        animationSpec = tween(durationMillis = 600),
        label = "streakProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFF7F50), Color(0xFFFF6A5C))
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = Color.White
            )

            Column {
                Text("7 Day Streak", color = Color.White)
                Text(
                    text = when {
                        streak == 0 -> "Add a transaction today to start your streak."
                        streak == 7 -> "Perfect streak. Keep the momentum."
                        else -> "You're on a $streak day streak."
                    },
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(7) { index ->
                val filled = animatedProgress.value * 7f > index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else Color.White.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
        }
    }
}

fun calculateStreak(days: List<Long>): Int {
    if (days.isEmpty()) return 0

    val uniqueDays = days.map(::getStartOfDay).distinct().sortedDescending()
    val today = getStartOfDay(System.currentTimeMillis())
    if (uniqueDays.firstOrNull() != today) return 0

    var streak = 0
    var expectedDay = today

    for (day in uniqueDays) {
        if (day == expectedDay) {
            streak++
            expectedDay -= DAY_IN_MILLIS
            continue
        }

        if (day < expectedDay) {
            break
        }
    }

    return streak.coerceAtMost(7)
}

private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
