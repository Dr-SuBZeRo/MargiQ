<div align="center">

  <img src="https://raw.githubusercontent.com/Dr-SuBZeRo/MargiQ/main/Icon/icon.png" alt="MargiQ App Icon" width="96" height="96" style="border-radius: 20px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);" />

  # MargiQ v2.3.1

  ### <i>Know your cost. Own your margin.</i>

  <p>
    <b>A high-precision, native Android profit and margin intelligence tool for pharmaceuticals, retail, and wholesale distribution in India.</b>
  </p>

  <p>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="Repository" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI" /></a>
    <a href="https://github.com/Dr-SuBZeRo/MargiQ/releases/latest"><img src="https://img.shields.io/badge/Release-v2.3.1-00897B?style=for-the-badge&logo=android" alt="Latest Release" /></a>
  </p>

</div>

---

## 🌟 Highlights of MargiQ v2.3.1 (`versionCode 17`)

MargiQ **v2.3.1** is a stabilization and integrity release introducing **Versioned Plaintext JSON Backup & Restore**, **Deterministic Type-Length-Value (TLV) Deduplication**, **Atomic Room Database Transactions**, **Privacy-Preserving Cloud Backup Exclusion**, and **F-Droid Build Configuration Support**.

---

### 🚀 Key Improvements & Architecture Updates

#### 1. 💾 Plaintext JSON History Backup & Restore
- **User-Controlled Export/Import**: Export and restore your complete calculation history via Android Storage Access Framework (`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`).
- **Standardized Versioned Format**: Backup documents use a versioned structure (`schemaVersion = 1`) with metadata headers (`com.margiq.calculation-backup`).
- **Exact Numerical Precision**: Numerical quantities, PTR, prices, GST rates, and margin outputs are stored as exact decimal strings, eliminating IEEE-754 binary floating-point rounding inaccuracies.

#### 2. 🛡️ Type-Length-Value (TLV) Canonical Fingerprinting
- **Deterministic Collision Prevention**: Implemented a length-prefixed binary TLV encoder covering all 38 material content fields of `CalculationEntity`.
- **Disambiguation Invariants**: Strict distinct encoding for `null` (`0x00`), empty string (`0x01` length 0), and literal string `"<null>"`.
- **Scale Normalization**: Decimals are normalized with `stripTrailingZeros()` so equivalent values (e.g. `100.00` vs `100`) do not cause false non-duplicates.
- **Surrogate Key Exclusion**: Local primary key `id` is excluded from the fingerprint, allowing imported records to match existing calculations faithfully regardless of local row IDs.

#### 3. 🔄 Atomic Room Database Import Modes
- **MERGE Strategy**: Automatically detects duplicate records via TLV fingerprints, skips identical entries, and inserts new records with fresh auto-generated monotonic IDs.
- **REPLACE Strategy**: Allows complete history restoration after explicit confirmation, resequencing auto-increment IDs safely to avoid primary key conflicts.
- **Transaction Rollback Protection**: Import batches run inside atomic `AppDatabase.withTransaction { ... }`; any I/O failure or corrupted record triggers a full rollback, leaving existing data untouched.
- **Future ID Integrity**: Future calculations created after an import are guaranteed to allocate IDs strictly greater than the highest imported ID (`newId > maxMergedId`).

#### 4. 🔒 Privacy & Scoped Backup Configuration
- **Cloud Backup Exclusion**: Configured `backup_rules.xml` (API < 31) and `data_extraction_rules.xml` (API $\ge$ 31) to strictly exclude the local database (`MargiQ.db`) from cloud backups (`Google Drive` / `Android Auto-Backup`).
- **Encrypted Device-to-Device Migration**: Whitelisted database and settings for encrypted direct phone-to-phone migration via cable or Wi-Fi Direct during device upgrades.

#### 5. 📦 F-Droid Build Configuration Support
- **Compile-Time Flag**: Added Gradle property `-PdisableUpdateChecker=true` to omit the GitHub update checker UI and buttons in F-Droid distributions.
- **Apache-2.0 License**: Added complete, unabridged Apache License 2.0 text and project documentation attribution.

---

### ⚠️ Skipped C7 Verification Risks & Known Limitations

1. **Room Schema Migration**:
   - Both `v2.3.0` and `v2.3.1` use Room database schema version 6 (`AppDatabase(version = 6)`).
   - Dedicated multi-version schema upgrade integration testing from legacy schemas (v1–v5) was skipped for this release cycle.
2. **F-Droid Cleanroom Build**:
   - The F-Droid build configuration has been verified in local simulation; official F-Droid KVM cleanroom builder execution will occur during F-Droid repository ingestion.
3. **Backup Encryption**:
   - Exported backup files are **plaintext JSON**. Users should store exported files securely.
   - Device-to-device transfer security relies on the Android OS implementation.
4. **Network & Permissions**:
   - Standard release APK includes `android.permission.INTERNET` exclusively for user-initiated GitHub update checks.
   - The F-Droid build disables the update checker feature at compile time, but `android.permission.INTERNET` remains in the manifest and networking classes remain in the DEX bytecode as R8 minification is disabled (`isMinifyEnabled = false`).

---

### 📦 Release Assets & Artifacts

| Artifact | Type | Description |
|:---|:---|:---|
| **`MargiQ-v2.3.1-release.apk`** | Signed APK | Official production release signed with developer keystore (`CN=subzero`) |
| **`MargiQ-v2.3.1-release.aab`** | App Bundle | Production Android App Bundle |
| **`MargiQ-v2.3.1-debug.apk`** | Debug APK | Development and verification build |
