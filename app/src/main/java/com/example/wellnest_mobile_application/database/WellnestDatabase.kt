package com.example.wellnest_mobile_application.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wellnest_mobile_application.database.daos.*
import com.example.wellnest_mobile_application.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        MoodEntryEntity::class,
        HydrationRecordEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellnestDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun hydrationRecordDao(): HydrationRecordDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: WellnestDatabase? = null
        
        fun getInstance(context: Context): WellnestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnestDatabase::class.java,
                    "wellnest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
