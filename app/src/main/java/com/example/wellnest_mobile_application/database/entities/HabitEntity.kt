package com.example.wellnest_mobile_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wellnest_mobile_application.models.HabitType

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var isCompleted: Boolean = false,
    var type: HabitType = HabitType.TIME,
    var durationMinutes: Int = 0,
    var timeRemaining: Int = 0,
    var stepGoal: Int = 0,
    var stepsDone: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
