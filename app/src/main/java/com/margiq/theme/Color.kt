package com.margiq.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Scheme Base Colors
val FinanceNavyPrimaryLight = Color(0xFF1A365D)
val FinanceNavyOnPrimaryLight = Color(0xFFFFFFFF)
val FinancePrimaryContainerLight = Color(0xFFD6E4FF)
val FinanceOnPrimaryContainerLight = Color(0xFF001C3B)

val FinanceSecondaryLight = Color(0xFF334155)
val FinanceOnSecondaryLight = Color(0xFFFFFFFF)
val FinanceSecondaryContainerLight = Color(0xFFE2E8F0)
val FinanceOnSecondaryContainerLight = Color(0xFF0F172A)

val FinanceTertiaryLight = Color(0xFF0369A1) // Tax / Info accent
val FinanceOnTertiaryLight = Color(0xFFFFFFFF)
val FinanceTertiaryContainerLight = Color(0xFFE0F2FE)
val FinanceOnTertiaryContainerLight = Color(0xFF0C4A6E)

val FinanceBackgroundLight = Color(0xFFF8FAFC)
val FinanceOnBackgroundLight = Color(0xFF0F172A)
val FinanceSurfaceLight = Color(0xFFFFFFFF)
val FinanceOnSurfaceLight = Color(0xFF0F172A)
val FinanceSurfaceVariantLight = Color(0xFFF1F5F9)
val FinanceOnSurfaceVariantLight = Color(0xFF475569)
val FinanceOutlineLight = Color(0xFFCBD5E1)
val FinanceOutlineVariantLight = Color(0xFFE2E8F0)

// Dark Scheme Base Colors
val FinanceNavyPrimaryDark = Color(0xFFA5C8FF)
val FinanceNavyOnPrimaryDark = Color(0xFF003061)
val FinancePrimaryContainerDark = Color(0xFF004688)
val FinanceOnPrimaryContainerDark = Color(0xFFD6E4FF)

val FinanceSecondaryDark = Color(0xFF94A3B8)
val FinanceOnSecondaryDark = Color(0xFF1E293B)
val FinanceSecondaryContainerDark = Color(0xFF334155)
val FinanceOnSecondaryContainerDark = Color(0xFFF1F5F9)

val FinanceTertiaryDark = Color(0xFF7DD3FC)
val FinanceOnTertiaryDark = Color(0xFF00354F)
val FinanceTertiaryContainerDark = Color(0xFF0369A1)
val FinanceOnTertiaryContainerDark = Color(0xFFE0F2FE)

val FinanceBackgroundDark = Color(0xFF0F172A)
val FinanceOnBackgroundDark = Color(0xFFF8FAFC)
val FinanceSurfaceDark = Color(0xFF1E293B)
val FinanceOnSurfaceDark = Color(0xFFF8FAFC)
val FinanceSurfaceVariantDark = Color(0xFF283548)
val FinanceOnSurfaceVariantDark = Color(0xFF94A3B8)
val FinanceOutlineDark = Color(0xFF475569)
val FinanceOutlineVariantDark = Color(0xFF334155)

// Financial Semantic Colors (Profit, Loss, Neutral, Taxes)
@Immutable
data class FinancialColors(
    val profit: Color,
    val onProfit: Color,
    val profitContainer: Color,
    val onProfitContainer: Color,
    val loss: Color,
    val onLoss: Color,
    val lossContainer: Color,
    val onLossContainer: Color,
    val itcBadge: Color,
    val itcBadgeContainer: Color,
    val taxBadge: Color,
    val taxBadgeContainer: Color,
    val metricCardBackground: Color,
    val comparisonCardBackground: Color
)

val LightFinancialColors = FinancialColors(
    profit = Color(0xFF15803D),
    onProfit = Color(0xFFFFFFFF),
    profitContainer = Color(0xFFDCFCE7),
    onProfitContainer = Color(0xFF14532D),
    loss = Color(0xFFDC2626),
    onLoss = Color(0xFFFFFFFF),
    lossContainer = Color(0xFFFEE2E2),
    onLossContainer = Color(0xFF7F1D1D),
    itcBadge = Color(0xFF047857),
    itcBadgeContainer = Color(0xFFD1FAE5),
    taxBadge = Color(0xFF0284C7),
    taxBadgeContainer = Color(0xFFE0F2FE),
    metricCardBackground = Color(0xFFFFFFFF),
    comparisonCardBackground = Color(0xFFF8FAFC)
)

val DarkFinancialColors = FinancialColors(
    profit = Color(0xFF4ADE80),
    onProfit = Color(0xFF052E16),
    profitContainer = Color(0xFF14532D),
    onProfitContainer = Color(0xFFBBF7D0),
    loss = Color(0xFFF87171),
    onLoss = Color(0xFF450A0A),
    lossContainer = Color(0xFF7F1D1D),
    onLossContainer = Color(0xFFFECACA),
    itcBadge = Color(0xFF34D399),
    itcBadgeContainer = Color(0xFF064E3B),
    taxBadge = Color(0xFF38BDF8),
    taxBadgeContainer = Color(0xFF0C4A6E),
    metricCardBackground = Color(0xFF1E293B),
    comparisonCardBackground = Color(0xFF162032)
)

val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }
