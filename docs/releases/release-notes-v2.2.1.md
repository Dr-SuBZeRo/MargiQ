<div align="center">

  <img src="https://raw.githubusercontent.com/Dr-SuBZeRo/MargiQ/main/Icon/icon.png" alt="MargiQ App Icon" width="96" height="96" style="border-radius: 20px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);" />

  # MargiQ v2.2.1

  ### <i>Know your cost. Own your margin.</i>

  <p>
    <b>A high-precision, native Android profit and margin intelligence tool for pharmaceuticals, retail, and wholesale distribution in India.</b>
  </p>

  <p>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="Repository" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.2.1-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
  </p>

</div>

---

## 🌟 Highlights of MargiQ v2.2.1 (`versionCode 15`)

MargiQ **v2.2.1** is a critical bug-fix update addressing **GitHub Issue [#1](https://github.com/Dr-SuBZeRo/MargiQ/issues/1)**, resolving an issue where the **Buy X Get Y Free** supplier offer was ignored in **Compare Products** calculations.

---

### 🚀 Key Improvements & Bug Fixes

#### 1. 🎯 Resolved Buy X Get Y Free in Product Comparison ([#1](https://github.com/Dr-SuBZeRo/MargiQ/issues/1))
- **Root Cause**: In `CompareSideState.toCalculationInput()`, purchase `quantity` was previously hardcoded to `1`. In `ProfitCalculationEngine`, free pack eligibility uses integer division `quantity / offerBuyQuantity`. With `quantity = 1`, any offer where `buy > 1` evaluated to `1 / buy = 0` offer cycles awarded (`freePacksReceived = 0`), completely ignoring the free units.
- **The Fix**: Updated `CompareSideState.toCalculationInput()` to evaluate the qualifying offer batch (`quantity = if (isSupplierOfferEnabled && buy > 0) buy else 1`), matching `SupplierOfferSide.toCalculationInput()`.
- **Exact Accuracy Verified**: For a scenario with PTR ₹70.10, 5% tax-exclusive GST, 2% supplier discount, Buy 5 Get 1 Free, and Selling Price ₹92.00:
  - **Effective Cost / Unit**: Correctly reduces from `₹72.13` to **`₹60.11`** (cost of 5 paid units distributed over 6 units received).
  - **Profit / Unit**: Correctly increases from `₹19.87` to **`₹31.89`**.
  - **Profit Margin**: Correctly reflects **`34.66%`** (previously `21.59%`).
  - **Markup**: Correctly reflects **`53.05%`** (previously `27.54%`).
  - **Total Units Received**: **`6`** (5 paid + 1 free).

#### 2. 🧪 Comprehensive Regression Testing Suite
- Added 8 dedicated unit test suites (260 lines of assertions) in `CompareViewModelTest.kt`:
  1. Exact reproduction test for Issue #1 with combined discount and tax-exclusive pricing.
  2. BOGO-disabled baseline invariance (preventing regressions for standard purchases).
  3. Various offer ratios (Buy 1 Get 1 Free, Buy 5 Get 1 Free, Buy 10 Get 2 Free).
  4. Input Tax Credit (ITC) interactions (recoverable GST vs non-recoverable expense).
  5. Tax-inclusive vs tax-exclusive purchase pricing modes under BOGO.
  6. Boundary conditions (blank, zero, and negative offer quantities).
  7. Independent side calculations and accurate lowest-cost winner detection between Option A and Option B.
  8. Propagation through `ShareReportFactory` across all share formats (Rich Text, Canvas Image, and HTML Email).

#### 3. 🔒 Shared Data Model Consistency
- Automatically corrects export representations across all sharing channels:
  - **CashQ-Style Rich Text** reports.
  - **Live Canvas Summary Cards** (Light & Dark modes).
  - **Professional HTML Email** drafts.

---

### 📦 Release Assets & Checksums

| Asset File | Distribution Format | File Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **`MargiQ-v2.2.1-release.apk`** | Signed Production APK | 13.37 MB | `D8DFF364ED1A8CE8999818DF7B30F6AEFAE1778E31661C7109DD6C294273AAAD` |
| **`MargiQ-v2.2.1-release.aab`** | Google Play App Bundle | 12.98 MB | `70FE889BE25A4337C6010A3D322439E2AC97A1405CE19BD342EA6468F50A67CE` |
| **`MargiQ-v2.2.1-debug.apk`** | Development / Debug APK | 20.23 MB | `EB68CE5C8D630C95D9F37AF8C949CE5B8E4DB89D13C1DC540A0EE935D6A3651E` |

---

### 🔐 Cryptographic Signature Verification

- **Signing Standard**: APK Signature Scheme v2 (Full APK Signature)
- **Signer Identity**: `CN=subzero, OU=subzero, O=subzero, L=erode, ST=tamilnadu, C=in`
- **Certificate Digest (SHA-256)**: `a78f5aa4924295b98af590d93ac32bd1dec5595d9e56e1409f67c812e17f0be7`
- **Application ID**: `com.margiq`
- **Version**: `2.2.1` (versionCode `15`)
- **Minimum Android Version**: Android 8.0 Oreo (API 26)
- **Target Android Version**: Android 16 (API 36)

---

### 📥 Installation Instructions

1. Download **`MargiQ-v2.2.1-release.apk`**.
2. Tap the downloaded APK in your browser or file manager.
3. If prompted by Android, enable **"Allow installation from this source"**.
4. Tap **Install** or **Update**. Your existing calculation history and settings will be preserved safely.
