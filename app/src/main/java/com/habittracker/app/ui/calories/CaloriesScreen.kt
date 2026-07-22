package com.habittracker.app.ui.calories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.habittracker.app.data.calories.CalorieLog
import com.habittracker.app.data.calories.FoodAnalysis
import com.habittracker.app.ui.common.ConfirmDeleteDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesScreen(viewModel: CaloriesViewModel, onOpenSettings: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val weekCalories by viewModel.weekCalories.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshApiKeyStatus() }

    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingDeleteEntry by remember { mutableStateOf<CalorieLog?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { viewModel.analyzePhoto(it) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraCaptureUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.analyzePhoto(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calories") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Calories settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSourceDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log a meal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { CaloriesSummaryCard(today = todayCalories, week = weekCalories) }

            if (!hasApiKey) {
                item { ApiKeyMissingCard(onOpenSettings = onOpenSettings) }
            }

            item { HorizontalDivider(modifier = Modifier.padding(top = 8.dp)) }

            if (entries.isEmpty()) {
                item {
                    Text(
                        "No meals logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    CalorieHistoryRow(entry = entry, onDelete = { pendingDeleteEntry = entry })
                }
            }
        }
    }

    if (showSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showSourceDialog = false },
            onTakePhoto = {
                showSourceDialog = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val uri = createCameraCaptureUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onChooseGallery = {
                showSourceDialog = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }

    when (val state = analysisState) {
        is AnalysisState.Analyzing -> AnalyzingDialog()
        is AnalysisState.Ready -> ConfirmAnalysisDialog(
            analysis = state.analysis,
            manualReason = state.manualReason,
            onSave = { description, calories, protein, carbs, fat ->
                viewModel.saveEntry(state.photoPath, description, calories, protein, carbs, fat)
            },
            onDiscard = { viewModel.dismissAnalysis() }
        )
        is AnalysisState.Error -> AlertDialog(
            onDismissRequest = { viewModel.dismissAnalysis() },
            title = { Text("Couldn't analyze photo") },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAnalysis() }) { Text("OK") }
            }
        )
        AnalysisState.Idle -> {}
    }

    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            message = "This meal log entry will be permanently deleted.",
            onConfirm = {
                viewModel.delete(entry)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null }
        )
    }
}

private fun createCameraCaptureUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_capture").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun PhotoSourceDialog(onDismiss: () -> Unit, onTakePhoto: () -> Unit, onChooseGallery: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a meal") },
        text = {
            Text(
                "How would you like to add a photo?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onTakePhoto) { Text("Take photo") }
        },
        dismissButton = {
            TextButton(onClick = onChooseGallery) { Text("Choose from gallery") }
        }
    )
}

@Composable
private fun AnalyzingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Analyzing photo…", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
private fun ConfirmAnalysisDialog(
    analysis: FoodAnalysis?,
    manualReason: String?,
    onSave: (foodDescription: String, calories: Int, protein: Double, carbs: Double, fat: Double) -> Unit,
    onDiscard: () -> Unit
) {
    var description by remember { mutableStateOf(analysis?.foodDescription.orEmpty()) }
    var calories by remember { mutableStateOf(analysis?.calories?.toString().orEmpty()) }
    var protein by remember { mutableStateOf(analysis?.proteinGrams?.toString().orEmpty()) }
    var carbs by remember { mutableStateOf(analysis?.carbsGrams?.toString().orEmpty()) }
    var fat by remember { mutableStateOf(analysis?.fatGrams?.toString().orEmpty()) }

    val caloriesInt = calories.toIntOrNull()
    val proteinD = protein.toDoubleOrNull()
    val carbsD = carbs.toDoubleOrNull()
    val fatD = fat.toDoubleOrNull()
    val isValid = description.isNotBlank() && caloriesInt != null && proteinD != null && carbsD != null && fatD != null

    AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text(if (analysis == null) "Enter meal details" else "Confirm meal") },
        text = {
            Column {
                Text(
                    manualReason ?: "AI confidence: ${analysis?.confidence}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Food") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onSave(description, caloriesInt!!, proteinD!!, carbsD!!, fatD!!) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) { Text("Discard") }
        }
    )
}

@Composable
private fun CaloriesSummaryCard(today: Int, week: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$today", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$week", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("this week", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ApiKeyMissingCard(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onOpenSettings
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Add your Anthropic API key for AI photo analysis — without one you'll enter meals manually.", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Tap to open Calories Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private val calorieTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
private fun CalorieHistoryRow(entry: CalorieLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.foodDescription, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${entry.calories} cal · ${calorieTimeFormat.format(Date(entry.timestampMillis))}",
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
