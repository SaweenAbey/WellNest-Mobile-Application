package com.example.wellnest_mobile_application.data

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnest_mobile_application.models.User
import com.example.wellnest_mobile_application.models.Habit
import com.example.wellnest_mobile_application.models.MoodEntry
import com.example.wellnest_mobile_application.models.HydrationRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "WellnestAppData"
        private const val KEY_USER = "current_user"
        private const val KEY_HABITS = "user_habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_RECORDS = "hydration_records"
        private const val KEY_WATER_GOAL = "daily_water_goal"
        private const val KEY_NOTIFICATION_ENABLED = "notifications_enabled"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // ===== USER MANAGEMENT =====
    fun saveUser(user: User): Boolean {
        val userJson = gson.toJson(user)
        return sharedPreferences.edit().putString(KEY_USER, userJson).commit()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) gson.fromJson(userJson, User::class.java) else null
    }

    fun logout() {
        sharedPreferences.edit().remove(KEY_USER).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains(KEY_USER)
    }

    // ===== HABIT MANAGEMENT =====
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS, habitsJson).apply()
    }

    fun getHabits(): MutableList<Habit> {
        val habitsJson = sharedPreferences.getString(KEY_HABITS, "[]")
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(habitsJson, type) ?: mutableListOf()
    }

    fun addHabit(habit: Habit) {
        val currentHabits = getHabits()
        // Generate unique ID
        val newId = if (currentHabits.isNotEmpty()) currentHabits.maxOf { it.id } + 1 else 1
        val newHabit = habit.copy(id = newId, isCompleted = false)
        currentHabits.add(newHabit)
        saveHabits(currentHabits)
    }

    fun updateHabit(updatedHabit: Habit) {
        val currentHabits = getHabits().map { habit ->
            if (habit.id == updatedHabit.id) updatedHabit else habit
        }
        saveHabits(currentHabits)
    }

    fun deleteHabit(habitId: Int) {
        val currentHabits = getHabits().filter { it.id != habitId }
        saveHabits(currentHabits)
    }

    fun toggleHabitCompletion(habitId: Int, completed: Boolean) {
        val currentHabits = getHabits().map { habit ->
            if (habit.id == habitId) habit.copy(isCompleted = completed) else habit
        }
        saveHabits(currentHabits)
    }

    fun getHabitById(habitId: Int): Habit? {
        return getHabits().find { it.id == habitId }
    }

    fun getHabitCompletionRate(): Float {
        val habits = getHabits()
        if (habits.isEmpty()) return 0f
        val completed = habits.count { it.isCompleted }
        return (completed.toFloat() / habits.size.toFloat()) * 100
    }

    // ===== MOOD JOURNAL MANAGEMENT =====
    fun saveMoodEntry(moodEntry: MoodEntry) {
        val currentEntries = getMoodEntries().toMutableList()
        currentEntries.add(moodEntry)
        val entriesJson = gson.toJson(currentEntries)
        sharedPreferences.edit().putString(KEY_MOOD_ENTRIES, entriesJson).apply()
    }

    fun getMoodEntries(): List<MoodEntry> {
        val entriesJson = sharedPreferences.getString(KEY_MOOD_ENTRIES, "[]")
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(entriesJson, type) ?: emptyList()
    }

    fun getMoodEntriesByDate(date: String): List<MoodEntry> {
        return getMoodEntries().filter { it.date == date }
    }

    // ===== HYDRATION TRACKER MANAGEMENT =====
    fun saveHydrationRecord(record: HydrationRecord) {
        val currentRecords = getHydrationRecords().toMutableList()
        currentRecords.add(record)
        val recordsJson = gson.toJson(currentRecords)
        sharedPreferences.edit().putString(KEY_HYDRATION_RECORDS, recordsJson).apply()
    }

    fun getHydrationRecords(): List<HydrationRecord> {
        val recordsJson = sharedPreferences.getString(KEY_HYDRATION_RECORDS, "[]")
        val type = object : TypeToken<List<HydrationRecord>>() {}.type
        return gson.fromJson(recordsJson, type) ?: emptyList()
    }

    fun getTodayWaterIntake(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return getHydrationRecords()
            .filter { it.date == today }
            .sumOf { it.amount }
    }

    // ===== SETTINGS MANAGEMENT =====
    fun setDailyWaterGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_WATER_GOAL, goal).apply()
    }

    fun getDailyWaterGoal(): Int {
        return sharedPreferences.getInt(KEY_WATER_GOAL, 2000) // Default 2000ml
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    // ===== UTILITY METHODS =====
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}
