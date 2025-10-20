package com.example.wellnest_mobile_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false,
    val sessionActive: Boolean = false
)
