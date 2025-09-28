package com.example.wellnest_mobile_application.models

data class Habit(
    val id: Int,
    var name: String,
    var isCompleted: Boolean = false,
    var durationMinutes: Int = 0,         // total time (e.g. 30 minutes meditation)
    var timeRemaining: Int = 0            // countdown left
)
