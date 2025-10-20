package com.example.wellnest_mobile_application.database

import androidx.room.TypeConverter
import com.example.wellnest_mobile_application.models.HabitType

class Converters {
    
    @TypeConverter
    fun fromHabitType(habitType: HabitType): String {
        return habitType.name
    }
    
    @TypeConverter
    fun toHabitType(habitType: String): HabitType {
        return HabitType.valueOf(habitType)
    }
}
