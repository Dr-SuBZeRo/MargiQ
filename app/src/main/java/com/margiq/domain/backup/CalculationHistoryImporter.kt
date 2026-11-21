package com.margiq.domain.backup

import androidx.room.withTransaction
import com.margiq.data.local.AppDatabase
import com.margiq.data.local.CalculationDao
import com.margiq.data.local.CalculationEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigDecimal
import java.security.MessageDigest

/**
 * Conflict resolution strategy when importing calculation history.
 *
 * ## ID Allocation and Collision Prevention Policy
 *
 * 1. **MERGE Strategy**:
 *    - All candidate records to be imported are assigned `id = 0` via `record.toEntity(assignNewId = true)`.
 *    - In SQLite with `INTEGER PRIMARY KEY AUTOINCREMENT`, an insert with `id = 0` instructs SQLite
 *      to assign `sqlite_sequence.seq + 1`, guaranteeing that newly merged records receive sequential IDs
 *      strictly greater than all existing records in the database.
 *    - Existing database records are untouched, and subsequent user calculations will continue to
 *      allocate IDs strictly higher than the merged records. No ID collisions can ever occur.
 *    - Duplicate candidate records (matching an existing calculation's 38-field content fingerprint)
 *      are safely skipped without touching the database.
 *
 * 2. **REPLACE Strategy**:
 *    - The database is completely cleared first via [CalculationDao.clearAllCalculations].
 *    - If all records in the backup document have distinct, positive IDs (`hasValidDistinctIds == true`),
 *      those explicit IDs are restored. Inserting explicit IDs automatically advances SQLite's internal
 *      `sqlite_sequence` to `max(sqlite_sequence.seq, highest_inserted_id)`.
 *    - If any record in the backup contains a non-positive (`<= 0`) or duplicate ID, all backup records
 *      have their IDs reset to `0` (`assignNewId = true`), allowing SQLite to auto-generate fresh,
 *      monotonic IDs (1, 2, 3...) safely without primary key collisions or insertion failures.
 *    - Future calculations created after a REPLACE import will automatically allocate IDs greater than
 *      the highest imported ID, completely preventing future auto-generated collisions.
 */
enum class ImportMode {
    /**
     * Appends new records into the database. Records that match existing entries
     * (identical content across all material fields) are skipped to prevent duplicates.
     * Imported records receive new auto-generated database IDs.
     */
    MERGE,

    /**
     * Replaces the entire local calculation history with the backup contents after explicit user confirmation.
     * Original database IDs are preserved if valid and distinct; otherwise safely regenerated to prevent collisions.
     */
    REPLACE
}

/**
 * Summary of an executed backup import operation.
 */
data class ImportSummary(
    val importedCount: Int,
    val skippedCount: Int,
    val totalInBackup: Int,
    val mode: ImportMode
)

/**
 * Executes atomic Room database import operations for calculation history backups.
 */
object CalculationHistoryImporter {

    /**
     * Imports a validated [CalculationBackupDocument] into [AppDatabase] atomically.
     *
     * @param database The target Room database.
     * @param document The validated backup document.
     * @param mode The selected conflict resolution strategy ([ImportMode.MERGE] or [ImportMode.REPLACE]).
     * @return Summary describing imported and skipped record counts.
     */
    suspend fun importBackup(
        database: AppDatabase,
        document: CalculationBackupDocument,
        mode: ImportMode
    ): ImportSummary {
        return database.withTransaction {
            executeImport(database.calculationDao(), document, mode)
        }
    }

    /**
     * Executes the import and deduplication logic against [CalculationDao].
     */
    suspend fun executeImport(
        dao: CalculationDao,
        document: CalculationBackupDocument,
        mode: ImportMode
    ): ImportSummary {
        return when (mode) {
            ImportMode.REPLACE -> {
                dao.clearAllCalculations()
                val hasValidDistinctIds = document.records.map { it.id }.distinct().size == document.records.size &&
                        document.records.all { it.id > 0 }
                val entitiesToInsert = document.records.map { it.toEntity(assignNewId = !hasValidDistinctIds) }
                if (entitiesToInsert.isNotEmpty()) {
                    dao.insertCalculations(entitiesToInsert)
                }
                ImportSummary(
                    importedCount = entitiesToInsert.size,
                    skippedCount = 0,
                    totalInBackup = document.records.size,
                    mode = mode
                )
            }

            ImportMode.MERGE -> {
                val existingCalculations = dao.getAllCalculationsList()
                val existingFingerprints = existingCalculations.map { computeContentFingerprint(it) }.toMutableSet()

                val entitiesToInsert = mutableListOf<CalculationEntity>()
                var skipped = 0

                for (record in document.records) {
                    val candidateEntity = record.toEntity(assignNewId = true)
                    val candidateFingerprint = computeContentFingerprint(candidateEntity)

                    if (candidateFingerprint in existingFingerprints) {
                        skipped++
                    } else {
                        existingFingerprints.add(candidateFingerprint)
                        entitiesToInsert.add(candidateEntity)
                    }
                }

                if (entitiesToInsert.isNotEmpty()) {
                    dao.insertCalculations(entitiesToInsert)
                }

                ImportSummary(
                    importedCount = entitiesToInsert.size,
                    skippedCount = skipped,
                    totalInBackup = document.records.size,
                    mode = mode
                )
            }
        }
    }

    /**
     * Binary Type-Length-Value (TLV) encoder for calculation records.
     * Guarantees deterministic, collision-free serialization:
     * - Every field is prefixed with an explicit 16-bit short tag (1..38) and 8-bit type tag.
     * - `null` is encoded with a dedicated type tag (TYPE_NULL, 0x00), making it strictly distinct
     *   from empty strings (TYPE_STRING with length 0) and literal `"<null>"` strings.
     * - Decimal fields are normalized using [BigDecimal.stripTrailingZeros] to prevent scale variations
     *   (e.g., "100.00" vs "100.0" vs "100") from generating false non-duplicates, while preserving
     *   full numerical precision without IEEE-754 binary floating-point conversion loss.
     * - Product and supplier names undergo `.trim().lowercase()` to enforce case-insensitive duplicate
     *   semantics suitable for retail pharmacy catalog calculations.
     * - Pack name, unit name, and calculation basis are trimmed but case-preserving.
     * - The local surrogate primary key [CalculationEntity.id] is intentionally excluded.
     */
    object CanonicalFingerprintEncoder {
        const val TYPE_NULL: Byte = 0x00
        const val TYPE_STRING: Byte = 0x01
        const val TYPE_INT: Byte = 0x02
        const val TYPE_LONG: Byte = 0x03
        const val TYPE_BOOLEAN: Byte = 0x04
        const val TYPE_DECIMAL: Byte = 0x05

        fun encode(entity: CalculationEntity): ByteArray {
            val bos = ByteArrayOutputStream(512)
            val dos = DataOutputStream(bos)

            fun writeNull(tag: Short) {
                dos.writeShort(tag.toInt())
                dos.writeByte(TYPE_NULL.toInt())
            }

            fun writeString(tag: Short, value: String?) {
                if (value == null) {
                    writeNull(tag)
                } else {
                    dos.writeShort(tag.toInt())
                    dos.writeByte(TYPE_STRING.toInt())
                    val bytes = value.toByteArray(Charsets.UTF_8)
                    dos.writeInt(bytes.size)
                    dos.write(bytes)
                }
            }

            fun writeDecimal(tag: Short, value: String?) {
                if (value == null) {
                    writeNull(tag)
                } else {
                    dos.writeShort(tag.toInt())
                    dos.writeByte(TYPE_DECIMAL.toInt())
                    val canonicalDecimal = try {
                        val bd = BigDecimal(value.trim())
                        if (bd.compareTo(BigDecimal.ZERO) == 0) "0" else bd.stripTrailingZeros().toPlainString()
                    } catch (_: Exception) {
                        value.trim()
                    }
                    val bytes = canonicalDecimal.toByteArray(Charsets.UTF_8)
                    dos.writeInt(bytes.size)
                    dos.write(bytes)
                }
            }

            fun writeInt(tag: Short, value: Int) {
                dos.writeShort(tag.toInt())
                dos.writeByte(TYPE_INT.toInt())
                dos.writeInt(4)
                dos.writeInt(value)
            }

            fun writeLong(tag: Short, value: Long) {
                dos.writeShort(tag.toInt())
                dos.writeByte(TYPE_LONG.toInt())
                dos.writeInt(8)
                dos.writeLong(value)
            }

            fun writeBoolean(tag: Short, value: Boolean) {
                dos.writeShort(tag.toInt())
                dos.writeByte(TYPE_BOOLEAN.toInt())
                dos.writeInt(1)
                dos.writeBoolean(value)
            }

            // 38 Documented Canonical Fields (stable documented order):
            // 1. Product & Supplier identity (case-insensitive duplicate semantics)
            writeString(1, entity.productName.trim().lowercase())
            writeString(2, entity.supplierName.trim().lowercase())
            writeLong(3, entity.timestamp)

            // 2. Purchase configuration & pricing
            writeDecimal(4, entity.ptr)
            writeDecimal(5, entity.purchaseTaxRate)
            writeString(6, entity.purchasePriceMode.trim())
            writeString(7, entity.purchaseTaxClassification.trim())
            writeBoolean(8, entity.isInputTaxCreditEnabled)

            // 3. Selling configuration & pricing
            writeDecimal(9, entity.sellingPrice)
            writeDecimal(10, entity.sellingTaxRate)
            writeString(11, entity.sellingPriceMode.trim())
            writeInt(12, entity.quantity)

            // 4. Pack & Unit details
            writeString(13, entity.packName.trim())
            writeString(14, entity.unitName.trim())
            writeInt(15, entity.unitsPerPack)
            writeInt(16, entity.sellingQuantity)
            writeDecimal(17, entity.costPerIndividualUnit)
            writeDecimal(18, entity.revenuePerIndividualUnit)
            writeDecimal(19, entity.profitPerIndividualUnit)

            // 5. Supplier discounts and free-pack schemes
            writeBoolean(20, entity.isSupplierDiscountEnabled)
            writeBoolean(21, entity.isSupplierOfferEnabled)
            writeInt(22, entity.offerBuyQuantity)
            writeInt(23, entity.offerFreeQuantity)
            writeInt(24, entity.freePacksReceived)
            writeInt(25, entity.totalPacksReceived)
            writeDecimal(26, entity.supplierDiscountPercent)
            writeDecimal(27, entity.supplierDiscountAmount)
            writeDecimal(28, entity.effectiveCostPerReceivedPack)

            // 6. Economics, revenue, margins, and totals
            writeDecimal(29, entity.economicPurchaseCost)
            writeDecimal(30, entity.netSalesRevenue)
            writeDecimal(31, entity.profitPerUnit)
            writeDecimal(32, entity.profitMargin)
            writeDecimal(33, entity.markup)
            writeDecimal(34, entity.costPercentage)
            writeDecimal(35, entity.totalProfit)
            writeDecimal(36, entity.totalPurchaseInvoiceTotal)
            writeDecimal(37, entity.totalSellingCustomerTotal)
            writeString(38, entity.calculationBasis.trim())

            dos.flush()
            return bos.toByteArray()
        }
    }

    /**
     * Computes a deterministic SHA-256 fingerprint encompassing every material field
     * of a calculation record using length-prefixed TLV binary serialization.
     * Database [CalculationEntity.id] is intentionally excluded as it represents a surrogate auto-increment key.
     */
    fun computeContentFingerprint(entity: CalculationEntity): String {
        val encodedBytes = CanonicalFingerprintEncoder.encode(entity)
        val digest = MessageDigest.getInstance("SHA-256").digest(encodedBytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Backward-compatible alias for [computeContentFingerprint].
     */
    fun createSignature(entity: CalculationEntity): String = computeContentFingerprint(entity)
}
