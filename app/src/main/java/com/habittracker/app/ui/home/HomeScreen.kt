package com.habittracker.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.habittracker.app.ui.common.StreakUtils
import com.habittracker.app.ui.nav.Routes
import com.habittracker.app.ui.smoking.SmokingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(smokingViewModel: SmokingViewModel, onNavigate: (String) -> Unit, onOpenSettings: () -> Unit) {
    val todayCount by smokingViewModel.todayCount.collectAsState()
    val lastLog by smokingViewModel.lastLogTimestamp.collectAsState()
    val now = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Tracker") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TrackerSummaryCard(
                    title = "Smoking",
                    icon = Icons.Filled.SmokingRooms,
                    primaryLine = lastLog?.let { "${StreakUtils.formatElapsed(now - it)} smoke-free" } ?: "No logs yet",
                    secondaryLine = "$todayCount today",
                    onClick = { onNavigate(Routes.SMOKING) }
                )
            }
            item {
                TrackerSummaryCard(
                    title = "Drinking",
                    icon = Icons.Filled.LocalBar,
                    primaryLine = "Coming soon",
                    secondaryLine = null,
                    onClick = { onNavigate(Routes.DRINKING) }
                )
            }
            item {
                TrackerSummaryCard(
                    title = "Workout",
                    icon = Icons.Filled.FitnessCenter,
                    primaryLine = "Coming soon",
                    secondaryLine = null,
                    onClick = { onNavigate(Routes.WORKOUT) }
                )
            }
            item {
                TrackerSummaryCard(
                    title = "Calories (via photo)",
                    icon = Icons.Filled.Restaurant,
                    primaryLine = "Coming soon",
                    secondaryLine = null,
                    onClick = { onNavigate(Routes.CALORIES) }
                )
            }
        }
    }
}

@Composable
private fun TrackerSummaryCard(
    title: String,
    icon: ImageVector,
    primaryLine: String,
    secondaryLine: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = primaryLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (secondaryLine != null) {
                    Text(text = secondaryLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
