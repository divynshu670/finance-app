package com.example.financecompanion.data.local.db

import android.content.Context
import androidx.room.Room
import com.example.financecompanion.dev.DevDataSeeder

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "finance_companion_db"
            ).build()
            DevDataSeeder.seedIfNeeded(context.applicationContext, instance)
            INSTANCE = instance
            instance
        }
    }
}
