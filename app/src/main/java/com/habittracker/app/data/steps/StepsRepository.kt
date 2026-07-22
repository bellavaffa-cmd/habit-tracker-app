package com.habittracker.app.data.steps

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

/**
 * Reads step counts from Health Connect — the standard Android hub that smartwatch companion
 * apps (Samsung Health, Fitbit, Google Fit/Wear OS, etc.) sync step data into. This app never
 * talks to a watch directly; there's no cross-brand protocol for that, so Health Connect is the
 * practical integration point.
 */
class StepsRepository(private val context: Context) {
    val readStepsPermission: String = HealthPermission.getReadPermission(StepsRecord::class)

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client: HealthConnectClient? by lazy {
        if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
    }

    suspend fun hasPermission(): Boolean {
        val hc = client ?: return false
        return readStepsPermission in hc.permissionController.getGrantedPermissions()
    }

    suspend fun readSteps(startMillis: Long, endMillis: Long): Long {
        val hc = client ?: return 0L
        val response = hc.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(startMillis),
                    Instant.ofEpochMilli(endMillis)
                )
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }
}
