package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.ui.common.ExpandableCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

@Composable
fun SmokingSummaryCard(
    todayCount: Int,
    weekCount: Int,
    monthCount: Int,
    last7Days: List<DayCount>,
    recentEntries: List<SmokingLog>,
    onRequestDelete: (SmokingLog) -> Unit
) {
    ExpandableCard(
        title = "Cigarettes smoked",
        summary = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(label = "Today", value = todayCount.toString())
                MiniStat(label = "This week", value = weekCount.toString())
                MiniStat(label = "This month", value = monthCount.toString())
            }
        },
        details = {
            Text("Last 7 days", style = MaterialTheme.typography.labelLarge)
            last7Days.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(day.count.toString())
                }
            }
            if (last7Days.isNotEmpty()) {
                val average = last7Days.sumOf { it.count } / last7Days.size.toDouble()
                Text(
                    "Daily average (7d): %.1f".format(average),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Log", style = MaterialTheme.typography.labelLarge)
            if (recentEntries.isEmpty()) {
                Text(
                    "No cigarettes logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                recentEntries.take(20).forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(timeFormat.format(Date(entry.timestampMillis)))
                        IconButton(onClick = { onRequestDelete(entry) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete entry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (recentEntries.size > 20) {
                    Text(
                        "Showing the 20 most recent entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun MiniStat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
