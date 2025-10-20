package com.example.wellnest_mobile_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_records")
data class HydrationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val date: String,
    val time: String,
    val createdAt: Long = System.currentTimeMillis()
)
