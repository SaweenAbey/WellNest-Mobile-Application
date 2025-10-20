package com.example.wellnest_mobile_application.database.daos

import androidx.room.*
import com.example.wellnest_mobile_application.database.entities.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    
    @Query("SELECT * FROM mood_entries ORDER BY createdAt DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>>
    
    @Query("SELECT * FROM mood_entries WHERE id = :entryId")
    suspend fun getMoodEntryById(entryId: Int): MoodEntryEntity?
    
    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getMoodEntriesByDate(date: String): List<MoodEntryEntity>
    
    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getMoodEntriesByDateRange(startDate: String, endDate: String): List<MoodEntryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(moodEntry: MoodEntryEntity): Long
    
    @Update
    suspend fun updateMoodEntry(moodEntry: MoodEntryEntity)
    
    @Delete
    suspend fun deleteMoodEntry(moodEntry: MoodEntryEntity)
    
    @Query("DELETE FROM mood_entries WHERE id = :entryId")
    suspend fun deleteMoodEntryById(entryId: Int)
    
    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getTotalMoodEntriesCount(): Int
    
    @Query("SELECT COUNT(*) FROM mood_entries WHERE date = :date")
    suspend fun getMoodEntriesCountByDate(date: String): Int
    
    @Query("SELECT DISTINCT date FROM mood_entries ORDER BY date DESC")
    suspend fun getAllMoodDates(): List<String>
    
    @Query("SELECT * FROM mood_entries WHERE mood = :mood")
    suspend fun getMoodEntriesByMood(mood: String): List<MoodEntryEntity>
    
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoodEntries()
}
