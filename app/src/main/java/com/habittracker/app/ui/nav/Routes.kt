package com.habittracker.app.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val SMOKING = "smoking"
    const val WORKOUT = "workout"
    const val CALORIES = "calories"
    const val HYDRATION = "hydration"
    const val SETTINGS = "settings"
    const val SMOKING_SETTINGS = "smoking_settings"
    const val CALORIES_SETTINGS = "calories_settings"
    const val PROFILE = "profile"
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.SMOKING, "Smoking", Icons.Filled.SmokingRooms),
    BottomNavItem(Routes.WORKOUT, "Workout", Icons.Filled.FitnessCenter),
    BottomNavItem(Routes.HYDRATION, "Hydration", Icons.Filled.LocalDrink),
    BottomNavItem(Routes.CALORIES, "Calories", Icons.Filled.Restaurant)
)
