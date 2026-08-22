package com.sahnurnursery.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sahnurnursery.app.dao.CustomerDao
import com.sahnurnursery.app.dao.ExpenseDao
import com.sahnurnursery.app.dao.PlantDao
import com.sahnurnursery.app.dao.SalesDao
import com.sahnurnursery.app.dao.StockDao
import com.sahnurnursery.app.entity.CustomerEntity
import com.sahnurnursery.app.entity.ExpenseEntity
import com.sahnurnursery.app.entity.PlantEntity
import com.sahnurnursery.app.entity.SalesEntity
import com.sahnurnursery.app.entity.StockEntity

@Database(
    entities = [
        PlantEntity::class,
        CustomerEntity::class,
        SalesEntity::class,
        ExpenseEntity::class,
        StockEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun customerDao(): CustomerDao
    abstract fun salesDao(): SalesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahnur_nursery_main_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
