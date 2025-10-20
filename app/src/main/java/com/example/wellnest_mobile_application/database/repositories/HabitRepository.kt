package com.example.wellnest_mobile_application.database.repositories

import com.example.wellnest_mobile_application.database.WellnestDatabase
import com.example.wellnest_mobile_application.database.daos.HabitDao
import com.example.wellnest_mobile_application.database.entities.HabitEntity
import com.example.wellnest_mobile_application.models.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val database: WellnestDatabase) {
    
    private val habitDao: HabitDao = database.habitDao()
    
    fun getHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { entity ->
                Habit(
                    id = entity.id,
                    name = entity.name,
                    isCompleted = entity.isCompleted,
                    type = entity.type,
                    durationMinutes = entity.durationMinutes,
                    timeRemaining = entity.timeRemaining,
                    stepGoal = entity.stepGoal,
                    stepsDone = entity.stepsDone
                )
            }
        }
    }
    
    suspend fun addHabit(habit: Habit): Long {
        val habitEntity = HabitEntity(
            name = habit.name,
            isCompleted = habit.isCompleted,
            type = habit.type,
            durationMinutes = habit.durationMinutes,
            timeRemaining = habit.timeRemaining,
            stepGoal = habit.stepGoal,
            stepsDone = habit.stepsDone
        )
        return habitDao.insertHabit(habitEntity)
    }
    
    suspend fun updateHabit(habit: Habit) {
        val habitEntity = HabitEntity(
            id = habit.id,
            name = habit.name,
            isCompleted = habit.isCompleted,
            type = habit.type,
            durationMinutes = habit.durationMinutes,
            timeRemaining = habit.timeRemaining,
            stepGoal = habit.stepGoal,
            stepsDone = habit.stepsDone
        )
        habitDao.updateHabit(habitEntity)
    }
    
    suspend fun deleteHabit(habitId: Int) {
        habitDao.deleteHabitById(habitId)
    }
    
    suspend fun updateHabitCompletion(habitId: Int, isCompleted: Boolean) {
        habitDao.updateHabitCompletion(habitId, isCompleted)
    }
    
    suspend fun updateHabitTimeRemaining(habitId: Int, timeRemaining: Int) {
        habitDao.updateHabitTimeRemaining(habitId, timeRemaining)
    }
    
    suspend fun updateHabitStepsDone(habitId: Int, stepsDone: Int) {
        habitDao.updateHabitStepsDone(habitId, stepsDone)
    }
    
    suspend fun getHabitCompletionPercentage(): Float {
        val totalHabits = habitDao.getTotalHabitsCount()
        val completedHabits = habitDao.getCompletedHabitsCount()
        return if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits.toFloat()) * 100
        } else {
            0f
        }
    }
    
    suspend fun clearAllHabits() {
        habitDao.deleteAllHabits()
    }
}
