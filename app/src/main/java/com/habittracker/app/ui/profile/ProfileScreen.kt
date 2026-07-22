package com.habittracker.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val sexOptions = listOf("Male", "Female", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    val profile by viewModel.profile.collectAsState()

    var sex by remember { mutableStateOf<String?>(null) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!initialized) {
            sex = profile.sex
            height = profile.heightCm?.let { formatNumber(it) } ?: ""
            weight = profile.weightKg?.let { formatNumber(it) } ?: ""
            age = profile.age?.toString() ?: ""
            initialized = true
        }
    }

    val heightF = height.toFloatOrNull()
    val weightF = weight.toFloatOrNull()
    val ageI = age.toIntOrNull()
    val isValid = sex != null && heightF != null && heightF > 0f && weightF != null && weightF > 0f && ageI != null && ageI > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Used to calculate general health stats like BMI on the Home tab.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Sex", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                sexOptions.forEach { option ->
                    FilterChip(selected = sex == option, onClick = { sex = option }, label = { Text(option) })
                }
            }

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )

            Button(
                enabled = isValid,
                onClick = { viewModel.save(sex!!, heightF!!, weightF!!, ageI!!) }
            ) { Text("Save") }
        }
    }
}

private fun formatNumber(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()
