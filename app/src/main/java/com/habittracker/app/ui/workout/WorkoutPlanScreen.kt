package com.habittracker.app.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.habittracker.app.data.workout.PlanExercise
import com.habittracker.app.data.workout.WorkoutPlan
import com.habittracker.app.data.workout.exerciseLibrary
import com.habittracker.app.data.workout.muscleGroups

private val planGoals = listOf("Build muscle", "Lose weight", "Endurance", "General fitness", "Flexibility")
private val planDurations = listOf(15, 30, 45, 60, 90)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanScreen(viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val planState by viewModel.planState.collectAsState()

    var goal by remember { mutableStateOf(planGoals.first()) }
    var duration by remember { mutableStateOf(60) }
    var focusExercise by remember { mutableStateOf<String?>(null) }
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Workout Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val state = planState
        if (state is WorkoutPlanState.Ready) {
            PlanResultView(
                plan = state.plan,
                modifier = Modifier.padding(padding),
                onNewPlan = { viewModel.dismissPlan() }
            )
        } else {
            PlanInputForm(
                modifier = Modifier.padding(padding),
                goal = goal,
                onGoalChange = { goal = it },
                duration = duration,
                onDurationChange = { duration = it },
                focusExercise = focusExercise,
                onPickExercise = { showExercisePicker = true },
                onClearExercise = { focusExercise = null },
                isGenerating = state is WorkoutPlanState.Generating,
                errorMessage = (state as? WorkoutPlanState.Error)?.message,
                onGenerate = { viewModel.generatePlan(goal, duration, focusExercise) }
            )
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            onDismiss = { showExercisePicker = false },
            onSelect = { name ->
                focusExercise = name
                showExercisePicker = false
            }
        )
    }
}

@Composable
private fun PlanInputForm(
    modifier: Modifier,
    goal: String,
    onGoalChange: (String) -> Unit,
    duration: Int,
    onDurationChange: (Int) -> Unit,
    focusExercise: String?,
    onPickExercise: () -> Unit,
    onClearExercise: () -> Unit,
    isGenerating: Boolean,
    errorMessage: String?,
    onGenerate: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Goal", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            planGoals.forEach { option ->
                FilterChip(selected = goal == option, onClick = { onGoalChange(option) }, label = { Text(option) })
            }
        }

        Text("Duration", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            planDurations.forEach { option ->
                FilterChip(selected = duration == option, onClick = { onDurationChange(option) }, label = { Text("${option}m") })
            }
        }

        Text("Focus exercise (optional)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickExercise) {
                Text(focusExercise ?: "Choose an exercise")
            }
            if (focusExercise != null) {
                TextButton(onClick = onClearExercise) { Text("Clear") }
            }
        }

        Text(
            "Uses your Profile (sex, age, height, weight) automatically if you've filled it in.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp)
        )

        if (errorMessage != null) {
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).size(18.dp), strokeWidth = 2.dp)
                Text("Generating…")
            } else {
                Text("Generate plan")
            }
        }
    }
}

@Composable
private fun PlanResultView(plan: WorkoutPlan, modifier: Modifier, onNewPlan: () -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(plan.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "~${plan.estimatedDurationMinutes} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(plan.exercises) { exercise -> PlanExerciseCard(exercise) }
        if (plan.tips.isNotBlank()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Tips", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                        Text(plan.tips, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = onNewPlan, modifier = Modifier.fillMaxWidth()) {
                Text("Generate a new plan")
            }
        }
    }
}

@Composable
private fun PlanExerciseCard(exercise: PlanExercise) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${exercise.sets} sets × ${exercise.reps} · rest ${exercise.restSeconds}s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (exercise.notes.isNotBlank()) {
                Text(
                    exercise.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var muscleFilter by remember { mutableStateOf("All") }
    val filtered = remember(muscleFilter) {
        if (muscleFilter == "All") exerciseLibrary else exerciseLibrary.filter { it.muscleGroup == muscleFilter }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Choose an exercise", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (listOf("All") + muscleGroups).forEach { group ->
                        FilterChip(selected = muscleFilter == group, onClick = { muscleFilter = group }, label = { Text(group) })
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    items(filtered) { exercise ->
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(exercise.name) }
                                .padding(vertical = 12.dp)
                        )
                        HorizontalDivider()
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}
