package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.app.data.smoking.CigarettePurchase
import com.habittracker.app.ui.common.ExpandableCard
import com.habittracker.app.ui.common.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

@Composable
fun PurchaseSummaryCard(
    currencySymbol: String,
    cigarettesThisWeek: Int,
    cigarettesThisMonth: Int,
    costThisWeek: Double,
    costThisMonth: Double,
    costPerCigarette: Double?,
    recentPurchases: List<CigarettePurchase>
) {
    ExpandableCard(
        title = "Purchases",
        summary = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(label = "Cig. this week", value = cigarettesThisWeek.toString())
                MiniStat(label = "Cig. this month", value = cigarettesThisMonth.toString())
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(label = "Spent this week", value = formatMoney(currencySymbol, costThisWeek))
                MiniStat(label = "Spent this month", value = formatMoney(currencySymbol, costThisMonth))
            }
        },
        details = {
            if (costPerCigarette != null) {
                Text(
                    "Cost per cigarette: ${formatMoney(currencySymbol, costPerCigarette)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text("Recent purchases", style = MaterialTheme.typography.labelLarge)
            if (recentPurchases.isEmpty()) {
                Text(
                    "No purchases logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                recentPurchases.take(5).forEach { purchase ->
                    Text(
                        "${dateFormat.format(Date(purchase.timestampMillis))} — ${purchase.packsBought} pack(s) @ " +
                            "${formatMoney(currencySymbol, purchase.pricePerPack)}, ${purchase.sticksPerPack}/pack",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    )
}
