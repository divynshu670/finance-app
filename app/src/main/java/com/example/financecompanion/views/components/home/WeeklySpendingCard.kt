package com.example.financecompanion.views.components.home

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financecompanion.R
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeeklySpendingCard(
    weeklyData: List<Float>
) {
    val chartHeight = dimensionResource(R.dimen.weekly_spending_chart_height)
    val guideColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = Color(0xFF08B57A)

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
            Text("Weekly Spending")
            Text("This week", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            if (weeklyData.isEmpty()) return@Canvas

            val minValue = min(weeklyData.minOrNull() ?: 0f, 0f)
            val maxValue = max(weeklyData.maxOrNull() ?: 0f, 0f)
            val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

            val labelWidth = 42.dp.toPx()
            val chartLeft = labelWidth + 8.dp.toPx()
            val chartRight = size.width
            val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
            val chartTop = 6.dp.toPx()
            val chartBottom = size.height - 6.dp.toPx()
            val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

            fun yFor(value: Float): Float {
                return chartTop + ((maxValue - value) / range) * chartHeight
            }

            val stepX = if (weeklyData.size > 1) {
                chartWidth / (weeklyData.size - 1)
            } else {
                0f
            }

            val points = weeklyData.mapIndexed { index, value ->
                val x = if (weeklyData.size > 1) {
                    chartLeft + index * stepX
                } else {
                    chartLeft + (chartWidth / 2f)
                }
                val y = yFor(value)
                Offset(x, y)
            }

            val topY = yFor(maxValue)
            val zeroY = yFor(0f)
            val bottomY = yFor(minValue)

            listOf(topY, zeroY, bottomY).distinct().forEach { y ->
                drawLine(
                    color = if (y == zeroY) zeroLineColor else guideColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = if (y == zeroY) 2f else 1f,
                    pathEffect = if (y == zeroY) null else PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 10f)
                    )
                )
            }

            val axisPaint = Paint().apply {
                color = axisTextColor.toArgb()
                textSize = 10.sp.toPx()
                isAntiAlias = true
            }

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.apply {
                    drawText(formatAxisLabel(maxValue), 0f, topY + axisPaint.textSize / 2f, axisPaint)
                    if (zeroY != topY && zeroY != bottomY) {
                        drawText("0.00", 0f, zeroY + axisPaint.textSize / 2f, axisPaint)
                    }
                    if (bottomY != topY) {
                        drawText(formatAxisLabel(minValue), 0f, bottomY + axisPaint.textSize / 2f, axisPaint)
                    }
                }
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
            }

            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.5.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

private fun formatAxisLabel(value: Float): String {
    return String.format("%.2f", value)
}
