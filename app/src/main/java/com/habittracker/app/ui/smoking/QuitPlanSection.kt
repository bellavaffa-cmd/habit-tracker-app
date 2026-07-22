package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.QuitPlanType
import com.habittracker.app.ui.common.ExpandableCard

private fun QuitPlanType.label(): String = when (this) {
    QuitPlanType.INTERVAL_TAPER -> "Interval tapering"
    QuitPlanType.DAILY_COUNT_TAPER -> "Daily-count tapering"
    QuitPlanType.COLD_TURKEY -> "Cold turkey"
}

private fun QuitPlanType.description(): String = when (this) {
    QuitPlanType.INTERVAL_TAPER -> "Gradually require a longer gap between cigarettes until your quit date."
    QuitPlanType.DAILY_COUNT_TAPER -> "Start with a daily limit that steps down to zero by your quit date."
    QuitPlanType.COLD_TURKEY -> "Just pick a quit date — no gradual steps."
}

@Composable
fun QuitPlanSection(
    progress: PlanProgress?,
    onStartPlan: (type: QuitPlanType, weeksToQuit: Int, startValue: Int) -> Unit,
    onCancelPlan: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ExpandableCard(
        title = "Quit plan",
        summary = {
            Text(
                text = if (progress == null) {
                    "No active plan"
                } else {
                    "${progress.plan.type.label()} — ${progress.daysRemaining} day(s) left"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        details = {
            if (progress == null) {
                OutlinedButton(onClick = { showDialog = true }) { Text("Start a plan") }
            } else {
                when (progress.plan.type) {
                    QuitPlanType.DAILY_COUNT_TAPER -> Text(
                        text = "Today's limit: ${progress.targetCountToday} · Smoked today: ${progress.actualCountToday}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (progress.actualCountToday > (progress.targetCountToday ?: Int.MAX_VALUE))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QuitPlanType.INTERVAL_TAPER -> Text(
                        text = "Today's minimum gap: ${progress.targetIntervalMinutesToday}m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QuitPlanType.COLD_TURKEY -> {}
                }
                TextButton(onClick = onCancelPlan, modifier = Modifier.padding(top = 4.dp)) { Text("Cancel plan") }
            }
        }
    )

    if (showDialog) {
        StartPlanDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type, weeks, startValue ->
                onStartPlan(type, weeks, startValue)
                showDialog = false
            }
        )
    }
}

@Composable
private fun StartPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: QuitPlanType, weeksToQuit: Int, startValue: Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(QuitPlanType.INTERVAL_TAPER) }
    var weeks by remember { mutableStateOf("8") }
    var startValue by remember { mutableStateOf("30") }

    val weeksInt = weeks.toIntOrNull()
    val startValueInt = startValue.toIntOrNull()
    val needsStartValue = selectedType != QuitPlanType.COLD_TURKEY
    val isValid = weeksInt != null && weeksInt > 0 && (!needsStartValue || (startValueInt != null && startValueInt > 0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start a quit plan") },
        text = {
            Column {
                QuitPlanType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = selectedType == type, onClick = { selectedType = type })
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(type.label(), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                type.description(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = weeks,
                    onValueChange = { weeks = it },
                    label = { Text("Weeks until quit date") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                )

                if (needsStartValue) {
                    OutlinedTextField(
                        value = startValue,
                        onValueChange = { startValue = it },
                        label = {
                            Text(
                                if (selectedType == QuitPlanType.INTERVAL_TAPER) "Starting interval (minutes)"
                                else "Starting daily count"
                            )
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(selectedType, weeksInt!!, startValueInt ?: 0) }
            ) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
