package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyBarChart(
    dailyExpenses: Map<Int, Double>,
    daysInMonth: Int,
    currency: String,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dailyExpenses) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    val maxExpense = (dailyExpenses.values.maxOrNull() ?: 1.0).coerceAtLeast(100.0)
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val scrollState = rememberScrollState()

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإنفاق اليومي خلال الشهر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "أعلى يوم: ${numberFormat.format(maxExpense)} $currency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable row of day bars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                val barWidth = 14.dp
                val barSpacing = 8.dp
                val totalWidth = (barWidth + barSpacing) * daysInMonth + 16.dp
                val chartHeight = 140.dp

                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                val highlightColor = MaterialTheme.colorScheme.tertiary

                Column {
                    Canvas(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(chartHeight)
                    ) {
                        val progress = animationProgress.value
                        val h = size.height

                        // Draw baseline
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, h),
                            end = Offset(size.width, h),
                            strokeWidth = 2f
                        )

                        for (day in 1..daysInMonth) {
                            val amount = dailyExpenses[day] ?: 0.0
                            val barHeightRatio = (amount / maxExpense).toFloat().coerceIn(0f, 1f)
                            val barH = (barHeightRatio * (h - 20f)) * progress
                            val x = (day - 1) * (barWidth.toPx() + barSpacing.toPx()) + 8.dp.toPx()
                            val isCurrentDay = day == today

                            // Background empty track
                            drawRoundRect(
                                color = outlineColor.copy(alpha = 0.2f),
                                topLeft = Offset(x, 10f),
                                size = Size(barWidth.toPx(), h - 10f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )

                            // Filled bar
                            if (barH > 4f) {
                                drawRoundRect(
                                    color = if (isCurrentDay) highlightColor else primaryColor,
                                    topLeft = Offset(x, h - barH),
                                    size = Size(barWidth.toPx(), barH),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day numbers below each bar
                    Row(
                        modifier = Modifier
                            .width(totalWidth)
                            .padding(start = 8.dp)
                    ) {
                        for (day in 1..daysInMonth) {
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .padding(end = 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontSize = 10.sp,
                                    fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal,
                                    color = if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (day < daysInMonth) {
                                Spacer(modifier = Modifier.width(barSpacing))
                            }
                        }
                    }
                }
            }
        }
    }
}
