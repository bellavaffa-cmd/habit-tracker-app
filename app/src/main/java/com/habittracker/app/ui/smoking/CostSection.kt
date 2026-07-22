package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.habittracker.app.data.smoking.CigarettePurchase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

@Composable
fun CostSection(
    costPerCigarette: Double?,
    spentToday: Double?,
    spentWeek: Double?,
    lastPurchase: CigarettePurchase?,
    onLogPurchase: (packsBought: Int, pricePerPack: Double, sticksPerPack: Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Cost", style = MaterialTheme.typography.titleMedium)

            if (costPerCigarette == null) {
                Text(
                    text = "Log a purchase to start tracking what smoking costs you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            } else {
                Text(
                    text = "%.2f per cigarette".format(costPerCigarette),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Spent today: %.2f · This week: %.2f".format(spentToday ?: 0.0, spentWeek ?: 0.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lastPurchase != null) {
                    Text(
                        text = "Last bought ${dateFormat.format(Date(lastPurchase.timestampMillis))}: " +
                            "${lastPurchase.packsBought} pack(s) @ %.2f, ${lastPurchase.sticksPerPack}/pack".format(lastPurchase.pricePerPack),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Log a purchase")
            }
        }
    }

    if (showDialog) {
        LogPurchaseDialog(
            onDismiss = { showDialog = false },
            onConfirm = { packs, price, sticks ->
                onLogPurchase(packs, price, sticks)
                showDialog = false
            }
        )
    }
}

@Composable
private fun LogPurchaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (packsBought: Int, pricePerPack: Double, sticksPerPack: Int) -> Unit
) {
    var packs by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var sticks by remember { mutableStateOf("20") }

    val packsInt = packs.toIntOrNull()
    val priceDouble = price.toDoubleOrNull()
    val sticksInt = sticks.toIntOrNull()
    val isValid = packsInt != null && packsInt > 0 && priceDouble != null && priceDouble > 0 && sticksInt != null && sticksInt > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a purchase") },
        text = {
            Column {
                OutlinedTextField(
                    value = packs,
                    onValueChange = { packs = it },
                    label = { Text("Packs bought") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per pack") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = sticks,
                    onValueChange = { sticks = it },
                    label = { Text("Cigarettes per pack") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(packsInt!!, priceDouble!!, sticksInt!!) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
