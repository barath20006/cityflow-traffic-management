package com.example.cv.model

import android.graphics.RectF

enum class VehicleClass(val label: String, val emoji: String) {
    CAR("Car", "🚗"),
    MOTORCYCLE("Motorcycle", "🏍️"),
    BUS("Bus", "🚌"),
    TRUCK("Truck", "🚚")
}

enum class TrafficDensityLevel(val label: String, val recommendedGreenSeconds: Int) {
    LOW("Low Traffic", 20),
    MEDIUM("Medium Traffic", 40),
    HEAVY("Heavy Traffic", 60)
}

enum class AlertType(val label: String) {
    HEAVY_CONGESTION("Heavy Congestion"),
    STOPPED_VEHICLE("Stopped Vehicle Hazard"),
    HIGH_LANE_TRAFFIC("Unusually High Lane Traffic"),
    FLOW_CHANGE("Traffic Flow Shift")
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class TrackedVehicle(
    val id: String,
    val classification: VehicleClass,
    val normBounds: RectF, // Normalized 0f..1f (left, top, right, bottom)
    val laneIndex: Int, // 1, 2, 3
    val speedKmh: Float,
    val isStopped: Boolean,
    val stoppedDurationSec: Float,
    val confidence: Float = 0.92f,
    val firstSeenMs: Long = System.currentTimeMillis()
)

data class LaneInfo(
    val laneId: Int, // 1, 2, 3
    val name: String,
    val vehicleCount: Int,
    val density: TrafficDensityLevel,
    val recommendedGreenSeconds: Int,
    val hasStoppedVehicle: Boolean,
    val passedCount: Int = 0 // Number of vehicles passed through this lane
)

data class TrafficAlert(
    val id: String,
    val type: AlertType,
    val message: String,
    val laneId: Int?,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: AlertSeverity
)

data class CvProcessingResult(
    val frameIndex: Int,
    val totalCount: Int,
    val carCount: Int,
    val bikeCount: Int,
    val busCount: Int,
    val truckCount: Int,
    val overallDensity: TrafficDensityLevel,
    val overallCongestionStatus: String,
    val activeSignalLane: Int,
    val recommendedSignalSeconds: Int,
    val lanes: List<LaneInfo>,
    val vehicles: List<TrackedVehicle>,
    val alerts: List<TrafficAlert>,
    val fps: Float = 29.8f,
    val inferenceLatencyMs: Int = 12,
    val passedVehiclesTotal: Int = 0,
    val slowMovingCount: Int = 0,
    val stoppedVehicleCount: Int = 0
)
