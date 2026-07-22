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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.habittracker.app.data.workout.GymExercise
import com.habittracker.app.data.workout.GymExerciseLog
import com.habittracker.app.data.workout.exerciseLibrary
import com.habittracker.app.data.workout.muscleGroups
import com.habittracker.app.ui.common.ConfirmDeleteDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymLogScreen(viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val gymEntries by viewModel.gymEntries.collectAsState()

    var muscleFilter by remember { mutableStateOf("All") }
    var selectedExercise by remember { mutableStateOf<GymExercise?>(null) }
    var pendingDeleteEntry by remember { mutableStateOf<GymExerciseLog?>(null) }

    val filteredExercises = remember(muscleFilter) {
        if (muscleFilter == "All") exerciseLibrary else exerciseLibrary.filter { it.muscleGroup == muscleFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Gym Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (listOf("All") + muscleGroups).forEach { group ->
                        FilterChip(
                            selected = muscleFilter == group,
                            onClick = { muscleFilter = group },
                            label = { Text(group) }
                        )
                    }
                }
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }
            items(filteredExercises) { exercise ->
                ExerciseRow(exercise = exercise, onClick = { selectedExercise = exercise })
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }
            item {
                Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            if (gymEntries.isEmpty()) {
                item {
                    Text(
                        "No gym exercises logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(gymEntries, key = { it.id }) { entry ->
                    GymHistoryRow(entry = entry, onDelete = { pendingDeleteEntry = entry })
                }
            }
        }
    }

    selectedExercise?.let { exercise ->
        LogSetsDialog(
            exercise = exercise,
            onDismiss = { selectedExercise = null },
            onSave = { sets, reps, weightKg ->
                viewModel.logGymExercise(exercise.name, exercise.muscleGroup, sets, reps, weightKg)
                selectedExercise = null
            }
        )
    }

    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            message = "This gym exercise log entry will be permanently deleted.",
            onConfirm = {
                viewModel.deleteGymEntry(entry)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null }
        )
    }
}

@Composable
private fun ExerciseRow(exercise: GymExercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
            Text(exercise.muscleGroup, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LogSetsDialog(
    exercise: GymExercise,
    onDismiss: () -> Unit,
    onSave: (sets: Int, reps: Int, weightKg: Float) -> Unit
) {
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val setsInt = sets.toIntOrNull()
    val repsInt = reps.toIntOrNull()
    val weightF = weight.toFloatOrNull()
    val isValid = setsInt != null && setsInt > 0 && repsInt != null && repsInt > 0 && weightF != null && weightF >= 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exercise.name) },
        text = {
            Column {
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps per set") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onSave(setsInt!!, repsInt!!, weightF!!) }
            ) { Text("Log") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val gymTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
private fun GymHistoryRow(entry: GymExerciseLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${entry.exerciseName} · ${entry.sets}×${entry.reps} @ ${formatWeight(entry.weightKg)}kg",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${entry.muscleGroup} · ${gymTimeFormat.format(Date(entry.timestampMillis))}",
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

private fun formatWeight(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()
