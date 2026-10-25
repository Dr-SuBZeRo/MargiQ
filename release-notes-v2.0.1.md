# MargiQ v2.0.1

**Know your cost. Own your margin.**

MargiQ v2.0.1 is an incremental refinement release focused on calculator workflow efficiency and settings interface cleanup. It combines product, pack, supplier discount, and promotional offer inputs into one consolidated, compact **Product Details** card, introduces an optional **Supplier Name** metadata field with Room v4 database migration and history integration, establishes independent toggle controls for supplier discounts and supplier offers, removes obsolete regional and number settings while keeping centralized Indian Rupee (₹) formatting, and preserves 100% of the verified financial calculation engine.

---

### 🚀 What's New in v2.0.1

#### 1. Consolidated Product Details Card
- **Unified Input Card:** Combined three previously separated cards (*Product Details*, *Pack Information*, and *Supplier Offers & Discounts*) into a single, cohesive, compact **Product Details** card.
- **Ordered Layout:**
  1. **Product / Medicine / Item Name:** Text input with example value restoration.
  2. **Supplier Name (Optional):** New metadata field to identify wholesaler/distributor.
  3. **Units per Pack:** Numeric input with preset chips (`1`, `10`, `15`, `30`, `100`).
  4. **Enable Supplier Discount:** Independent switch controlling discount application.
  5. **Supplier Discount (%):** Input and quick-select chips (`0%`, `2%`, `5%`, `10%`, `15%`), visible only when discount toggle is enabled.
  6. **Enable Buy X, Get Y Free Offer:** Independent switch controlling free goods calculation.
  7. **Buy Quantity & Free Quantity:** Offer inputs and quick-select chips (`Buy 1, Get 1`, `Buy 10, Get 2`, `Buy 5, Get 1`), visible only when offer toggle is enabled.

#### 2. Optional Supplier Name Metadata
- **Identity & Traceability:** Allows recording wholesaler, distributor, or agency name without affecting mathematical formulas.
- **Room Database Migration (v3 → v4):** Non-destructive migration `MIGRATION_3_4` adds `supplierName` (default `''`) and `isSupplierDiscountEnabled` (default `0`), safely preserving all historical records.
- **History & Search:** Calculation history displays the supplier name on cards when present, and searching by query matches both product name and supplier name.
- **Share & Export:** Formatted share text automatically includes `Supplier: <name>` when populated.

#### 3. Independent Supplier Discount & Offer Toggles
- **Strict Decoupling:** Users can enable either toggle, neither, or both simultaneously.
- **Zero-Consideration When Disabled:** Disabling the discount switch treats the discount strictly as 0%, ignoring any text stored in the discount field. Disabling the offer switch contributes zero free packs, ignoring stored Buy/Free quantities.

#### 4. Settings Interface Cleanup
- **Removed Section:** Removed the obsolete "Regional & Number Details" section and unused helper composables from Settings.
- **Preserved Preferences:** Maintained Theme Selection (Light, Dark, System), Dynamic Color, Calculator Defaults, Privacy & Local Storage, About MargiQ, and Creator Footer.
- **Centralized INR Formatting:** Retained standard Indian Rupee (`₹`) currency formatting and Indian numbering system digit grouping (e.g. `₹1,00,000.00`) centrally in `IndianCurrencyFormatter`.

#### 5. Verification & Quality
- **Unit & Migration Tests:** All automated unit tests, Room SQLite migration tests (1→2→3→4), calculation engine tests, and ViewModel tests passed with exit code 0.
- **Android Lint Analysis:** `lintDebug` passed with exit code 0.
- **Release Build:** `assembleDebug` completed successfully with exit code 0.

---

### 📦 Release Deliverables

- **Verified Debug APK:** `G:\MargiQ\MargiQ-v2.0.1-debug.apk`
- **Normal Output Path:** `G:\MargiQ\app\build\outputs\apk\debug\app-debug.apk`
- **Project Archive:** `G:\MargiQ\MargiQ_Project.zip`
- **Application ID:** `com.margiq`
- **Version Name:** `2.0.1` (versionCode `7`)
- **Target SDK:** Android 36 | **Min SDK:** Android 26
- **SHA-256 Checksum:**
  ```
  95cefdca559452c9855628cc523aade1a8febe796f5356e7293cab681e168e7d
  ```

---

### ⚠️ Scope & Notes
- MargiQ is a specialized pricing and margin calculator for retail, pharmacy, and wholesale pricing. It does not act as an inventory management or POS ledger.
