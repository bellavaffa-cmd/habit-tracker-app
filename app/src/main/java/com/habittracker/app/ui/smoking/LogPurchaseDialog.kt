package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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

@Composable
fun LogPurchaseDialog(
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per pack") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = sticks,
                    onValueChange = { sticks = it },
                    label = { Text("Cigarettes per pack") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
