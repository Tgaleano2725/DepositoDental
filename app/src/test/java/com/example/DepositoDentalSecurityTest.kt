package com.example

import com.example.data.DentalCategory
import com.example.data.MovementType
import com.example.security.CryptoManager
import com.example.security.InputValidator
import com.example.security.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DepositoDentalSecurityTest {

    @Test
    fun passwordHasher_verifiesExactRequestedCredentials() {
        val secret = "c@n\$ult0ri02022"
        val hashed = PasswordHasher.hashPassword(secret)

        assertNotEquals(secret, hashed.hashBase64)
        assertTrue(PasswordHasher.verifyPassword(secret, hashed.hashBase64, hashed.saltBase64))
        assertFalse(PasswordHasher.verifyPassword("wrongPassword123", hashed.hashBase64, hashed.saltBase64))
    }

    @Test
    fun cryptoManager_encryptsWithUniqueIvAndDecryptsCleanly() {
        val crypto = CryptoManager(keyAlias = "test_deposito_dental_key")
        val plain = "Resina Compuesta 3M Filtek Z350 A2"

        val cipher1 = crypto.encrypt(plain)
        val cipher2 = crypto.encrypt(plain)

        assertTrue(cipher1.startsWith("enc:v1:"))
        assertNotEquals(plain, cipher1)
        assertNotEquals(cipher1, cipher2) // Unique random IV each time
        assertEquals(plain, crypto.decrypt(cipher1))
        assertEquals(plain, crypto.decrypt(cipher2))
    }

    @Test
    fun productValidator_rejectsInjectionAndInvalidInputs() {
        val bad = InputValidator.validateProductForm(
            name = "Resina'; DROP TABLE dental_products;--",
            category = DentalCategory.RESTAURACION,
            quantityText = "-5",
            priceText = "0.00",
            minStockText = "-1",
            expiryDateText = "2027-02-30"
        )

        assertFalse(bad.isValid)
        assertTrue(bad.nameError != null)
        assertTrue(bad.quantityError != null)
        assertTrue(bad.priceError != null)
        assertTrue(bad.minStockError != null)
        assertTrue(bad.expiryDateError != null)

        val good = InputValidator.validateProductForm(
            name = "Anestesia Lidocaína 2%",
            category = DentalCategory.ANESTESIA,
            quantityText = "12",
            priceText = "34.50",
            minStockText = "5",
            expiryDateText = "2027-08-15"
        )

        assertTrue(good.isValid)
        assertNull(good.nameError)
        assertEquals(12, good.parsedQuantity)
        assertEquals(34.50, good.parsedPrice, 0.001)
    }

    @Test
    fun movementValidator_blocksNegativeStockExit() {
        val overExit = InputValidator.validateMovement(
            productId = 1L,
            currentStock = 3,
            type = MovementType.EXIT,
            quantityText = "5",
            reason = "Consultorio 1"
        )
        assertFalse(overExit.isValid)
        assertTrue(overExit.quantityError?.contains("insuficiente") == true)

        val validExit = InputValidator.validateMovement(
            productId = 1L,
            currentStock = 3,
            type = MovementType.EXIT,
            quantityText = "2",
            reason = "Consultorio 1"
        )
        assertTrue(validExit.isValid)
    }
}
