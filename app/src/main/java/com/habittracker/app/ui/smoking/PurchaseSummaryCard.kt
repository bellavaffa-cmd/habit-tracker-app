package com.habittracker.app.ui.smoking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    packsThisWeek: Int,
    packsThisMonth: Int,
    costThisWeek: Double,
    costThisMonth: Double,
    costPerCigarette: Double?,
    recentPurchases: List<CigarettePurchase>,
    onRequestDelete: (CigarettePurchase) -> Unit
) {
    ExpandableCard(
        title = "Purchases",
        summary = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(label = "Packs this week", value = packsThisWeek.toString())
                MiniStat(label = "Packs this month", value = packsThisMonth.toString())
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
                recentPurchases.take(10).forEach { purchase ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${dateFormat.format(Date(purchase.timestampMillis))} — ${purchase.packsBought} pack(s) @ " +
                                "${formatMoney(currencySymbol, purchase.pricePerPack)}, ${purchase.sticksPerPack}/pack",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRequestDelete(purchase) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete purchase",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (recentPurchases.size > 10) {
                    Text(
                        "Showing the 10 most recent purchases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    )
}
