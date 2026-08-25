package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.data.model.SearchHistory
import com.example.data.model.StockLog

@Database(
    entities = [
        Plant::class,
        Customer::class,
        CustomerPurchase::class,
        Sale::class,
        Expense::class,
        StockLog::class,
        SearchHistory::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerPurchaseDao(): CustomerPurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockLogDao(): StockLogDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahnur_nursery_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
