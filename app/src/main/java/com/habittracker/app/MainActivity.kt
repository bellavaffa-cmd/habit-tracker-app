package com.habittracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.habittracker.app.ui.nav.HabitTrackerNavHost
import com.habittracker.app.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HabitTrackerApplication

        setContent {
            HabitTrackerTheme {
                HabitTrackerNavHost(smokingRepository = app.smokingRepository)
            }
        }
    }
}
