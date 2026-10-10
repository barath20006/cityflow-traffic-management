package com.example.cv.engine

import com.example.cv.model.LaneInfo
import com.example.cv.model.TrafficDensityLevel

/**
 * Smart Traffic Signal Management Module
 * Analyzes traffic density in each lane and recommends signal timing:
 * - Low traffic -> 20 seconds green
 * - Medium traffic -> 40 seconds green
 * - Heavy traffic -> 60 seconds green
 * Automatically gives more green-light time to lanes with higher traffic density.
 */
class SignalRecommendationEngine {

    fun calculateLaneDensity(vehicleCount: Int): TrafficDensityLevel {
        return when {
            vehicleCount <= 2 -> TrafficDensityLevel.LOW
            vehicleCount <= 5 -> TrafficDensityLevel.MEDIUM
            else -> TrafficDensityLevel.HEAVY
        }
    }

    fun recommendTimingForLane(density: TrafficDensityLevel): Int {
        return density.recommendedGreenSeconds
    }

    /**
     * Determines which lane should be granted priority green signal
     * based on density weighting (buses/trucks carry higher priority).
     */
    fun selectPriorityLane(lanes: List<LaneInfo>): LaneInfo {
        if (lanes.isEmpty()) {
            return LaneInfo(1, "Lane 1", 0, TrafficDensityLevel.LOW, 20, false)
        }
        // Lane with highest vehicle count gets priority
        return lanes.maxByOrNull { it.vehicleCount } ?: lanes.first()
    }
}
