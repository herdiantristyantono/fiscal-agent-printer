package com.android.fiscalagenttixtax.data.fiscal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventDao
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventEntity

@Database(
    entities = [FiscalEventEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FiscalDatabase : RoomDatabase() {

    abstract fun fiscalEventDao(): FiscalEventDao

    companion object {
        @Volatile
        private var INSTANCE: FiscalDatabase? = null

        fun getInstance(context: Context): FiscalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FiscalDatabase::class.java,
                    "fiscal.db"
                )
                    .fallbackToDestructiveMigration() // dev only
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}