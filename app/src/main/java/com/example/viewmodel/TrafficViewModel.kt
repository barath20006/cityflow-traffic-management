package com.example.viewmodel

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cv.engine.OpenCvVisionEngine
import com.example.cv.engine.TrafficScenario
import com.example.cv.model.*
import com.example.data.AppDatabase
import com.example.data.TrafficRepository
import com.example.engine.TrafficVoiceManager
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class TrafficViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TrafficRepository
    val voiceManager: TrafficVoiceManager = TrafficVoiceManager(application)
    val visionEngine: OpenCvVisionEngine = OpenCvVisionEngine(application)

    // OpenCV Real-Time State Flows
    val cvResult: StateFlow<CvProcessingResult> = visionEngine.processingResult
    val cvIsPlaying: StateFlow<Boolean> = visionEngine.isPlaying
    val cvVideoTitle: StateFlow<String> = visionEngine.videoSourceTitle
    val cvIsUploadedVideo: StateFlow<Boolean> = visionEngine.isUploadedVideo
    val cvActiveScenario: StateFlow<TrafficScenario> = visionEngine.activeScenario
    private val _selectedCvVehicle = MutableStateFlow<TrackedVehicle?>(null)
    val selectedCvVehicle: StateFlow<TrackedVehicle?> = _selectedCvVehicle.asStateFlow()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _junctions = MutableStateFlow<List<JunctionData>>(emptyList())
    val junctions: StateFlow<List<JunctionData>> = _junctions.asStateFlow()

    private val _selectedJunctionId = MutableStateFlow("j1")
    val selectedJunctionId: StateFlow<String> = _selectedJunctionId.asStateFlow()

    val selectedJunction: StateFlow<JunctionData?> = combine(_junctions, _selectedJunctionId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val violations: StateFlow<List<SpeedViolationRecord>>

    private val _voiceLogs = MutableStateFlow<List<VoiceLog>>(emptyList())
    val voiceLogs: StateFlow<List<VoiceLog>> = _voiceLogs.asStateFlow()

    private val _isAutoVoiceEnabled = MutableStateFlow(true)
    val isAutoVoiceEnabled: StateFlow<Boolean> = _isAutoVoiceEnabled.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _rushHourMode = MutableStateFlow(false)
    val rushHourMode: StateFlow<Boolean> = _rushHourMode.asStateFlow()

    private val _selectedVehicle = MutableStateFlow<DetectedVehicle?>(null)
    val selectedVehicle: StateFlow<DetectedVehicle?> = _selectedVehicle.asStateFlow()

    private val _stats = MutableStateFlow(
        TrafficStats(
            totalVehiclesTracked = 1420,
            totalViolationsDetected = 38,
            activeCongestionAvg = 32,
            carbonSavedKg = 124.5f,
            adaptiveCycleSavingsSeconds = 1860,
            breakdown = mapOf(
                VehicleType.CAR to 620,
                VehicleType.BIKE to 430,
                VehicleType.AUTO to 195,
                VehicleType.BUS to 85,
                VehicleType.LORRY to 54,
                VehicleType.TRUCK to 36
            )
        )
    )
    val stats: StateFlow<TrafficStats> = _stats.asStateFlow()

    private var simulationJob: Job? = null
    private var tickCount = 0

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TrafficRepository(db.violationDao())
        violations = repository.allViolations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        initializeJunctions()
        startSimulation()
    }

    private fun initializeJunctions() {
        val initialJunctions = listOf(
            JunctionData(
                id = "j1",
                code = "JNC-01-TECH",
                name = "MG Road & Cyber Highway",
                district = "Central Hub Sector 4",
                speedLimitKmh = 50,
                activeDirection = LaneDirection.NORTH_SOUTH,
                nsSignal = SignalColor.GREEN,
                ewSignal = SignalColor.RED,
                secondsRemaining = 24,
                baseDurationSeconds = 30,
                adaptiveExtensionSeconds = 0,
                adaptiveReason = "Optimal flow maintained across arterial corridor",
                isEmergencyCorridor = false,
                emergencyVehicleId = null,
                vehicles = generateInitialVehicles("j1", 50),
                congestionIndex = 38
            ),
            JunctionData(
                id = "j2",
                code = "JNC-02-METRO",
                name = "Central Transit Interchange",
                district = "Bus Terminal & Metro Line",
                speedLimitKmh = 40,
                activeDirection = LaneDirection.NORTH_SOUTH,
                nsSignal = SignalColor.GREEN,
                ewSignal = SignalColor.RED,
                secondsRemaining = 18,
                baseDurationSeconds = 25,
                adaptiveExtensionSeconds = 6,
                adaptiveReason = "Mass transit prioritization (+6s for 3 waiting transit buses)",
                isEmergencyCorridor = false,
                emergencyVehicleId = null,
                vehicles = generateInitialVehicles("j2", 40),
                congestionIndex = 48
            ),
            JunctionData(
                id = "j3",
                code = "JNC-03-PORT",
                name = "Harbor Freight Expressway",
                district = "Port Logistics Corridor",
                speedLimitKmh = 60,
                activeDirection = LaneDirection.EAST_WEST,
                nsSignal = SignalColor.RED,
                ewSignal = SignalColor.GREEN,
                secondsRemaining = 32,
                baseDurationSeconds = 35,
                adaptiveExtensionSeconds = 0,
                adaptiveReason = "Heavy multi-axle freight queue clearing",
                isEmergencyCorridor = false,
                emergencyVehicleId = null,
                vehicles = generateInitialVehicles("j3", 60),
                congestionIndex = 26
            ),
            JunctionData(
                id = "j4",
                code = "JNC-04-RING",
                name = "Outer Ring Express Loop",
                district = "Suburban Arterial Road",
                speedLimitKmh = 70,
                activeDirection = LaneDirection.NORTH_SOUTH,
                nsSignal = SignalColor.GREEN,
                ewSignal = SignalColor.RED,
                secondsRemaining = 15,
                baseDurationSeconds = 30,
                adaptiveExtensionSeconds = 0,
                adaptiveReason = "High-speed flow monitoring active",
                isEmergencyCorridor = false,
                emergencyVehicleId = null,
                vehicles = generateInitialVehicles("j4", 70),
                congestionIndex = 20
            )
        )
        _junctions.value = initialJunctions
    }

    private fun generateInitialVehicles(junctionId: String, speedLimit: Int): List<DetectedVehicle> {
        val types = when (junctionId) {
            "j2" -> listOf(VehicleType.BUS, VehicleType.AUTO, VehicleType.AUTO, VehicleType.CAR, VehicleType.BIKE)
            "j3" -> listOf(VehicleType.TRUCK, VehicleType.LORRY, VehicleType.LORRY, VehicleType.CAR, VehicleType.BIKE)
            else -> listOf(VehicleType.CAR, VehicleType.CAR, VehicleType.BIKE, VehicleType.AUTO, VehicleType.BUS)
        }
        val prefixes = listOf("DL-01-AX", "MH-12-CQ", "KA-04-MB", "TN-07-G", "TS-09-ER", "KL-01-BW")

        return types.mapIndexed { index, type ->
            val isViolator = index == 0 && Random.nextBoolean()
            val speed = if (isViolator) (speedLimit + Random.nextInt(12, 28)).toFloat()
            else (speedLimit - Random.nextInt(2, 18)).toFloat().coerceAtLeast(20f)
            val dir = if (index % 2 == 0) LaneDirection.NORTH_SOUTH else LaneDirection.EAST_WEST
            val plate = "${prefixes.random()}-${Random.nextInt(1000, 9999)}"

            DetectedVehicle(
                id = UUID.randomUUID().toString().take(8),
                plateNumber = plate,
                type = type,
                speedKmh = speed,
                isSpeeding = isViolator,
                lane = dir,
                progress = (index * 0.18f + 0.05f).coerceIn(0f, 0.95f),
                laneIndex = index % 2
            )
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(400) // 2.5 ticks per second
                tickCount++
                val isSecondTick = (tickCount % 2 == 0)

                updateSimulation(isSecondTick)
            }
        }
    }

    private fun updateSimulation(isSecondTick: Boolean) {
        val currentList = _junctions.value.toMutableList()
        val currentViolations = mutableListOf<SpeedViolationRecord>()
        val rush = _rushHourMode.value

        for (i in currentList.indices) {
            val j = currentList[i]
            var nsSig = j.nsSignal
            var ewSig = j.ewSignal
            var remaining = j.secondsRemaining
            var activeDir = j.activeDirection
            var extension = j.adaptiveExtensionSeconds
            var reason = j.adaptiveReason
            var isEmergency = j.isEmergencyCorridor

            // 1. Update Signals on second tick
            if (isSecondTick) {
                if (isEmergency) {
                    // Lock active direction to Green
                    remaining = 99
                    reason = "🚨 Emergency Green Corridor Active: Ambulance Priority Passage"
                } else {
                    remaining -= 1

                    // Adaptive extension check when countdown approaches 4 seconds
                    if (remaining == 4 && extension == 0) {
                        val activeQueuePcu = calculateLanePcu(j.vehicles, activeDir)
                        val waitingQueuePcu = calculateLanePcu(j.vehicles, if (activeDir == LaneDirection.NORTH_SOUTH) LaneDirection.EAST_WEST else LaneDirection.NORTH_SOUTH)
                        val heavyBusCount = j.vehicles.count { it.lane == activeDir && (it.type == VehicleType.BUS || it.type == VehicleType.LORRY) }

                        if (heavyBusCount >= 2 || (activeQueuePcu > waitingQueuePcu + 4)) {
                            val bonus = if (heavyBusCount >= 2) 10 else 6
                            remaining += bonus
                            extension = bonus
                            reason = "Adaptive AI: +${bonus}s Green extended for $heavyBusCount mass transit/heavy vehicles in queue."
                            recordVoiceLog("Adaptive signal extended by ${bonus}s at ${j.name}", "SIGNAL")
                        }
                    }

                    if (remaining <= 0) {
                        extension = 0
                        // Phase transition
                        if (nsSig == SignalColor.GREEN) {
                            nsSig = SignalColor.YELLOW
                            remaining = 3
                            reason = "Phase switching to East-West corridor"
                        } else if (nsSig == SignalColor.YELLOW) {
                            nsSig = SignalColor.RED
                            ewSig = SignalColor.GREEN
                            activeDir = LaneDirection.EAST_WEST
                            remaining = if (rush) 35 else 24
                            reason = "East-West green corridor active"
                        } else if (ewSig == SignalColor.GREEN) {
                            ewSig = SignalColor.YELLOW
                            remaining = 3
                            reason = "Phase switching to North-South corridor"
                        } else if (ewSig == SignalColor.YELLOW) {
                            ewSig = SignalColor.RED
                            nsSig = SignalColor.GREEN
                            activeDir = LaneDirection.NORTH_SOUTH
                            remaining = if (rush) 35 else 24
                            reason = "North-South green corridor active"
                        }
                    }
                }
            }

            // 2. Update Vehicles positions & detections
            val updatedVehicles = mutableListOf<DetectedVehicle>()
            val maxVehicles = if (rush) 9 else 6
            var ambulancePassed = false

            for (v in j.vehicles) {
                val signalForVehicle = if (v.lane == LaneDirection.NORTH_SOUTH) nsSig else ewSig
                val isAtStopLine = v.progress in 0.40f..0.52f

                val speedDelta = (v.speedKmh / 150f) * 0.04f
                val newProgress = when {
                    signalForVehicle == SignalColor.RED && isAtStopLine -> v.progress // Wait at red
                    signalForVehicle == SignalColor.YELLOW && isAtStopLine && v.progress < 0.46f -> v.progress // Stop for yellow
                    else -> v.progress + speedDelta
                }

                if (v.type == VehicleType.AMBULANCE && newProgress >= 1.0f) {
                    ambulancePassed = true
                }

                var hasLoggedViolation = v.violationLogged
                // Speeding Check
                if (v.speedKmh > j.speedLimitKmh && !hasLoggedViolation && v.progress in 0.2f..0.85f) {
                    val violation = SpeedViolationRecord(
                        id = UUID.randomUUID().toString(),
                        junctionId = j.id,
                        junctionName = j.name,
                        plateNumber = v.plateNumber,
                        vehicleType = v.type,
                        speedKmh = v.speedKmh,
                        speedLimitKmh = j.speedLimitKmh,
                        fineAmount = calculateFine(v.speedKmh, j.speedLimitKmh, v.type),
                        timestamp = System.currentTimeMillis()
                    )
                    currentViolations.add(violation)
                    hasLoggedViolation = true
                    triggerHapticWarning()

                    if (_isAutoVoiceEnabled.value && !_isMuted.value) {
                        val speechText = "Alert: Speed violation detected. ${v.type.label} ${v.plateNumber} speed is ${v.speedKmh.toInt()} kilometers per hour. Speed limit is ${j.speedLimitKmh}."
                        voiceManager.speak(speechText, flush = false)
                    }
                    recordVoiceLog("Speed violation captured: ${v.plateNumber} (${v.speedKmh.toInt()} km/h)", "SPEED")
                }

                if (newProgress < 1.05f) {
                    updatedVehicles.add(
                        v.copy(
                            progress = newProgress,
                            isSpeeding = v.speedKmh > j.speedLimitKmh,
                            violationLogged = hasLoggedViolation
                        )
                    )
                }
            }

            // If ambulance completed crossing, cancel emergency corridor
            if (ambulancePassed) {
                isEmergency = false
                remaining = 15
                reason = "Emergency vehicle cleared. Adaptive signaling restored."
                recordVoiceLog("Emergency corridor cleared at ${j.name}", "EMERGENCY")
            }

            // Spawn new vehicle if room
            if (updatedVehicles.size < maxVehicles && Random.nextInt(100) < 40) {
                val newVehicle = createRandomVehicle(j.id, j.speedLimitKmh, activeDir)
                updatedVehicles.add(newVehicle)
            }

            val congestion = ((updatedVehicles.size.toFloat() / maxVehicles.toFloat()) * 85).toInt().coerceIn(10, 95)

            currentList[i] = j.copy(
                nsSignal = nsSig,
                ewSignal = ewSig,
                secondsRemaining = remaining,
                activeDirection = activeDir,
                adaptiveExtensionSeconds = extension,
                adaptiveReason = reason,
                isEmergencyCorridor = isEmergency,
                emergencyVehicleId = if (isEmergency) j.emergencyVehicleId else null,
                vehicles = updatedVehicles,
                congestionIndex = congestion
            )
        }

        _junctions.value = currentList

        // Synchronize selected vehicle with updated live vehicle state
        _selectedVehicle.value?.let { currentSelected ->
            val activeJunctionVehicles = currentList.find { it.id == _selectedJunctionId.value }?.vehicles ?: emptyList()
            _selectedVehicle.value = activeJunctionVehicles.find { it.id == currentSelected.id }
        }

        // Persist newly detected violations to Room
        if (currentViolations.isNotEmpty()) {
            viewModelScope.launch {
                currentViolations.forEach { repository.recordViolation(it) }
            }
            // Update stats
            val prev = _stats.value
            _stats.value = prev.copy(
                totalViolationsDetected = prev.totalViolationsDetected + currentViolations.size,
                totalVehiclesTracked = prev.totalVehiclesTracked + currentViolations.size * 2
            )
        }
    }

    private fun calculateLanePcu(vehicles: List<DetectedVehicle>, lane: LaneDirection): Float {
        return vehicles.filter { it.lane == lane }.sumOf { it.type.pcuWeight.toDouble() }.toFloat()
    }

    private fun calculateFine(speed: Float, limit: Int, type: VehicleType): Int {
        val over = (speed - limit).toInt()
        val base = when {
            over > 30 -> 3000
            over > 15 -> 1500
            else -> 800
        }
        val typeMultiplier = when (type) {
            VehicleType.BUS, VehicleType.TRUCK, VehicleType.LORRY -> 1.5
            VehicleType.CAR -> 1.0
            VehicleType.AUTO, VehicleType.BIKE -> 0.75
            else -> 1.0
        }
        return (base * typeMultiplier).toInt()
    }

    private fun createRandomVehicle(junctionId: String, speedLimit: Int, preferredDir: LaneDirection): DetectedVehicle {
        val types = when (junctionId) {
            "j2" -> listOf(VehicleType.BUS, VehicleType.BUS, VehicleType.AUTO, VehicleType.AUTO, VehicleType.CAR, VehicleType.BIKE)
            "j3" -> listOf(VehicleType.TRUCK, VehicleType.LORRY, VehicleType.LORRY, VehicleType.TRUCK, VehicleType.CAR)
            else -> listOf(VehicleType.CAR, VehicleType.CAR, VehicleType.BIKE, VehicleType.BIKE, VehicleType.AUTO, VehicleType.BUS, VehicleType.LORRY)
        }
        val type = types.random()
        val isSpeeding = Random.nextInt(100) < 18 // 18% speeders
        val speed = if (isSpeeding) {
            (speedLimit + Random.nextInt(10, 32)).toFloat()
        } else {
            (speedLimit - Random.nextInt(2, 16)).toFloat().coerceAtLeast(22f)
        }
        val lane = if (Random.nextInt(100) < 65) preferredDir else (if (preferredDir == LaneDirection.NORTH_SOUTH) LaneDirection.EAST_WEST else LaneDirection.NORTH_SOUTH)
        val prefixes = listOf("DL-01-A", "KA-05-M", "MH-02-C", "TN-09-B", "TS-10-H", "KL-07-K", "GJ-01-R")
        val plate = "${prefixes.random()}${Random.nextInt(1000, 9999)}"

        return DetectedVehicle(
            id = UUID.randomUUID().toString().take(8),
            plateNumber = plate,
            type = type,
            speedKmh = speed,
            isSpeeding = isSpeeding,
            lane = lane,
            progress = 0.02f,
            laneIndex = Random.nextInt(2)
        )
    }

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    fun toggleHelpDialog(show: Boolean) {
        _showHelpDialog.value = show
    }

    fun spawnVehicle(preferredType: VehicleType? = null) {
        val jId = _selectedJunctionId.value
        val j = _junctions.value.find { it.id == jId } ?: return
        val type = preferredType ?: listOf(
            VehicleType.CAR, VehicleType.BIKE, VehicleType.AUTO, VehicleType.BUS, VehicleType.LORRY, VehicleType.TRUCK
        ).random()
        val isSpeeding = Random.nextInt(100) < 35 // 35% chance to test speed radar
        val speed = if (isSpeeding) {
            (j.speedLimitKmh + Random.nextInt(12, 28)).toFloat()
        } else {
            (j.speedLimitKmh - Random.nextInt(4, 18)).toFloat().coerceAtLeast(24f)
        }
        val prefixes = listOf("DL-01-A", "KA-05-M", "MH-02-C", "TN-09-B", "TS-10-H")
        val plate = "${prefixes.random()}${Random.nextInt(1000, 9999)}"
        val newV = DetectedVehicle(
            id = UUID.randomUUID().toString().take(8),
            plateNumber = plate,
            type = type,
            speedKmh = speed,
            isSpeeding = isSpeeding,
            lane = j.activeDirection,
            progress = 0.04f,
            laneIndex = Random.nextInt(2)
        )
        _junctions.value = _junctions.value.map {
            if (it.id == jId) it.copy(vehicles = it.vehicles + newV) else it
        }
        _selectedVehicle.value = newV
        recordVoiceLog("Added new ${type.label} ($plate) to ${j.name}", "ALERT")
    }

    fun selectJunction(id: String) {
        _selectedJunctionId.value = id
        _selectedVehicle.value = null
    }

    fun selectVehicle(vehicle: DetectedVehicle?) {
        _selectedVehicle.value = vehicle
    }

    fun setSpeedLimit(junctionId: String, newLimit: Int) {
        _junctions.value = _junctions.value.map { j ->
            if (j.id == junctionId) {
                val updatedVehicles = j.vehicles.map { v ->
                    v.copy(isSpeeding = v.speedKmh > newLimit)
                }
                j.copy(speedLimitKmh = newLimit, vehicles = updatedVehicles)
            } else j
        }
        recordVoiceLog("Speed limit updated to $newLimit km/h at $junctionId", "ALERT")
    }

    fun triggerEmergencyCorridor(junctionId: String) {
        val j = _junctions.value.find { it.id == junctionId } ?: return
        if (j.isEmergencyCorridor) return // Already active, avoid duplicate ambulance

        val ambulance = DetectedVehicle(
            id = "AMB-${UUID.randomUUID().toString().take(4)}",
            plateNumber = "EMERGENCY-108",
            type = VehicleType.AMBULANCE,
            speedKmh = 88f,
            isSpeeding = false,
            lane = LaneDirection.NORTH_SOUTH,
            progress = 0.05f,
            laneIndex = 0
        )

        _junctions.value = _junctions.value.map {
            if (it.id == junctionId) {
                it.copy(
                    isEmergencyCorridor = true,
                    emergencyVehicleId = ambulance.id,
                    nsSignal = SignalColor.GREEN,
                    ewSignal = SignalColor.RED,
                    activeDirection = LaneDirection.NORTH_SOUTH,
                    secondsRemaining = 99,
                    adaptiveReason = "🚨 Green Corridor Priority dispatched for Ambulance",
                    vehicles = it.vehicles + ambulance
                )
            } else it
        }

        triggerHapticWarning()

        val announcement = "Emergency Alert. Priority Green Corridor activated at ${j.name}. All lanes clear the intersection immediately."
        speakNow(announcement, flush = true)
        recordVoiceLog(announcement, "EMERGENCY")
    }

    fun forceSignalSwitch(junctionId: String) {
        _junctions.value = _junctions.value.map { j ->
            if (j.id == junctionId) {
                val nextDir = if (j.activeDirection == LaneDirection.NORTH_SOUTH) LaneDirection.EAST_WEST else LaneDirection.NORTH_SOUTH
                j.copy(
                    activeDirection = nextDir,
                    nsSignal = if (nextDir == LaneDirection.NORTH_SOUTH) SignalColor.GREEN else SignalColor.RED,
                    ewSignal = if (nextDir == LaneDirection.EAST_WEST) SignalColor.GREEN else SignalColor.RED,
                    secondsRemaining = 25,
                    adaptiveExtensionSeconds = 0,
                    adaptiveReason = "Manual operator override enacted",
                    isEmergencyCorridor = false,
                    emergencyVehicleId = null
                )
            } else j
        }
        recordVoiceLog("Manual signal override at $junctionId", "SIGNAL")
    }

    fun toggleAutoVoice() {
        _isAutoVoiceEnabled.value = !_isAutoVoiceEnabled.value
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            voiceManager.stop()
        }
    }

    fun toggleRushHour() {
        _rushHourMode.value = !_rushHourMode.value
        val msg = if (_rushHourMode.value) "Rush Hour Traffic Protocol activated city-wide" else "Normal traffic flow restored"
        speakNow(msg, flush = false)
        recordVoiceLog(msg, "ALERT")
    }

    fun broadcastAnnouncement(message: String, category: String = "CUSTOM") {
        if (message.isBlank()) return
        speakNow(message, flush = true)
        recordVoiceLog(message, category)
    }

    fun clearViolations() {
        viewModelScope.launch {
            repository.clearViolations()
        }
    }

    fun dismissViolation(id: String) {
        viewModelScope.launch {
            repository.dismissViolation(id)
        }
    }

    fun payViolation(id: String) {
        viewModelScope.launch {
            repository.updateViolationStatus(id, "PAID")
        }
    }

    private fun speakNow(text: String, flush: Boolean) {
        if (!_isMuted.value) {
            voiceManager.speak(text, flush)
        }
    }

    private fun recordVoiceLog(message: String, category: String) {
        val entry = VoiceLog(
            id = UUID.randomUUID().toString(),
            message = message,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        _voiceLogs.value = (listOf(entry) + _voiceLogs.value).take(40)
    }

    private fun triggerHapticWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    fun selectCvVehicle(vehicle: TrackedVehicle?) {
        _selectedCvVehicle.value = vehicle
    }

    fun loadCustomCvVideo(uri: Uri, fileName: String) {
        visionEngine.loadCustomVideo(uri, fileName)
        recordVoiceLog("Loaded uploaded video: $fileName", "CV_DETECTION")
        speakNow("Video uploaded. Running OpenCV vehicle detection.", false)
    }

    fun setTrafficScenario(scenario: TrafficScenario) {
        visionEngine.setScenario(scenario)
        val name = when (scenario) {
            TrafficScenario.LIVE_MIXED -> "Live Highway Flow"
            TrafficScenario.LOW_TRAFFIC -> "Low Traffic (20s Green)"
            TrafficScenario.MEDIUM_TRAFFIC -> "Medium Traffic (40s Green)"
            TrafficScenario.HEAVY_CONGESTION -> "Heavy Congestion (60s Green)"
            TrafficScenario.STOPPED_HAZARD -> "Stopped Vehicle Hazard"
        }
        recordVoiceLog("CV Scenario: $name", "SCENARIO")
    }

    fun toggleCvPlayback() {
        visionEngine.togglePlayback()
    }

    fun stepCvFrame() {
        visionEngine.stepFrame()
    }

    fun injectCvStoppedHazard() {
        visionEngine.injectStoppedVehicle()
        triggerHapticWarning()
        speakNow("Warning: Stopped vehicle hazard detected in Lane 2.", true)
        recordVoiceLog("Hazard Alert: Vehicle stationary in Lane 2", "HAZARD")
    }

    fun resetCvEngine() {
        visionEngine.reset()
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        voiceManager.shutdown()
        visionEngine.shutdown()
    }
}
