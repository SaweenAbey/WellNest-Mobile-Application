package com.example.wellnest_mobile_application.database.repositories

import com.example.wellnest_mobile_application.database.WellnestDatabase
import com.example.wellnest_mobile_application.database.daos.MoodEntryDao
import com.example.wellnest_mobile_application.database.entities.MoodEntryEntity
import com.example.wellnest_mobile_application.models.MoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoodEntryRepository(private val database: WellnestDatabase) {
    
    private val moodEntryDao: MoodEntryDao = database.moodEntryDao()
    
    fun getMoodEntries(): Flow<List<MoodEntry>> {
        return moodEntryDao.getAllMoodEntries().map { entities ->
            entities.map { entity ->
                MoodEntry(
                    id = entity.id,
                    mood = entity.mood,
                    emoji = entity.emoji,
                    date = entity.date,
                    time = entity.time,
                    note = entity.note,
                    durationMinutes = entity.durationMinutes
                )
            }
        }
    }
    
    suspend fun saveMoodEntry(moodEntry: MoodEntry): Long {
        val moodEntryEntity = MoodEntryEntity(
            mood = moodEntry.mood,
            emoji = moodEntry.emoji,
            date = moodEntry.date,
            time = moodEntry.time,
            note = moodEntry.note,
            durationMinutes = moodEntry.durationMinutes
        )
        return moodEntryDao.insertMoodEntry(moodEntryEntity)
    }
    
    suspend fun getMoodEntriesByDate(date: String): List<MoodEntry> {
        val entities = moodEntryDao.getMoodEntriesByDate(date)
        return entities.map { entity ->
            MoodEntry(
                id = entity.id,
                mood = entity.mood,
                emoji = entity.emoji,
                date = entity.date,
                time = entity.time,
                note = entity.note,
                durationMinutes = entity.durationMinutes
            )
        }
    }
    
    suspend fun getMoodScore(mood: String): Float {
        return when (mood.lowercase()) {
            "excited" -> 5f
            "happy" -> 4f
            "calm" -> 3f
            "sad" -> 2f
            "tired" -> 2f
            "angry" -> 1f
            "anxious" -> 1f
            else -> 3f
        }
    }
    
    suspend fun getMoodTrendData(days: Int = 7): List<Pair<String, Float>> {
        val trendData = mutableListOf<Pair<String, Float>>()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days + 1)
        
        repeat(days) {
            val date = dateFormat.format(calendar.time)
            val dayMoods = getMoodEntriesByDate(date)
            
            val averageScore = if (dayMoods.isNotEmpty()) {
                dayMoods.map { getMoodScore(it.mood) }.average().toFloat()
            } else {
                3f
            }
            
            trendData.add(Pair(date, averageScore))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        return trendData
    }
    
    suspend fun getWeeklyMoodAverage(): Float {
        val trendData = getMoodTrendData(7)
        return if (trendData.isNotEmpty()) {
            trendData.map { it.second }.average().toFloat()
        } else {
            3f
        }
    }
    
    suspend fun deleteMoodEntryById(entryId: Int) {
        moodEntryDao.deleteMoodEntryById(entryId)
    }
    
    suspend fun clearAllMoodEntries() {
        moodEntryDao.deleteAllMoodEntries()
    }
}
