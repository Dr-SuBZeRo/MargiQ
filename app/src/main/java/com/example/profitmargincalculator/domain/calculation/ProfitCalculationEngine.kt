package com.example.profitmargincalculator.domain.calculation

import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure Kotlin financial calculation engine for Indian retail, pharmacy,
 * and product pricing calculations with GST, pack sizes, and individual-unit metrics.
 *
 * Uses BigDecimal with 10 decimal digits of precision for intermediate
 * arithmetic to prevent premature rounding errors.
 */
object ProfitCalculationEngine {

    private const val CALC_SCALE = 10
    private val HUNDRED = BigDecimal("100")

    fun calculate(input: CalculationInput): CalculationResult {
        // Validation of edge cases
        if (input.ptr < BigDecimal.ZERO) {
            return CalculationResult.invalid("Purchase price (PTR) cannot be negative.")
        }
        if (input.sellingPrice < BigDecimal.ZERO) {
            return CalculationResult.invalid("Selling price (MRP) cannot be negative.")
        }
        if (input.purchaseTaxRate < BigDecimal.ZERO) {
            return CalculationResult.invalid("Purchase tax rate cannot be negative.")
        }
        if (input.sellingTaxRate < BigDecimal.ZERO) {
            return CalculationResult.invalid("Selling tax rate cannot be negative.")
        }
        if (input.quantity <= 0) {
            return CalculationResult.invalid("Purchase quantity must be at least 1.")
        }
        if (input.unitsPerPack <= 0) {
            return CalculationResult.invalid("Units per pack must be at least 1.")
        }
        if (input.sellingQuantity < 0) {
            return CalculationResult.invalid("Selling quantity cannot be negative.")
        }

        // 1. Purchase Side Calculations (Per Pack)
        val taxablePurchaseBase: BigDecimal
        val purchaseTaxAmount: BigDecimal
        val purchaseInvoiceTotal: BigDecimal

        when (input.purchasePriceMode) {
            TaxPriceMode.TAX_EXCLUSIVE -> {
                // PTR is exclusive of tax
                taxablePurchaseBase = input.ptr
                purchaseTaxAmount = if (input.purchaseTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    input.ptr.multiply(input.purchaseTaxRate).divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                }
                purchaseInvoiceTotal = taxablePurchaseBase.add(purchaseTaxAmount)
            }
            TaxPriceMode.TAX_INCLUSIVE -> {
                // PTR already includes tax
                if (input.purchaseTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    taxablePurchaseBase = input.ptr
                    purchaseTaxAmount = BigDecimal.ZERO
                    purchaseInvoiceTotal = input.ptr
                } else {
                    val rateDivisor = BigDecimal.ONE.add(
                        input.purchaseTaxRate.divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                    )
                    taxablePurchaseBase = input.ptr.divide(rateDivisor, CALC_SCALE, RoundingMode.HALF_UP)
                    purchaseTaxAmount = input.ptr.subtract(taxablePurchaseBase)
                    purchaseInvoiceTotal = input.ptr
                }
            }
        }

        // 2. Input Tax Credit (ITC) & Economic Cost (Per Pack)
        val isItcEligible = input.purchaseTaxClassification == PurchaseTaxClassification.RECOVERABLE_GST &&
                input.isInputTaxCreditEnabled

        val eligibleInputTaxCredit: BigDecimal
        val economicPurchaseCost: BigDecimal
        val calculationBasis: String

        if (isItcEligible) {
            eligibleInputTaxCredit = purchaseTaxAmount
            // Economic cost excludes recoverable GST because it is credited back
            economicPurchaseCost = taxablePurchaseBase
            calculationBasis = "Profit after excluding eligible GST credit"
        } else {
            eligibleInputTaxCredit = BigDecimal.ZERO
            // Economic cost includes full purchase tax because credit is not claimed/eligible
            economicPurchaseCost = purchaseInvoiceTotal
            calculationBasis = if (input.purchaseTaxClassification == PurchaseTaxClassification.NON_RECOVERABLE) {
                "Profit with non-recoverable tax in purchase cost"
            } else {
                "Profit with input tax credit disabled"
            }
        }

        // 3. Selling Side Calculations (Per Pack)
        val netSalesRevenue: BigDecimal
        val sellingTaxAmount: BigDecimal
        val customerSellingPrice: BigDecimal

        when (input.sellingPriceMode) {
            TaxPriceMode.TAX_EXCLUSIVE -> {
                // Selling price excludes tax; tax will be added to the customer invoice
                netSalesRevenue = input.sellingPrice
                sellingTaxAmount = if (input.sellingTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    input.sellingPrice.multiply(input.sellingTaxRate).divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                }
                customerSellingPrice = netSalesRevenue.add(sellingTaxAmount)
            }
            TaxPriceMode.TAX_INCLUSIVE -> {
                // Selling price (e.g. MRP) includes tax; output GST is derived and excluded from revenue
                if (input.sellingTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    netSalesRevenue = input.sellingPrice
                    sellingTaxAmount = BigDecimal.ZERO
                    customerSellingPrice = input.sellingPrice
                } else {
                    val rateDivisor = BigDecimal.ONE.add(
                        input.sellingTaxRate.divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                    )
                    netSalesRevenue = input.sellingPrice.divide(rateDivisor, CALC_SCALE, RoundingMode.HALF_UP)
                    sellingTaxAmount = input.sellingPrice.subtract(netSalesRevenue)
                    customerSellingPrice = input.sellingPrice
                }
            }
        }

        // 4. Profit & Core Financial Metrics (Per Pack)
        // Profit per pack = Net sales revenue - Economic purchase cost
        val profitPerPack = netSalesRevenue.subtract(economicPurchaseCost)

        // Profit margin (%) = (Profit per pack / Net sales revenue) * 100
        val profitMargin = if (netSalesRevenue.compareTo(BigDecimal.ZERO) > 0) {
            profitPerPack.divide(netSalesRevenue, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        // Markup (%) = (Profit per pack / Economic purchase cost) * 100
        val markup = if (economicPurchaseCost.compareTo(BigDecimal.ZERO) > 0) {
            profitPerPack.divide(economicPurchaseCost, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        // Cost percentage of net sales revenue (%) = (Economic purchase cost / Net sales revenue) * 100
        val costPercentage = if (netSalesRevenue.compareTo(BigDecimal.ZERO) > 0) {
            economicPurchaseCost.divide(netSalesRevenue, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        // 5. Individual Unit Calculations
        val unitsPerPackBd = BigDecimal(input.unitsPerPack)
        val costPerIndividualUnit = economicPurchaseCost.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val purchaseInvoicePerIndividualUnit = purchaseInvoiceTotal.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val customerPricePerIndividualUnit = customerSellingPrice.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val netRevenuePerIndividualUnit = netSalesRevenue.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val profitPerIndividualUnit = profitPerPack.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)

        // 6. Quantity Scaled Totals
        val purchasePacks = input.quantity
        val sellingPacks = if (input.sellingQuantity <= 0) purchasePacks else input.sellingQuantity
        val totalIndividualUnits = purchasePacks * input.unitsPerPack
        val totalIndividualUnitsSold = sellingPacks * input.unitsPerPack

        val purchaseQtyBd = BigDecimal(purchasePacks)
        val sellingQtyBd = BigDecimal(sellingPacks)

        val totalPurchaseInvoiceTotal = purchaseInvoiceTotal.multiply(purchaseQtyBd)
        val totalEconomicPurchaseCost = economicPurchaseCost.multiply(purchaseQtyBd)
        val totalEligibleInputTaxCredit = eligibleInputTaxCredit.multiply(purchaseQtyBd)

        val totalSellingCustomerTotal = customerSellingPrice.multiply(sellingQtyBd)
        val totalNetSalesRevenue = netSalesRevenue.multiply(sellingQtyBd)
        val totalOutputTax = sellingTaxAmount.multiply(sellingQtyBd)
        val totalProfit = profitPerPack.multiply(sellingQtyBd)

        // 7. Simple Cash Price Comparison (Customer Selling Price - Purchase Invoice Amount)
        val simpleCashProfitPerUnit = customerSellingPrice.subtract(purchaseInvoiceTotal)
        val simpleCashProfitMargin = if (customerSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            simpleCashProfitPerUnit.divide(customerSellingPrice, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        return CalculationResult(
            enteredPtr = input.ptr,
            purchaseTaxRate = input.purchaseTaxRate,
            taxablePurchaseBase = taxablePurchaseBase,
            purchaseTaxAmount = purchaseTaxAmount,
            purchaseInvoiceTotal = purchaseInvoiceTotal,
            eligibleInputTaxCredit = eligibleInputTaxCredit,
            economicPurchaseCost = economicPurchaseCost,

            enteredSellingPrice = input.sellingPrice,
            sellingTaxRate = input.sellingTaxRate,
            sellingTaxAmount = sellingTaxAmount,
            customerSellingPrice = customerSellingPrice,
            netSalesRevenue = netSalesRevenue,

            profitPerUnit = profitPerPack,
            profitMargin = profitMargin,
            markup = markup,
            costPercentage = costPercentage,

            packName = input.packName.ifBlank { "Pack" },
            unitName = input.unitName.ifBlank { "Unit" },
            unitsPerPack = input.unitsPerPack,
            totalIndividualUnits = totalIndividualUnits,
            sellingQuantity = sellingPacks,
            totalIndividualUnitsSold = totalIndividualUnitsSold,

            costPerIndividualUnit = costPerIndividualUnit,
            purchaseInvoicePerIndividualUnit = purchaseInvoicePerIndividualUnit,
            customerPricePerIndividualUnit = customerPricePerIndividualUnit,
            netRevenuePerIndividualUnit = netRevenuePerIndividualUnit,
            profitPerIndividualUnit = profitPerIndividualUnit,

            quantity = purchasePacks,
            totalPurchaseInvoiceTotal = totalPurchaseInvoiceTotal,
            totalEconomicPurchaseCost = totalEconomicPurchaseCost,
            totalSellingCustomerTotal = totalSellingCustomerTotal,
            totalNetSalesRevenue = totalNetSalesRevenue,
            totalOutputTax = totalOutputTax,
            totalEligibleInputTaxCredit = totalEligibleInputTaxCredit,
            totalProfit = totalProfit,

            simpleCashProfitPerUnit = simpleCashProfitPerUnit,
            simpleCashProfitMargin = simpleCashProfitMargin,

            isInputTaxCreditApplied = isItcEligible,
            calculationBasis = calculationBasis,
            isValid = true,
            validationMessage = null
        )
    }
}
