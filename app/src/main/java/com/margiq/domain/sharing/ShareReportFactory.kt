package com.margiq.domain.sharing

import com.margiq.data.local.CalculationEntity
import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import com.margiq.ui.calculator.CalculatorUiState
import com.margiq.ui.compare.CompareSideState
import com.margiq.ui.compare.SupplierComparisonBaseProduct
import com.margiq.ui.compare.SupplierOfferSide
import java.math.BigDecimal

object ShareReportFactory {

    /**
     * Builds a canonical report from the active calculator state.
     */
    fun fromCalculator(uiState: CalculatorUiState): ShareReport.SingleCalculation {
        return fromCalculation(
            input = uiState.calculationInput,
            result = uiState.calculationResult,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Builds a canonical report from an immutable calculation history record.
     * Guaranteed to use the historical inputs and snapshot without touching active state.
     */
    fun fromHistory(entity: CalculationEntity): ShareReport.SingleCalculation {
        val input = entity.toCalculationInput()
        val result = ProfitCalculationEngine.calculate(input)
        return fromCalculation(
            input = input,
            result = result,
            timestamp = entity.timestamp
        )
    }

    /**
     * Helper to map calculation input and result into [ShareReport.SingleCalculation].
     */
    fun fromCalculation(
        input: CalculationInput,
        result: CalculationResult,
        timestamp: Long
    ): ShareReport.SingleCalculation {
        return ShareReport.SingleCalculation(
            productName = input.productName.ifBlank { "Untitled Product" },
            supplierName = input.supplierName,
            unitsPerPack = input.unitsPerPack,
            quantity = input.quantity,
            enteredPtr = input.ptr,
            purchasePriceMode = input.purchasePriceMode,
            purchaseTaxRate = input.purchaseTaxRate,
            isSupplierDiscountEnabled = input.isSupplierDiscountEnabled,
            supplierDiscountPercent = input.supplierDiscountPercent,
            isSupplierOfferEnabled = input.isSupplierOfferEnabled,
            offerBuyQuantity = input.offerBuyQuantity,
            offerFreeQuantity = input.offerFreeQuantity,
            freePacksReceived = result.freePacksReceived,
            totalPacksReceived = result.totalPacksReceived,
            inputTaxCreditEnabled = input.isInputTaxCreditEnabled,
            purchaseTaxClassification = input.purchaseTaxClassification,
            enteredSellingPrice = input.sellingPrice,
            sellingPriceMode = input.sellingPriceMode,
            sellingTaxRate = input.sellingTaxRate,
            invoiceTotal = result.roundedPurchaseInvoice,
            eligibleInputTaxCredit = result.roundedEligibleInputTaxCredit,
            effectiveCostPerPack = result.roundedEffectiveCostPerReceivedPack,
            customerSellingPrice = result.roundedCustomerSellingPrice,
            netSalesRevenue = result.roundedNetSalesRevenue,
            outputTax = result.roundedSellingTaxAmount,
            profitPerPack = result.roundedProfitPerUnit,
            profitMarginPercent = result.roundedProfitMargin,
            markupPercent = result.roundedMarkup,
            totalBatchProfit = result.roundedTotalProfit,
            costPerIndividualUnit = if (input.unitsPerPack > 1) result.roundedCostPerIndividualUnit else null,
            customerPricePerIndividualUnit = if (input.unitsPerPack > 1) result.roundedCustomerPricePerIndividualUnit else null,
            profitPerIndividualUnit = if (input.unitsPerPack > 1) result.roundedProfitPerIndividualUnit else null,
            calculationBasis = result.calculationBasis,
            title = input.productName.ifBlank { "Pricing & Margin Report" },
            timestamp = timestamp
        )
    }

    /**
     * Builds a canonical report from a Product Comparison session.
     */
    fun fromProductComparison(
        outcome: ProductComparisonOutcome,
        sideA: CompareSideState,
        sideB: CompareSideState
    ): ShareReport.ProductComparison {
        val hl = outcome.highlight
        val resA = outcome.resultA
        val resB = outcome.resultB
        val nameA = sideA.productName.ifBlank { "Option A" }
        val nameB = sideB.productName.ifBlank { "Option B" }

        val primarySummary = if (hl != null && hl.costSavingsPercent != null && hl.costSavingsPercent.compareTo(BigDecimal.ZERO) > 0) {
            val cheaperName = if (hl.lowestCostSide == WinnerSide.SIDE_A) nameA else nameB
            "$cheaperName saves ₹${hl.costDifferencePerUnit.toPlainString()}/unit (${hl.costSavingsPercent.toPlainString()}%)"
        } else if (hl?.lowestCostSide == WinnerSide.TIE) {
            "Both options have identical cost per unit"
        } else {
            "Commercial comparison completed"
        }

        return ShareReport.ProductComparison(
            titleA = nameA,
            titleB = nameB,
            unitsPerPackA = sideA.unitsPerPackText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            unitsPerPackB = sideB.unitsPerPackText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            costPerPackA = resA.roundedEffectiveCostPerReceivedPack,
            costPerPackB = resB.roundedEffectiveCostPerReceivedPack,
            costPerUnitA = resA.roundedCostPerIndividualUnit,
            costPerUnitB = resB.roundedCostPerIndividualUnit,
            sellingPriceA = resA.roundedCustomerSellingPrice,
            sellingPriceB = resB.roundedCustomerSellingPrice,
            profitPerUnitA = resA.roundedProfitPerIndividualUnit,
            profitPerUnitB = resB.roundedProfitPerIndividualUnit,
            profitMarginA = resA.roundedProfitMargin,
            profitMarginB = resB.roundedProfitMargin,
            markupA = resA.roundedMarkup,
            markupB = resB.roundedMarkup,
            purchaseTaxA = resA.roundedPurchaseInvoice.subtract(resA.taxablePurchaseBase).abs(),
            purchaseTaxB = resB.roundedPurchaseInvoice.subtract(resB.taxablePurchaseBase).abs(),
            sellingTaxA = resA.roundedSellingTaxAmount,
            sellingTaxB = resB.roundedSellingTaxAmount,
            itcTreatmentA = if (resA.isInputTaxCreditApplied) "ITC Active" else "Cost Component",
            itcTreatmentB = if (resB.isInputTaxCreditApplied) "ITC Active" else "Cost Component",
            lowestCostWinner = hl?.lowestCostSide ?: WinnerSide.NONE,
            highestProfitWinner = hl?.highestProfitPerUnitSide ?: WinnerSide.NONE,
            highestMarginWinner = hl?.highestMarginSide ?: WinnerSide.NONE,
            primaryHighlightSummary = primarySummary,
            secondaryHighlightSummary = null,
            title = "Product Comparison: $nameA vs $nameB"
        )
    }

    /**
     * Builds a canonical report from a Supplier Comparison session.
     */
    fun fromSupplierComparison(
        outcome: ProductComparisonOutcome,
        base: SupplierComparisonBaseProduct,
        offerA: SupplierOfferSide,
        offerB: SupplierOfferSide
    ): ShareReport.SupplierComparison {
        val hl = outcome.highlight
        val resA = outcome.resultA
        val resB = outcome.resultB
        val packSize = base.unitsPerPackText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val supplierA = offerA.supplierName.ifBlank { "Supplier A" }
        val supplierB = offerB.supplierName.ifBlank { "Supplier B" }

        val primarySummary = if (hl != null && hl.costSavingsPercent != null && hl.costSavingsPercent.compareTo(BigDecimal.ZERO) > 0) {
            val cheaperName = if (hl.lowestCostSide == WinnerSide.SIDE_A) supplierA else supplierB
            "$cheaperName saves ₹${hl.costDifferencePerUnit.toPlainString()}/unit (${hl.costSavingsPercent.toPlainString()}%)"
        } else if (hl?.lowestCostSide == WinnerSide.TIE) {
            "Both supplier offers have identical cost per unit"
        } else {
            "Supplier quote comparison completed"
        }

        return ShareReport.SupplierComparison(
            productName = base.productName.ifBlank { "Base Product" },
            supplierNameA = supplierA,
            supplierNameB = supplierB,
            unitsPerPack = packSize,
            baseSellingPrice = BigDecimal(base.sellingPriceText.ifBlank { "0" }),
            baseSellingTaxRate = BigDecimal(base.sellingTaxRateText.ifBlank { "0" }),
            baseSellingPriceMode = base.sellingPriceMode,
            ptrA = BigDecimal(offerA.ptrText.ifBlank { "0" }),
            ptrB = BigDecimal(offerB.ptrText.ifBlank { "0" }),
            purchaseTaxRateA = BigDecimal(offerA.purchaseTaxRateText.ifBlank { "0" }),
            purchaseTaxRateB = BigDecimal(offerB.purchaseTaxRateText.ifBlank { "0" }),
            purchasePriceModeA = offerA.purchasePriceMode,
            purchasePriceModeB = offerB.purchasePriceMode,
            costPerPackA = resA.roundedEffectiveCostPerReceivedPack,
            costPerPackB = resB.roundedEffectiveCostPerReceivedPack,
            costPerUnitA = resA.roundedCostPerIndividualUnit,
            costPerUnitB = resB.roundedCostPerIndividualUnit,
            profitPerUnitA = resA.roundedProfitPerIndividualUnit,
            profitPerUnitB = resB.roundedProfitPerIndividualUnit,
            profitMarginA = resA.roundedProfitMargin,
            profitMarginB = resB.roundedProfitMargin,
            markupA = resA.roundedMarkup,
            markupB = resB.roundedMarkup,
            purchaseTaxA = resA.roundedPurchaseInvoice.subtract(resA.taxablePurchaseBase).abs(),
            purchaseTaxB = resB.roundedPurchaseInvoice.subtract(resB.taxablePurchaseBase).abs(),
            sellingTaxA = resA.roundedSellingTaxAmount,
            sellingTaxB = resB.roundedSellingTaxAmount,
            itcTreatmentA = if (resA.isInputTaxCreditApplied) "ITC Active" else "Cost Component",
            itcTreatmentB = if (resB.isInputTaxCreditApplied) "ITC Active" else "Cost Component",
            lowestCostWinner = hl?.lowestCostSide ?: WinnerSide.NONE,
            highestProfitWinner = hl?.highestProfitPerUnitSide ?: WinnerSide.NONE,
            highestMarginWinner = hl?.highestMarginSide ?: WinnerSide.NONE,
            primaryHighlightSummary = primarySummary,
            secondaryHighlightSummary = null,
            title = if (base.productName.isNotBlank()) "Supplier Comparison — ${base.productName}" else "Supplier Comparison Report"
        )
    }
}
