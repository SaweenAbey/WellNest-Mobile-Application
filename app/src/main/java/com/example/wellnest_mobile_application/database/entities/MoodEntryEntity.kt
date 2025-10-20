package com.example.wellnest_mobile_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mood: String,
    val emoji: String,
    val date: String,
    val time: String,
    val note: String = "",
    val durationMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
