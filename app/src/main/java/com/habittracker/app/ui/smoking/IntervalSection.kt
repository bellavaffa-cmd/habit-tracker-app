package com.habittracker.app.ui.smoking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private val presetIntervals = listOf(30, 60, 90, 120)

/** The interval reminder's settings editor (chips + custom permission handling) — lives in Smoking Settings. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalSection(
    manualIntervalMinutes: Int?,
    effectiveIntervalMinutes: Int?,
    isPlanControlled: Boolean,
    onSetInterval: (Int?) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Interval reminder", style = MaterialTheme.typography.titleMedium)

            if (isPlanControlled) {
                Text(
                    text = "Set by your active quit plan: wait ${effectiveIntervalMinutes}m between cigarettes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Get notified once you're allowed another cigarette.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = manualIntervalMinutes == null,
                        onClick = { onSetInterval(null) },
                        label = { Text("Off") }
                    )
                    presetIntervals.forEach { minutes ->
                        FilterChip(
                            selected = manualIntervalMinutes == minutes,
                            onClick = {
                                requestNotificationPermissionIfNeeded()
                                onSetInterval(minutes)
                            },
                            label = { Text("${minutes}m") }
                        )
                    }
                }
            }
        }
    }
}
