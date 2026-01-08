package com.example.bloodbank

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EmergencyRequest::class, BloodBank::class],
    version = 4,
    exportSchema = false
)

abstract class EmergencyDatabase : RoomDatabase() {

    abstract fun emergencyRequestDao(): EmergencyRequestDao
    abstract fun bloodBankDao(): BloodBankDao

    companion object {
        @Volatile
        private var INSTANCE: EmergencyDatabase? = null

        fun getDatabase(context: Context): EmergencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmergencyDatabase::class.java,
                    "emergency_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
