package com.example

import com.example.cv.engine.SignalRecommendationEngine
import com.example.cv.engine.TrafficAlertManager
import com.example.cv.model.*
import org.junit.Assert.*
import org.junit.Test
import android.graphics.RectF

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSignalRecommendationTimingRules() {
        val engine = SignalRecommendationEngine()

        // Low traffic (<= 2) -> 20 seconds
        val lowDensity = engine.calculateLaneDensity(2)
        assertEquals(TrafficDensityLevel.LOW, lowDensity)
        assertEquals(20, engine.recommendTimingForLane(lowDensity))

        // Medium traffic (3..5) -> 40 seconds
        val medDensity = engine.calculateLaneDensity(4)
        assertEquals(TrafficDensityLevel.MEDIUM, medDensity)
        assertEquals(40, engine.recommendTimingForLane(medDensity))

        // Heavy traffic (> 5) -> 60 seconds
        val heavyDensity = engine.calculateLaneDensity(8)
        assertEquals(TrafficDensityLevel.HEAVY, heavyDensity)
        assertEquals(60, engine.recommendTimingForLane(heavyDensity))
    }

    @Test
    fun testPriorityLaneSelectionGivesMoreGreenToHigherDensity() {
        val engine = SignalRecommendationEngine()
        val lanes = listOf(
            LaneInfo(1, "Lane 1", 2, TrafficDensityLevel.LOW, 20, false, 5),
            LaneInfo(2, "Lane 2", 7, TrafficDensityLevel.HEAVY, 60, false, 12),
            LaneInfo(3, "Lane 3", 4, TrafficDensityLevel.MEDIUM, 40, false, 8)
        )

        val priority = engine.selectPriorityLane(lanes)
        assertEquals(2, priority.laneId)
        assertEquals(60, priority.recommendedGreenSeconds)
        assertEquals(TrafficDensityLevel.HEAVY, priority.density)
    }

    @Test
    fun testTrafficAlertManagerDetectsHazardAndCongestion() {
        val alertManager = TrafficAlertManager()
        val lanes = listOf(
            LaneInfo(1, "Lane 1", 1, TrafficDensityLevel.LOW, 20, false),
            LaneInfo(2, "Lane 2", 6, TrafficDensityLevel.HEAVY, 60, true)
        )

        val stoppedVehicle = TrackedVehicle(
            id = "HAZARD-01",
            classification = VehicleClass.CAR,
            normBounds = RectF(0.4f, 0.4f, 0.6f, 0.6f),
            laneIndex = 2,
            speedKmh = 0f,
            isStopped = true,
            stoppedDurationSec = 4.5f
        )

        val alerts = alertManager.evaluateAlerts(
            vehicles = listOf(stoppedVehicle),
            lanes = lanes,
            overallDensity = TrafficDensityLevel.HEAVY
        )

        assertTrue(alerts.any { it.type == AlertType.STOPPED_VEHICLE })
        assertTrue(alerts.any { it.type == AlertType.HEAVY_CONGESTION })
    }
}
