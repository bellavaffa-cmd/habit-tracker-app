package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.app.ui.common.ExpandableCard
import com.habittracker.app.ui.common.formatMoney

@Composable
fun ProgressSummaryCard(
    thisWeekCount: Int,
    previousWeekCount: Int,
    currencySymbol: String,
    thisWeekSpendEstimate: Double?,
    previousWeekSpendEstimate: Double?
) {
    val delta = previousWeekCount - thisWeekCount // positive = smoked fewer than last week = improvement
    val percent = if (previousWeekCount > 0) delta * 100f / previousWeekCount else null
    val hasData = previousWeekCount > 0 || thisWeekCount > 0
    val improving = delta > 0
    val same = delta == 0

    val statusColor = when {
        !hasData || same -> MaterialTheme.colorScheme.onSurfaceVariant
        improving -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val statusText = when {
        !hasData -> "No data yet"
        same -> "Same as last week ($thisWeekCount cigarettes)"
        improving -> "$delta fewer cigarettes than last week" + (percent?.let { " (-%.0f%%)".format(it) } ?: "")
        else -> "${-delta} more cigarettes than last week" + (percent?.let { " (+%.0f%%)".format(-it) } ?: "")
    }

    ExpandableCard(
        title = "Weekly progress",
        summary = {
            Text(
                statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        details = {
            Text("This week: $thisWeekCount cigarettes" + (thisWeekSpendEstimate?.let { " (~${formatMoney(currencySymbol, it)})" } ?: ""))
            Text(
                "Last week: $previousWeekCount cigarettes" + (previousWeekSpendEstimate?.let { " (~${formatMoney(currencySymbol, it)})" } ?: ""),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Estimated spend uses your latest purchase's price per cigarette.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    )
}
