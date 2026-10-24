package com.example.profitmargincalculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter
import com.example.profitmargincalculator.theme.FinanceTheme

@Composable
fun FinancialBreakdownCard(
    result: CalculationResult,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Clickable Header to Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Detailed Financial Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isExpanded) "Tap to collapse detailed accounting" else "Tap to view purchase, tax & revenue details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 1: PURCHASE SIDE
                    BreakdownSectionHeader(title = "PURCHASE ACCOUNTING")
                    BreakdownLineItem(label = "Entered PTR", value = IndianCurrencyFormatter.formatCurrency(result.enteredPtr))
                    BreakdownLineItem(label = "Purchase Tax Rate", value = IndianCurrencyFormatter.formatPercentage(result.purchaseTaxRate))
                    BreakdownLineItem(label = "Taxable Purchase Base", value = IndianCurrencyFormatter.formatCurrency(result.taxablePurchaseBase))
                    BreakdownLineItem(label = "Purchase GST / Tax", value = IndianCurrencyFormatter.formatCurrency(result.purchaseTaxAmount))
                    BreakdownLineItem(
                        label = "Purchase Invoice Total (Cash Paid)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedPurchaseInvoice),
                        isBold = true
                    )
                    BreakdownLineItem(
                        label = "Eligible Input Tax Credit (ITC)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedEligibleInputTaxCredit),
                        highlightColor = if (result.isInputTaxCreditApplied) financeColors.profit else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BreakdownLineItem(
                        label = "Economic Purchase Cost (for profit)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedEconomicCost),
                        isBold = true,
                        highlightColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 2: SELLING SIDE
                    BreakdownSectionHeader(title = "SELLING & REVENUE ACCOUNTING")
                    BreakdownLineItem(label = "Entered Selling Price (MRP)", value = IndianCurrencyFormatter.formatCurrency(result.enteredSellingPrice))
                    BreakdownLineItem(label = "Selling Tax Rate", value = IndianCurrencyFormatter.formatPercentage(result.sellingTaxRate))
                    BreakdownLineItem(label = "Output GST Collected", value = IndianCurrencyFormatter.formatCurrency(result.roundedSellingTaxAmount))
                    BreakdownLineItem(
                        label = "Customer Selling Price",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedCustomerSellingPrice),
                        isBold = true
                    )
                    BreakdownLineItem(
                        label = "Net Sales Revenue (Business Revenue)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedNetSalesRevenue),
                        isBold = true,
                        highlightColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 3: PROFIT & MARGIN RATIOS
                    BreakdownSectionHeader(title = "PER-UNIT METRICS")
                    BreakdownLineItem(
                        label = "Profit Per Unit",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedProfitPerUnit),
                        isBold = true,
                        highlightColor = if (result.isLoss) financeColors.loss else financeColors.profit
                    )
                    BreakdownLineItem(label = "Profit Margin (Profit ÷ Revenue)", value = IndianCurrencyFormatter.formatPercentage(result.roundedProfitMargin))
                    BreakdownLineItem(label = "Markup (Profit ÷ Economic Cost)", value = IndianCurrencyFormatter.formatPercentage(result.roundedMarkup))
                    BreakdownLineItem(label = "Cost % of Revenue (Cost ÷ Revenue)", value = IndianCurrencyFormatter.formatPercentage(result.roundedCostPercentage))

                    if (result.quantity > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // SECTION 4: MULTI-UNIT TOTALS
                        BreakdownSectionHeader(title = "TOTALS FOR QUANTITY = ${result.quantity}")
                        BreakdownLineItem(label = "Total Purchase Invoice Amount", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalPurchaseInvoice))
                        BreakdownLineItem(label = "Total Eligible Input Tax Credit", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalInputCredit))
                        BreakdownLineItem(label = "Total Customer Amount Collected", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalCustomerCollection))
                        BreakdownLineItem(label = "Total Output GST Payable", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalOutputTax))
                        BreakdownLineItem(
                            label = "Total Net Profit",
                            value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalProfit),
                            isBold = true,
                            highlightColor = if (result.isLoss) financeColors.loss else financeColors.profit
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tax note banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tax note",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Note: In tax-inclusive MRP pricing, output GST is excluded from business revenue because it is collected on behalf of the government and remitted. When ITC is enabled, eligible input GST is credited back and deducted from economic cost.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BreakdownSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun BreakdownLineItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
