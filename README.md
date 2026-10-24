# MargiQ (Native Android)
> **Know your cost. Own your margin.**  
> *Created by SuBZeRo* • **Version 1.1.0**

A complete, production-grade native Android application for retail, wholesale, pharmacy, and product-pricing calculations in India. Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room Database** (with automatic backward-compatible schema migrations), and **DataStore Preferences**, following modern Android architecture and clean MVVM principles.

---

## 📱 Features

### 1. Pack & Individual Unit Profitability (New in v1.1.0)
- **Pack Name & Selling Unit**: Support for any packaging type (e.g., *Strip*, *Box*, *Bottle*, *Pack*, *Vial*, *Carton*, or custom entry).
- **Individual Unit Name**: Precise unit specification (e.g., *Tablet*, *Capsule*, *Piece*, *ml*, *g*, *Sachet*, or custom entry).
- **Units per Pack**: Manually editable positive integer (e.g., 10, 15, 30, 100).
- **Purchase & Selling Quantities**: Decoupled purchase quantity (packs purchased) and selling quantity (packs sold) for tracking partial sales and total inventory.
- **Dual Profit Breakdown**:
  - **Per Pack**: Economic purchase cost, net sales revenue, profit per pack, margin (%), and markup (%).
  - **Per Individual Unit**: Cost per unit ($C / U$), customer price per unit, net revenue per unit ($R / U$), and profit per unit ($F / U$).
  - **Totals**: Total individual units purchased ($P \times U$), total units sold, total invoice, total economic cost, and total profit ($S \times F$).
- **Live Recalculation**: Adjusting units per pack or quantities updates all per-unit and total figures instantaneously without losing state.

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
- **Pack & Unit Summary Section**: Dedicated card showing Pack Type, Units per Pack, Purchase Quantity, Total Units, Cost per Pack, Cost per Unit, Profit per Pack, Profit per Unit, and Total Profit.
- **Line-Item Financial Breakdown**: Detailed purchase base, taxes, customer price, net sales revenue, and cost percentages.
- **Cash Flow vs. Business Profit Comparison**: Explicitly distinguishes between raw cash difference (`MRP - Invoice = Cash Difference`) and GST-exclusive business profit so users are never misled.

### 4. Calculation History (Room Database v2)
- **Non-Destructive Room Migration (v1 → v2)**: Preserves all historical calculations while adding pack and unit fields with safe defaults (`Pack`, `Unit`, `1`).
- **Search & Sort**: Search calculations by product name; sort by newest or oldest.
- **Reopen & Duplicate**: One-tap reopen into the main calculator with live recalculation; duplicate calculations for rapid price testing.
- **Manage History**: Delete individual records or clear all history with confirmation dialogs.

### 5. Settings, Theming & Identity
- **Dynamic Version & Creator Footer**: Professional, understated footer displaying:
  ```
  MargiQ
  Created by SuBZeRo
  Version 1.1.0
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
- $P$ = number of packs purchased
- $S$ = number of packs sold
- $U$ = individual units per pack
- $C$ = economic purchase cost per pack
- $R$ = net sales revenue per pack
- $F$ = profit per pack ($R - C$)

Then:
$$\text{Total Individual Units Purchased} = P \times U$$
$$\text{Total Individual Units Sold} = S \times U$$
$$\text{Cost Per Individual Unit} = \frac{C}{U}$$
$$\text{Net Revenue Per Individual Unit} = \frac{R}{U}$$
$$\text{Profit Per Individual Unit} = \frac{F}{U}$$
$$\text{Total Purchase Cost} = P \times C$$
$$\text{Total Profit for Packs Sold} = S \times F$$
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

---

## 🚀 How to Open in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug, Meerkat or newer).
2. Select **Open** and navigate to: `G:\SM` (or `G:\MargiQ`).
3. Allow Gradle Sync to finish.
4. Select the **app** configuration and click **Run** (or `Shift + F10`) to launch on your device or emulator.

---

## 🔨 How to Build Manually from Command Line

```bash
# Ensure JAVA_HOME points to JDK 17+
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

# Run Unit Tests (includes Section 5 regression suite)
.\gradlew.bat test

# Build Debug APK
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
`G:\SM\app\build\outputs\apk\debug\app-debug.apk`

---

## 📲 How to Install & Upgrade

Because the application ID (`com.example.profitmargincalculator`) and debug signing configuration are preserved, this update cleanly upgrades existing installations without data loss.

### Option A: Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Direct APK Transfer
1. Transfer `app-debug.apk` to your Android device via USB, WhatsApp, or cloud storage.
2. Tap the APK file in the file manager and select **Install** / **Update**.
3. All previously saved calculations will remain intact and will be migrated to the new schema with default pack settings.

---

## 🧪 Automated Test Verification

All 26 automated unit tests pass with zero failures:
- `ProfitCalculationEngineTest`: 14 tests (Section 5 regression, pack sizes 1/10/15/30/100, live updates, decoupled quantities, non-recoverable tax, ITC disabled, tax-inclusive/exclusive, zero tax, break-even, loss, zero/invalid pack sizes, division-by-zero prevention).
- `CalculatorViewModelTest`: 7 tests (default state, live pack updates, live input updates, Room repository saving, history restoration, example reset, form clearing).
- `AppDatabaseMigrationTest`: 1 test (verifies Room `MIGRATION_1_2` SQLite alter table statements).
- `IndianCurrencyFormatterTest`: 4 tests (currency, percentage, negative formatting, input parsing).
