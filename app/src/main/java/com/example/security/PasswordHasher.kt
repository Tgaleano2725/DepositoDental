package com.example.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and verifies passwords using salted PBKDF2WithHmacSHA256
 * with constant-time comparison to resist timing attacks.
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    data class HashedSecret(
        val hashBase64: String,
        val saltBase64: String
    )

    fun hashPassword(password: String): HashedSecret {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = deriveKey(password, salt)
        return HashedSecret(
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP),
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    fun verifyPassword(
        password: String,
        expectedHashBase64: String,
        saltBase64: String
    ): Boolean {
        return try {
            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            val expectedHash = Base64.decode(expectedHashBase64, Base64.NO_WRAP)
            val candidateHash = deriveKey(password, salt)
            MessageDigest.isEqual(candidateHash, expectedHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
