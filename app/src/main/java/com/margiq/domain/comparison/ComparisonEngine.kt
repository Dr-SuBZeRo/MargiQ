package com.margiq.domain.comparison

import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import java.math.BigDecimal
import java.math.RoundingMode

enum class WinnerSide { SIDE_A, SIDE_B, TIE, NONE }

data class ComparisonHighlight(
    val lowestCostSide: WinnerSide,
    val highestProfitPerUnitSide: WinnerSide,
    val highestMarginSide: WinnerSide,
    val costDifferencePerUnit: BigDecimal,
    val costDifferencePerPack: BigDecimal,
    val costSavingsPercent: BigDecimal?,
    val profitDifferencePerUnit: BigDecimal,
    val marginDifferencePercent: BigDecimal?,
    val markupDifferencePercent: BigDecimal?
)

data class ProductComparisonOutcome(
    val resultA: CalculationResult,
    val resultB: CalculationResult,
    val highlight: ComparisonHighlight?,
    val differences: ComparisonDifferences? = null,
    val isValid: Boolean,
    val validationMessage: String?
)

object ComparisonEngine {

    fun calculateDifferences(resultA: CalculationResult, resultB: CalculationResult): ComparisonDifferences {
        val unitsPerPackEqual = resultA.unitsPerPack == resultB.unitsPerPack
        val quantitiesEqual = resultA.quantity == resultB.quantity && unitsPerPackEqual

        return ComparisonDifferences(
            costPerUnit = MetricDifference(
                metricKey = "cost_per_unit",
                metricName = "Cost / Unit",
                rawA = resultA.costPerIndividualUnit,
                rawB = resultB.costPerIndividualUnit,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.LOWER_IS_BETTER,
                unitSuffix = "/unit"
            ),
            costPerPack = MetricDifference(
                metricKey = "cost_per_pack",
                metricName = "Cost / Pack",
                rawA = resultA.effectiveCostPerReceivedPack,
                rawB = resultB.effectiveCostPerReceivedPack,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = if (unitsPerPackEqual) FavourabilityRule.LOWER_IS_BETTER else FavourabilityRule.NEUTRAL,
                unitSuffix = "/pack"
            ),
            ptr = MetricDifference(
                metricKey = "ptr",
                metricName = "Purchase Price (PTR)",
                rawA = resultA.enteredPtr,
                rawB = resultB.enteredPtr,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = if (unitsPerPackEqual) FavourabilityRule.LOWER_IS_BETTER else FavourabilityRule.NEUTRAL
            ),
            purchaseTaxAmount = MetricDifference(
                metricKey = "purchase_tax_amount",
                metricName = "Purchase Tax Amount",
                rawA = resultA.purchaseTaxAmount,
                rawB = resultB.purchaseTaxAmount,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            purchaseTaxRate = MetricDifference(
                metricKey = "purchase_tax_rate",
                metricName = "Purchase Tax Rate",
                rawA = resultA.purchaseTaxRate,
                rawB = resultB.purchaseTaxRate,
                type = MetricDifferenceType.PERCENTAGE_POINTS,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            supplierDiscountPercent = MetricDifference(
                metricKey = "supplier_discount_percent",
                metricName = "Supplier Discount %",
                rawA = resultA.supplierDiscountPercent,
                rawB = resultB.supplierDiscountPercent,
                type = MetricDifferenceType.PERCENTAGE_POINTS,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            supplierDiscountAmount = MetricDifference(
                metricKey = "supplier_discount_amount",
                metricName = "Supplier Discount Amount",
                rawA = resultA.supplierDiscountAmount,
                rawB = resultB.supplierDiscountAmount,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            eligibleItc = MetricDifference(
                metricKey = "eligible_itc",
                metricName = "Eligible ITC",
                rawA = resultA.eligibleInputTaxCredit,
                rawB = resultB.eligibleInputTaxCredit,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            customerSellingPrice = MetricDifference(
                metricKey = "customer_selling_price",
                metricName = "Selling Price (MRP)",
                rawA = resultA.customerSellingPrice,
                rawB = resultB.customerSellingPrice,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            sellingTaxAmount = MetricDifference(
                metricKey = "selling_tax_amount",
                metricName = "Selling Tax Amount",
                rawA = resultA.sellingTaxAmount,
                rawB = resultB.sellingTaxAmount,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            sellingTaxRate = MetricDifference(
                metricKey = "selling_tax_rate",
                metricName = "Selling Tax Rate",
                rawA = resultA.sellingTaxRate,
                rawB = resultB.sellingTaxRate,
                type = MetricDifferenceType.PERCENTAGE_POINTS,
                favourabilityRule = FavourabilityRule.NEUTRAL
            ),
            netSalesRevenue = MetricDifference(
                metricKey = "net_sales_revenue",
                metricName = "Net Sales Revenue",
                rawA = resultA.netSalesRevenue,
                rawB = resultB.netSalesRevenue,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = if (unitsPerPackEqual) FavourabilityRule.HIGHER_IS_BETTER else FavourabilityRule.NEUTRAL
            ),
            profitPerUnit = MetricDifference(
                metricKey = "profit_per_unit",
                metricName = "Profit / Unit",
                rawA = resultA.profitPerIndividualUnit,
                rawB = resultB.profitPerIndividualUnit,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER,
                unitSuffix = "/unit"
            ),
            profitPerPack = MetricDifference(
                metricKey = "profit_per_pack",
                metricName = "Profit / Pack",
                rawA = resultA.profitPerUnit,
                rawB = resultB.profitPerUnit,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = if (unitsPerPackEqual) FavourabilityRule.HIGHER_IS_BETTER else FavourabilityRule.NEUTRAL,
                unitSuffix = "/pack"
            ),
            profitMargin = MetricDifference(
                metricKey = "profit_margin",
                metricName = "Profit Margin",
                rawA = resultA.profitMargin,
                rawB = resultB.profitMargin,
                type = MetricDifferenceType.PERCENTAGE_POINTS,
                favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER
            ),
            markup = MetricDifference(
                metricKey = "markup",
                metricName = "Markup",
                rawA = resultA.markup,
                rawB = resultB.markup,
                type = MetricDifferenceType.PERCENTAGE_POINTS,
                favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER
            ),
            batchProfit = MetricDifference(
                metricKey = "batch_profit",
                metricName = "Batch Profit",
                rawA = resultA.totalProfit,
                rawB = resultB.totalProfit,
                type = MetricDifferenceType.CURRENCY,
                favourabilityRule = if (quantitiesEqual) FavourabilityRule.HIGHER_IS_BETTER else FavourabilityRule.NEUTRAL
            ),
            unitsPerPack = MetricDifference(
                metricKey = "units_per_pack",
                metricName = "Units per Pack",
                rawA = BigDecimal(resultA.unitsPerPack),
                rawB = BigDecimal(resultB.unitsPerPack),
                type = MetricDifferenceType.INTEGER,
                favourabilityRule = FavourabilityRule.NEUTRAL,
                unitSuffix = " units/pack"
            ),
            totalPacksReceived = MetricDifference(
                metricKey = "total_packs_received",
                metricName = "Total Packs Received",
                rawA = BigDecimal(resultA.totalPacksReceived),
                rawB = BigDecimal(resultB.totalPacksReceived),
                type = MetricDifferenceType.INTEGER,
                favourabilityRule = FavourabilityRule.NEUTRAL,
                unitSuffix = " packs"
            ),
            freePacksReceived = MetricDifference(
                metricKey = "free_packs_received",
                metricName = "Free Packs Received",
                rawA = BigDecimal(resultA.freePacksReceived),
                rawB = BigDecimal(resultB.freePacksReceived),
                type = MetricDifferenceType.INTEGER,
                favourabilityRule = FavourabilityRule.NEUTRAL,
                unitSuffix = " free packs"
            ),
            areUnitsPerPackEqual = unitsPerPackEqual
        )
    }

    fun compare(inputA: CalculationInput, inputB: CalculationInput): ProductComparisonOutcome {
        val resultA = ProfitCalculationEngine.calculate(inputA)
        val resultB = ProfitCalculationEngine.calculate(inputB)

        if (!resultA.isValid || !resultB.isValid) {
            val msg = when {
                !resultA.isValid && !resultB.isValid -> "Side A: ${resultA.validationMessage} | Side B: ${resultB.validationMessage}"
                !resultA.isValid -> "Side A: ${resultA.validationMessage}"
                else -> "Side B: ${resultB.validationMessage}"
            }
            return ProductComparisonOutcome(
                resultA = resultA,
                resultB = resultB,
                highlight = null,
                isValid = false,
                validationMessage = msg
            )
        }

        // 1. Cost Comparison (normalized per individual unit)
        val costUnitA = resultA.roundedCostPerIndividualUnit
        val costUnitB = resultB.roundedCostPerIndividualUnit
        val costPackA = resultA.roundedEffectiveCostPerReceivedPack
        val costPackB = resultB.roundedEffectiveCostPerReceivedPack

        val lowestCostSide = when {
            costUnitA < costUnitB -> WinnerSide.SIDE_A
            costUnitB < costUnitA -> WinnerSide.SIDE_B
            else -> WinnerSide.TIE
        }

        val costDiffPerUnit = (costUnitA - costUnitB).abs()
        val costDiffPerPack = (costPackA - costPackB).abs()
        val higherCostUnit = maxOf(costUnitA, costUnitB)
        val costSavingsPct = if (higherCostUnit.compareTo(BigDecimal.ZERO) > 0) {
            costDiffPerUnit.divide(higherCostUnit, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
        } else null

        // 2. Profit Comparison (per individual unit)
        val profitUnitA = resultA.roundedProfitPerIndividualUnit
        val profitUnitB = resultB.roundedProfitPerIndividualUnit

        val highestProfitSide = when {
            profitUnitA > profitUnitB -> WinnerSide.SIDE_A
            profitUnitB > profitUnitA -> WinnerSide.SIDE_B
            else -> WinnerSide.TIE
        }
        val profitDiffPerUnit = (profitUnitA - profitUnitB).abs()

        // 3. Profit Margin Comparison
        val marginA = resultA.roundedProfitMargin ?: BigDecimal.ZERO
        val marginB = resultB.roundedProfitMargin ?: BigDecimal.ZERO

        val highestMarginSide = when {
            marginA > marginB -> WinnerSide.SIDE_A
            marginB > marginA -> WinnerSide.SIDE_B
            else -> WinnerSide.TIE
        }
        val marginDiff = (marginA - marginB).abs()

        // 4. Markup Comparison
        val markupA = resultA.roundedMarkup ?: BigDecimal.ZERO
        val markupB = resultB.roundedMarkup ?: BigDecimal.ZERO
        val markupDiff = (markupA - markupB).abs()

        val highlight = ComparisonHighlight(
            lowestCostSide = lowestCostSide,
            highestProfitPerUnitSide = highestProfitSide,
            highestMarginSide = highestMarginSide,
            costDifferencePerUnit = costDiffPerUnit,
            costDifferencePerPack = costDiffPerPack,
            costSavingsPercent = costSavingsPct,
            profitDifferencePerUnit = profitDiffPerUnit,
            marginDifferencePercent = marginDiff,
            markupDifferencePercent = markupDiff
        )

        val differences = calculateDifferences(resultA, resultB)

        return ProductComparisonOutcome(
            resultA = resultA,
            resultB = resultB,
            highlight = highlight,
            differences = differences,
            isValid = true,
            validationMessage = null
        )
    }
}
