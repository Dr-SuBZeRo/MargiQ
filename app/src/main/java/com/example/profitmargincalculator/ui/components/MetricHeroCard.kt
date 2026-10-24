package com.example.profitmargincalculator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter
import com.example.profitmargincalculator.theme.FinanceTheme

@Composable
fun MetricHeroCard(
    result: CalculationResult,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors

    val statusBg by animateColorAsState(
        targetValue = when {
            !result.isValid -> MaterialTheme.colorScheme.errorContainer
            result.isLoss -> financeColors.lossContainer
            result.isBreakEven -> MaterialTheme.colorScheme.surfaceVariant
            else -> financeColors.profitContainer
        },
        label = "statusBg"
    )

    val statusTextColor by animateColorAsState(
        targetValue = when {
            !result.isValid -> MaterialTheme.colorScheme.onErrorContainer
            result.isLoss -> financeColors.onLossContainer
            result.isBreakEven -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> financeColors.onProfitContainer
        },
        label = "statusTextColor"
    )

    val profitColor by animateColorAsState(
        targetValue = when {
            !result.isValid -> MaterialTheme.colorScheme.error
            result.isLoss -> financeColors.loss
            result.isBreakEven -> MaterialTheme.colorScheme.onSurface
            else -> financeColors.profit
        },
        label = "profitColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row with Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTIMATED PROFIT PER UNIT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = when {
                            !result.isValid -> "INVALID"
                            result.isLoss -> "LOSS"
                            result.isBreakEven -> "BREAK-EVEN"
                            else -> "PROFITABLE"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Big Number
            Text(
                text = if (result.isValid) {
                    IndianCurrencyFormatter.formatCurrency(result.roundedProfitPerUnit)
                } else {
                    "—"
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = profitColor
            )

            Text(
                text = result.calculationBasis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Metric Summary Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricSubItem(
                    label = "Profit Margin",
                    value = IndianCurrencyFormatter.formatPercentage(result.roundedProfitMargin),
                    isHighlighted = true,
                    highlightColor = profitColor
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                MetricSubItem(
                    label = "Markup",
                    value = IndianCurrencyFormatter.formatPercentage(result.roundedMarkup),
                    isHighlighted = false
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                MetricSubItem(
                    label = "Total Profit (${result.quantity})",
                    value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalProfit),
                    isHighlighted = true,
                    highlightColor = profitColor
                )
            }
        }
    }
}

@Composable
private fun MetricSubItem(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    highlightColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isHighlighted) highlightColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
