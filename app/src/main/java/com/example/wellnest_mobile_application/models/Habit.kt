package com.example.wellnest_mobile_application.models

enum class HabitType {
    TIME,
    STEPS
}

data class Habit(
    val id: Int,
    var name: String,
    var isCompleted: Boolean = false,


    var type: HabitType = HabitType.TIME,

    var durationMinutes: Int = 0,
    var timeRemaining: Int = 0,
    var stepGoal: Int = 0,
    var stepsDone: Int = 0
)
