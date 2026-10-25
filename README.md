<div align="center">

  <img src="Icon/icon.png" alt="MargiQ App Icon" width="128" height="128" style="border-radius: 28px; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);" />

  # MargiQ

  ### <i>Know your cost. Own your margin.</i>

  <p align="center">
    <b>A high-precision, native Android profit and margin intelligence tool for pharmaceuticals, retail, and wholesale distribution in India.</b>
  </p>

  <p align="center">
    <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="Repository" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.0.1-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/issues"><img src="https://img.shields.io/badge/Issues-Report%20a%20bug-red?style=for-the-badge&logo=github" alt="Report a bug" /></a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
    <img src="https://img.shields.io/badge/Language-Kotlin_2.2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose" />
    <img src="https://img.shields.io/badge/Architecture-Clean_MVVM-FF6F00?style=flat-square" alt="Architecture" />
    <img src="https://img.shields.io/badge/Database-Room_v4-2E7D32?style=flat-square&logo=sqlite&logoColor=white" alt="Room" />
    <img src="https://img.shields.io/badge/Creator-SuBZeRo-0F766E?style=flat-square" alt="Creator" />
  </p>

</div>

---

## 📖 Overview

**MargiQ** cuts through complex wholesale pricing, supplier promotion mechanics, and multi-tier Goods and Services Tax (GST) rules in India. Designed specifically for retail pharmacies, stockists, distributors, and merchandise buyers, MargiQ provides real-time economic cost and unit profitability analysis rather than standard POS ledgering.

Whether dealing with **Input Tax Credit (ITC)** eligibility, trade discounts, or promotional bonus schemes (*Buy X, Get Y Free*), MargiQ delivers uncompromised accuracy with zero internet dependencies.

<br />

## 🌟 Key Highlights & Features

<table>
  <tr>
    <td width="50%">
      <h3>💎 Real-Time Profitability Engine</h3>
      <ul>
        <li><b>Dynamic Metric Hero:</b> Instant visibility into profit per pack, unit margin (%), markup (%), and total batch profit.</li>
        <li><b>Exact Precision:</b> Built with <code>BigDecimal</code> monetary math—zero floating-point inaccuracies.</li>
        <li><b>Cash Flow vs. Business Profit:</b> Clear separation of upfront supplier invoice disbursements from true net operating profits.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📦 Consolidated Product Details</h3>
      <ul>
        <li><b>Unified Input Card:</b> Product name, optional supplier identification, units per pack, supplier discounts, and promotional offers unified in one compact view.</li>
        <li><b>Wholesaler Traceability:</b> Associate purchases with specific distributors or agencies without altering calculation values.</li>
        <li><b>Sub-Unit Economics:</b> Instant breakdown of cost and margin down to the individual tablet, capsule, or millilitre.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>⚖️ Indian GST & ITC Architecture</h3>
      <ul>
        <li><b>Tax Inclusivity Modes:</b> Independent tax-inclusive / tax-exclusive selectors for both Purchase (PTR) and Selling (MRP) pricing.</li>
        <li><b>Statutory Classifications:</b> Dedicated models for <b>Recoverable GST</b> (standard registered dealers) vs. <b>Non-Recoverable GST</b> (composition scheme / consumers).</li>
        <li><b>ITC Offset Toggle:</b> Automatically isolates claimable input credit from pure inventory balance-sheet costs.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🏷️ Independent Supplier Benefits</h3>
      <ul>
        <li><b>Trade Discounts:</b> Apply percentage discounts to taxable purchase amounts prior to GST assessment.</li>
        <li><b>Buy X, Get Y Free:</b> Handles promotional bonus cycles and automatically averages economic purchase costs across all received stock.</li>
        <li><b>Fully Decoupled:</b> Toggle discount and offer mechanisms independently, simultaneously, or leave both off with zero side effects.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>💾 Local Storage & History</h3>
      <ul>
        <li><b>Private Room v4 DB:</b> 100% on-device SQLite database with zero telemetry, accounts, or analytics.</li>
        <li><b>Deep Search & Recall:</b> Search saved calculations instantly across both product name and supplier name.</li>
        <li><b>Non-Destructive Migrations:</b> Fully tested, automated migrations from schema v1 through v4 without data loss.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🎨 Modern Android Experience</h3>
      <ul>
        <li><b>Material Design 3:</b> Dynamic emerald/teal theme with complete light, dark, and system-adaptive modes.</li>
        <li><b>Native Splash API:</b> Modern, instantaneous startup screen styled for Android 12+.</li>
        <li><b>Native INR Formatting:</b> Standard Indian digit grouping (e.g. <code>₹1,00,000.00</code>) formatted centrally.</li>
      </ul>
    </td>
  </tr>
</table>

<br />

## 📐 Mathematical Formulation

### 1. Supplier Promotion Cycles: Buy X, Get Y Free

When a wholesaler provides complimentary stock at zero invoice consideration, MargiQ determines whole promotion cycles and distributes the economic cash outlay over the aggregate volume received:

$$\text{Offer Cycles} = \left\lfloor \frac{\text{Paid Quantity}}{\text{Buy Quantity } (X)} \right\rfloor$$

$$\text{Free Packs Received} = \text{Offer Cycles} \times \text{Free Quantity } (Y)$$

$$\text{Total Packs Received} = \text{Paid Quantity} + \text{Free Packs Received}$$

$$\text{Effective Cost Per Received Pack} = \frac{\text{Total Economic Purchase Cost}}{\text{Total Packs Received}}$$

$$\text{Cost Per Individual Unit} = \frac{\text{Effective Cost Per Received Pack}}{\text{Units Per Pack}}$$

---

### 2. Supplier Trade Discount Engine

Supplier percentage discounts are computed directly on the gross paid merchandise value prior to the calculation of purchase tax:

$$\text{Gross Paid Value} = \text{Purchase Price (PTR)} \times \text{Paid Quantity}$$

$$\text{Discount Amount} = \text{Gross Paid Value} \times \left(\frac{\text{Supplier Discount \%}}{100}\right)$$

$$\text{Taxable Purchase Base} = \text{Gross Paid Value} - \text{Discount Amount}$$

$$\text{Purchase GST} = \text{Taxable Purchase Base} \times \left(\frac{\text{Purchase Tax Rate \%}}{100}\right)$$

$$\text{Supplier Invoice Total} = \text{Taxable Purchase Base} + \text{Purchase GST}$$

---

### 3. Financial Metrics & Margin Breakdown

| Metric | Mathematical Derivation | Practical Commercial Meaning |
|:---|:---|:---|
| **Net Sales Revenue** | $\frac{\text{Selling Price (MRP)}}{1 + \frac{\text{Selling Tax Rate}}{100}}$ | Actual funds retained per pack after paying output GST liability. |
| **Economic Purchase Cost** | $\text{Taxable Base} + (\text{Purchase Tax} \times [1 - \text{ITC Enabled}])$ | Pure balance-sheet inventory cost (excludes recoverable tax credit). |
| **Profit Per Pack** | $\text{Net Sales Revenue} - \text{Effective Cost Per Received Pack}$ | True business profit earned per unit sold. |
| **Profit Margin (%)** | $\left( \frac{\text{Profit Per Pack}}{\text{Net Sales Revenue}} \right) \times 100$ | Earnings as a percentage of top-line net sales revenue. |
| **Markup (%)** | $\left( \frac{\text{Profit Per Pack}}{\text{Effective Cost Per Received Pack}} \right) \times 100$ | Margin of pricing above baseline acquisition cost. |
| **Total Batch Profit** | $\text{Profit Per Pack} \times \text{Total Packs Received}$ | Total projected earnings realized when all stock is liquidated. |

> [!NOTE]
> **Statutory Notice:** MargiQ is a pricing analysis instrument. Actual GST input credit admissibility, reverse charges, and promotional taxability depend on prevailing tax notifications and distributor invoice compliance.

<br />

## 🛠️ Architecture & Tech Stack

```text
MargiQ Architecture
 ├── 📱 UI Layer (Jetpack Compose + Material 3)
 │    ├── StateFlow reactive binding via ViewModel
 │    └── Unified single-activity architecture
 ├── 🧠 Domain Layer (Pure Kotlin + BigDecimal)
 │    ├── ProfitCalculationEngine (Stateless financial logic)
 │    └── IndianCurrencyFormatter (L10n currency & number grouping)
 └── 🗄️ Data Layer (Room + DataStore)
      ├── CalculationDao & CalculationEntity (SQLite DB v4)
      └── UserPreferencesRepository (Preferences DataStore)
```

| Component | Technical Details |
|:---|:---|
| **Language** | Kotlin 2.2.0 |
| **UI Framework** | Jetpack Compose (BOM 2024.12.01), Material 3 |
| **Architecture** | Modern Android Architecture (MVVM + Unidirectional Data Flow) |
| **Database** | Room SQLite `2.6.1` with KSP schema auto-exporting |
| **Preferences** | Jetpack DataStore Preferences `1.1.1` |
| **Precision Math** | `java.math.BigDecimal` with `RoundingMode.HALF_UP` |
| **Build Tooling** | Gradle 9.1.0, Android Gradle Plugin `8.7.3`, Version Catalogs |

<br />

## 📂 Project Structure

```text
MargiQ/
├── .github/
│   └── workflows/                # CI / CD verification pipelines
├── app/
│   ├── schemas/                   # Versioned Room database schemas (1.json -> 4.json)
│   └── src/
│       ├── main/
│       │   ├── java/com/margiq/
│       │   │   ├── data/          # Room DB, entities, DAOs, and preferences
│       │   │   ├── domain/        # Calculation engine & currency formatters
│       │   │   └── ui/            # Compose screens, components, theme, and viewmodels
│       │   └── res/               # Vector drawables, mipmaps, XML configurations
│       └── test/                  # Unit tests (math engine, viewmodels, DB migrations)
├── Icon/                          # Source branding and icon assets
├── gradle/
│   └── libs.versions.toml         # Unified dependency version catalog
├── build.gradle.kts               # Root build orchestration
├── gradle.properties              # JVM daemon allocation & compiler flags
├── gradlew / gradlew.bat          # Native Gradle wrapper binaries
└── README.md                      # Project documentation
```

<br />

## 🚀 Getting Started

### Prerequisites

- **Android Studio:** Ladybug (2024.2.1+) or newer
- **Java Development Kit:** JDK 17 or JDK 21 (Amazon Corretto / Eclipse Temurin recommended)
- **Android SDK:** Platform 36 (Android 16), Build Tools 36.0.0
- **Minimum OS Support:** Android 8.0 (Oreo, API Level 26)

### 1. Clone the Source

```bash
git clone https://github.com/Dr-SuBZeRo/MargiQ.git
cd MargiQ
```

### 2. Building on Windows (PowerShell)

```powershell
# 1. Set JDK home (if not globally configured)
$env:JAVA_HOME = "C:\Users\<User>\.jdks\corretto-21.0.5"

# 2. Run unit and database migration test suites
.\gradlew.bat test

# 3. Perform static lint inspection
.\gradlew.bat lintDebug

# 4. Compile the debug APK
.\gradlew.bat assembleDebug
```

The compiled release artifact will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

<br />

## 🧪 Quality Assurance & Test Verification

All modifications adhere to rigorous test-driven validation:

```text
> Task :app:testDebugUnitTest
  ✔ testInitialUiStateMatchesDefaultReferenceValues
  ✔ testSupplierOfferToggleAndInputsUpdateLiveCalculation
  ✔ testDisabledSupplierDiscountIgnoresStoredPercentage
  ✔ testBothSupplierDiscountAndOfferOperateIndependentlyAndSimultaneously
  ✔ testMigration1To2To3To4OnRealSqlitePreservesExistingRecordsAndReopensCorrectly
  ✔ testSupplierDiscount_10PercentDiscountOnTaxExclusivePTR
  ✔ testSupplierDiscount_TaxInclusivePTR_CorrectTaxExtraction
  ✔ testSupplierDiscount_100PercentDiscountHandledWithoutCrashOrDivisionByZero
  ... (48 unit & regression tests executed successfully)

BUILD SUCCESSFUL in 47s
```

<br />

## 👤 Author & Licensing

<div align="center">
  <p><b>Created by SuBZeRo</b></p>
  <a href="https://github.com/Dr-SuBZeRo"><img src="https://img.shields.io/badge/GitHub-Profile-black?style=flat-square&logo=github" alt="GitHub" /></a>
  <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/Project-MargiQ-00897B?style=flat-square" alt="MargiQ" /></a>
  
  <br /><br />
  <p><sub>Application ID: <code>com.margiq</code> | Current Release: <code>v2.0.1</code> (versionCode <code>7</code>)</sub></p>
  <p><b>MargiQ</b> &bull; <i>Know your cost. Own your margin.</i></p>
</div>
