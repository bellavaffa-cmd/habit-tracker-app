package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittracker.app.ui.common.StreakUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeOfDayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

/** Read-only live status for the interval reminder — the editable settings live in Smoking Settings. */
@Composable
fun IntervalStatusCard(effectiveIntervalMinutes: Int?, nextAllowedTimestamp: Long?, now: Long) {
    if (effectiveIntervalMinutes == null || nextAllowedTimestamp == null) return

    val allowed = now >= nextAllowedTimestamp
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = if (allowed) {
                    "You can smoke now"
                } else {
                    "Wait ${StreakUtils.formatElapsed(nextAllowedTimestamp - now)} — allowed at ${timeOfDayFormat.format(Date(nextAllowedTimestamp))}"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (allowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
