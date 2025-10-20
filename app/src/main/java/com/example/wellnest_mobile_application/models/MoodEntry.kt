package com.example.wellnest_mobile_application.models

data class MoodEntry(
    val id: Int,
    val mood: String,
    val emoji: String,
    val date: String,
    val time: String,
    val note: String = "",
    val durationMinutes: Int = 0
)
