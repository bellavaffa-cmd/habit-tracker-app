package com.habittracker.app.ui.smoking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            if (hasData) {
                WeekComparisonChart(
                    thisWeekCount = thisWeekCount,
                    previousWeekCount = previousWeekCount,
                    thisWeekColor = statusColor
                )
            }
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

@Composable
private fun WeekComparisonChart(thisWeekCount: Int, previousWeekCount: Int, thisWeekColor: Color) {
    val max = maxOf(thisWeekCount, previousWeekCount, 1)
    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp).padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)
    ) {
        WeekBar(label = "Last week", count = previousWeekCount, max = max, color = MaterialTheme.colorScheme.onSurfaceVariant)
        WeekBar(label = "This week", count = thisWeekCount, max = max, color = thisWeekColor)
    }
}

@Composable
private fun WeekBar(label: String, count: Int, max: Int, color: Color) {
    Column(
        modifier = Modifier.width(64.dp).fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(0.6f).padding(top = 4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.08f + 0.92f * count / max.toFloat())
                    .background(color, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
