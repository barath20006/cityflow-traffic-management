package com.example.model

enum class VehicleType(
    val label: String,
    val emoji: String,
    val iconName: String,
    val defaultMaxSpeedKmh: Int,
    val pcuWeight: Float // Passenger Car Unit weight for adaptive signal calculation
) {
    BIKE("Motorbike", "🏍️", "Two-Wheeler", 80, 0.5f),
    CAR("Sedan / SUV", "🚗", "Passenger Car", 90, 1.0f),
    AUTO("Auto-Rickshaw", "🛺", "Three-Wheeler", 55, 0.8f),
    BUS("Public Transit Bus", "🚌", "Heavy Bus", 60, 2.8f),
    LORRY("Goods Lorry", "🚛", "Medium Goods", 60, 2.2f),
    TRUCK("Heavy Cargo Truck", "🚚", "Multi-Axle Truck", 55, 3.2f),
    AMBULANCE("Emergency Vehicle", "🚑", "Priority Siren", 95, 1.0f)
}

enum class SignalColor {
    RED,
    YELLOW,
    GREEN
}

enum class LaneDirection(val displayName: String) {
    NORTH_SOUTH("North - South Corridor"),
    EAST_WEST("East - West Crossroad")
}

enum class CongestionLevel(val label: String) {
    SMOOTH("Optimal Flow"),
    MODERATE("Moderate Density"),
    HEAVY("Heavy Queues"),
    GRIDLOCK("Critical Congestion")
}

data class DetectedVehicle(
    val id: String,
    val plateNumber: String,
    val type: VehicleType,
    val speedKmh: Float,
    val isSpeeding: Boolean,
    val lane: LaneDirection,
    val progress: Float, // 0f to 1f position across intersection
    val laneIndex: Int, // 0, 1 (left/right lane)
    val violationLogged: Boolean = false,
    val detectedAt: Long = System.currentTimeMillis()
)

data class JunctionData(
    val id: String,
    val code: String,
    val name: String,
    val district: String,
    val speedLimitKmh: Int,
    val activeDirection: LaneDirection,
    val nsSignal: SignalColor,
    val ewSignal: SignalColor,
    val secondsRemaining: Int,
    val baseDurationSeconds: Int,
    val adaptiveExtensionSeconds: Int,
    val adaptiveReason: String,
    val isEmergencyCorridor: Boolean,
    val emergencyVehicleId: String?,
    val vehicles: List<DetectedVehicle>,
    val congestionIndex: Int // 0 - 100%
)

data class SpeedViolationRecord(
    val id: String,
    val junctionId: String,
    val junctionName: String,
    val plateNumber: String,
    val vehicleType: VehicleType,
    val speedKmh: Float,
    val speedLimitKmh: Int,
    val fineAmount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ISSUED" // "ISSUED", "PAID", "DISMISSED"
)

data class VoiceLog(
    val id: String,
    val message: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrafficStats(
    val totalVehiclesTracked: Int,
    val totalViolationsDetected: Int,
    val activeCongestionAvg: Int,
    val carbonSavedKg: Float,
    val adaptiveCycleSavingsSeconds: Int,
    val breakdown: Map<VehicleType, Int>
)
