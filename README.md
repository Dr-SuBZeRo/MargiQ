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
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.1.5-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/issues"><img src="https://img.shields.io/badge/Issues-Report%20a%20bug-red?style=for-the-badge&logo=github" alt="Report a bug" /></a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
    <img src="https://img.shields.io/badge/Language-Kotlin_2.2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose" />
    <img src="https://img.shields.io/badge/Architecture-Clean_MVVM-FF6F00?style=flat-square" alt="Architecture" />
    <img src="https://img.shields.io/badge/Database-Room_SQLite_v5-2E7D32?style=flat-square&logo=sqlite&logoColor=white" alt="Room" />
    <img src="https://img.shields.io/badge/Security-Production_Signed-1565C0?style=flat-square&logo=googleplay&logoColor=white" alt="Signed Release" />
    <img src="https://img.shields.io/badge/Creator-SuBZeRo-0F766E?style=flat-square" alt="Creator" />
  </p>

</div>

---

## 📖 Overview

**MargiQ** cuts through complex wholesale pricing, supplier promotion mechanics, and multi-tier Goods and Services Tax (GST) rules in India. Designed specifically for retail pharmacies, stockists, distributors, and merchandise buyers, MargiQ provides real-time economic cost and unit profitability analysis rather than standard POS ledgering.

In **v2.1.5**, MargiQ introduces a **Premium Sharing UX Redesign** inspired by clean CashQ WhatsApp messaging, live image previews with dynamic content-driven height, professional HTML email drafts with responsive financial tables, and consistent `MargiQ` two-tone branding ("Margi" in theme text color, "Q" in emerald profit green) across all app surfaces.

Whether evaluating **Input Tax Credit (ITC)** eligibility, trade discounts, promotional bonus schemes (*Buy X, Get Y Free*), or contrasting deals between rival wholesalers, MargiQ delivers uncompromised monetary accuracy with zero internet dependencies.

<br />

## 🌟 Key Highlights & Features

<table>
  <tr>
    <td width="50%">
      <h3>💎 Real-Time Profitability Engine</h3>
      <ul>
        <li><b>Dynamic Metric Hero:</b> Instant visibility into profit per pack, unit margin (%), markup (%), and total batch profit.</li>
        <li><b>Exact Precision:</b> Built with <code>BigDecimal</code> monetary math (<code>HALF_UP</code> rounding)—zero floating-point inaccuracies.</li>
        <li><b>Cash Flow vs. Business Profit:</b> Clear separation of upfront supplier invoice disbursements from true net operating profits.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>⚔️ Commercial Comparison Suite</h3>
      <ul>
        <li><b>Dual Analytical Modes:</b> Compare two distinct products side-by-side or analyze competing wholesaler offers for the same product.</li>
        <li><b>3 Objective Winner Badges:</b> Instant visual indicators for <b>Lowest Cost Per Unit</b>, <b>Highest Profit Per Unit</b>, and <b>Highest Margin %</b>.</li>
        <li><b>Savings Callout Banner:</b> Highlight exact cash savings and batch profit uplift before committing purchase capital.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📚 Shared Product Library</h3>
      <ul>
        <li><b>Single Source of Truth:</b> Catalog reusable product pricing records with PTR, MRP, tax modes, ITC rules, and promotional schemes.</li>
        <li><b>Instant Live Search:</b> Search across product names and supplier names instantly.</li>
        <li><b>Cross-Screen Interoperability:</b> Load library products directly into the Calculator or either side of the Comparison Suite in one tap.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>⚖️ Indian GST & ITC Architecture</h3>
      <ul>
        <li><b>Tax Inclusivity Modes:</b> Independent tax-inclusive / tax-exclusive selectors for both Purchase (PTR) and Selling (MRP) pricing across all calculators and comparisons.</li>
        <li><b>Statutory Classifications:</b> Dedicated models for <b>Recoverable GST</b> (standard registered dealers) vs. <b>Non-Recoverable GST</b> (composition scheme / consumers).</li>
        <li><b>ITC Offset Toggle:</b> Automatically isolates claimable input credit from pure inventory balance-sheet costs.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>🏷️ Independent Supplier Benefits</h3>
      <ul>
        <li><b>Trade Discounts:</b> Apply percentage discounts to taxable purchase amounts prior to GST assessment.</li>
        <li><b>Buy X, Get Y Free:</b> Handles promotional bonus cycles and automatically averages economic purchase costs across all received stock.</li>
        <li><b>Fully Decoupled:</b> Toggle discount and offer mechanisms independently, simultaneously, or leave both off with zero side effects.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🎨 Streamlined Modern Android UX</h3>
      <ul>
        <li><b>Material Design 3 Drawer:</b> Clean top app bar with unified navigation drawer for Calculator, Product Library, Compare, History, and Settings.</li>
        <li><b>Indian Rupee Typography:</b> Centralized Indian digit grouping format (e.g. <code>₹1,00,000.00</code>) formatted seamlessly.</li>
        <li><b>Private Room SQLite DB:</b> 100% on-device database with zero telemetry, accounts, ads, or background tracking.</li>
      </ul>
    </td>
  </tr>
</table>

<br />

## 💡 Practical Commercial Examples & Scenarios

### Example 1: Standard Pharma Pricing with Recoverable ITC
*A registered pharmacy buys an ethical antibiotic under standard GST terms.*

```text
[Inputs]
• Purchase Price (PTR): ₹100.00 (Tax Exclusive)
• Purchase GST Rate: 12.00%
• Input Tax Credit (ITC): Enabled (Claimable)
• Selling Price (MRP): ₹150.00 (Tax Inclusive)
• Selling GST Rate: 12.00%
• Pack Configuration: 50 packs (10 tablets per pack)

[Calculated Outputs]
✔ Net Sales Revenue Per Pack:      ₹133.93  [₹150.00 / (1 + 0.12)]
✔ Economic Purchase Cost Per Pack: ₹100.00  [GST ₹12.00 credited back to cash ledger]
✔ Supplier Invoice Cash Outlay:    ₹112.00  [₹100.00 base + ₹12.00 GST disbursement]
✔ Net Profit Per Pack:             ₹33.93   [₹133.93 - ₹100.00]
✔ Profit Margin (%):               25.33%   [(₹33.93 / ₹133.93) × 100]
✔ Markup (%):                      33.93%   [(₹33.93 / ₹100.00) × 100]
✔ Total Batch Profit (50 packs):   ₹1,696.43
```

---

### Example 2: Compounded Discount + "Buy 10, Get 2 Free" Scheme
*A distributor offers a 5% trade discount plus 2 free packs for every 10 packs invoiced.*

```text
[Inputs]
• Invoiced (Paid) Quantity: 30 packs
• Deal: Buy 10, Get 2 Free
• Supplier Trade Discount: 5.00%
• PTR: ₹200.00 (Tax Exclusive) | Purchase GST: 18.00% | ITC: Enabled
• MRP: ₹300.00 (Tax Inclusive) | Selling GST: 18.00%

[Calculated Outputs]
✔ Free Bonus Packs Received:       6 packs  [⌊30/10⌋ × 2 = 6 free packs]
✔ Total Stock Received:            36 packs [30 paid + 6 free]
✔ Gross Merchandise Value:         ₹6,000.00
✔ Trade Discount (5%):             -₹300.00 [₹6,000.00 × 0.05]
✔ Taxable Purchase Base:           ₹5,700.00
✔ Effective Cost Per Received Pack:₹158.33  [₹5,700.00 / 36 packs received]
✔ Net Sales Revenue Per Pack:      ₹254.24  [₹300.00 / (1 + 0.18)]
✔ Profit Per Pack:                 ₹95.91   [₹254.24 - ₹158.33]
✔ Profit Margin (%):               37.72%   |  Markup (%): 60.57%
✔ Total Liquidated Batch Profit:   ₹3,452.63 [36 packs × ₹95.91]
```

---

### Example 3: Side-by-Side Product Comparison (Normalized Pack Sizes)
*Comparing Brand A (10 tablets/pack) against Brand B (20 tablets/economy pack).*

```text
[Side A: Brand A (10s)]                 [Side B: Brand B (20s)]
• PTR: ₹80.00 (Excl, 12% GST, ITC)      • PTR: ₹140.00 (Excl, 12% GST, ITC)
• MRP: ₹120.00 (Incl, 12% GST)          • MRP: ₹220.00 (Incl, 12% GST)
• Units per Pack: 10 tablets            • Units per Pack: 20 tablets

[Normalized Comparison Results]
• Cost Per Individual Tablet:  ₹8.00/tab             ₹7.00/tab   [Brand B is ₹1.00 cheaper/tab]
• Selling Price Per Tablet:    ₹10.71/tab            ₹9.82/tab
• Profit Per Individual Tablet:₹2.71/tab             ₹2.82/tab   [Brand B yields +₹0.11 profit/tab]
• Profit Margin (%):           25.33%                28.73%      [Brand B yields +3.40% margin]

[Commercial Winner Badges]
🏆 Lowest Cost Per Unit:    SIDE B (Brand B at ₹7.00/unit)
🏆 Highest Profit Per Unit: SIDE B (Brand B at ₹2.82/unit)
🏆 Highest Profit Margin:   SIDE B (Brand B at 28.73%)
```

---

### Example 4: Competing Supplier Offer Analysis
*Wholesaler A offers a flat 8% cash discount vs. Wholesaler B offering 3% discount + Buy 10 Get 1 Free on 50 packs of a ₹500 PTR product.*

```text
[Wholesaler A: Flat Discount]           [Wholesaler B: Discount + Bonus]
• PTR: ₹500.00 (Excl, 12% GST, ITC)     • PTR: ₹500.00 (Excl, 12% GST, ITC)
• Quantity: 50 packs                    • Quantity: 50 packs (+5 free packs = 55 total)
• Discount: 8.00%                       • Discount: 3.00% + Buy 10 Get 1 Free

[Comparative Cost Metrics]
• Total Batch Cost:            ₹23,000.00            ₹24,250.00 (Delivers 55 packs)
• Effective Cost Per Pack:     ₹460.00               ₹440.91    [Wholesaler B is ₹19.09 cheaper/pack]
• Total Units Delivered:       50 packs              55 packs

[Commercial Winner Badges]
🏆 Lowest Effective Cost:   SIDE B (Wholesaler B at ₹440.91/pack)
💰 Total Batch Savings:     ₹1,050.00 in economic inventory savings across the order
```

---

### Example 5: Persistent Shared Product Library Flow
*Centralize reusable product formulations and eliminate repetitive data entry.*

```text
1. Define in Calculator:
   Enter product details (e.g., "Paracetamol 650mg", Supplier: "Apex Pharma", PTR: ₹28.50, MRP: ₹45.00).
2. Tap "Save Product":
   Instant persistence into local Room SQLite DB (v5) with automatic name/supplier deduplication.
3. Recall in Comparison Suite:
   Open Compare Screen → Tap "Select Product" → Live search finds "Paracetamol" in milliseconds.
4. Compare Commercial Offers:
   Test competing wholesaler terms against your baseline record without mutating saved data.
5. Export to Calculator:
   Tap "Use in Calculator" to immediately load any compared offer or library product into the main calculator.
```

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
| :--- | :--- | :--- |
| **Net Sales Revenue** | $\frac{\text{Selling Price (MRP)}}{1 + \frac{\text{Selling Tax Rate}}{100}}$ | Actual funds retained per pack after paying output GST liability (when tax-inclusive). |
| **Economic Purchase Cost** | $\text{Taxable Base} + (\text{Purchase Tax} \times [1 - \text{ITC Enabled}])$ | Pure balance-sheet inventory cost (excludes recoverable tax credit). |
| **Profit Per Pack** | $\text{Net Sales Revenue} - \text{Effective Cost Per Received Pack}$ | True business profit earned per unit sold. |
| **Profit Margin (%)** | $\left( \frac{\text{Profit Per Pack}}{\text{Net Sales Revenue}} \right) \times 100$ | Earnings as a percentage of top-line net sales revenue. |
| **Markup (%)** | $\left( \frac{\text{Profit Per Pack}}{\text{Effective Cost Per Received Pack}} \right) \times 100$ | Margin of pricing above baseline acquisition cost. |
| **Total Batch Profit** | $\text{Profit Per Pack} \times \text{Total Packs Received}$ | Total projected earnings realized when all stock is liquidated. |

---

### 4. Side-by-Side Comparison Metrics

When evaluating Option A vs. Option B in the Comparison Engine:

$$\Delta \text{ Cost Per Unit} = \text{Cost Per Unit}_A - \text{Cost Per Unit}_B$$

$$\Delta \text{ Profit Per Unit} = \text{Profit Per Unit}_A - \text{Profit Per Unit}_B$$

$$\text{Batch Economic Savings} = \left| \text{Total Batch Cost}_A - \text{Total Batch Cost}_B \right|$$

$$\text{Winner Determination} = \begin{cases}
\textbf{Lowest Cost Winner:} & \min(\text{Effective Cost Per Unit}) \\
\textbf{Highest Profit Winner:} & \max(\text{Profit Per Unit}) \\
\textbf{Highest Margin Winner:} & \max(\text{Margin \%})
\end{cases}$$

> [!NOTE]
> **Statutory Notice:** MargiQ is a pricing analysis instrument. Actual GST input credit admissibility, reverse charges, and promotional taxability depend on prevailing tax notifications and distributor invoice compliance.

<br />

## 🛠️ Architecture & Tech Stack

```text
MargiQ Architecture
 ├── 📱 UI Layer (Jetpack Compose + Material 3)
 │    ├── Material 3 Navigation Drawer (Calculator, Product Library, Compare, History, Settings)
 │    ├── Comparison Suite (Side-by-side Hero Summary & Winner Badges)
 │    ├── Product Catalog Suite (Live Filterable Search, CRUD Dialogs)
 │    ├── History Selection Flow (Transient, non-mutating input loader)
 │    └── StateFlow reactive binding via ViewModel
 ├── 🧠 Domain Layer (Pure Kotlin + BigDecimal)
 │    ├── ProfitCalculationEngine (Stateless financial calculations)
 │    ├── ComparisonEngine (Dual-option commercial & offer analysis)
 │    └── IndianCurrencyFormatter (L10n currency & number grouping)
 └── 🗄️ Data Layer (Room SQLite + DataStore)
      ├── CalculationDao & CalculationEntity & ProductEntity (SQLite DB v5)
      └── UserPreferencesRepository (Preferences DataStore)
```

| Component | Technical Details |
|:---|:---|
| **Language** | Kotlin 2.2.0 |
| **UI Framework** | Jetpack Compose (BOM 2024.12.01), Material 3 |
| **Architecture** | Modern Android Architecture (MVVM + Unidirectional Data Flow) |
| **Database** | Room SQLite `2.6.1` with KSP schema auto-exporting (Schemas 1 → 5) |
| **Preferences** | Jetpack DataStore Preferences `1.1.1` |
| **Precision Math** | `java.math.BigDecimal` with `RoundingMode.HALF_UP` |
| **Security & Signing** | Release signing via local keystore (V2 Scheme) |
| **Build Tooling** | Gradle 9.1.0, Android Gradle Plugin `8.7.3`, Version Catalogs |

<br />

## 📂 Project Structure

```text
MargiQ/
├── .github/
│   └── workflows/                # CI / CD verification pipelines
├── app/
│   ├── schemas/                  # Versioned Room database schemas (1.json -> 5.json)
│   └── src/
│       ├── main/
│       │   ├── java/com/margiq/
│       │   │   ├── data/         # Room DB, entities (Calculation, Product), DAOs, repo
│       │   │   ├── domain/       # Calculation engine, comparison engine & formatters
│       │   │   └── ui/           # Screens (Calculator, Product Library, Compare, History, Settings)
│       │   └── res/              # Vector drawables, mipmaps, XML configurations
│       └── test/                 # Unit tests (math engine, comparison engine, migrations)
├── Icon/                         # Source branding and icon assets
├── gradle/
│   └── libs.versions.toml        # Unified dependency version catalog
├── build.gradle.kts              # Root build orchestration
├── gradle.properties             # JVM daemon allocation & compiler flags
├── gradlew / gradlew.bat         # Native Gradle wrapper binaries
└── README.md                     # Project documentation
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

# 2. Run unit, comparison, and database migration test suites
.\gradlew.bat test

# 3. Perform static lint inspection
.\gradlew.bat lintDebug

# 4. Compile the release APK
.\gradlew.bat assembleRelease
```

The compiled production release artifact will be generated at:
```text
app/build/outputs/apk/release/app-release.apk
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
  ✔ testCompareProductsWithDifferentTaxRatesAndSellingModes
  ✔ testCompareProductsWithItcEligibilityDifference
  ✔ testCompareSupplierOffersCorrectlyIdentifiesLowestCostOption
  ✔ testLoadCalculationIntoSideAUpdatesInputsAndRetriggersCalculation
  ✔ testLoadCalculationIntoSideBUpdatesInputsAndRetriggersCalculation
  ✔ testLoadCalculationIntoSupplierBasePopulatesInputs
  ✔ testLoadCalculationFromHistoryDoesNotMutateSavedRecord
  ✔ testMigration1To2To3To4To5OnRealSqlitePreservesExistingRecordsAndReopensCorrectly
  ... (66 unit, comparison, library & migration tests executed successfully)

BUILD SUCCESSFUL
```

<br />

## 👤 Author & Licensing

<div align="center">
  <p><b>Created by SuBZeRo</b></p>
  <a href="https://github.com/Dr-SuBZeRo"><img src="https://img.shields.io/badge/GitHub-Profile-black?style=flat-square&logo=github" alt="GitHub" /></a>
  <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/Project-MargiQ-00897B?style=flat-square" alt="MargiQ" /></a>
  
  <br /><br />
  <p><sub>Application ID: <code>com.margiq</code> | Current Release: <code>v2.1.4</code> (versionCode <code>12</code>)</sub></p>
  <p><b>MargiQ</b> &bull; <i>Know your cost. Own your margin.</i></p>
</div>
