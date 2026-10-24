::: {align="center"}
`<img src="Icon/icon.png" alt="MargiQ app icon" width="112" />`{=html}

```{=html}
<h1>
```
MargiQ
```{=html}
</h1>
```
```{=html}
<p>
```
`<strong>`{=html}Know your cost. Own your margin.`</strong>`{=html}
```{=html}
</p>
```
```{=html}
<p>
```
A native Android profit and margin calculator for product pricing,
purchase economics, GST, and supplier benefits. Built with Kotlin,
Jetpack Compose, and Material 3.
```{=html}
</p>
```
```{=html}
<p>
```
`<a href="https://github.com/Dr-SuBZeRo/MargiQ">`{=html}
`<img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="GitHub Repository" />`{=html}
`</a>`{=html}
`<a href="https://github.com/Dr-SuBZeRo/MargiQ/actions">`{=html}
`<img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions" />`{=html}
`</a>`{=html}
`<a href="https://github.com/Dr-SuBZeRo/MargiQ/issues">`{=html}
`<img src="https://img.shields.io/badge/Issues-Report%20a%20bug-red?style=for-the-badge&logo=github" alt="Report an issue" />`{=html}
`</a>`{=html}
`<a href="https://github.com/Dr-SuBZeRo/MargiQ/pulls">`{=html}
`<img src="https://img.shields.io/badge/Contribute-Pull%20requests-8957E5?style=for-the-badge&logo=github" alt="Pull requests" />`{=html}
`</a>`{=html}
```{=html}
</p>
```
```{=html}
<p>
```
`<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />`{=html}
`<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />`{=html}
`<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />`{=html}
`<img src="https://img.shields.io/badge/Version-1.2.0-0F766E?style=flat-square" alt="Version 1.2.0" />`{=html}
```{=html}
</p>
```
```{=html}
<p>
```
`<em>`{=html}Created by
`<a href="https://github.com/Dr-SuBZeRo">`{=html}SuBZeRo`</a>`{=html}`</em>`{=html}
```{=html}
</p>
```
:::

------------------------------------------------------------------------

## Overview

**MargiQ** helps you understand the real economics behind a product's
selling price. Enter your purchase price, tax settings, selling price,
and supplier benefits to see how they affect cost, profit, margin, and
markup.

Designed for product-pricing workflows---including retail, wholesale,
and pharmacy scenarios---MargiQ keeps the experience focused on
profitability rather than point-of-sale or stock management.

## At a glance

  -----------------------------------------------------------------------
  Capability                          What it does
  ----------------------------------- -----------------------------------
  Live profitability                  Recalculates financial results as
                                      inputs change

  GST-aware pricing                   Supports tax-inclusive and
                                      tax-exclusive purchase and selling
                                      prices

  Supplier offers                     Models **Buy X, Get Y Free** offers
                                      and spreads eligible purchase cost
                                      across received packs

  Supplier discounts                  Applies percentage discounts before
                                      calculating purchase GST

  Unit economics                      Calculates effective cost, revenue,
                                      and profit per individual unit

  Cash flow vs. profit                Separates supplier cash outlay and
                                      customer collection from
                                      GST-exclusive business profit

  Calculation history                 Saves, searches, reopens,
                                      duplicates, and deletes past
                                      calculations

  Personalisation                     Supports light, dark, and system
                                      theme modes, with configurable
                                      defaults

  Indian currency                     Displays INR (₹) with Indian number
                                      grouping
  -----------------------------------------------------------------------

## Core features

### Profitability at a glance

The summary highlights profit per pack, profit per individual unit,
margin, markup, and relevant totals. The detailed breakdown explains how
purchase cost, sales revenue, and tax affect the result.

### GST-aware purchase and selling prices

Configure purchase and selling prices independently:

-   **Tax-exclusive:** tax is added to the entered base price.
-   **Tax-inclusive:** the tax component is extracted from the entered
    price.
-   **Purchase GST / Input Tax Credit:** distinguish recoverable GST
    from non-recoverable purchase tax, according to the selected
    settings.
-   **Sales GST:** separate output tax collected from net business
    revenue.

### Supplier offers: Buy X, Get Y Free

Represent common purchase offers such as **Buy 1, Get 1 Free**, **Buy
10, Get 2 Free**, or **Buy 5, Get 1 Free**.

MargiQ calculates complete offer cycles and allocates the paid-goods
economic cost over the total packs received. Free packs add no purchase
consideration in the calculator's current model.

For the current calculation model:

\[ `\text{Offer cycles}`{=tex} =
`\left`{=tex}`\lfloor `{=tex}`\frac{\text{Paid quantity}}{\text{Buy quantity}}`{=tex}
`\right`{=tex}`\rfloor`{=tex} \]

\[ `\text{Free packs}`{=tex} = `\text{Offer cycles}`{=tex}
`\times `{=tex}`\text{Free quantity}`{=tex} \]

\[ `\text{Total packs received}`{=tex} = `\text{Paid packs}`{=tex} +
`\text{Free packs}`{=tex} \]

### Supplier discount

Enter a supplier discount from **0% to 100%**. The discount is applied
to paid-goods purchase value before purchase GST is calculated.

\[ `\text{Discount amount}`{=tex} =
`\text{Gross paid-goods value}`{=tex}
`\times `{=tex}`\frac{\text{Discount \%}}{100}`{=tex} \]

\[ `\text{Net taxable purchase value}`{=tex} =
`\text{Gross paid-goods value}`{=tex} - `\text{Discount amount}`{=tex}
\]

For tax-inclusive purchase prices, the calculation extracts the tax
component from the discounted inclusive value using the selected tax
rate.

### Individual-unit economics

Enter **Units per Pack** to calculate the effective cost, net revenue,
and profit per tablet, capsule, piece, or other individual unit.

\[ `\text{Effective cost per unit}`{=tex} =
`\frac{\text{Effective economic cost per received pack}}{\text{Units per pack}}`{=tex}
\]

Unit-level values are derived from pack-level calculations. MargiQ is a
profitability calculator; it does not currently model individual
loose-unit sales or operate as a POS/inventory ledger.

### Calculation history and preferences

-   Save calculations for later reference.
-   Search and sort saved records.
-   Reopen or duplicate a previous calculation.
-   Delete selected records or clear history with confirmation.
-   Choose light, dark, or system appearance.
-   Configure preferred calculation defaults.
-   Preserve saved records through versioned Room database migrations.

## Calculation model

MargiQ uses decimal arithmetic for monetary calculations and keeps
internal precision separate from displayed currency rounding.

  -----------------------------------------------------------------------
  Metric                              Meaning
  ----------------------------------- -----------------------------------
  **Net sales revenue**               Selling value excluding output GST,
                                      where applicable

  **Economic purchase cost**          Purchase cost after applying the
                                      configured discount and relevant
                                      recoverable/non-recoverable tax
                                      treatment

  **Profit per pack**                 Net sales revenue per pack minus
                                      effective economic cost per
                                      received pack

  **Profit margin**                   Profit ÷ net sales revenue × 100

  **Markup**                          Profit ÷ effective economic cost ×
                                      100

  **Supplier invoice total**          Amount payable for discounted paid
                                      goods, including applicable
                                      purchase GST

  **Effective cost per received       Economic cost allocated across paid
  pack**                              and free packs received
  -----------------------------------------------------------------------

> **Tax note:** Actual GST treatment for discounts, promotional goods,
> and free supplies can depend on applicable regulations and supplier
> invoice structure. MargiQ implements the assumptions described in its
> calculation model; it is not a substitute for professional tax advice.

## Technology stack

  Layer                   Technology
  ----------------------- -------------------------------------------------
  Language                Kotlin
  UI                      Jetpack Compose, Material 3
  Architecture            MVVM with separated UI, domain, and data layers
  State                   Kotlin Coroutines, Flow / StateFlow
  Local persistence       Room
  Preferences             Jetpack DataStore Preferences
  Monetary calculations   `BigDecimal` with explicit rounding
  Build system            Gradle Wrapper, Kotlin DSL, version catalog
  Launch branding         Android SplashScreen API

## Project structure

``` text
MargiQ/
├── app/
│   ├── schemas/                 # Versioned Room database schemas
│   └── src/
│       ├── main/
│       │   ├── java/            # Kotlin application source
│       │   └── res/             # Android resources and branding
│       ├── test/                # Unit and migration tests
│       └── androidTest/         # Instrumentation tests, if configured
├── gradle/
│   └── libs.versions.toml       # Dependency version catalog
├── .github/
│   └── workflows/               # CI workflows
├── gradlew
├── gradlew.bat
└── README.md
```

## Get the source

``` bash
git clone https://github.com/Dr-SuBZeRo/MargiQ.git
cd MargiQ
```

Open the project root in Android Studio and allow Gradle sync to finish.

### Prerequisites

-   Android Studio compatible with the project's Android Gradle Plugin.
-   JDK 17 or JDK 21, according to the project's current Gradle
    configuration.
-   Android SDK components required by the project.

### Build on Windows

``` powershell
# If required, point JAVA_HOME to your installed JDK.
$env:JAVA_HOME = "C:\Path\To\Your\JDK"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Run unit tests
.\gradlew.bat test

# Run Android lint
.\gradlew.bat lintDebug

# Build the debug APK
.\gradlew.bat assembleDebug
```

The debug APK is normally generated at:

``` text
app/build/outputs/apk/debug/app-debug.apk
```

Use the Gradle Wrapper included in the repository so the project uses
its configured Gradle version.

## Quality checks

Before submitting a change, run:

``` powershell
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Review the test and lint reports rather than assuming a successful
command launch means the checks passed. Add regression tests for changes
to tax calculations, supplier benefits, monetary rounding, and database
migrations.

## Contributing

Issues and pull requests are welcome.

1.  Check existing issues before opening a duplicate.
2.  Describe the problem or feature and the expected behavior.
3.  Keep changes focused and add tests for calculation or persistence
    changes.
4.  Run the relevant Gradle checks.
5.  Open a pull request with a concise summary and verification results.

Use the repository's **Issues** page to report bugs or propose
improvements.

::: {align="center"}
`<a href="https://github.com/Dr-SuBZeRo/MargiQ/issues">`{=html}
`<img src="https://img.shields.io/badge/Report%20a%20bug-Open%20an%20issue-E05D44?style=for-the-badge&logo=github" alt="Report a bug" />`{=html}
`</a>`{=html}
`<a href="https://github.com/Dr-SuBZeRo/MargiQ/pulls">`{=html}
`<img src="https://img.shields.io/badge/Contribute-Submit%20a%20PR-8957E5?style=for-the-badge&logo=github" alt="Contribute" />`{=html}
`</a>`{=html} `<a href="https://github.com/Dr-SuBZeRo/MargiQ">`{=html}
`<img src="https://img.shields.io/badge/Explore-MargiQ-0F766E?style=for-the-badge&logo=github" alt="Explore MargiQ" />`{=html}
`</a>`{=html}
:::

## Version information

-   **App:** MargiQ
-   **Version documented here:** 1.2.0
-   **Maintainer / creator:** [SuBZeRo](https://github.com/Dr-SuBZeRo)
-   **Repository:**
    [Dr-SuBZeRo/MargiQ](https://github.com/Dr-SuBZeRo/MargiQ)
-   **Application ID:** `com.example.profitmargincalculator`

------------------------------------------------------------------------

::: {align="center"}
**MargiQ** · *Know your cost. Own your margin.*

`<sub>`{=html}Built with care by SuBZeRo.`</sub>`{=html}
:::
