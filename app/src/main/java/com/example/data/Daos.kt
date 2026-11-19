package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM dental_products ORDER BY updatedAt DESC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM dental_products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE dental_products SET quantity = :newQuantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQuantity: Int, updatedAt: Long)

    @Query("DELETE FROM dental_products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT COUNT(*) FROM dental_products")
    suspend fun countProducts(): Int
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT 100")
    fun observeRecentMovements(): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long
}
