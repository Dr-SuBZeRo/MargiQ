package com.margiq.domain.calculation

import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure Kotlin financial calculation engine for MargiQ.
 * Models product profitability, supplier discounts, and Buy X Get Y Free offers
 * under Indian GST with pack-size unit economics.
 *
 * Uses BigDecimal with 10 decimal digits of precision for intermediate
 * arithmetic to prevent premature rounding errors.
 *
 * ### Supplier Benefit Semantics (MargiQ v1.2.0):
 * - Supplier Discount (%): Applied to paid goods before purchase GST is calculated.
 *   On tax-inclusive PTR, the discount is applied to the gross price before extracting
 *   the net taxable base and tax component. Free goods already have zero consideration
 *   and receive no additional discount.
 * - Buy X, Get Y Free Offers: Free packs are supplied at zero additional purchase consideration.
 *   Supplier GST is charged only on the paid quantity under the specified calculation model.
 *   Offer cycles scale with integer division: cycles = paidQuantity / buyQuantity.
 *   Free packs = cycles * freeQuantity. Total received packs = paidQuantity + freePacks.
 * - Effective Economic Purchase Cost per Received Pack: Total economic purchase cost of
 *   paid goods is spread evenly over all received packs (paid + free).
 * - GST/ITC Classification: Eligible recoverable GST is excluded from economic cost when ITC
 *   is enabled. Non-recoverable tax or disabled ITC is included in economic cost.
 * - Profit per Received Pack: Net selling revenue per pack minus effective economic cost per pack.
 *
 * Disclaimer: Actual supplier invoicing and free-goods GST treatment depend on statutory
 * regulations, invoice structures, and dealer registration.
 */
object ProfitCalculationEngine {

    private const val CALC_SCALE = 10
    private val HUNDRED = BigDecimal("100")

    fun calculate(input: CalculationInput): CalculationResult {
        // 1. Input Validation
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
        if (input.isSupplierDiscountEnabled) {
            if (input.supplierDiscountPercent < BigDecimal.ZERO || input.supplierDiscountPercent > HUNDRED) {
                return CalculationResult.invalid("Supplier discount must be between 0% and 100%.")
            }
        }
        if (input.isSupplierOfferEnabled) {
            if (input.offerBuyQuantity <= 0) {
                return CalculationResult.invalid("Buy quantity must be greater than zero when the offer is enabled.")
            }
            if (input.offerFreeQuantity < 0) {
                return CalculationResult.invalid("Free quantity cannot be negative.")
            }
        }

        // 2. Supplier Offer Cycles & Pack Quantities
        val paidPacks = input.quantity
        val offerBuyQty = if (input.isSupplierOfferEnabled) input.offerBuyQuantity else 1
        val offerFreeQty = if (input.isSupplierOfferEnabled) input.offerFreeQuantity else 0

        val offerCycles = if (input.isSupplierOfferEnabled) paidPacks / offerBuyQty else 0
        val freePacksLong: Long
        val totalPacksReceivedLong: Long
        try {
            freePacksLong = if (input.isSupplierOfferEnabled) {
                Math.multiplyExact(offerCycles.toLong(), offerFreeQty.toLong())
            } else {
                0L
            }
            totalPacksReceivedLong = Math.addExact(paidPacks.toLong(), freePacksLong)
        } catch (e: ArithmeticException) {
            return CalculationResult.invalid("Offer quantity calculation exceeds integer limits (overflow).")
        }

        if (freePacksLong > Int.MAX_VALUE || totalPacksReceivedLong > Int.MAX_VALUE) {
            return CalculationResult.invalid("Total received packs exceed maximum supported limit.")
        }

        val freePacks = freePacksLong.toInt()
        val totalPacksReceived = totalPacksReceivedLong.toInt()

        // Guard total individual units against integer overflow
        val totalIndividualUnitsLong: Long
        val totalSellingUnitsLong: Long
        try {
            totalIndividualUnitsLong = Math.multiplyExact(totalPacksReceived.toLong(), input.unitsPerPack.toLong())
            totalSellingUnitsLong = Math.multiplyExact(input.sellingQuantity.toLong(), input.unitsPerPack.toLong())
        } catch (e: ArithmeticException) {
            return CalculationResult.invalid("Quantity or pack size is too large (integer overflow).")
        }

        if (totalIndividualUnitsLong > Int.MAX_VALUE || totalSellingUnitsLong > Int.MAX_VALUE) {
            return CalculationResult.invalid("Total calculated individual units exceed maximum supported limit.")
        }
        val totalIndividualUnits = totalIndividualUnitsLong.toInt()

        // 3. Purchase Side Valuation (Paid Quantity Only)
        val paidQtyBd = BigDecimal(paidPacks)
        val grossPaidPurchaseValue = input.ptr.multiply(paidQtyBd)

        val effectiveDiscountPercent = if (input.isSupplierDiscountEnabled) input.supplierDiscountPercent else BigDecimal.ZERO
        val discountFraction = effectiveDiscountPercent.divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
        val ptrDiscountAmount = input.ptr.multiply(discountFraction)
        val discountedPtr = input.ptr.subtract(ptrDiscountAmount)
        val totalSupplierDiscountAmount = ptrDiscountAmount.multiply(paidQtyBd)

        val taxablePurchaseBasePerPaidPack: BigDecimal
        val purchaseTaxAmountPerPaidPack: BigDecimal
        val purchaseInvoicePerPaidPack: BigDecimal

        when (input.purchasePriceMode) {
            TaxPriceMode.TAX_EXCLUSIVE -> {
                taxablePurchaseBasePerPaidPack = discountedPtr
                purchaseTaxAmountPerPaidPack = if (input.purchaseTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    taxablePurchaseBasePerPaidPack.multiply(input.purchaseTaxRate).divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                }
                purchaseInvoicePerPaidPack = taxablePurchaseBasePerPaidPack.add(purchaseTaxAmountPerPaidPack)
            }
            TaxPriceMode.TAX_INCLUSIVE -> {
                if (input.purchaseTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    taxablePurchaseBasePerPaidPack = discountedPtr
                    purchaseTaxAmountPerPaidPack = BigDecimal.ZERO
                    purchaseInvoicePerPaidPack = discountedPtr
                } else {
                    val rateDivisor = BigDecimal.ONE.add(
                        input.purchaseTaxRate.divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                    )
                    taxablePurchaseBasePerPaidPack = discountedPtr.divide(rateDivisor, CALC_SCALE, RoundingMode.HALF_UP)
                    purchaseTaxAmountPerPaidPack = discountedPtr.subtract(taxablePurchaseBasePerPaidPack)
                    purchaseInvoicePerPaidPack = discountedPtr
                }
            }
        }

        val totalTaxablePurchaseBase = taxablePurchaseBasePerPaidPack.multiply(paidQtyBd)
        val totalPurchaseTaxAmount = purchaseTaxAmountPerPaidPack.multiply(paidQtyBd)
        val totalPurchaseInvoiceTotal = purchaseInvoicePerPaidPack.multiply(paidQtyBd)

        // 4. Input Tax Credit & Paid Goods Economic Purchase Cost
        val isItcEligible = input.purchaseTaxClassification == PurchaseTaxClassification.RECOVERABLE_GST &&
                input.isInputTaxCreditEnabled

        val totalEligibleInputTaxCredit: BigDecimal
        val totalEconomicPurchaseCost: BigDecimal
        val calculationBasis: String

        if (isItcEligible) {
            totalEligibleInputTaxCredit = totalPurchaseTaxAmount
            totalEconomicPurchaseCost = totalTaxablePurchaseBase
            calculationBasis = "Profit after excluding eligible GST credit"
        } else {
            totalEligibleInputTaxCredit = BigDecimal.ZERO
            totalEconomicPurchaseCost = totalPurchaseInvoiceTotal
            calculationBasis = if (input.purchaseTaxClassification == PurchaseTaxClassification.NON_RECOVERABLE) {
                "Profit with non-recoverable tax in purchase cost"
            } else {
                "Profit with input tax credit disabled"
            }
        }

        // 5. Cost Allocation to Received Packs (Spread over paid + free packs)
        val totalPacksReceivedBd = BigDecimal(totalPacksReceived)
        val effectiveCostPerReceivedPack = totalEconomicPurchaseCost.divide(totalPacksReceivedBd, CALC_SCALE, RoundingMode.HALF_UP)
        val effectiveInvoicePerReceivedPack = totalPurchaseInvoiceTotal.divide(totalPacksReceivedBd, CALC_SCALE, RoundingMode.HALF_UP)
        val eligibleInputTaxCreditPerReceivedPack = totalEligibleInputTaxCredit.divide(totalPacksReceivedBd, CALC_SCALE, RoundingMode.HALF_UP)

        val unitsPerPackBd = BigDecimal(input.unitsPerPack)
        val costPerIndividualUnit = effectiveCostPerReceivedPack.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val purchaseInvoicePerIndividualUnit = effectiveInvoicePerReceivedPack.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)

        // 6. Selling Side Calculations (Per Pack)
        val netSalesRevenue: BigDecimal
        val sellingTaxAmount: BigDecimal
        val customerSellingPrice: BigDecimal

        when (input.sellingPriceMode) {
            TaxPriceMode.TAX_EXCLUSIVE -> {
                netSalesRevenue = input.sellingPrice
                sellingTaxAmount = if (input.sellingTaxRate.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    input.sellingPrice.multiply(input.sellingTaxRate).divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                }
                customerSellingPrice = netSalesRevenue.add(sellingTaxAmount)
            }
            TaxPriceMode.TAX_INCLUSIVE -> {
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

        val customerPricePerIndividualUnit = customerSellingPrice.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)
        val netRevenuePerIndividualUnit = netSalesRevenue.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)

        // 7. Profit & Ratios per Received Pack
        val profitPerPack = netSalesRevenue.subtract(effectiveCostPerReceivedPack)
        val profitPerIndividualUnit = profitPerPack.divide(unitsPerPackBd, CALC_SCALE, RoundingMode.HALF_UP)

        val profitMargin = if (netSalesRevenue.compareTo(BigDecimal.ZERO) > 0) {
            profitPerPack.divide(netSalesRevenue, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        val markup = if (effectiveCostPerReceivedPack.compareTo(BigDecimal.ZERO) > 0) {
            profitPerPack.divide(effectiveCostPerReceivedPack, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        val costPercentage = if (netSalesRevenue.compareTo(BigDecimal.ZERO) > 0) {
            effectiveCostPerReceivedPack.divide(netSalesRevenue, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        // 8. Batch / Sales Totals
        // Support decoupled selling quantity if explicitly passed as 0 or different, otherwise batch total = total packs received
        val isZeroSelling = input.sellingQuantity == 0 && input.quantity > 0
        val isExplicitDecoupled = input.sellingQuantity > 0 && input.sellingQuantity != input.quantity && !input.isSupplierOfferEnabled

        val effectiveSellingPacks = when {
            isZeroSelling -> 0
            isExplicitDecoupled -> input.sellingQuantity
            else -> totalPacksReceived
        }

        val effectiveSellingQtyBd = BigDecimal(effectiveSellingPacks)

        val totalSellingCustomerTotal: BigDecimal
        val totalNetSalesRevenue: BigDecimal
        val totalOutputTax: BigDecimal
        val totalProfit: BigDecimal
        val totalIndividualUnitsSold: Int
        val remainingPacks: Int
        val remainingIndividualUnits: Int

        if (isZeroSelling) {
            totalSellingCustomerTotal = BigDecimal.ZERO
            totalNetSalesRevenue = BigDecimal.ZERO
            totalOutputTax = BigDecimal.ZERO
            totalProfit = BigDecimal.ZERO
            totalIndividualUnitsSold = 0
            remainingPacks = totalPacksReceived
            remainingIndividualUnits = totalIndividualUnits
        } else if (isExplicitDecoupled) {
            totalSellingCustomerTotal = customerSellingPrice.multiply(effectiveSellingQtyBd)
            totalNetSalesRevenue = netSalesRevenue.multiply(effectiveSellingQtyBd)
            totalOutputTax = sellingTaxAmount.multiply(effectiveSellingQtyBd)
            totalProfit = profitPerPack.multiply(effectiveSellingQtyBd)
            val soldUnitsLong = Math.multiplyExact(effectiveSellingPacks.toLong(), input.unitsPerPack.toLong())
            totalIndividualUnitsSold = soldUnitsLong.toInt()
            remainingPacks = totalPacksReceived - effectiveSellingPacks
            remainingIndividualUnits = totalIndividualUnits - totalIndividualUnitsSold
        } else {
            totalSellingCustomerTotal = customerSellingPrice.multiply(totalPacksReceivedBd)
            totalNetSalesRevenue = netSalesRevenue.multiply(totalPacksReceivedBd)
            totalOutputTax = sellingTaxAmount.multiply(totalPacksReceivedBd)
            totalProfit = profitPerPack.multiply(totalPacksReceivedBd)
            totalIndividualUnitsSold = totalIndividualUnits
            remainingPacks = 0
            remainingIndividualUnits = 0
        }

        // 9. Simple Cash Price Comparison
        val simpleCashProfitPerUnit = customerSellingPrice.subtract(effectiveInvoicePerReceivedPack)
        val simpleCashProfitMargin = if (customerSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            simpleCashProfitPerUnit.divide(customerSellingPrice, CALC_SCALE, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            null
        }

        return CalculationResult(
            enteredPtr = input.ptr,
            purchaseTaxRate = input.purchaseTaxRate,
            taxablePurchaseBase = taxablePurchaseBasePerPaidPack,
            purchaseTaxAmount = purchaseTaxAmountPerPaidPack,
            purchaseInvoiceTotal = purchaseInvoicePerPaidPack,
            eligibleInputTaxCredit = eligibleInputTaxCreditPerReceivedPack,
            economicPurchaseCost = effectiveCostPerReceivedPack,

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
            sellingQuantity = effectiveSellingPacks,
            totalIndividualUnitsSold = totalIndividualUnitsSold,
            remainingIndividualUnits = remainingIndividualUnits,
            remainingPacks = remainingPacks,

            costPerIndividualUnit = costPerIndividualUnit,
            purchaseInvoicePerIndividualUnit = purchaseInvoicePerIndividualUnit,
            customerPricePerIndividualUnit = customerPricePerIndividualUnit,
            netRevenuePerIndividualUnit = netRevenuePerIndividualUnit,
            profitPerIndividualUnit = profitPerIndividualUnit,

            quantity = paidPacks,
            totalPurchaseInvoiceTotal = totalPurchaseInvoiceTotal,
            totalEconomicPurchaseCost = totalEconomicPurchaseCost,
            totalSellingCustomerTotal = totalSellingCustomerTotal,
            totalNetSalesRevenue = totalNetSalesRevenue,
            totalOutputTax = totalOutputTax,
            totalEligibleInputTaxCredit = totalEligibleInputTaxCredit,
            totalProfit = totalProfit,

            supplierName = input.supplierName,
            isSupplierDiscountEnabled = input.isSupplierDiscountEnabled,
            isSupplierOfferEnabled = input.isSupplierOfferEnabled,
            offerBuyQuantity = offerBuyQty,
            offerFreeQuantity = offerFreeQty,
            freePacksReceived = freePacks,
            totalPacksReceived = totalPacksReceived,
            supplierDiscountPercent = effectiveDiscountPercent,
            supplierDiscountAmount = totalSupplierDiscountAmount,
            grossPaidPurchaseValue = grossPaidPurchaseValue,
            effectiveCostPerReceivedPack = effectiveCostPerReceivedPack,
            effectiveInvoicePerReceivedPack = effectiveInvoicePerReceivedPack,

            simpleCashProfitPerUnit = simpleCashProfitPerUnit,
            simpleCashProfitMargin = simpleCashProfitMargin,

            isInputTaxCreditApplied = isItcEligible,
            calculationBasis = calculationBasis,
            isValid = true,
            validationMessage = null
        )
    }
}
