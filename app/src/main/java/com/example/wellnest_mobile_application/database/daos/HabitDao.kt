package com.example.wellnest_mobile_application.database.daos

import androidx.room.*
import com.example.wellnest_mobile_application.database.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Int): HabitEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long
    
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
    
    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Int)
    
    @Query("UPDATE habits SET isCompleted = :isCompleted WHERE id = :habitId")
    suspend fun updateHabitCompletion(habitId: Int, isCompleted: Boolean)
    
    @Query("UPDATE habits SET timeRemaining = :timeRemaining WHERE id = :habitId")
    suspend fun updateHabitTimeRemaining(habitId: Int, timeRemaining: Int)
    
    @Query("UPDATE habits SET stepsDone = :stepsDone WHERE id = :habitId")
    suspend fun updateHabitStepsDone(habitId: Int, stepsDone: Int)
    
    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getTotalHabitsCount(): Int
    
    @Query("SELECT COUNT(*) FROM habits WHERE isCompleted = 1")
    suspend fun getCompletedHabitsCount(): Int
    
    @Query("SELECT * FROM habits WHERE isCompleted = 1")
    suspend fun getCompletedHabits(): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE isCompleted = 0")
    suspend fun getPendingHabits(): List<HabitEntity>
    
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
