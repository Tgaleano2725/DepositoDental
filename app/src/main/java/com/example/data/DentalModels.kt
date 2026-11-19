package com.example.data

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class DentalCategory(val label: String) {
    RESTAURACION("Restauración"),
    ENDODONCIA("Endodoncia"),
    ORTODONCIA("Ortodoncia"),
    ANESTESIA("Anestesia"),
    ESTERILIZACION("Esterilización"),
    INSTRUMENTAL("Instrumental"),
    DESECHABLES("Desechables"),
    PROFILAXIS("Profilaxis");

    companion object {
        fun fromStored(value: String): DentalCategory {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true)
            } ?: RESTAURACION
        }
    }
}

enum class MovementType(val code: String, val label: String) {
    ENTRY("ENTRY", "Entrada"),
    EXIT("EXIT", "Salida");

    companion object {
        fun fromCode(code: String): MovementType {
            return entries.firstOrNull { it.code == code } ?: ENTRY
        }
    }
}

enum class ExpiryStatus {
    NONE,
    VALID,
    EXPIRING_SOON,
    EXPIRED
}

data class DentalProduct(
    val id: Long = 0L,
    val name: String,
    val category: DentalCategory,
    val quantity: Int,
    val minStock: Int = 5,
    val unitPrice: Double,
    val expiryDate: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalValue: Double get() = quantity * unitPrice
    val isOutOfStock: Boolean get() = quantity <= 0
    val isLowStock: Boolean get() = quantity <= minStock

    val daysUntilExpiry: Long?
        get() = expiryDate?.let { DateUtils.daysUntil(it) }

    val expiryStatus: ExpiryStatus
        get() {
            val days = daysUntilExpiry ?: return ExpiryStatus.NONE
            return when {
                days < 0 -> ExpiryStatus.EXPIRED
                days <= 30 -> ExpiryStatus.EXPIRING_SOON
                else -> ExpiryStatus.VALID
            }
        }

    val hasAnyAlert: Boolean
        get() = isLowStock || expiryStatus == ExpiryStatus.EXPIRING_SOON || expiryStatus == ExpiryStatus.EXPIRED
}

data class StockMovement(
    val id: Long = 0L,
    val productId: Long,
    val productName: String,
    val type: MovementType,
    val quantityDelta: Int,
    val reason: String,
    val timestamp: Long
)

data class EncryptedSamplePreview(
    val productId: Long,
    val plainNameHint: String,
    val encryptedName: String,
    val encryptedCategory: String,
    val encryptedPrice: String
)

data class CategoryMetric(
    val category: DentalCategory,
    val productCount: Int,
    val totalUnits: Int,
    val totalValue: Double,
    val shareFraction: Float
)

object DateUtils {
    private const val ISO_PATTERN = "yyyy-MM-dd"

    fun daysUntil(isoDate: String): Long? {
        return try {
            val sdf = SimpleDateFormat(ISO_PATTERN, Locale.US).apply { isLenient = false }
            val targetDate = sdf.parse(isoDate) ?: return null
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val targetCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMillis)
        } catch (_: Exception) {
            null
        }
    }

    fun isoDateOffsetDays(offsetDays: Int): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        return SimpleDateFormat(ISO_PATTERN, Locale.US).format(cal.time)
    }

    fun formatTimestamp(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("es", "MX"))
        return sdf.format(Date(epochMillis))
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(amount)
    }

    fun millisToIsoDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat(ISO_PATTERN, Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(utcMillis))
    }
}
