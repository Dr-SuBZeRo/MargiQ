# MargiQ (Native Android)
> **Know your cost. Own your margin.**  
> *Created by SuBZeRo* • **Version 1.1.1**

A complete, production-grade native Android application for retail, wholesale, pharmacy, and product-pricing calculations in India. Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room Database** (with automatic backward-compatible schema migrations), and **DataStore Preferences**, following modern Android architecture and clean MVVM principles.

---

## 📱 Features

### 1. Pack & Individual Unit Profitability
- **Pack Name & Selling Unit**: Support for any packaging type (e.g., *Strip*, *Box*, *Bottle*, *Pack*, *Vial*, *Carton*, or custom entry).
- **Individual Unit Name**: Precise unit specification (e.g., *Tablet*, *Capsule*, *Piece*, *ml*, *g*, *Sachet*, or custom entry).
- **Units per Pack**: Manually editable positive integer (e.g., 10, 15, 30, 100).
- **Purchase & Selling Quantities**: Decoupled purchase quantity (packs purchased) and selling quantity (packs sold) for tracking inventory and realized profits.
- **Zero Packs Sold Support**: Setting selling quantity to 0 represents 0 packs sold (₹0 customer collection, ₹0 net revenue, ₹0 realized profit, with 100% of purchased inventory remaining).
- **Dual Profit Breakdown**:
  - **Per Pack**: Economic purchase cost, net sales revenue, profit per pack, margin (%), and markup (%).
  - **Per Individual Unit**: Cost per unit, customer price per unit, net revenue per unit, and profit per unit.
  - **Totals**: Total individual units purchased, total units sold, remaining inventory, total invoice, total customer collection, and realized profit.
- **Live Recalculation**: Adjusting units per pack or quantities updates all per-unit and total figures instantaneously without losing state.

> [!NOTE]
> **Pack-Based Sales Scope & Inventory Limitation**:  
> MargiQ calculates per-unit economics (cost, selling price, net revenue, and profit per tablet/piece) by dividing whole-pack economics by `unitsPerPack`. Sales transactions and inventory tracking are modeled at the whole-pack level (Packs Purchased vs. Packs Sold). Partial-pack loose-unit sales (e.g. selling 3 individual loose tablets from an opened blister pack) are outside the current release scope and are not modeled as fractional packs. All multi-pack totals scale with whole packs sold.

### 2. Comprehensive Tax & Pricing Engine
- **Live Financial Calculation**: Automatically calculates profit, margins, taxes, and economic costs as you type — no manual calculate button needed.
- **Purchase Side (PTR)**:
  - Enter Purchase Price (PTR).
  - Tax-exclusive vs. Tax-inclusive price mode toggle.
  - Purchase GST rate (%) with quick preset chips (0%, 5%, 12%, 18%, 28%).
  - Tax Classification: **Recoverable GST** (ITC eligible) vs. **Non-recoverable Tax / Expense**.
  - **Input Tax Credit (ITC)** toggle: cleanly excludes eligible purchase GST from economic purchase cost while tracking cash paid to supplier.
- **Selling Side (MRP)**:
  - Enter Selling Price (MRP).
  - Tax-inclusive (standard Indian MRP) vs. Tax-exclusive price mode toggle.
  - Selling GST rate (%) with quick preset chips (0%, 5%, 12%, 18%, 28%).
  - Output GST extraction: collected tax is excluded from business revenue.

### 3. Financial Dashboard & Insights
- **Hero Card**: Displays Profit Per Pack, Per-Unit Profit Callout, Profit Margin (%), Markup (%), Total Profit, and Pack Details with color coding (Green for profit, Red for loss, Neutral for break-even).
- **Line-Item Financial Breakdown**: Detailed purchase base, taxes, customer price, net sales revenue, margin/markup ratios, and multi-pack totals.
- **Cash Flow vs. Business Profit Comparison**: Explicitly distinguishes between raw cash difference (`MRP - Invoice = Cash Difference`) and GST-exclusive business profit so users are never misled.

### 4. Calculation History & Room Database Safety (v2)
- **Non-Destructive Room Migration (v1 → v2)**: Preserves all historical calculations while adding pack and unit fields with safe defaults (`Pack`, `Unit`, `1`, `1`, `'0.00'`, `'0.00'`, `'0.00'`).
- **Destructive Fallback Disabled**: Strict migration enforcement ensures existing user databases are never wiped on version upgrades.
- **Schema Export Enabled**: Versioned schema JSON files committed to `app/schemas/` for complete auditability.
- **Search & Sort**: Search calculations by product name; sort by newest or oldest.
- **Reopen & Duplicate**: One-tap reopen into the main calculator with live recalculation; duplicate calculations for rapid price testing.
- **Manage History**: Delete individual records or clear all history with confirmation dialogs.

### 5. Settings, Theming & Identity
- **Dynamic Version & Creator Footer**: Professional, understated footer displaying:
  ```
  MargiQ
  Created by SuBZeRo
  Version 1.1.1
  ```
  Reads the version dynamically from `BuildConfig.VERSION_NAME`.
- **Theme Selection**: **Follow System**, **Light Theme**, **Dark Theme**.
- **Dynamic Color**: Material You dynamic color theming on Android 12+ with branded deep teal-green and metallic gold fallback.
- **Custom Calculator Defaults**: Set preferred default tax rates, modes, and quantities.
- **Regional Formatting**: Indian Numbering System (Lakhs & Crores grouping) with ₹ (INR).
- **Branded App Icon**: Official MargiQ launcher icon featuring deep teal-green background, cream and metallic gold calculator with Rupee symbol, ascending green growth bars, gold upward arrow, percentage symbol, and adaptive icon variants.

---

## 📐 Mathematical Formulation & Mandatory Section 5 Verification

### Mathematical Rules
Let:
- $P$ = number of whole packs purchased ($P \ge 1$)
- $S$ = number of whole packs sold ($S \ge 0$)
- $U$ = individual units per pack ($U \ge 1$)
- $C$ = economic purchase cost per pack
- $R$ = net sales revenue per pack
- $F$ = profit per pack ($R - C$)

Then:
$$\text{Total Individual Units Purchased} = P \times U$$
$$\text{Total Individual Units Sold} = S \times U$$
$$\text{Remaining Individual Units} = (P \times U) - (S \times U)$$
$$\text{Remaining Whole Packs} = P - S$$
$$\text{Cost Per Individual Unit} = \frac{C}{U}$$
$$\text{Net Revenue Per Individual Unit} = \frac{R}{U}$$
$$\text{Profit Per Individual Unit} = \frac{F}{U}$$
$$\text{Total Purchase Cost} = P \times C$$
$$\text{Total Realized Profit} = \begin{cases} S \times F & \text{if } S > 0 \\ 0 & \text{if } S = 0 \end{cases}$$
$$\text{Profit Margin (\%)} = \frac{F}{R} \times 100 \quad (R > 0)$$
$$\text{Markup (\%)} = \frac{F}{C} \times 100 \quad (C > 0)$$

### Mandatory Section 5 Regression Test Results
- **Inputs**:
  - Purchase Price per Strip: ₹48.00 (Tax-exclusive, 5% GST, Recoverable GST, ITC Enabled)
  - Selling Price per Strip: ₹135.00 (Tax-inclusive, 5% GST)
  - Pack Name: Strip, Individual Unit: Tablet, Units per pack: 10
  - Purchase Quantity: 5 strips, Selling Quantity: 5 strips
- **Purchase Side (Per Strip)**:
  - Taxable Base: **₹48.00**
  - Purchase GST (ITC): **₹2.40**
  - Invoice Total: **₹50.40**
  - Economic Purchase Cost: **₹48.00**
- **Purchase Totals (5 Strips)**:
  - Total Purchase Invoice: **₹252.00**
  - Total Economic Cost: **₹240.00**
  - Total Input GST Credit: **₹12.00**
- **Selling Side (Per Strip)**:
  - Customer Selling Price: **₹135.00**
  - Net Sales Revenue: **₹128.57** ($\frac{135}{1.05}$)
  - Output GST: **₹6.43**
- **Per Tablet (Individual Unit)**:
  - Economic Cost: **₹4.80**
  - Customer Price: **₹13.50**
  - Net Sales Revenue: **₹12.86**
  - Profit: **₹8.06**
- **Per Strip**:
  - Profit: **₹80.57**
  - Profit Margin: **62.67%**
  - Markup: **167.86%**
- **For 5 Strips Sold**:
  - Total Customer Collection: **₹675.00**
  - Total Net Sales Revenue: **₹642.86**
  - Total Output GST: **₹32.14**
  - Total Profit: **₹402.86**
  - Total Tablets: **50 Tablets**

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.2 / 2.3
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: MVVM with clean layer separation (`domain`, `data`, `ui`, `theme`)
- **State Management**: StateFlow, Kotlin Coroutines, lifecycle-aware state collection
- **Local Database**: Room 2.8.5 with KSP and automated migration `MIGRATION_1_2`
- **Preferences**: Jetpack DataStore Preferences
- **Precision**: Deterministic `BigDecimal` monetary arithmetic with `RoundingMode.HALF_UP`
- **Navigation**: AndroidX Navigation 3 Compose with type-safe NavKeys
- **Build System**: Gradle 9.1 with Kotlin DSL and Version Catalogs (`libs.versions.toml`)
- **CI / Automation**: GitHub Actions running automated unit tests and debug APK compilation

---

## 💻 JDK Requirements & Windows Build Commands

### Prerequisites
- **JDK Requirement**: **JDK 17** (recommended) or **JDK 21** (e.g. Eclipse Adoptium Temurin or Amazon Corretto).
- **Gradle Wrapper**: The project includes `gradlew.bat` configured for Gradle 9.1; no global Gradle installation is required.

### Copy-and-Paste Commands (PowerShell)
```powershell
# 1. Set JAVA_HOME (adjust to your local JDK path if needed)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. Run All Automated Unit & Migration Tests
.\gradlew.bat test

# 3. Assemble Local Debug APK
.\gradlew.bat assembleDebug
```

### Copy-and-Paste Commands (Windows Command Prompt)
```cmd
:: 1. Set JAVA_HOME
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: 2. Run All Automated Unit & Migration Tests
gradlew.bat test

:: 3. Assemble Local Debug APK
gradlew.bat assembleDebug
```

The compiled APK will be generated at:
`app\build\outputs\apk\debug\app-debug.apk`

---

## 🔒 Release Signing vs. Local Debug Builds

- **Debug Builds (`assembleDebug`)**:
  - Automatically signed with Android's default local debug keystore.
  - Ideal for local development, emulator testing, and side-loading via ADB or APK file transfer.
- **Production Releases (`assembleRelease`)**:
  - Production builds intentionally do **not** use the debug keystore.
  - Release signing keys must be supplied securely via environment variables or CI secrets (e.g. `KEYSTORE_FILE`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`) to prevent checking private credentials into Git.

---

## 📲 How to Install & Upgrade

Because the application ID (`com.example.profitmargincalculator`) and installation identity are preserved, this update cleanly upgrades existing installations without data loss.

### Option A: Install via ADB
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Direct APK Transfer
1. Transfer `app-debug.apk` to your Android device via USB, WhatsApp, or cloud storage.
2. Tap the APK file in the file manager and select **Install** / **Update**.
3. All previously saved calculations will remain intact and will be migrated to the new schema with default pack settings.

---

## 🧪 Automated Test Suite

All automated tests run via `.\gradlew.bat test`:
- **`ProfitCalculationEngineTest`**:
  - Mandatory Section 5 regression scenario (PTR ₹48, MRP ₹135, 5% GST, ITC enabled).
  - Explicit pack size economics (1, 10, 15, 30, 100 units per pack) with exact rounded unit assertions.
  - Zero packs sold behavior (zero customer collection, zero revenue, zero profit, full inventory preserved).
  - Partial sales quantity with decoupled purchase and selling quantities.
  - Arithmetic overflow guards on large quantities and pack sizes.
  - Full-precision intermediate arithmetic preventing display rounding errors.
  - Tax-inclusive and tax-exclusive pricing.
  - Recoverable GST with ITC enabled vs. disabled.
  - Non-recoverable tax classification.
  - Zero tax rates, break-even, loss, and zero-revenue edge cases.
  - Negative selling quantity, zero pack sizes, and boundary rejection.
- **`CalculatorViewModelTest`**:
  - Initial UI state matching default reference values.
  - Live recalculation on changing pack and unit names or pack sizes.
  - Live recalculation on changing pricing inputs.
  - Room repository persistence and loading.
  - Example scenario restoration and form clearing.
  - Handling of zero, blank, and invalid negative selling quantities without fallback to purchase quantity.
- **`AppDatabaseMigrationTest`**:
  - Verifies Room `MIGRATION_1_2` executes all 7 SQLite `ALTER TABLE` statements.
  - Real SQLite in-memory database test: migrates a real version-1 database with existing user records, verifies default values are added, asserts no data loss, and verifies records reopen cleanly into the calculator engine.
- **`IndianCurrencyFormatterTest`**:
  - INR currency formatting (Lakhs and Crores grouping).
  - Percentage formatting.
  - Negative value formatting.
  - User input parsing.
