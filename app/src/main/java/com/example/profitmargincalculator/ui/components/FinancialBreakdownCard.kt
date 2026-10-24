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
                        text = if (isExpanded) "Tap to collapse detailed accounting" else "Tap to view supplier, tax & revenue accounting breakdown",
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

                    // SECTION 1: PURCHASE & SUPPLIER BENEFIT ACCOUNTING
                    BreakdownSectionHeader(title = "PURCHASE & SUPPLIER BENEFIT ACCOUNTING")
                    BreakdownLineItem(label = "Original PTR per Pack", value = IndianCurrencyFormatter.formatCurrency(result.enteredPtr))
                    if (result.supplierDiscountPercent > java.math.BigDecimal.ZERO) {
                        BreakdownLineItem(label = "Supplier Discount Rate", value = IndianCurrencyFormatter.formatPercentage(result.supplierDiscountPercent))
                        BreakdownLineItem(label = "Supplier Discount Amount", value = "-${IndianCurrencyFormatter.formatCurrency(result.roundedSupplierDiscountAmount)}", highlightColor = financeColors.profit)
                    }
                    BreakdownLineItem(label = "Purchase Tax Rate", value = IndianCurrencyFormatter.formatPercentage(result.purchaseTaxRate))
                    BreakdownLineItem(label = "Taxable Purchase Base (Per Paid Pack)", value = IndianCurrencyFormatter.formatCurrency(result.taxablePurchaseBase))
                    BreakdownLineItem(label = "Purchase GST / Tax (Per Paid Pack)", value = IndianCurrencyFormatter.formatCurrency(result.purchaseTaxAmount))
                    BreakdownLineItem(
                        label = "Supplier Invoice Total (Cash Paid)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalPurchaseInvoice),
                        isBold = true
                    )
                    BreakdownLineItem(
                        label = "Eligible Input Tax Credit (ITC)",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalInputCredit),
                        highlightColor = if (result.isInputTaxCreditApplied) financeColors.profit else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BreakdownLineItem(
                        label = "Total Economic Purchase Cost",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalEconomicCost),
                        isBold = true
                    )

                    if (result.isSupplierOfferEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BreakdownLineItem(label = "Supplier Offer", value = "Buy ${result.offerBuyQuantity}, Get ${result.offerFreeQuantity} Free")
                        BreakdownLineItem(label = "Paid Packs", value = "${result.quantity}")
                        BreakdownLineItem(label = "Free Packs Received", value = "+${result.freePacksReceived}", highlightColor = financeColors.profit)
                        BreakdownLineItem(label = "Total Received Packs", value = "${result.totalPacksReceived}", isBold = true)
                    }

                    BreakdownLineItem(
                        label = "Effective Cost Per Received Pack",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedEffectiveCostPerReceivedPack),
                        isBold = true,
                        highlightColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 2: SELLING SIDE
                    BreakdownSectionHeader(title = "SELLING & REVENUE ACCOUNTING (PER PACK)")
                    BreakdownLineItem(label = "Entered Selling Price (MRP)", value = IndianCurrencyFormatter.formatCurrency(result.enteredSellingPrice))
                    BreakdownLineItem(label = "Selling Tax Rate", value = IndianCurrencyFormatter.formatPercentage(result.sellingTaxRate))
                    BreakdownLineItem(label = "Output GST Embedded / Collected", value = IndianCurrencyFormatter.formatCurrency(result.roundedSellingTaxAmount))
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
                    BreakdownSectionHeader(title = "MARGIN & MARKUP RATIOS")
                    BreakdownLineItem(
                        label = "Profit Per Received Pack",
                        value = IndianCurrencyFormatter.formatCurrency(result.roundedProfitPerUnit),
                        isBold = true,
                        highlightColor = if (result.isLoss) financeColors.loss else financeColors.profit
                    )
                    if (result.unitsPerPack > 1) {
                        BreakdownLineItem(
                            label = "Profit Per Individual Unit",
                            value = IndianCurrencyFormatter.formatCurrency(result.roundedProfitPerIndividualUnit),
                            highlightColor = if (result.isLoss) financeColors.loss else financeColors.profit
                        )
                    }
                    BreakdownLineItem(label = "Profit Margin (Profit ÷ Revenue)", value = IndianCurrencyFormatter.formatPercentage(result.roundedProfitMargin))
                    BreakdownLineItem(label = "Markup (Profit ÷ Economic Cost)", value = IndianCurrencyFormatter.formatPercentage(result.roundedMarkup))
                    BreakdownLineItem(label = "Cost % of Revenue (Cost ÷ Revenue)", value = IndianCurrencyFormatter.formatPercentage(result.roundedCostPercentage))

                    if (result.quantity > 1 || result.totalPacksReceived > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // SECTION 4: MULTI-PACK TOTALS
                        BreakdownSectionHeader(title = "BATCH TOTALS (${result.quantity} packs paid, ${result.totalPacksReceived} received)")
                        BreakdownLineItem(label = "Total Purchase Outlay (Payable to Supplier)", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalPurchaseInvoice))
                        BreakdownLineItem(label = "Total Economic Purchase Cost", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalEconomicCost))
                        BreakdownLineItem(label = "Total Eligible Input Tax Credit", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalInputCredit))
                        BreakdownLineItem(label = "Total Customer Collection (${result.totalPacksReceived} packs)", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalCustomerCollection))
                        BreakdownLineItem(label = "Total Net Sales Revenue", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalNetSalesRevenue))
                        BreakdownLineItem(label = "Total Output GST Payable", value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalOutputTax))
                        BreakdownLineItem(
                            label = "Total Net Profit",
                            value = IndianCurrencyFormatter.formatCurrency(result.roundedTotalProfit),
                            isBold = true,
                            highlightColor = if (result.isLoss) financeColors.loss else financeColors.profit
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tax & Free-Goods note banner
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
                                text = "Note: Business profit is calculated from tax-adjusted net revenue and effective economic cost. GST is charged only on discounted paid-goods consideration; free goods receive zero additional purchase consideration. In recoverable GST with ITC enabled, input GST is credited back and excluded from cost. Actual supplier invoicing and free-goods GST treatment depend on statutory regulations and invoice structures.",
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
