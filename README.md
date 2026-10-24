# Profit & Margin Calculator (Native Android)

A complete, production-grade native Android application for retail, wholesale, pharmacy, and product-pricing calculations in India. Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room Database**, and **DataStore Preferences**, following modern Android development best practices and clean MVVM architecture.

---

## 📱 Features

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
- **Quantity Scaling**:
  - Bulk pricing support for 1 to n units with automatic calculation of total invoices, total customer collections, total taxes, and total profit.
- **Financial Dashboard**:
  - Hero Card displaying Profit Per Unit, Profit Margin (%), Markup (%), and Total Profit with color coding (Green for profit, Red for loss, Neutral for break-even).
  - Detailed line-item breakdown of purchase base, taxes, customer price, net sales revenue, and cost percentages.
  - **Cash Flow vs. Business Profit Comparison**: Explicitly distinguishes between raw cash difference (`MRP - Invoice = Cash Difference`) and GST-exclusive business profit so users are never misled.
- **Calculation History (Room Database)**:
  - Persist calculations with product name, timestamp, and full inputs/results.
  - Search history by product name.
  - Sort by newest or oldest.
  - One-tap reopen into the main calculator with live recalculation.
  - Duplicate calculations.
  - Delete individual records or clear all history with confirmation dialogs.
- **Settings & Theme Engine (DataStore Preferences)**:
  - Theme Selection: **Follow System**, **Light Theme**, **Dark Theme**.
  - Dynamic Color theming on Android 12+ with branded fallback.
  - Custom Calculator Defaults: Set preferred default tax rates, modes, and quantity.
  - Reset defaults action.
  - Regional formatting: Indian Numbering System (Lakhs & Crores grouping) with ₹ (INR).
  - Privacy policy and About screen.
- **Usability & Sharing**:
  - Formatted text summary copy to clipboard.
  - Native Android share sheet integration (`ACTION_SEND`).
  - Clear form and restore reference example buttons.
  - 100% offline, zero permissions, no analytics, no accounts.

---

## 📐 Mathematical Formulation & Reference Calculation

### 1. Purchase Calculations
- **Tax-exclusive PTR**:
  $$\text{Taxable Base} = \text{PTR}$$
  $$\text{Purchase Tax} = \text{PTR} \times \frac{\text{Rate}}{100}$$
  $$\text{Invoice Total} = \text{Taxable Base} + \text{Purchase Tax}$$
- **Tax-inclusive PTR**:
  $$\text{Taxable Base} = \frac{\text{PTR}}{1 + \frac{\text{Rate}}{100}}$$
  $$\text{Purchase Tax} = \text{PTR} - \text{Taxable Base}$$
  $$\text{Invoice Total} = \text{PTR}$$
- **Economic Purchase Cost**:
  - When Recoverable GST & ITC Enabled: $\text{Economic Cost} = \text{Taxable Base}$
  - When ITC Disabled or Non-recoverable: $\text{Economic Cost} = \text{Invoice Total}$

### 2. Selling Calculations
- **Tax-inclusive Selling Price (Standard Indian MRP)**:
  $$\text{Net Sales Revenue} = \frac{\text{MRP}}{1 + \frac{\text{Rate}}{100}}$$
  $$\text{Output GST} = \text{MRP} - \text{Net Sales Revenue}$$
  $$\text{Customer Price} = \text{MRP}$$
- **Tax-exclusive Selling Price**:
  $$\text{Net Sales Revenue} = \text{Price}$$
  $$\text{Output GST} = \text{Price} \times \frac{\text{Rate}}{100}$$
  $$\text{Customer Price} = \text{Price} + \text{Output GST}$$

### 3. Core Profit Metrics
$$\text{Profit Per Unit} = \text{Net Sales Revenue} - \text{Economic Purchase Cost}$$
$$\text{Profit Margin (\%)} = \frac{\text{Profit Per Unit}}{\text{Net Sales Revenue}} \times 100$$
$$\text{Markup (\%)} = \frac{\text{Profit Per Unit}}{\text{Economic Purchase Cost}} \times 100$$
$$\text{Cost \% of Revenue} = \frac{\text{Economic Purchase Cost}}{\text{Net Sales Revenue}} \times 100$$
$$\text{Total Profit} = \text{Profit Per Unit} \times \text{Quantity}$$

### 4. Mandatory Reference Case Verification
- **Inputs**: PTR = ₹48.00 (Tax-exclusive, 5% GST), MRP = ₹135.00 (Tax-inclusive, 5% GST), Recoverable GST with ITC Enabled, Quantity = 1.
- **Expected Results**:
  - Taxable Purchase Base = ₹48.00
  - Purchase GST (Eligible ITC) = ₹2.40
  - Purchase Invoice Total = ₹50.40
  - Economic Purchase Cost = ₹48.00
  - Customer Selling Price = ₹135.00
  - Net Sales Revenue = ₹128.57
  - Output GST = ₹6.43
  - **Profit Per Unit = ₹80.57**
  - **Profit Margin = 62.67%**
  - **Markup = 167.86%**
  - **Cost % of Revenue = 37.33%**
  - Raw Cash Difference = ₹135.00 − ₹50.40 = ₹84.60 (clearly distinguished in comparison card).

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.3
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: MVVM with clean separation (domain, data, ui, theme)
- **State Management**: StateFlow, Coroutines, lifecycle-aware collection
- **Local Database**: Room 2.8.5 with Kotlin Symbol Processing (KSP)
- **Preferences**: Jetpack DataStore Preferences
- **Precision**: Deterministic `BigDecimal` monetary arithmetic with `RoundingMode.HALF_UP`
- **Navigation**: Navigation 3 Compose with type-safe NavKeys
- **Build System**: Gradle 9.1 with Kotlin DSL and Version Catalogs (`libs.versions.toml`)

---

## 🚀 How to Open in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug, or newer).
2. Select **Open** from the welcome screen or **File > Open**.
3. Navigate to the project root directory: `g:\SM`.
4. Wait for Gradle Sync to complete.
5. Select the **app** configuration and click **Run** (green play icon) or press `Shift + F10` to deploy to a connected Android phone or emulator.

---

## 🔨 How to Build Manually from Command Line

Ensure `JAVA_HOME` points to a JDK 17+ or JDK 21 installation:

```bash
# Set JAVA_HOME (Windows CMD example)
set JAVA_HOME=C:\Users\legen\.jdks\corretto-21.0.5
set PATH=%JAVA_HOME%\bin;%PATH%

# Run Unit Tests
.\gradlew.bat test

# Build Debug APK
.\gradlew.bat assembleDebug
```

The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📲 How to Install on an Android Phone

### Option A: Install via ADB (USB Debugging)
1. Enable **Developer Options** and **USB Debugging** on your Android phone.
2. Connect your phone via USB.
3. Verify your device is connected:
   ```bash
   adb devices
   ```
4. Install the debug APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Option B: Direct File Transfer
1. Copy `app-debug.apk` to your phone via USB, Google Drive, or Bluetooth.
2. On your phone, tap the APK in your File Manager.
3. Allow "Install from unknown sources" if prompted, and complete the installation.

---

## 🧪 Automated Test Verification

All unit tests pass with 100% success rate:

```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 31s
26 actionable tasks: 7 executed, 19 up-to-date
```

### Covered Test Cases:
- Default mandatory reference calculation (PTR ₹48, MRP ₹135, 5% GST, ITC enabled)
- Non-recoverable tax calculation (PTR ₹48, MRP ₹135, tax added to cost)
- Input Tax Credit disabled scenario
- Tax-inclusive purchase pricing
- Tax-exclusive selling pricing
- Zero tax rates (0% purchase & 0% selling)
- Break-even scenario (0% profit)
- Negative profit / Loss scenario
- Multi-unit scaling (Quantity > 1)
- Zero sales revenue edge case (no division by zero)
- Zero economic cost edge case (no division by zero)
- Negative price / tax validation edge cases
- High precision and large monetary values
- Indian currency formatting and number grouping
- Input parsing robustness
- ViewModel state management, live recalculations, and Room repository persistence
