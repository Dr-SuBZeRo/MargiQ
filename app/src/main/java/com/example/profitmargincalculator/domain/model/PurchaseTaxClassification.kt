package com.example.profitmargincalculator.domain.model

enum class PurchaseTaxClassification(val label: String, val description: String) {
    RECOVERABLE_GST("Recoverable GST", "Eligible for Input Tax Credit (ITC) under GST"),
    NON_RECOVERABLE("Non-recoverable Tax", "Taxes & duties added as economic cost")
}
