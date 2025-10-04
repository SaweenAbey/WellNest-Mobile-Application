package com.example.wellnest_mobile_application.models

data class MoodEntry(
    val id: Int,
    val mood: String,
    val emoji: String,
    val date: String, // Format: "yyyy-MM-dd"
    val time: String, // Format: "HH:mm"
    val note: String = "",
    val durationMinutes: Int = 0
)
