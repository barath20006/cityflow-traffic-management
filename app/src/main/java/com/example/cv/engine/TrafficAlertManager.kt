package com.example.cv.engine

import com.example.cv.model.*
import java.util.UUID

class TrafficAlertManager {

    private val alertsHistory = mutableListOf<TrafficAlert>()
    private var previousTotalCount = 0

    fun evaluateAlerts(
        vehicles: List<TrackedVehicle>,
        lanes: List<LaneInfo>,
        overallDensity: TrafficDensityLevel
    ): List<TrafficAlert> {
        val newAlerts = mutableListOf<TrafficAlert>()
        val totalVehicles = vehicles.size

        // 1. Alert: Stopped Vehicle Hazard (stopped for >= 3.0 seconds)
        val stoppedVehicles = vehicles.filter { it.isStopped && it.stoppedDurationSec >= 3.0f }
        for (stopped in stoppedVehicles) {
            newAlerts.add(
                TrafficAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.STOPPED_VEHICLE,
                    message = "⚠️ Stopped Vehicle Hazard: ${stopped.classification.label} stationary in Lane ${stopped.laneIndex} for ${stopped.stoppedDurationSec.toInt()}s",
                    laneId = stopped.laneIndex,
                    severity = AlertSeverity.CRITICAL
                )
            )
        }

        // 2. Alert: Heavy Congestion
        if (overallDensity == TrafficDensityLevel.HEAVY) {
            newAlerts.add(
                TrafficAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.HEAVY_CONGESTION,
                    message = "🚨 Heavy Traffic Congestion Detected: Total $totalVehicles vehicles queued. Adaptive Green extended to 60s.",
                    laneId = null,
                    severity = AlertSeverity.WARNING
                )
            )
        }

        // 3. Alert: Unusually High Lane Traffic
        val highTrafficLane = lanes.find { it.vehicleCount >= 6 }
        if (highTrafficLane != null) {
            newAlerts.add(
                TrafficAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.HIGH_LANE_TRAFFIC,
                    message = "⚠️ High Lane Load in ${highTrafficLane.name}: ${highTrafficLane.vehicleCount} vehicles queued. Priority signal requested.",
                    laneId = highTrafficLane.laneId,
                    severity = AlertSeverity.WARNING
                )
            )
        }

        // 4. Alert: Significant Traffic Flow Shift
        if (previousTotalCount > 0 && Math.abs(totalVehicles - previousTotalCount) >= 5) {
            newAlerts.add(
                TrafficAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.FLOW_CHANGE,
                    message = "ℹ️ Rapid Traffic Flow Shift: Vehicle surge detected (+${totalVehicles - previousTotalCount} change).",
                    laneId = null,
                    severity = AlertSeverity.INFO
                )
            )
        }
        previousTotalCount = totalVehicles

        // Keep last 30 alerts
        alertsHistory.addAll(0, newAlerts)
        while (alertsHistory.size > 30) {
            alertsHistory.removeAt(alertsHistory.lastIndex)
        }

        return alertsHistory.toList()
    }

    fun clearAlerts() {
        alertsHistory.clear()
    }
}
