package com.example.wellnest_mobile_application.models

data class User(
    val fullName: String,
    val email: String,
    val password: String,
    val registrationDate: Long = System.currentTimeMillis()
)