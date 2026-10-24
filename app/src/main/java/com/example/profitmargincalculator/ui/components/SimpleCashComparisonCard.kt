package com.example.profitmargincalculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter

@Composable
fun SimpleCashComparisonCard(
    result: CalculationResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Cash Flow vs Business Profit Comparison",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Understanding the difference between simple cash subtraction and GST-exclusive accounting:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Method A: Business Profit (Standard)
            ComparisonMethodRow(
                methodTitle = "1. GST-Exclusive Business Profit (Recommended)",
                profit = IndianCurrencyFormatter.formatCurrency(result.roundedProfitPerUnit),
                margin = IndianCurrencyFormatter.formatPercentage(result.roundedProfitMargin),
                formula = "Net Sales Revenue (${IndianCurrencyFormatter.formatCurrency(result.roundedNetSalesRevenue)}) − Economic Cost (${IndianCurrencyFormatter.formatCurrency(result.roundedEconomicCost)})",
                explanation = "Accounts for output GST remitted to government & input GST recovered."
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Method B: Simple Cash Subtraction
            ComparisonMethodRow(
                methodTitle = "2. Raw Cash Subtraction (Retail Cash Register)",
                profit = IndianCurrencyFormatter.formatCurrency(result.simpleCashProfitPerUnit),
                margin = IndianCurrencyFormatter.formatPercentage(result.simpleCashProfitMargin),
                formula = "Cash Collected (${IndianCurrencyFormatter.formatCurrency(result.roundedCustomerSellingPrice)}) − Invoice Paid (${IndianCurrencyFormatter.formatCurrency(result.roundedPurchaseInvoice)})",
                explanation = "Raw cash difference before settling GST liabilities with the tax department."
            )
        }
    }
}

@Composable
private fun ComparisonMethodRow(
    methodTitle: String,
    profit: String,
    margin: String,
    formula: String,
    explanation: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = methodTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$profit ($margin)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formula,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = explanation,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}
