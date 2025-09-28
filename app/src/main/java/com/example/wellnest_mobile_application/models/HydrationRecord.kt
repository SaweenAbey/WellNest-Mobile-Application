package com.example.wellnest_mobile_application.models

data class HydrationRecord(
    val id: Int,
    val amount: Int, // in ml
    val date: String, // Format: "yyyy-MM-dd"
    val time: String // Format: "HH:mm"
)
