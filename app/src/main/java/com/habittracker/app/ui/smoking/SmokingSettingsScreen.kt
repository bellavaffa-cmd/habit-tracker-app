package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.QuitPlanType

private val commonCurrencies = listOf("$", "€", "£", "¥", "₹")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmokingSettingsScreen(viewModel: SmokingViewModel, onBack: () -> Unit) {
    val manualIntervalMinutes by viewModel.manualIntervalMinutes.collectAsState()
    val effectiveIntervalMinutes by viewModel.effectiveIntervalMinutes.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smoking Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurrencySection(currencySymbol = currencySymbol, onSetCurrency = { viewModel.setCurrencySymbol(it) })
            IntervalSection(
                manualIntervalMinutes = manualIntervalMinutes,
                effectiveIntervalMinutes = effectiveIntervalMinutes,
                isPlanControlled = activePlan?.type == QuitPlanType.INTERVAL_TAPER,
                onSetInterval = { viewModel.setManualIntervalMinutes(it) }
            )
        }
    }
}

@Composable
private fun CurrencySection(currencySymbol: String, onSetCurrency: (String) -> Unit) {
    var custom by remember(currencySymbol) { mutableStateOf(currencySymbol) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Currency", style = MaterialTheme.typography.titleMedium)
            Text(
                "Used for cost figures throughout the smoking tracker.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                commonCurrencies.forEach { symbol ->
                    FilterChip(
                        selected = currencySymbol == symbol,
                        onClick = { onSetCurrency(symbol) },
                        label = { Text(symbol) }
                    )
                }
            }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it },
                label = { Text("Custom symbol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            TextButton(
                onClick = { onSetCurrency(custom) },
                enabled = custom.isNotBlank() && custom != currencySymbol
            ) { Text("Save custom symbol") }
        }
    }
}
