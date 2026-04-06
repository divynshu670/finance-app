package com.example.financecompanion.views.components.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.financecompanion.R
import com.example.financecompanion.utils.formatCurrencyDisplay

@Composable
fun CategoryCard(
    categoryData: List<Pair<String, Double>>
) {
    val chartProgress = remember { Animatable(0f) }
    val chartItems = remember(categoryData) {
        categoryData.ifEmpty { listOf("Other" to 0.0) }
    }

    LaunchedEffect(chartItems) {
        chartProgress.snapTo(0f)
        chartProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {

        Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDonutChart(
                categoryData = chartItems,
                progress = chartProgress.value
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chartItems.forEachIndexed { index, (category, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(10.dp),
                                shape = RoundedCornerShape(50),
                                color = chartColor(index)
                            ) {}

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = category,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = formatCurrencyDisplay(amount),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(
    categoryData: List<Pair<String, Double>>,
    progress: Float
) {
    val donutSize = dimensionResource(R.dimen.home_category_donut_size)
    val total = categoryData.sumOf { it.second }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = Modifier.size(donutSize)
    ) {
        val strokeWidth = 22.dp.toPx()
        val diameter = size.minDimension
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f
        )

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        if (total <= 0.0) return@Canvas

        var startAngle = -90f
        categoryData.forEachIndexed { index, (_, amount) ->
            if (amount <= 0.0) return@forEachIndexed

            val sweepAngle = ((amount / total) * 360f * progress).toFloat()
            drawArc(
                color = chartColor(index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += (amount / total * 360f).toFloat()
        }
    }
}

private fun chartColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF08B57A),
        Color(0xFF2F80ED),
        Color(0xFFF2994A),
        Color(0xFFBDBDBD)
    )
    return colors[index % colors.size]
}
