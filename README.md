# MargiQ (Native Android)
> **Know your cost. Own your margin.**  
> *Created by SuBZeRo* • **Version 1.2.0** (versionCode 4)

A complete, production-grade native Android application for retail, wholesale, pharmacy, and product-pricing calculations in India. Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room Database** (with automatic backward-compatible schema migrations v1 → v2 → v3), and **DataStore Preferences**, following modern Android architecture and clean MVVM principles.

---

## 📱 What's New in v1.2.0

### 1. Dedicated Product Profitability & Simplification
- **Streamlined Workflow**: MargiQ is a dedicated product profitability calculator. Removed unnecessary POS-style inputs (**Pack Name**, **Individual Unit Name**, and **Selling Quantity**) along with inventory balance summaries.
- **Editable Units per Pack**: Kept a simple, responsive integer input for **Units per Pack** (with presets: `1`, `10`, `15`, `30`, `100`), deriving individual tablet, capsule, or piece unit economics directly from the purchase-pack calculation.
- **Dedicated Sequence**:
  1. Hero Profitability Summary
  2. Action Bar (Save, Share, Copy)
  3. Product Details
  4. Units per Pack (Pack Information)
  5. Supplier Offers & Discounts
  6. Purchase Side (PTR per Pack, Tax, Mode, ITC, Purchase Quantity)
  7. Selling Side (MRP per Pack, Tax, Mode)
  8. Detailed Financial Breakdown
  9. Simple Cash Flow Comparison

### 2. Supplier Offers: Buy X, Get Y Free
- **Dedicated Section**: "Supplier Offers & Discounts" card with an intuitive toggle: **Enable Buy X, Get Y Free Offer**.
- **Inputs**: Buy Quantity (Packs) — $X$, and Free Quantity (Packs) — $Y$, with quick preset chips (`Buy 1, Get 1`, `Buy 10, Get 2`, `Buy 5, Get 1`).
- **Accounting Semantics**: Free packs are supplied at zero additional purchase consideration. Purchase GST charged by the supplier is calculated on the paid quantity only, not on the free quantity.
- **Multiple Offer Cycles**: Integer-safe division ensures offers scale across cycles:
  $$\text{Offer Cycles} = \lfloor \text{Paid Quantity} / X \rfloor$$
  $$\text{Free Packs Earned} = \text{Offer Cycles} \times Y$$
  $$\text{Total Packs Received} = \text{Paid Quantity} + \text{Free Packs Earned}$$
- **Effective Economic Cost**: The paid purchase cost (adjusted for recoverable GST/ITC) is spread evenly over all received packs:
  $$\text{Effective Cost Per Received Pack} = \frac{\text{Total Economic Cost of Paid Goods}}{\text{Total Packs Received}}$$

### 3. Supplier Discount Engine (%)
- **Optional Discount Field**: **Supplier Discount (%)**, validated for $0\%$ through $100\%$, with quick presets (`0%`, `2%`, `5%`, `10%`, `15%`).
- **Sequencing**: Applied to the taxable purchase value *before* purchase GST is calculated. Applies only to paid goods; free goods receive no additional discount.
- **Tax-Inclusive PTR Support**: For tax-inclusive PTR, the discount is applied to the gross price before deriving the net taxable base and tax component:
  $$\text{Discounted PTR} = \text{PTR} \times (1 - \text{Discount} / 100)$$
  $$\text{Taxable Base} = \frac{\text{Discounted PTR}}{1 + \text{Purchase Tax Rate} / 100}$$

### 4. Splash Screen Branding
- **Official Android SplashScreen API**: Integrated `androidx.core:core-splashscreen` with the official high-resolution MargiQ branding asset (`ic_splash_logo.png`).
- **Brand Identity**: Centered, undistorted launch visual on deep teal-black background (`#0D1F1C`), providing a seamless transition into the app without artificial loading delays.

### 5. Room Database Safety (v3 Migration)
- **Automatic Non-Destructive Migration (`MIGRATION_2_3`)**: Adds 8 new columns (`isSupplierOfferEnabled`, `offerBuyQuantity`, `offerFreeQuantity`, `freePacksReceived`, `totalPacksReceived`, `supplierDiscountPercent`, `supplierDiscountAmount`, `effectiveCostPerReceivedPack`) with safe defaults.
- **Zero Data Loss**: Previous version 1 and version 2 saved calculations are preserved intact and default safely to no offer and 0% discount.
- **Destructive Fallback Disabled**: Enforced strictly to ensure user databases are never wiped.
- **Versioned JSON Schemas**: Schema `3.json` committed to `app/schemas/`.

---

## 📐 Mathematical Formulation & Master Examples

### Master Example 1: Buy 1, Get 1 Free (Phase 2)
- **Inputs**:
  - Purchase Price (PTR): ₹100.00 (Tax-exclusive, 5% GST)
  - Paid Quantity: 1 pack
  - Supplier Offer: Buy 1, Get 1 Free ($X = 1, Y = 1$)
  - Selling Price (MRP): ₹150.00 (Tax-inclusive, 5% GST)
  - Units per Pack: 10
- **Results**:
  - Paid Packs: 1, Free Packs: 1, Total Packs Received: 2
  - Taxable Purchase Base: ₹100.00
  - Purchase GST (on paid pack only): ₹5.00
  - Supplier Invoice Total: ₹105.00
  - **Recoverable GST (ITC Enabled)**:
    - Total Economic Cost: ₹100.00
    - Effective Cost Per Received Pack: **₹50.00**
    - Effective Cost Per Unit: **₹5.00**
    - Net Sales Revenue Per Pack: **₹142.86**
    - Profit Per Received Pack: **₹92.86**
    - Profit Per Unit: **₹9.29**
    - Total Profit (2 packs): **₹185.71**
  - **Non-Recoverable GST (No ITC)**:
    - Total Economic Cost: ₹105.00
    - Effective Cost Per Received Pack: **₹52.50**
    - Effective Cost Per Unit: **₹5.25**
    - Profit Per Received Pack: **₹90.36**
    - Profit Per Unit: **₹9.04**
    - Total Profit (2 packs): **₹180.71**

### Master Example 2: Combined 10% Discount + Buy 10, Get 2 Free (Phase 3)
- **Inputs**:
  - PTR: ₹100.00 per paid pack (Tax-exclusive, 5% GST)
  - Paid Purchase Quantity: 10 packs
  - Supplier Discount: 10%
  - Supplier Offer: Buy 10, Get 2 Free ($X = 10, Y = 2$)
  - Selling Price: ₹150.00 (Tax-inclusive, 5% GST), Units per pack: 10
- **Results**:
  - Gross Paid Goods Value: $10 \times ₹100 = \mathbf{₹1,000.00}$
  - Supplier Discount: $10\% \times ₹1,000 = \mathbf{₹100.00}$
  - Net Taxable Purchase Value: $\mathbf{₹900.00}$
  - Purchase GST: $5\% \times ₹900 = \mathbf{₹45.00}$
  - Supplier Invoice Total: $\mathbf{₹945.00}$
  - Free Packs Earned: 2, Total Received Packs: 12
  - **Recoverable GST (ITC Enabled)**:
    - Total Economic Cost: ₹900.00
    - Effective Cost Per Received Pack: $\frac{₹900}{12} = \mathbf{₹75.00}$
    - Effective Cost Per Unit: $\frac{₹75}{10} = \mathbf{₹7.50}$
    - Net Sales Revenue Per Pack: **₹142.86**
    - Profit Per Received Pack: **₹67.86**
    - Profit Margin: **47.50%**
    - Markup: **90.48%**
    - Total Batch Profit (12 received packs): **₹814.29**
    - Total Customer Collection: **₹1,800.00**

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.3
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: MVVM with unidirectional data flow
- **Database**: Room 2.8.5 with KSP schema exports (`1.json`, `2.json`, `3.json`)
- **Splash Screen**: `androidx.core:core-splashscreen`
- **Preferences**: Jetpack DataStore Preferences
- **Precision**: Deterministic `BigDecimal` monetary arithmetic with `RoundingMode.HALF_UP`
- **Build System**: Gradle 9.1 with Kotlin DSL and Version Catalogs (`libs.versions.toml`)

---

## 💻 Build, Test & Verification

### Build Prerequisites
- **JDK**: JDK 17 or JDK 21 (Amazon Corretto 21 recommended on Windows).
- **Gradle**: Wrapper included (`gradlew.bat`).

### Run Unit & Migration Tests
```powershell
$env:JAVA_HOME = "C:\Users\legen\.jdks\corretto-21.0.5"
.\gradlew.bat test
```
Result: **48 passed, 0 failed**.

### Run Android Lint
```powershell
$env:JAVA_HOME = "C:\Users\legen\.jdks\corretto-21.0.5"
.\gradlew.bat lintDebug
```
Result: **0 errors**.

### Assemble Debug APK
```powershell
$env:JAVA_HOME = "C:\Users\legen\.jdks\corretto-21.0.5"
.\gradlew.bat assembleDebug
```
Generated at:
`app\build\outputs\apk\debug\app-debug.apk`

---

## 📦 Deliverables

- **Standard Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK Copy**: `G:\MargiQ-v1.2.0-debug.apk`
- **Clean Project ZIP**: `G:\SM\MargiQ_Project.zip`
- **Application ID**: `com.example.profitmargincalculator`
- **Version**: `1.2.0` (versionCode `4`)
