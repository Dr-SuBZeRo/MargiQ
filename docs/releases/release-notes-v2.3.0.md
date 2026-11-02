<div align="center">

  <img src="https://raw.githubusercontent.com/Dr-SuBZeRo/MargiQ/main/Icon/icon.png" alt="MargiQ App Icon" width="96" height="96" style="border-radius: 20px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);" />

  # MargiQ v2.3.0

  ### <i>Know your cost. Own your margin.</i>

  <p>
    <b>A high-precision, native Android profit and margin intelligence tool for pharmaceuticals, retail, and wholesale distribution in India.</b>
  </p>

  <p>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="Repository" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.3.0-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
  </p>

</div>

---

## 🌟 Highlights of MargiQ v2.3.0 (`versionCode 16`)

MargiQ **v2.3.0** is a major feature update delivering **Comparison Metric Difference Analysis**, resolving **GitHub Issue [#2](https://github.com/Dr-SuBZeRo/MargiQ/issues/2)** across both **Compare Products** and **Compare Suppliers** workflows.

Users can now immediately identify the exact numerical gap between Option A and Option B, understand which option is commercially advantageous, and export consistent difference reports across all channels.

---

### 🚀 Key Improvements & Features

#### 1. 📊 Centralized Domain Metric Differences ([#2](https://github.com/Dr-SuBZeRo/MargiQ/issues/2))
- **Standardized Convention**: Implemented strict signed difference convention: `Difference = Option B - Option A`.
  - Positive indicates Option B is numerically higher.
  - Negative indicates Option A is numerically higher.
- **Unrounded Precision**: Domain calculations preserve full-precision unrounded `BigDecimal` arithmetic from `CalculationResult`. Presentation rounding occurs exclusively at final formatting to eliminate compounding rounding errors.
- **Percentage-Point Discipline**: Margin, markup, and tax rate differences are explicitly presented as **percentage points** (`percentage points` / `% pts`), completely eliminating confusion with relative percentage changes.

#### 2. 🎯 Commercial Favourability Intelligence
- **Context-Aware Rules**: Rather than naively assuming higher is always better, MargiQ applies strict commercial evaluation:
  - `LOWER_IS_BETTER`: Purchase PTR, cost per individual unit, and cost per pack (when pack sizes match).
  - `HIGHER_IS_BETTER`: Profit per unit, profit per pack, profit margin, markup, and batch profit.
  - `NEUTRAL`: Tax rates, tax amounts, selling price/MRP, supplier discounts, free scheme packs, and pack costs with unequal units per pack.
- **True Advantage Protection**: Informational metrics such as supplier discount % or free packs are classified as neutral so that a high discount on an inflated base price does not falsely crown an expensive option as a winner.

#### 3. 📱 Side-by-Side Comparison UI
- **Mobile-Friendly Layout**: Embedded compact difference badges directly beneath metric rows, avoiding horizontal crowding on narrow phone screens while displaying signed deltas, absolute values, and clear narrative explanations.
- **Material 3 Theme Styling**: Theme-aware status colors (green for commercial advantage, slate for neutral metrics and ties) with high-contrast accessibility.
- **Expandable Detailed Analysis**: Core metrics (Unit Cost, Profit, Margin, Markup) remain immediately visible, while secondary tax and quantity rows expand neatly.

#### 4. 📤 Full Export Channel Parity
- **WhatsApp-Friendly Rich Text**: Structured `SIDE-BY-SIDE METRICS & DIFFERENCES` and `SUPPLIER OFFER METRICS & DIFFERENCES` sections with clean bullet points and direction indicators.
- **HTML Email Drafts**: Responsive 4-column side-by-side comparison tables with dedicated `Difference (B - A)` columns and clear advantage badges.
- **Canvas Image Cards**: Generates high-resolution comparison cards with a 4th `Diff (B-A)` column, dynamic canvas height calculation, and automated text fitting to guarantee that long product and supplier names never clip.

#### 5. 🧪 Comprehensive Test Coverage
- Verified across **136 unit tests** covering edge cases: positive/negative signed deltas, zero ties, double-loss scenarios (identifying smaller loss as favourable), BOGO scheme offers, differing pack sizes, and large currency values ($\ge \text{₹}10\text{ Lakhs}$).

---

### 📦 Release Assets

| Asset | Format | Description |
|:---|:---|:---|
| **`MargiQ-v2.3.0-release.apk`** | Signed APK | Production release signed with official keystore |
| **`MargiQ-v2.3.0-release.aab`** | App Bundle | Google Play publishing format |
| **`MargiQ-v2.3.0-debug.apk`** | Debug APK | Development and verification build |

---

### 📲 Installation Instructions

1. Download **`MargiQ-v2.3.0-release.apk`** from the release assets below.
2. Open the downloaded APK on your Android device.
3. If prompted, enable **"Allow installation from this source"**.
4. Tap **Install** or **Update**. All existing calculation history and settings will be preserved safely.

