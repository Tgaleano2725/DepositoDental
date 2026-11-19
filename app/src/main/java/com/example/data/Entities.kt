package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dental_products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val encryptedName: String,
    val encryptedCategory: String,
    val quantity: Int,
    val minStock: Int,
    val encryptedUnitPrice: String,
    val encryptedExpiryDate: String?,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val encryptedProductName: String,
    val type: String,
    val quantityDelta: Int,
    val encryptedReason: String,
    val timestamp: Long = System.currentTimeMillis()
)
