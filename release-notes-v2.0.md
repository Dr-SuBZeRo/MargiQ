# MargiQ v2.0

**Know your cost. Own your margin.**

MargiQ v2.0 is a major milestone release featuring a complete package migration to com.margiq, a refreshed visual identity with custom 3D launcher icons and native Android splash screen, independent supplier discount & promotional offer controls, and comprehensive GST and profitability modeling.

---

### 🚀 What's New in v2.0

#### 1. Complete Package Migration to com.margiq
- **Application ID & Namespace:** Migrated the entire codebase and build configuration to com.margiq (Version code: 6, Version name: 2.0).
- **Clean Architecture:** Updated data, domain, and UI layers under the unified com.margiq namespace.
- **Room Database Schemas:** Exported official Room schema definitions under com.margiq.data.local.AppDatabase.

#### 2. Visual Identity & Brand Integration
- **Custom Launcher Icons:** High-resolution squircle, circular, and adaptive launcher icons (API 26+) featuring the 3D MargiQ calculator, ₹ currency symbol, and growth analytics motif.
- **Native Android SplashScreen API:** Configured Theme.App.Starting with @drawable/ic_splash_logo on a sleek #0D1F1C dark emerald background.
- **In-App Branding:** Seamlessly integrated ic_app_logo into the CalculatorScreen TopAppBar and the SettingsScreen creator footer.

#### 3. Product Details & Supplier Offers
- **Clean Product Details Interface:** Enter product name, pack name, unit name, and units per pack to derive accurate individual-unit economics (e.g. tablet, capsule, bottle).
- **Independent Supplier Discounts:** Apply supplier percentage discounts to paid-goods values before purchase GST calculation.
- **Buy X, Get Y Free Promotion Engine:** Configurable promotional offer cycles (e.g. Buy 10 Get 2 Free) with economic purchase cost allocated across total received packs.

#### 4. GST & Profitability Engine
- **Flexible Tax Modes:** Independent tax-inclusive and tax-exclusive modes for both purchase price and selling price.
- **Input Tax Credit (ITC):** Support for recoverable and non-recoverable purchase GST according to statutory business classification.
- **Unit Economics:** Real-time metrics for net revenue, profit per pack, profit per individual unit, margin %, and markup %.
- **Cash Flow vs. Business Profit:** Clear separation between gross cash outlay to suppliers and net GST-exclusive business profit.

#### 5. Local Storage & Migrations
- **Room Database Persistence:** Encrypted internal database storing calculation history with search, sort, reopen, and duplicate capabilities.
- **Versioned Migrations:** Complete compatibility and automated migration across schema versions 1 -> 2 -> 3.
- **Dynamic Version Reporting:** Settings and About screens dynamically read runtime version information from BuildConfig.VERSION_NAME.

#### 6. Quality & Verification
- **Unit & Migration Tests:** 48 passing automated tests covering financial math precision, tax calculation modes, and schema migrations.
- **Android Lint Analysis:** Zero errors on SDK 36.

---

### 📦 Release Deliverable

- **Asset:** \MargiQ-v2.0-debug.apk\
- **Application ID:** \com.margiq\
- **Version Name:** \2.0\ (versionCode \6\)
- **Target SDK:** Android 36 (Android 16) | **Min SDK:** Android 26 (Android 8.0)
- **SHA-256 Checksum:**
  \\\
  FE3407A8B7BFF77979C7B9D829EE038CEC4AE50977804EB2ED0BA8269F4212D5
  \\\

> **Note on Signing:** This release provides the verified debug APK tested and confirmed directly by the user on physical Android devices.

---

### ⚠️ Scope & Known Limitations
- MargiQ is a professional margin and cost estimation calculator designed for retailers, distributors, and pharmacies. It is not an inventory ledger, POS checkout system, or substitute for statutory accounting advice.