package com.example.financecompanion.views.screen.insight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.R
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.utils.formatCurrencyDisplay
import com.example.financecompanion.viewmodel.CategoryInsightUiState
import com.example.financecompanion.viewmodel.InsightsViewModel
import com.example.financecompanion.viewmodel.MonthlyTrendUiState
import com.example.financecompanion.viewmodel.TopCategoryUiState
import com.example.financecompanion.views.components.common.ResponsiveScreenContainer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val viewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModel.factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val screenHorizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val screenVerticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    ResponsiveScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = screenHorizontalPadding,
                end = screenHorizontalPadding,
                top = screenVerticalPadding,
                bottom = screenVerticalPadding + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {
            item {
                WeeklyComparisonCard(
                    thisWeekTotal = uiState.weeklyComparison.thisWeekTotal,
                    lastWeekTotal = uiState.weeklyComparison.lastWeekTotal
                )
            }

            item {
                MonthlyTrendCard(monthlyTrend = uiState.monthlyTrend)
            }

            item {
                SpendingByCategoryCard(categoryBreakdown = uiState.categoryBreakdown)
            }

            item {
                TopCategoryCard(topCategory = uiState.topCategory)
            }

            item {
                AverageTransactionCard(averageExpense = uiState.averageExpense)
            }
        }
    }
}

@Composable
private fun WeeklyComparisonCard(
    thisWeekTotal: Double,
    lastWeekTotal: Double
) {
    val comparison = buildWeeklyComparisonSummary(
        thisWeekTotal = thisWeekTotal,
        lastWeekTotal = lastWeekTotal,
        neutralColor = MaterialTheme.colorScheme.onSurfaceVariant,
        increaseColor = MaterialTheme.colorScheme.error,
        decreaseColor = Color(0xFF08B57A)
    )

    InsightCard {
        InsightHeader(
            title = "Weekly Comparison",
            subtitle = "Track how this week compares with last week"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightMetric(
                modifier = Modifier.weight(1f),
                label = "This Week",
                value = formatCurrencyDisplay(thisWeekTotal)
            )
            InsightMetric(
                modifier = Modifier.weight(1f),
                label = "Last Week",
                value = formatCurrencyDisplay(lastWeekTotal)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = comparison.message,
            color = comparison.color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MonthlyTrendCard(
    monthlyTrend: List<MonthlyTrendUiState>
) {
    val chartProgress = remember { Animatable(0f) }
    val chartHeight = dimensionResource(R.dimen.insights_monthly_chart_height)
    val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val positiveBarColor = MaterialTheme.colorScheme.primary
    val negativeBarColor = MaterialTheme.colorScheme.error

    LaunchedEffect(monthlyTrend) {
        chartProgress.snapTo(0f)
        chartProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    InsightCard {
        InsightHeader(
            title = "Monthly Trend",
            subtitle = "Last 6 months spending"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            if (monthlyTrend.isEmpty()) return@Canvas

            val minValue = min(monthlyTrend.minOf { it.amount }.toFloat(), 0f)
            val maxValue = max(monthlyTrend.maxOf { it.amount }.toFloat(), 0f)
            val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

            val chartTop = 8.dp.toPx()
            val chartBottom = size.height - 8.dp.toPx()
            val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
            val zeroY = if (minValue == 0f && maxValue == 0f) {
                chartBottom
            } else {
                chartTop + ((maxValue - 0f) / range) * chartHeight
            }
            val barSpacing = 10.dp.toPx()
            val barWidth = ((size.width - (barSpacing * (monthlyTrend.size - 1))) / monthlyTrend.size)
                .coerceAtLeast(8.dp.toPx())

            drawLine(
                color = baselineColor,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 2.dp.toPx()
            )

            monthlyTrend.forEachIndexed { index, item ->
                val x = index * (barWidth + barSpacing)
                val valueHeight = (abs(item.amount).toFloat() / range) * chartHeight * chartProgress.value
                val topLeft = if (item.amount >= 0.0) {
                    Offset(x, zeroY - valueHeight)
                } else {
                    Offset(x, zeroY)
                }

                drawRoundRect(
                    color = if (item.amount >= 0.0) {
                        positiveBarColor
                    } else {
                        negativeBarColor
                    },
                    topLeft = topLeft,
                    size = Size(barWidth, valueHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            monthlyTrend.forEach { item ->
                Text(
                    text = item.label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SpendingByCategoryCard(
    categoryBreakdown: List<CategoryInsightUiState>
) {
    InsightCard {
        InsightHeader(
            title = "Spending by Category",
            subtitle = "All expense categories"
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categoryBreakdown.isEmpty()) {
            InsightEmptyState(message = "No expense data available")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                categoryBreakdown.forEach { item ->
                    CategoryBreakdownRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun TopCategoryCard(
    topCategory: TopCategoryUiState?
) {
    InsightCard {
        InsightHeader(
            title = "Top Category",
            subtitle = "Highest expense category"
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (topCategory == null) {
            InsightEmptyState(message = "No data available")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topCategory.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatCurrencyDisplay(topCategory.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AverageTransactionCard(
    averageExpense: Double
) {
    InsightCard {
        InsightHeader(
            title = "Average Transaction",
            subtitle = "Average expense amount"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = formatCurrencyDisplay(averageExpense),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InsightCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun InsightMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InsightHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CategoryBreakdownRow(
    item: CategoryInsightUiState
) {
    val icon = categoryIconForName(item.name)
    val rawProgress = (item.percentage / 100.0).toFloat().coerceAtLeast(0f)
    val targetProgress = when {
        rawProgress <= 0f -> 0.03f
        rawProgress < 0.03f -> 0.03f
        else -> rawProgress.coerceAtMost(1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 700),
        label = "categoryProgress"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatPercentage(item.percentage)}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = formatCurrencyDisplay(item.amount),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun InsightEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private data class WeeklyComparisonSummary(
    val message: String,
    val color: Color
)

private fun buildWeeklyComparisonSummary(
    thisWeekTotal: Double,
    lastWeekTotal: Double,
    neutralColor: Color,
    increaseColor: Color,
    decreaseColor: Color
): WeeklyComparisonSummary {
    if (thisWeekTotal == 0.0 && lastWeekTotal == 0.0) {
        return WeeklyComparisonSummary(
            message = "No spending yet",
            color = neutralColor
        )
    }

    if (lastWeekTotal == 0.0) {
        return if (thisWeekTotal > 0.0) {
            WeeklyComparisonSummary(
                message = "100% increase vs last week",
                color = increaseColor
            )
        } else {
            WeeklyComparisonSummary(
                message = "100% decrease vs last week",
                color = decreaseColor
            )
        }
    }

    val difference = thisWeekTotal - lastWeekTotal
    if (difference == 0.0) {
        return WeeklyComparisonSummary(
            message = "No change vs last week",
            color = neutralColor
        )
    }

    val percentage = (difference / abs(lastWeekTotal)) * 100.0
    val isIncrease = thisWeekTotal > lastWeekTotal

    return WeeklyComparisonSummary(
        message = "${formatPercentage(abs(percentage))}% ${if (isIncrease) "increase" else "decrease"} vs last week",
        color = if (isIncrease) increaseColor else decreaseColor
    )
}

private fun formatPercentage(value: Double): String {
    val rounded = String.format(java.util.Locale.US, "%.1f", value)
    return rounded.removeSuffix(".0")
}

private fun categoryIconForName(name: String): ImageVector {
    return TransactionCategory
        .byType(TransactionType.EXPENSE)
        .firstOrNull { it.label.equals(name, ignoreCase = true) }
        ?.icon
        ?: TransactionCategory.OTHER_EXPENSE.icon
}
