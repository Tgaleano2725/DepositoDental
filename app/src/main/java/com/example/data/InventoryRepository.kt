package com.example.data

import androidx.room.withTransaction
import com.example.security.CryptoManager
import com.example.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class InventoryRepository(
    private val database: DepositoDentalDatabase,
    val cryptoManager: CryptoManager
) {
    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()

    val productsFlow: Flow<List<DentalProduct>> = productDao.observeAllProducts()
        .map { entities ->
            entities.mapNotNull { entity -> decryptProduct(entity) }
        }

    val encryptedSampleFlow: Flow<EncryptedSamplePreview?> = productDao.observeAllProducts()
        .map { entities ->
            val first = entities.firstOrNull() ?: return@map null
            val decrypted = decryptProduct(first)
            EncryptedSamplePreview(
                productId = first.id,
                plainNameHint = decrypted?.name ?: "Producto #${first.id}",
                encryptedName = first.encryptedName,
                encryptedCategory = first.encryptedCategory,
                encryptedPrice = first.encryptedUnitPrice
            )
        }

    val movementsFlow: Flow<List<StockMovement>> = movementDao.observeRecentMovements()
        .map { entities ->
            entities.map { entity ->
                StockMovement(
                    id = entity.id,
                    productId = entity.productId,
                    productName = safeDecrypt(entity.encryptedProductName, "Producto #${entity.productId}"),
                    type = MovementType.fromCode(entity.type),
                    quantityDelta = entity.quantityDelta,
                    reason = safeDecrypt(entity.encryptedReason, ""),
                    timestamp = entity.timestamp
                )
            }
        }

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        ensureDefaultAdminExists()
        seedSampleCatalogIfEmpty()
    }

    private suspend fun ensureDefaultAdminExists() {
        val existing = userDao.getByUsername(DEFAULT_ADMIN_USERNAME)
        if (existing == null) {
            val hashed = PasswordHasher.hashPassword(DEFAULT_ADMIN_PASSWORD)
            userDao.insertUser(
                UserEntity(
                    username = DEFAULT_ADMIN_USERNAME,
                    passwordHash = hashed.hashBase64,
                    passwordSalt = hashed.saltBase64
                )
            )
        }
    }

    suspend fun authenticate(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        ensureDefaultAdminExists()
        val user = userDao.getByUsername(username.trim()) ?: return@withContext false
        PasswordHasher.verifyPassword(
            password = password,
            expectedHashBase64 = user.passwordHash,
            saltBase64 = user.passwordSalt
        )
    }

    suspend fun createProduct(
        name: String,
        category: DentalCategory,
        quantity: Int,
        minStock: Int,
        unitPrice: Double,
        expiryDate: String?
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val productId = productDao.insertProduct(
                ProductEntity(
                    encryptedName = cryptoManager.encrypt(name),
                    encryptedCategory = cryptoManager.encrypt(category.name),
                    quantity = quantity,
                    minStock = minStock,
                    encryptedUnitPrice = cryptoManager.encrypt(String.format(Locale.US, "%.2f", unitPrice)),
                    encryptedExpiryDate = expiryDate?.let { cryptoManager.encrypt(it) },
                    updatedAt = now
                )
            )
            if (quantity > 0) {
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = productId,
                        encryptedProductName = cryptoManager.encrypt(name),
                        type = MovementType.ENTRY.code,
                        quantityDelta = quantity,
                        encryptedReason = cryptoManager.encrypt("Alta inicial en inventario"),
                        timestamp = now
                    )
                )
            }
            productId
        }
    }

    suspend fun updateProduct(
        id: Long,
        name: String,
        category: DentalCategory,
        quantity: Int,
        minStock: Int,
        unitPrice: Double,
        expiryDate: String?
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val previous = productDao.getProductById(id)
            productDao.updateProduct(
                ProductEntity(
                    id = id,
                    encryptedName = cryptoManager.encrypt(name),
                    encryptedCategory = cryptoManager.encrypt(category.name),
                    quantity = quantity,
                    minStock = minStock,
                    encryptedUnitPrice = cryptoManager.encrypt(String.format(Locale.US, "%.2f", unitPrice)),
                    encryptedExpiryDate = expiryDate?.let { cryptoManager.encrypt(it) },
                    updatedAt = now
                )
            )
            if (previous != null && previous.quantity != quantity) {
                val delta = quantity - previous.quantity
                val type = if (delta > 0) MovementType.ENTRY else MovementType.EXIT
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = id,
                        encryptedProductName = cryptoManager.encrypt(name),
                        type = type.code,
                        quantityDelta = kotlin.math.abs(delta),
                        encryptedReason = cryptoManager.encrypt("Ajuste manual al editar producto"),
                        timestamp = now
                    )
                )
            }
        }
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun recordStockMovement(
        productId: Long,
        type: MovementType,
        quantityDelta: Int,
        reason: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val newQty = database.withTransaction {
                val entity = productDao.getProductById(productId)
                    ?: throw IllegalStateException("El producto ya no existe.")
                val decryptedName = safeDecrypt(entity.encryptedName, "Producto #$productId")
                val updatedQuantity = when (type) {
                    MovementType.ENTRY -> entity.quantity + quantityDelta
                    MovementType.EXIT -> {
                        if (quantityDelta > entity.quantity) {
                            throw IllegalArgumentException("Stock insuficiente (disponible: ${entity.quantity}).")
                        }
                        entity.quantity - quantityDelta
                    }
                }
                val now = System.currentTimeMillis()
                productDao.updateQuantity(productId, updatedQuantity, now)
                val finalReason = reason.ifBlank {
                    if (type == MovementType.ENTRY) "Reposición de almacén" else "Consumo en consultorio"
                }
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = productId,
                        encryptedProductName = cryptoManager.encrypt(decryptedName),
                        type = type.code,
                        quantityDelta = quantityDelta,
                        encryptedReason = cryptoManager.encrypt(finalReason),
                        timestamp = now
                    )
                )
                updatedQuantity
            }
            Result.success(newQty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedSampleCatalogIfEmpty() {
        if (productDao.countProducts() > 0) return
        val now = System.currentTimeMillis()
        val seedItems = listOf(
            SeedItem("Resina Compuesta 3M Filtek Z350 A2", DentalCategory.RESTAURACION, 18, 6, 48.50, DateUtils.isoDateOffsetDays(290)),
            SeedItem("Anestesia Lidocaína 2% c/Epinefrina x50", DentalCategory.ANESTESIA, 3, 8, 32.00, DateUtils.isoDateOffsetDays(18)),
            SeedItem("Guantes de Nitrilo Azules M Caja x100", DentalCategory.DESECHABLES, 24, 10, 11.75, DateUtils.isoDateOffsetDays(420)),
            SeedItem("Ácido Grabador Fosfórico 37% Jeringa", DentalCategory.RESTAURACION, 4, 5, 8.90, DateUtils.isoDateOffsetDays(12)),
            SeedItem("Limas Endodónticas K-File #15-40", DentalCategory.ENDODONCIA, 14, 5, 19.40, null),
            SeedItem("Brackets Metálicos Roth 0.022 Kit", DentalCategory.ORTODONCIA, 9, 4, 27.80, null),
            SeedItem("Bolsas de Esterilización Autoclave x200", DentalCategory.ESTERILIZACION, 2, 6, 16.50, DateUtils.isoDateOffsetDays(210)),
            SeedItem("Espejo Bucal #5 Acero Inoxidable", DentalCategory.INSTRUMENTAL, 15, 5, 9.25, null),
            SeedItem("Alginato Cromático Impresión 454g", DentalCategory.RESTAURACION, 6, 4, 14.90, DateUtils.isoDateOffsetDays(-4)),
            SeedItem("Pasta Profiláctica Fluorada Menta", DentalCategory.PROFILAXIS, 11, 5, 12.40, DateUtils.isoDateOffsetDays(155))
        )

        database.withTransaction {
            seedItems.forEachIndexed { idx, item ->
                val timestamp = now - ((seedItems.size - idx) * 3_600_000L)
                val productId = productDao.insertProduct(
                    ProductEntity(
                        encryptedName = cryptoManager.encrypt(item.name),
                        encryptedCategory = cryptoManager.encrypt(item.category.name),
                        quantity = item.quantity,
                        minStock = item.minStock,
                        encryptedUnitPrice = cryptoManager.encrypt(String.format(Locale.US, "%.2f", item.price)),
                        encryptedExpiryDate = item.expiryDate?.let { cryptoManager.encrypt(it) },
                        updatedAt = timestamp
                    )
                )
                if (idx < 4) {
                    val isExit = idx % 2 == 1
                    movementDao.insertMovement(
                        StockMovementEntity(
                            productId = productId,
                            encryptedProductName = cryptoManager.encrypt(item.name),
                            type = if (isExit) MovementType.EXIT.code else MovementType.ENTRY.code,
                            quantityDelta = if (isExit) 2 else 10,
                            encryptedReason = cryptoManager.encrypt(
                                if (isExit) "Entrega a consultorio 1" else "Compra a proveedor dental"
                            ),
                            timestamp = timestamp + 900_000L
                        )
                    )
                }
            }
        }
    }

    private fun decryptProduct(entity: ProductEntity): DentalProduct? {
        return try {
            val name = cryptoManager.decrypt(entity.encryptedName)
            val category = DentalCategory.fromStored(cryptoManager.decrypt(entity.encryptedCategory))
            val price = cryptoManager.decrypt(entity.encryptedUnitPrice).toDoubleOrNull() ?: 0.0
            val expiry = entity.encryptedExpiryDate?.let { cryptoManager.decrypt(it) }?.ifBlank { null }
            DentalProduct(
                id = entity.id,
                name = name,
                category = category,
                quantity = entity.quantity,
                minStock = entity.minStock,
                unitPrice = price,
                expiryDate = expiry,
                updatedAt = entity.updatedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun safeDecrypt(cipherText: String, fallback: String): String {
        return try {
            cryptoManager.decrypt(cipherText)
        } catch (_: Exception) {
            fallback
        }
    }

    private data class SeedItem(
        val name: String,
        val category: DentalCategory,
        val quantity: Int,
        val minStock: Int,
        val price: Double,
        val expiryDate: String?
    )

    companion object {
        const val DEFAULT_ADMIN_USERNAME = "Andre05"
        private const val DEFAULT_ADMIN_PASSWORD = "c@n\$ult0ri02022"
    }
}
