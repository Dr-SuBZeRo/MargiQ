<div align="center">

  <img src="https://raw.githubusercontent.com/Dr-SuBZeRo/MargiQ/main/Icon/icon.png" alt="MargiQ App Icon" width="96" height="96" style="border-radius: 20px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);" />

  # MargiQ v2.1.4

  ### <i>Know your cost. Own your margin.</i>

  <p>
    <b>A high-precision, native Android profit and margin intelligence tool for pharmaceuticals, retail, and wholesale distribution in India.</b>
  </p>

  <p>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="Repository" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.1.4-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
  </p>

</div>

---

## 🌟 Highlights of MargiQ v2.1.4 (`versionCode 12`)

MargiQ **v2.1.4** introduces a **Unified Sharing System** supporting Rich Text, high-resolution Image summary cards (with Follow App, Light, and Dark appearance selector and MediaStore `Pictures/MargiQ` saving), and structured HTML Email drafts with plain-text fallback across single calculations, history snapshots, and product/supplier comparisons. Additionally, header and navigation drawer branding is refined to the polished `MagiQ` two-tone wordmark while preserving full accessibility and 100% offline-first privacy.

---

### 🚀 Key Improvements & Features

#### 1. 📤 Unified Share Format Dialog
- **Consistent Export Across All Surfaces**: Added a polished Material 3 format selection bottom sheet/dialog accessible from the **Calculator top action bar**, **History item cards**, and the **Comparison Suite** (top bar and in-card).
- **Three Core Export Formats**:
  1. **Rich Text**: Beautifully indented, Unicode-friendly plain text with INR (`₹`) formatting, structured financial metrics, and quick buttons to **Share Text** or **Copy to Clipboard**.
  2. **Image Summary Card**: High-resolution 1080px Canvas bitmap rendering complete purchase economics, selling prices, profitability ratios, and comparison winner highlights.
  3. **HTML Email**: Clean, responsive HTML email drafts featuring inline CSS tables, safe input character escaping, and plain-text fallback for instant dispatching to email clients.

#### 2. 🎨 Premium Image Renderer with Appearance Control
- **Light & Dark Theme Styling**: Users can choose image styling via the appearance selector:
  - **Follow App**: Automatically adapts to the active application theme.
  - **Light Card**: Crisp white background (`#FFFFFF`) with deep slate typography (`#0F172A`).
  - **Dark Card**: Premium obsidian background (`#0B0F19`) with high-contrast emerald highlights (`#10B981`).
- **MediaStore & FileProvider Integration**:
  - **Save Image**: Direct saving to the standard `Pictures/MargiQ` gallery folder via Android's MediaStore API (with `IS_PENDING` workflow on API 29+ and directory fallback on API 26–28).
  - **Share Image**: Seamless native file sharing through secure `FileProvider` (`${applicationId}.fileprovider`) with temporary read grants.

#### 3. 🔒 Authoritative Canonical Data Model (`ShareReport`)
- **Single Source of Truth**: All export formats consume an immutable `ShareReport` model (`SingleCalculation`, `ProductComparison`, `SupplierComparison`).
- **Zero Recalculation**: Export engines never recompute financial logic, ensuring 100% mathematical consistency with visible screen numbers.
- **Immutable History Invariance**: Sharing saved history items strictly reads the persisted database snapshot without modifying or recalculating stored entries.

#### 4. 💎 Brand Wordmark Refinement (`MagiQ`)
- **Polished Visual Typography**: Replaced static header text with the two-tone `MargiQBrandWordmark` composable:
  - **"Magi"** renders dynamically in theme text color (`onSurface`), ensuring perfect readability across light and dark modes.
  - The terminal **"Q"** is highlighted in brand profit green (`FinanceTheme.colors.profit`).
  - Screen reader semantics announce the brand as `"MargiQ"` for accessibility compliance.
- Applied across the **Navigation Drawer Header**, **Calculator Top Bar**, **Settings About Card**, and **Settings Footer**.

#### 5. 🛡️ Privacy, Permissions & Offline Integrity
- **100% Offline-First Architecture**: Zero external network dependencies for calculations or sharing; images and text are created and saved entirely on-device.
- **Minimal Permissions**: Scoped `WRITE_EXTERNAL_STORAGE` (`maxSdkVersion="28"`) for legacy Android compatibility; no extra runtime permissions required on modern Android (API 29+).

---

### 📦 Release Assets & Checksums

| Asset File | Distribution Format | File Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **`MargiQ-v2.1.4-release.apk`** | Signed Production APK | 13.41 MB | `CF14213D68681C53B7710CA38DD2914735F8066D95450FE2DC2A817B74CBB1AC` |
| **`MargiQ-v2.1.4-release.aab`** | Google Play App Bundle | 13.01 MB | `835DE363BEBE8A995A9E5476725912F344B1D36F9F5AC17739BACDF3ECE939E5` |
| **`MargiQ-v2.1.4-debug.apk`** | Development / Debug APK | 20.26 MB | `6C67DD3C5861A2E203CC4BAABD73B853D21D982B3A580711737EE6AC93575CAD` |

---

### 🔐 Cryptographic Signature Verification

- **Signing Standard**: APK Signature Scheme v2 (Full APK Signature)
- **Signer Identity**: `CN=subzero, OU=subzero, O=subzero, L=erode, ST=tamilnadu, C=in`
- **Certificate Digest (SHA-256)**: `a78f5aa4924295b98af590d93ac32bd1dec5595d9e56e1409f67c812e17f0be7`
- **Application ID**: `com.margiq`
- **Version**: `2.1.4` (versionCode `12`)
- **Minimum Android Version**: Android 8.0 Oreo (API 26)
- **Target Android Version**: Android 16 (API 36)

---

### 📥 Installation Instructions

1. Download **`MargiQ-v2.1.4-release.apk`**.
2. Tap the downloaded file in your browser or file manager.
3. If prompted by Android, enable **"Allow installation from this source"**.
4. Tap **Install** or **Update** to install MargiQ. Your existing calculation history and settings will be preserved safely.
