package com.habittracker.app.ui.workout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.workout.WorkoutLog
import com.habittracker.app.ui.common.ConfirmDeleteDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val workoutTypes = listOf("Cardio", "Strength", "Yoga", "Walking", "Cycling", "Swimming", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    val entries by viewModel.entries.collectAsState()
    val todayMinutes by viewModel.todayMinutes.collectAsState()
    val weekMinutes by viewModel.weekMinutes.collectAsState()
    val weekWorkoutCount by viewModel.weekWorkoutCount.collectAsState()

    var showLogDialog by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<WorkoutLog?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Workout") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showLogDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log a workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp, start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { WorkoutSummaryCard(todayMinutes = todayMinutes, weekMinutes = weekMinutes, weekWorkoutCount = weekWorkoutCount) }
            item { HorizontalDivider() }
            if (entries.isEmpty()) {
                item {
                    Text(
                        "No workouts logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    WorkoutHistoryRow(entry = entry, onDelete = { pendingDeleteEntry = entry })
                }
            }
        }
    }

    if (showLogDialog) {
        LogWorkoutDialog(
            onDismiss = { showLogDialog = false },
            onSave = { type, duration, notes ->
                viewModel.logWorkout(type, duration, notes)
                showLogDialog = false
            }
        )
    }

    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            message = "This workout log entry will be permanently deleted.",
            onConfirm = {
                viewModel.delete(entry)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null }
        )
    }
}

@Composable
private fun WorkoutSummaryCard(todayMinutes: Int, weekMinutes: Int, weekWorkoutCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${todayMinutes}m", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${weekMinutes}m", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("this week", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$weekWorkoutCount", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("workouts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LogWorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, durationMinutes: Int, notes: String?) -> Unit
) {
    var type by remember { mutableStateOf(workoutTypes.first()) }
    var duration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val durationInt = duration.toIntOrNull()
    val isValid = durationInt != null && durationInt > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a workout") },
        text = {
            Column {
                Text("Type", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    workoutTypes.forEach { option ->
                        FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option) })
                    }
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { onSave(type, durationInt!!, notes) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val workoutTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
private fun WorkoutHistoryRow(entry: WorkoutLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${entry.type} · ${entry.durationMinutes}m", style = MaterialTheme.typography.bodyLarge)
            Text(
                workoutTimeFormat.format(Date(entry.timestampMillis)) + (entry.notes?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete entry",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
