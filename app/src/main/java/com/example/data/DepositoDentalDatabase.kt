package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DepositoDentalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        private const val DATABASE_NAME = "deposito_dental_secure.db"

        @Volatile
        private var INSTANCE: DepositoDentalDatabase? = null

        fun getInstance(context: Context): DepositoDentalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DepositoDentalDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
