package com.habittracker.app.ui.smoking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmokingScreen(viewModel: SmokingViewModel, onOpenSettings: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val previousWeekCount by viewModel.previousWeekCount.collectAsState()
    val last7Days by viewModel.last7DaysBreakdown.collectAsState()
    val lastLog by viewModel.lastLogTimestamp.collectAsState()

    val effectiveIntervalMinutes by viewModel.effectiveIntervalMinutes.collectAsState()
    val nextAllowedTimestamp by viewModel.nextAllowedTimestamp.collectAsState()
    val planProgress by viewModel.planProgress.collectAsState()

    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val costPerCigarette by viewModel.costPerCigarette.collectAsState()
    val cigarettesThisWeek by viewModel.cigarettesPurchasedThisWeek.collectAsState()
    val cigarettesThisMonth by viewModel.cigarettesPurchasedThisMonth.collectAsState()
    val costThisWeek by viewModel.costThisWeek.collectAsState()
    val costThisMonth by viewModel.costThisMonth.collectAsState()
    val weekSpendEstimate by viewModel.weekSpendEstimate.collectAsState()
    val previousWeekSpendEstimate by viewModel.previousWeekSpendEstimate.collectAsState()

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    var showPurchaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smoking") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Smoking settings")
                    }
                }
            )
        },
        floatingActionButton = {
            LogActionsFab(
                onLogCigarette = { viewModel.logNow() },
                onLogPurchase = { showPurchaseDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { StreakCard(lastLog = lastLog, now = now) }

            if (effectiveIntervalMinutes != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        IntervalStatusCard(
                            effectiveIntervalMinutes = effectiveIntervalMinutes,
                            nextAllowedTimestamp = nextAllowedTimestamp,
                            now = now
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SmokingSummaryCard(
                        todayCount = todayCount,
                        weekCount = weekCount,
                        monthCount = monthCount,
                        last7Days = last7Days
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    PurchaseSummaryCard(
                        currencySymbol = currencySymbol,
                        cigarettesThisWeek = cigarettesThisWeek,
                        cigarettesThisMonth = cigarettesThisMonth,
                        costThisWeek = costThisWeek,
                        costThisMonth = costThisMonth,
                        costPerCigarette = costPerCigarette,
                        recentPurchases = purchases
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ProgressSummaryCard(
                        thisWeekCount = weekCount,
                        previousWeekCount = previousWeekCount,
                        currencySymbol = currencySymbol,
                        thisWeekSpendEstimate = weekSpendEstimate,
                        previousWeekSpendEstimate = previousWeekSpendEstimate
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    QuitPlanSection(
                        progress = planProgress,
                        onStartPlan = { type, weeks, startValue -> viewModel.startPlan(type, weeks, startValue) },
                        onCancelPlan = { viewModel.cancelPlan() }
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(top = 8.dp)) }

            if (entries.isEmpty()) {
                item {
                    Text(
                        "No cigarettes logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    HistoryRow(entry = entry, onDelete = { viewModel.delete(entry) })
                }
            }
        }
    }

    if (showPurchaseDialog) {
        LogPurchaseDialog(
            onDismiss = { showPurchaseDialog = false },
            onConfirm = { packs, price, sticks ->
                viewModel.logPurchase(packs, price, sticks)
                showPurchaseDialog = false
            }
        )
    }
}

@Composable
private fun LogActionsFab(onLogCigarette: () -> Unit, onLogPurchase: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = expanded) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExtendedFloatingActionButton(
                    text = { Text("Log a purchase") },
                    icon = { Icon(Icons.Filled.LocalMall, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onLogPurchase()
                    }
                )
                ExtendedFloatingActionButton(
                    text = { Text("Log a cigarette") },
                    icon = { Icon(Icons.Filled.SmokingRooms, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onLogCigarette()
                    }
                )
            }
        }
        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(if (expanded) Icons.Filled.Close else Icons.Filled.Add, contentDescription = "Log")
        }
    }
}

@Composable
private fun StreakCard(lastLog: Long?, now: Long) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smoke-free for",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (lastLog == null) "No logs yet" else StreakUtils.formatElapsedWithSeconds(now - lastLog),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
private fun HistoryRow(entry: SmokingLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = timeFormat.format(Date(entry.timestampMillis)))
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete entry",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
