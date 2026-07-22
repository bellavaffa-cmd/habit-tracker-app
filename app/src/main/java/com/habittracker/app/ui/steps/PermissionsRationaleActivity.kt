package com.habittracker.app.ui.steps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.app.ui.theme.HabitTrackerTheme

/**
 * Health Connect's required "permission rationale" screen — reached from Health Connect's own
 * permission UI (via the manifest's ACTION_VIEW_PERMISSION_USAGE / ACTION_SHOW_PERMISSIONS_RATIONALE
 * intent filters), never launched from inside this app. Without this activity declared, Android 14+
 * can refuse to show the health-permission request dialog at all.
 */
class PermissionsRationaleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                RationaleScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RationaleScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Health data use") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Habit Tracker reads your step count from Health Connect to show it in the " +
                    "Workout tab. Step data stays on this device — it is never uploaded, shared, " +
                    "or stored anywhere outside the app's local database.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
