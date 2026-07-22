package com.habittracker.app.data.smoking

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromQuitPlanType(type: QuitPlanType): String = type.name

    @TypeConverter
    fun toQuitPlanType(value: String): QuitPlanType = QuitPlanType.valueOf(value)
}
