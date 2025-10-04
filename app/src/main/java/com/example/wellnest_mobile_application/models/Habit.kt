package com.example.wellnest_mobile_application.models

enum class HabitType {
    TIME,   // e.g., Sleep, Meditation (measured in minutes)
    STEPS   // e.g., Walk (measured in steps)
}

data class Habit(
    val id: Int,
    var name: String,
    var isCompleted: Boolean = false,

    // Type of habit
    var type: HabitType = HabitType.TIME,

    // TIME-based fields
    var durationMinutes: Int = 0,         // goal total time (e.g. 30 minutes)
    var timeRemaining: Int = 0,           // countdown left (minutes)

    // STEPS-based fields
    var stepGoal: Int = 0,                // goal steps (e.g. 5000)
    var stepsDone: Int = 0                // current steps completed
)
