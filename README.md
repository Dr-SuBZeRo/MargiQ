![MargiQ app icon](Icon/icon.png)

# MargiQ

**Know your cost. Own your margin.**

A native Android profit and margin calculator for product pricing, GST, and supplier benefits.

[![GitHub Repository](https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github)](https://github.com/Dr-SuBZeRo/MargiQ)
[![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/Dr-SuBZeRo/MargiQ/actions)
[![Report a bug](https://img.shields.io/badge/Issues-Report%20a%20bug-red?style=for-the-badge&logo=github)](https://github.com/Dr-SuBZeRo/MargiQ/issues)
[![Pull requests](https://img.shields.io/badge/Contribute-Pull%20requests-8957E5?style=for-the-badge&logo=github)](https://github.com/Dr-SuBZeRo/MargiQ/pulls)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Creator](https://img.shields.io/badge/Creator-SuBZeRo-0F766E?style=flat-square)

---

## Overview

**MargiQ** helps you understand the economics behind a product's selling price. Enter purchase and selling prices, tax settings, and supplier benefits to calculate effective cost, profit, margin, and markup.

Built for product-pricing workflows—including retail, wholesale, and pharmacy scenarios—MargiQ focuses on profitability rather than point-of-sale or stock management.

## Features

- **Live profitability summary** — profit per pack and individual unit, margin, markup, and cost percentage.
- **GST-aware pricing** — tax-inclusive and tax-exclusive purchase and selling prices.
- **Input Tax Credit settings** — account for recoverable and non-recoverable purchase GST according to selected settings.
- **Supplier offers** — model *Buy X, Get Y Free* promotions and allocate purchase economics across packs received.
- **Supplier discounts** — apply percentage discounts to paid-goods value before purchase GST is calculated.
- **Unit economics** — calculate effective cost and profit per tablet, capsule, piece, or other unit.
- **Cash flow vs. business profit** — distinguish supplier outlay and customer collection from GST-exclusive business profit.
- **Calculation history** — save, search, sort, reopen, duplicate, and delete past calculations.
- **Theme options** — light, dark, and system appearance.
- **Local persistence** — Room database and versioned migrations for saved calculations.
- **INR formatting** — Indian rupee symbol and number grouping.

## Supplier offers: Buy X, Get Y Free

Configure offers such as **Buy 1, Get 1 Free**, **Buy 10, Get 2 Free**, or **Buy 5, Get 1 Free**.

The calculation model uses complete offer cycles:

```text
Offer cycles = floor(Paid quantity / Buy quantity)
Free packs = Offer cycles × Free quantity
Packs received = Paid packs + Free packs
```

Free packs add no purchase consideration in the calculator's current model. The effective economic cost is allocated over the total packs received, so the offer can improve estimated profit per pack.

## Supplier discounts

A supplier discount is applied to paid-goods purchase value before purchase GST:

```text
Discount amount = Gross paid-goods value × Discount percentage
Net taxable value = Gross paid-goods value − Discount amount
Purchase GST = Net taxable value × applicable GST rate
```

For tax-inclusive purchase prices, the tax component is extracted from the discounted inclusive amount using the configured rate.

## GST and profitability model

MargiQ supports tax-inclusive and tax-exclusive purchase and selling prices, plus purchase-tax classification and Input Tax Credit settings.

| Metric | Meaning |
|---|---|
| **Net sales revenue** | Selling value excluding output GST, where applicable |
| **Economic purchase cost** | Purchase cost after discount and the configured recoverable/non-recoverable tax treatment |
| **Profit per pack** | Net sales revenue per pack minus effective economic cost per received pack |
| **Profit margin** | Profit divided by net sales revenue, multiplied by 100 |
| **Markup** | Profit divided by effective economic cost, multiplied by 100 |
| **Supplier invoice total** | Amount payable for discounted paid goods, including applicable purchase GST |
| **Effective cost per received pack** | Economic cost allocated across paid and free packs received |

> **Tax note:** Actual GST treatment for discounts, promotional goods, and free supplies can depend on applicable regulations and supplier invoice structure. MargiQ implements the assumptions defined by its calculation model and is not a substitute for professional tax advice.

## Individual-unit calculations

Enter **Units per Pack** to derive effective cost and profit per individual unit.

```text
Effective cost per unit = Effective economic cost per received pack / Units per pack
```

Unit values are derived from pack-level calculations. MargiQ is a profitability calculator; it does not currently model loose-unit sales as separate transactions or operate as a POS/inventory ledger.

## Technology stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM with separated UI, domain, and data layers |
| Asynchronous state | Kotlin Coroutines and Flow |
| Local database | Room |
| Preferences | Jetpack DataStore |
| Monetary calculations | `BigDecimal` with explicit rounding |
| Build system | Gradle Wrapper, Kotlin DSL, version catalog |
| CI | GitHub Actions, if enabled in the repository |

## Project structure

```text
MargiQ/
├── .github/
│   └── workflows/          # CI workflows
├── app/
│   ├── schemas/             # Exported Room database schemas
│   └── src/
│       ├── main/
│       │   ├── java/        # Kotlin application source
│       │   └── res/         # Android resources
│       ├── test/            # Unit tests
│       └── androidTest/     # Instrumentation tests, if configured
├── gradle/
│   └── libs.versions.toml   # Dependency version catalog
├── gradlew
├── gradlew.bat
└── README.md
```

## Get started

### Clone the repository

```bash
git clone https://github.com/Dr-SuBZeRo/MargiQ.git
cd MargiQ
```

Open the project root in Android Studio and allow Gradle sync to complete.

### Requirements

- Android Studio compatible with the project's Android Gradle Plugin.
- JDK 17 or JDK 21, as supported by the checked-out project configuration.
- Android SDK components required by the project.

### Build on Windows

```powershell
# Set JAVA_HOME if your environment needs it.
$env:JAVA_HOME = "C:\Path\To\Your\JDK"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Unit tests
.\gradlew.bat test

# Android lint
.\gradlew.bat lintDebug

# Debug APK
.\gradlew.bat assembleDebug
```

The debug APK is normally created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Use the included Gradle Wrapper to build with the project's configured Gradle version.

## Quality checks

Before submitting a change, run:

```powershell
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Review the test and lint reports. For changes to financial calculations, add regression tests covering tax modes, supplier discounts, free-goods offers, decimal precision, invalid input, and database migrations.

## Contributing

Issues and pull requests are welcome.

1. Search existing issues before creating a duplicate.
2. Describe the problem or feature request clearly.
3. Keep pull requests focused and include tests for calculation or persistence changes.
4. Run the relevant Gradle checks.
5. Include verification results in the pull request description.

[**Report a bug**](https://github.com/Dr-SuBZeRo/MargiQ/issues) · [**Open pull requests**](https://github.com/Dr-SuBZeRo/MargiQ/pulls) · [**Browse the source**](https://github.com/Dr-SuBZeRo/MargiQ)

## Project information

- **Project:** MargiQ
- **Tagline:** Know your cost. Own your margin.
- **Creator:** [SuBZeRo](https://github.com/Dr-SuBZeRo)
- **Repository:** [Dr-SuBZeRo/MargiQ](https://github.com/Dr-SuBZeRo/MargiQ)
- **Application ID:** `com.margiq`

---

**MargiQ** · *Know your cost. Own your margin.*

*Built with care by SuBZeRo.*
