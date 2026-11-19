package com.example.security

import com.example.data.DentalCategory
import com.example.data.MovementType
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

object InputValidator {

    // Disallows SQL/script injection characters and control characters
    private val UNSAFE_CHARS_REGEX = Regex("[<>\"';\\\\`{}\\[\\]]")
    private val USERNAME_ALLOWED_REGEX = Regex("^[A-Za-z0-9._-]{3,32}$")
    private val DATE_ISO_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    data class LoginValidationResult(
        val usernameError: String? = null,
        val passwordError: String? = null
    ) {
        val isValid: Boolean get() = usernameError == null && passwordError == null
    }

    data class ProductValidationResult(
        val nameError: String? = null,
        val categoryError: String? = null,
        val quantityError: String? = null,
        val priceError: String? = null,
        val minStockError: String? = null,
        val expiryDateError: String? = null,
        val sanitizedName: String = "",
        val parsedCategory: DentalCategory? = null,
        val parsedQuantity: Int = 0,
        val parsedPrice: Double = 0.0,
        val parsedMinStock: Int = 5,
        val sanitizedExpiryDate: String? = null
    ) {
        val isValid: Boolean
            get() = nameError == null &&
                categoryError == null &&
                quantityError == null &&
                priceError == null &&
                minStockError == null &&
                expiryDateError == null &&
                parsedCategory != null
    }

    data class MovementValidationResult(
        val productError: String? = null,
        val quantityError: String? = null,
        val reasonError: String? = null,
        val parsedQuantity: Int = 0,
        val sanitizedReason: String = ""
    ) {
        val isValid: Boolean
            get() = productError == null && quantityError == null && reasonError == null
    }

    fun sanitizeSearch(raw: String): String {
        return raw.replace(UNSAFE_CHARS_REGEX, "").take(60)
    }

    fun validateLogin(username: String, password: String): LoginValidationResult {
        val trimmedUser = username.trim()
        val usernameError = when {
            trimmedUser.isEmpty() -> "El usuario es obligatorio."
            !USERNAME_ALLOWED_REGEX.matches(trimmedUser) -> "Usuario inválido (3–32 caracteres alfanuméricos)."
            else -> null
        }
        val passwordError = when {
            password.isEmpty() -> "La contraseña es obligatoria."
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            password.length > 64 -> "La contraseña excede el límite permitido."
            password.any { it.isISOControl() } -> "La contraseña contiene caracteres no válidos."
            else -> null
        }
        return LoginValidationResult(usernameError = usernameError, passwordError = passwordError)
    }

    fun validateProductForm(
        name: String,
        category: DentalCategory?,
        quantityText: String,
        priceText: String,
        minStockText: String,
        expiryDateText: String
    ): ProductValidationResult {
        val trimmedName = name.trim().replace(Regex("\\s+"), " ")
        val nameError = when {
            trimmedName.isEmpty() -> "El nombre del producto es obligatorio."
            trimmedName.length < 3 -> "Ingresa al menos 3 caracteres."
            trimmedName.length > 80 -> "Máximo 80 caracteres permitidos."
            UNSAFE_CHARS_REGEX.containsMatchIn(trimmedName) || trimmedName.any { it.isISOControl() } ->
                "No se permiten caracteres especiales peligrosos (< > ' \" ; \\)."
            else -> null
        }

        val categoryError = if (category == null) "Selecciona una categoría dental." else null

        val cleanQty = quantityText.trim()
        val parsedQty = cleanQty.toIntOrNull()
        val quantityError = when {
            cleanQty.isEmpty() -> "La cantidad es obligatoria."
            parsedQty == null -> "Ingresa un número entero válido."
            parsedQty < 0 -> "La cantidad no puede ser negativa."
            parsedQty > 100_000 -> "La cantidad máxima permitida es 100,000."
            else -> null
        }

        val normalizedPrice = priceText.trim().replace(',', '.')
        val parsedBigDecimal = normalizedPrice.toBigDecimalOrNull()
        val priceError = when {
            normalizedPrice.isEmpty() -> "El precio unitario es obligatorio."
            parsedBigDecimal == null -> "Ingresa un precio numérico válido (ej. 24.50)."
            parsedBigDecimal <= BigDecimal.ZERO -> "El precio debe ser mayor a 0."
            parsedBigDecimal > BigDecimal("999999.99") -> "El precio excede el límite permitido."
            parsedBigDecimal.scale() > 2 -> "Usa máximo 2 decimales."
            else -> null
        }

        val cleanMinStock = minStockText.trim()
        val parsedMinStock = if (cleanMinStock.isEmpty()) 5 else cleanMinStock.toIntOrNull()
        val minStockError = when {
            parsedMinStock == null -> "Ingresa un entero válido para el stock mínimo."
            parsedMinStock < 0 -> "El stock mínimo no puede ser negativo."
            parsedMinStock > 10_000 -> "El stock mínimo máximo es 10,000."
            else -> null
        }

        val cleanExpiry = expiryDateText.trim()
        val expiryError = when {
            cleanExpiry.isEmpty() -> null
            !DATE_ISO_REGEX.matches(cleanExpiry) -> "Usa el formato AAAA-MM-DD (ej. 2027-06-30)."
            !isValidCalendarDate(cleanExpiry) -> "La fecha de caducidad no es una fecha real."
            else -> null
        }

        return ProductValidationResult(
            nameError = nameError,
            categoryError = categoryError,
            quantityError = quantityError,
            priceError = priceError,
            minStockError = minStockError,
            expiryDateError = expiryError,
            sanitizedName = trimmedName,
            parsedCategory = category,
            parsedQuantity = parsedQty ?: 0,
            parsedPrice = parsedBigDecimal?.toDouble() ?: 0.0,
            parsedMinStock = parsedMinStock ?: 5,
            sanitizedExpiryDate = cleanExpiry.ifEmpty { null }
        )
    }

    fun validateMovement(
        productId: Long?,
        currentStock: Int,
        type: MovementType,
        quantityText: String,
        reason: String
    ): MovementValidationResult {
        val productError = if (productId == null) "Selecciona un producto del inventario." else null

        val cleanQty = quantityText.trim()
        val parsedQty = cleanQty.toIntOrNull()
        val quantityError = when {
            cleanQty.isEmpty() -> "Ingresa la cantidad del movimiento."
            parsedQty == null || parsedQty <= 0 -> "La cantidad debe ser un entero mayor a 0."
            parsedQty > 100_000 -> "Cantidad fuera del rango permitido."
            type == MovementType.EXIT && parsedQty > currentStock ->
                "Stock insuficiente (disponible: $currentStock)."
            else -> null
        }

        val trimmedReason = reason.trim().replace(Regex("\\s+"), " ")
        val reasonError = when {
            trimmedReason.length > 100 -> "El motivo debe tener máximo 100 caracteres."
            UNSAFE_CHARS_REGEX.containsMatchIn(trimmedReason) || trimmedReason.any { it.isISOControl() } ->
                "El motivo contiene caracteres no permitidos."
            else -> null
        }

        return MovementValidationResult(
            productError = productError,
            quantityError = quantityError,
            reasonError = reasonError,
            parsedQuantity = parsedQty ?: 0,
            sanitizedReason = trimmedReason
        )
    }

    fun isValidCalendarDate(isoDate: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }
            val parsed = sdf.parse(isoDate) ?: return false
            sdf.format(parsed) == isoDate
        } catch (_: Exception) {
            false
        }
    }
}
