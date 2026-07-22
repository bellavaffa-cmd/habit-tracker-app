package com.habittracker.app

import android.app.Application
import com.habittracker.app.data.AppDatabase
import com.habittracker.app.data.calories.CalorieLogRepository
import com.habittracker.app.data.calories.CaloriesSettingsRepository
import com.habittracker.app.data.smoking.CigarettePurchaseRepository
import com.habittracker.app.data.smoking.QuitPlanRepository
import com.habittracker.app.data.smoking.SmokingRepository
import com.habittracker.app.data.smoking.SmokingSettingsRepository
import com.habittracker.app.update.UpdateManager

class HabitTrackerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val smokingRepository: SmokingRepository by lazy { SmokingRepository(database.smokingDao()) }
    val smokingSettingsRepository: SmokingSettingsRepository by lazy { SmokingSettingsRepository(this) }
    val cigarettePurchaseRepository: CigarettePurchaseRepository by lazy { CigarettePurchaseRepository(database.cigarettePurchaseDao()) }
    val quitPlanRepository: QuitPlanRepository by lazy { QuitPlanRepository(database.quitPlanDao()) }
    val updateManager: UpdateManager by lazy { UpdateManager(this) }
    val calorieLogRepository: CalorieLogRepository by lazy { CalorieLogRepository(database.calorieLogDao()) }
    val caloriesSettingsRepository: CaloriesSettingsRepository by lazy { CaloriesSettingsRepository(this) }
}
