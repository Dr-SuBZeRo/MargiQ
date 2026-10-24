package com.example.profitmargincalculator.domain.model

enum class TaxPriceMode(val label: String, val shortDescription: String) {
    TAX_EXCLUSIVE("Tax-exclusive", "Price excludes tax"),
    TAX_INCLUSIVE("Tax-inclusive", "Price includes tax")
}
