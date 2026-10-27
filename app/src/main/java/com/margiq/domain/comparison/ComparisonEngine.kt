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
    val isValid: Boolean,
    val validationMessage: String?
)

object ComparisonEngine {

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

        return ProductComparisonOutcome(
            resultA = resultA,
            resultB = resultB,
            highlight = highlight,
            isValid = true,
            validationMessage = null
        )
    }
}
