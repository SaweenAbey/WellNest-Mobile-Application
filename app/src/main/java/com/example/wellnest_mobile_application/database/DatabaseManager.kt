package com.example.wellnest_mobile_application.database

import android.content.Context
import com.example.wellnest_mobile_application.database.repositories.*

class DatabaseManager(private val context: Context) {
    
    private val database: WellnestDatabase = WellnestDatabase.getInstance(context)
    
    val userRepository: UserRepository by lazy { UserRepository(database) }
    val habitRepository: HabitRepository by lazy { HabitRepository(database) }
    val moodEntryRepository: MoodEntryRepository by lazy { MoodEntryRepository(database) }
    val hydrationRecordRepository: HydrationRecordRepository by lazy { HydrationRecordRepository(database) }
    val appSettingsRepository: AppSettingsRepository by lazy { AppSettingsRepository(database) }
    
    suspend fun initializeDatabase() {
        appSettingsRepository.initializeDefaultSettings()
    }
    
    suspend fun clearAllData() {
        userRepository.clearAllData()
        habitRepository.clearAllHabits()
        moodEntryRepository.clearAllMoodEntries()
        hydrationRecordRepository.clearAllHydrationRecords()
        appSettingsRepository.clearAllSettings()
    }
}
