package com.habittracker.app.ui.smoking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.CigarettePurchase
import com.habittracker.app.data.smoking.SmokingCardId
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.ui.common.ConfirmDeleteDialog
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
    val packsThisWeek by viewModel.packsPurchasedThisWeek.collectAsState()
    val packsThisMonth by viewModel.packsPurchasedThisMonth.collectAsState()
    val costThisWeek by viewModel.costThisWeek.collectAsState()
    val costThisMonth by viewModel.costThisMonth.collectAsState()
    val weekSpendEstimate by viewModel.weekSpendEstimate.collectAsState()
    val previousWeekSpendEstimate by viewModel.previousWeekSpendEstimate.collectAsState()

    val cardOrder by viewModel.cardOrder.collectAsState()
    val hiddenCards by viewModel.hiddenCards.collectAsState()

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    var showPurchaseDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<SmokingLog?>(null) }
    var pendingDeletePurchase by remember { mutableStateOf<CigarettePurchase?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smoking") },
                actions = {
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(
                            if (editMode) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = if (editMode) "Done editing" else "Edit layout"
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Smoking settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!editMode) {
                LogActionsFab(
                    onLogCigarette = { viewModel.logNow() },
                    onLogPurchase = { showPurchaseDialog = true }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                StreakCard(
                    lastLog = lastLog,
                    now = now,
                    effectiveIntervalMinutes = effectiveIntervalMinutes,
                    nextAllowedTimestamp = nextAllowedTimestamp
                )
            }

            items(cardOrder, key = { it.name }) { id ->
                val hidden = id in hiddenCards
                if (editMode || !hidden) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        EditableCardWrapper(
                            id = id,
                            editMode = editMode,
                            hidden = hidden,
                            canMoveUp = cardOrder.indexOf(id) > 0,
                            canMoveDown = cardOrder.indexOf(id) < cardOrder.size - 1,
                            onMoveUp = { viewModel.moveCard(id, -1) },
                            onMoveDown = { viewModel.moveCard(id, 1) },
                            onToggleHidden = { viewModel.setCardHidden(id, !hidden) }
                        ) {
                            SmokingCardContent(
                                id = id,
                                viewModel = viewModel,
                                todayCount = todayCount,
                                weekCount = weekCount,
                                monthCount = monthCount,
                                previousWeekCount = previousWeekCount,
                                last7Days = last7Days,
                                entries = entries,
                                onRequestDeleteEntry = { pendingDeleteEntry = it },
                                currencySymbol = currencySymbol,
                                packsThisWeek = packsThisWeek,
                                packsThisMonth = packsThisMonth,
                                costThisWeek = costThisWeek,
                                costThisMonth = costThisMonth,
                                costPerCigarette = costPerCigarette,
                                purchases = purchases,
                                onRequestDeletePurchase = { pendingDeletePurchase = it },
                                weekSpendEstimate = weekSpendEstimate,
                                previousWeekSpendEstimate = previousWeekSpendEstimate,
                                planProgress = planProgress
                            )
                        }
                    }
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

    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            message = "This cigarette log entry will be permanently deleted.",
            onConfirm = {
                viewModel.delete(entry)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null }
        )
    }

    pendingDeletePurchase?.let { purchase ->
        ConfirmDeleteDialog(
            message = "This purchase entry will be permanently deleted.",
            onConfirm = {
                viewModel.deletePurchase(purchase)
                pendingDeletePurchase = null
            },
            onDismiss = { pendingDeletePurchase = null }
        )
    }
}

@Composable
private fun SmokingCardContent(
    id: SmokingCardId,
    viewModel: SmokingViewModel,
    todayCount: Int,
    weekCount: Int,
    monthCount: Int,
    previousWeekCount: Int,
    last7Days: List<DayCount>,
    entries: List<SmokingLog>,
    onRequestDeleteEntry: (SmokingLog) -> Unit,
    currencySymbol: String,
    packsThisWeek: Int,
    packsThisMonth: Int,
    costThisWeek: Double,
    costThisMonth: Double,
    costPerCigarette: Double?,
    purchases: List<CigarettePurchase>,
    onRequestDeletePurchase: (CigarettePurchase) -> Unit,
    weekSpendEstimate: Double?,
    previousWeekSpendEstimate: Double?,
    planProgress: PlanProgress?
) {
    when (id) {
        SmokingCardId.SMOKING_SUMMARY -> SmokingSummaryCard(
            todayCount = todayCount,
            weekCount = weekCount,
            monthCount = monthCount,
            last7Days = last7Days,
            recentEntries = entries,
            onRequestDelete = onRequestDeleteEntry
        )
        SmokingCardId.PURCHASE_SUMMARY -> PurchaseSummaryCard(
            currencySymbol = currencySymbol,
            packsThisWeek = packsThisWeek,
            packsThisMonth = packsThisMonth,
            costThisWeek = costThisWeek,
            costThisMonth = costThisMonth,
            costPerCigarette = costPerCigarette,
            recentPurchases = purchases,
            onRequestDelete = onRequestDeletePurchase
        )
        SmokingCardId.PROGRESS_SUMMARY -> ProgressSummaryCard(
            thisWeekCount = weekCount,
            previousWeekCount = previousWeekCount,
            currencySymbol = currencySymbol,
            thisWeekSpendEstimate = weekSpendEstimate,
            previousWeekSpendEstimate = previousWeekSpendEstimate
        )
        SmokingCardId.QUIT_PLAN -> QuitPlanSection(
            progress = planProgress,
            onStartPlan = { type, weeks, startValue -> viewModel.startPlan(type, weeks, startValue) },
            onCancelPlan = { viewModel.cancelPlan() }
        )
    }
}

@Composable
private fun EditableCardWrapper(
    id: SmokingCardId,
    editMode: Boolean,
    hidden: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleHidden: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!editMode) {
        content()
        return
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                id.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = onToggleHidden) {
                    Icon(
                        if (hidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (hidden) "Show card" else "Hide card"
                    )
                }
            }
        }
        Box(modifier = Modifier.alpha(if (hidden) 0.4f else 1f)) {
            content()
        }
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

private val intervalTimeOfDayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
private fun StreakCard(lastLog: Long?, now: Long, effectiveIntervalMinutes: Int?, nextAllowedTimestamp: Long?) {
    val allowed = nextAllowedTimestamp != null && now >= nextAllowedTimestamp

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
            Text(
                text = when {
                    effectiveIntervalMinutes == null || nextAllowedTimestamp == null ->
                        "No interval reminder set — configure it in Smoking Settings."
                    allowed -> "You can smoke now"
                    else -> "Wait ${StreakUtils.formatElapsed(nextAllowedTimestamp - now)} — allowed at ${intervalTimeOfDayFormat.format(Date(nextAllowedTimestamp))}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    effectiveIntervalMinutes == null || nextAllowedTimestamp == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    allowed -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
