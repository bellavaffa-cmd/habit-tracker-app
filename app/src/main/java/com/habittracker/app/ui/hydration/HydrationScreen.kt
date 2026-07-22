package com.habittracker.app.ui.hydration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.habittracker.app.data.hydration.HydrationLog
import com.habittracker.app.ui.common.ConfirmDeleteDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class QuickAmount(val label: String, val ml: Int)

private val quickAmounts = listOf(
    QuickAmount("Glass", 250),
    QuickAmount("Bottle", 500),
    QuickAmount("Large bottle", 1000)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(viewModel: HydrationViewModel) {
    val entries by viewModel.entries.collectAsState()
    val todayMl by viewModel.todayMl.collectAsState()
    val recommendedMl by viewModel.recommendedMl.collectAsState()

    var showCustomDialog by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<HydrationLog?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Hydration") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HydrationProgressCard(todayMl = todayMl, recommendedMl = recommendedMl) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    quickAmounts.forEach { amount ->
                        OutlinedButton(
                            onClick = { viewModel.logDrink(amount.ml) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${amount.label}\n${amount.ml} ml", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { showCustomDialog = true }) { Text("Custom amount") }
            }
            item { HorizontalDivider() }
            if (entries.isEmpty()) {
                item {
                    Text(
                        "No drinks logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    HydrationHistoryRow(entry = entry, onDelete = { pendingDeleteEntry = entry })
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { ml ->
                viewModel.logDrink(ml)
                showCustomDialog = false
            }
        )
    }

    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            message = "This drink log entry will be permanently deleted.",
            onConfirm = {
                viewModel.delete(entry)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null }
        )
    }
}

@Composable
private fun HydrationProgressCard(todayMl: Int, recommendedMl: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Today", style = MaterialTheme.typography.titleMedium)
            Text(
                "$todayMl ml of $recommendedMl ml goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { (todayMl.toFloat() / recommendedMl.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Text(
                "General estimate based on your profile — adjust to what feels right for you.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CustomAmountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val amountInt = amount.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a custom amount") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = amountInt != null && amountInt > 0,
                onClick = { onConfirm(amountInt!!) }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val hydrationTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
private fun HydrationHistoryRow(entry: HydrationLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("${entry.amountMl} ml", style = MaterialTheme.typography.bodyLarge)
            Text(
                hydrationTimeFormat.format(Date(entry.timestampMillis)),
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
