package com.example.wellnest_mobile_application.data

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnest_mobile_application.models.User
import com.example.wellnest_mobile_application.models.Habit
import com.example.wellnest_mobile_application.models.MoodEntry
import com.example.wellnest_mobile_application.models.HydrationRecord
import com.example.wellnest_mobile_application.models.HabitType
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
        private const val KEY_HYDRATION_REMINDER_ENABLED = "hydration_reminder_enabled"
        private const val KEY_HYDRATION_REMINDER_INTERVAL = "hydration_reminder_interval"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
        private const val KEY_SNAP_DURATION = "snap_duration"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

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

    fun updateHabitSteps(habitId: Int, newSteps: Int) {
        val currentHabits = getHabits().map { habit ->
            if (habit.id == habitId && habit.type == HabitType.STEPS) {
                // Auto-complete if goal is reached
                val isCompleted = newSteps >= habit.stepGoal
                habit.copy(stepsDone = newSteps, isCompleted = isCompleted)
            } else {
                habit
            }
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

    fun setDailyWaterGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_WATER_GOAL, goal).apply()
    }

    fun getDailyWaterGoal(): Int {
        return sharedPreferences.getInt(KEY_WATER_GOAL, 4000)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    fun setHydrationReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HYDRATION_REMINDER_ENABLED, enabled).apply()
    }

    fun isHydrationReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HYDRATION_REMINDER_ENABLED, false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE_ENABLED, false)
    }

    fun setHydrationReminderInterval(intervalMinutes: Int) {
        sharedPreferences.edit().putInt(KEY_HYDRATION_REMINDER_INTERVAL, intervalMinutes).apply()
    }

    fun getHydrationReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_HYDRATION_REMINDER_INTERVAL, 60) // Default 60 minutes
    }

    fun setSnapDuration(durationSeconds: Int) {
        sharedPreferences.edit().putInt(KEY_SNAP_DURATION, durationSeconds).apply()
    }

    fun getSnapDuration(): Int {
        return sharedPreferences.getInt(KEY_SNAP_DURATION, 60) // Default 60 seconds
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}