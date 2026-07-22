package com.habittracker.app

import android.app.Application
import com.habittracker.app.data.AppDatabase
import com.habittracker.app.data.smoking.SmokingRepository

class HabitTrackerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val smokingRepository: SmokingRepository by lazy { SmokingRepository(database.smokingDao()) }
}
