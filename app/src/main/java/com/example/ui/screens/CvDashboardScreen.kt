package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cv.engine.TrafficScenario
import com.example.cv.model.*
import com.example.ui.components.CvVideoPlayerCanvas
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

@Composable
fun CvDashboardScreen(
    viewModel: TrafficViewModel,
    onOpenGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cvResult by viewModel.cvResult.collectAsState()
    val isPlaying by viewModel.cvIsPlaying.collectAsState()
    val videoTitle by viewModel.cvVideoTitle.collectAsState()
    val isUploadedVideo by viewModel.cvIsUploadedVideo.collectAsState()
    val activeScenario by viewModel.cvActiveScenario.collectAsState()
    val selectedVehicle by viewModel.selectedCvVehicle.collectAsState()

    var showGuideDetails by remember { mutableStateOf(false) }

    // Video Picker Launcher using Android Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "Uploaded_Traffic_Video.mp4"
            viewModel.loadCustomCvVideo(uri, fileName)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header with Command Center Title & Video Upload Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SignalGreen, CircleShape)
                    )
                    Text(
                        text = "OPENCV VISION DASHBOARD",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Smart City Traffic AI",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Upload & Guide Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Upload Video Button
                Button(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("upload_video_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Video",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Upload Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Help / Easy Guide Button
                FilledTonalButton(
                    onClick = { showGuideDetails = !showGuideDetails },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyberCyan.copy(alpha = 0.18f),
                        contentColor = CyberCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("cv_how_it_works_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How It Works",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "How It Works", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Beginner-Friendly Explanation Card (User-Friendly Guidance)
        AnimatedVisibility(visible = showGuideDetails) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💡", fontSize = 20.sp)
                        Text(
                            text = "Easy Explanation: How this System Works",
                            color = CyberCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "1. OpenCV & YOLO Vehicle Detection: Bounding boxes are drawn around every vehicle (Cars, Bikes, Buses, Trucks) with speed tracking.\n" +
                               "2. Multi-Lane Counting: Vehicles queued and passing each lane are counted in real-time.\n" +
                               "3. Traffic Density & Smart Signals:\n" +
                               "   • Low Traffic (≤2 cars) → 20 seconds green\n" +
                               "   • Medium Traffic (3-5 cars) → 40 seconds green\n" +
                               "   • Heavy Traffic (≥6 cars) → 60 seconds green\n" +
                               "   The system automatically gives MORE green time to lanes with higher density!\n" +
                               "4. Automatic Hazard Alerts: Triggers instant alerts when a car stops or heavy gridlock occurs.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGuideDetails = false }) {
                            Text(text = "Got it! Hide Guide", color = CyberCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Scenario Presets & Video Input Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📹 VIDEO SOURCE & TEST SCENARIOS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isUploadedVideo) {
                        Text(
                            text = "📁 Custom Upload Active",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Preset Scenario Chips (Low, Medium, Heavy, Hazard, Live)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        Triple(TrafficScenario.LIVE_MIXED, "Highway Live", "🛣️"),
                        Triple(TrafficScenario.LOW_TRAFFIC, "Low Traffic (20s)", "🟢"),
                        Triple(TrafficScenario.MEDIUM_TRAFFIC, "Medium Flow (40s)", "🟡"),
                        Triple(TrafficScenario.HEAVY_CONGESTION, "Heavy Rush (60s)", "🔴"),
                        Triple(TrafficScenario.STOPPED_HAZARD, "Stopped Hazard", "⚠️")
                    )

                    items(presets) { (sc, label, icon) ->
                        val isSelected = activeScenario == sc && !isUploadedVideo
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.2f) else CityDarkSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else CityDarkOutline,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.setTrafficScenario(sc) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("preset_${sc.name}")
                        ) {
                            Text(
                                text = "$icon $label",
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Video Player Action Bar (Play/Pause, Step Frame, Test Hazard, Reset)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play / Pause Button
                    Button(
                        onClick = { viewModel.toggleCvPlayback() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) CityDarkSurfaceVariant else SignalGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("cv_play_pause_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isPlaying) "Pause" else "Play", fontSize = 11.sp)
                    }

                    // Step Frame Button
                    OutlinedButton(
                        onClick = { viewModel.stepCvFrame() },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("cv_step_frame_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Step",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "Step", fontSize = 11.sp)
                    }

                    // Test Stopped Car Hazard Button
                    Button(
                        onClick = { viewModel.injectCvStoppedHazard() },
                        colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp)
                            .testTag("cv_inject_stopped_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(text = "⚠️ Test Hazard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetCvEngine() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(CityDarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .testTag("cv_reset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Live / Uploaded Processed Video Feed Viewport
        CvVideoPlayerCanvas(
            result = cvResult,
            sourceTitle = videoTitle,
            isPlaying = isPlaying,
            selectedVehicle = selectedVehicle,
            onSelectVehicle = { viewModel.selectCvVehicle(it) }
        )

        // Selected Vehicle Telemetry Card (When user taps on any vehicle in video)
        AnimatedVisibility(visible = selectedVehicle != null) {
            selectedVehicle?.let { v ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                        .testTag("cv_vehicle_inspector")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = v.classification.emoji, fontSize = 28.sp)
                            Column {
                                Text(
                                    text = "${v.classification.label} (${v.id})",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lane ${v.laneIndex} • Speed: ${v.speedKmh.toInt()} km/h • Confidence: ${(v.confidence * 100).toInt()}%",
                                    color = CyberCyan,
                                    fontSize = 12.sp
                                )
                                if (v.isStopped) {
                                    Text(
                                        text = "⚠️ STOPPED for ${v.stoppedDurationSec.toInt()}s (Hazard)",
                                        color = SignalRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.selectCvVehicle(null) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5. Total Vehicles Detected & Traffic Density Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Vehicles Detected Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TOTAL DETECTED",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${cvResult.totalCount}",
                        color = CyberCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Active in Frame",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Overall Traffic Density Card (Low, Medium, Heavy)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (cvResult.overallDensity) {
                        TrafficDensityLevel.LOW -> SignalGreen.copy(alpha = 0.12f)
                        TrafficDensityLevel.MEDIUM -> SignalYellow.copy(alpha = 0.12f)
                        TrafficDensityLevel.HEAVY -> SignalRed.copy(alpha = 0.15f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .border(
                        1.dp,
                        when (cvResult.overallDensity) {
                            TrafficDensityLevel.LOW -> SignalGreen.copy(alpha = 0.5f)
                            TrafficDensityLevel.MEDIUM -> SignalYellow.copy(alpha = 0.5f)
                            TrafficDensityLevel.HEAVY -> SignalRed.copy(alpha = 0.6f)
                        },
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TRAFFIC DENSITY",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    when (cvResult.overallDensity) {
                                        TrafficDensityLevel.LOW -> SignalGreen
                                        TrafficDensityLevel.MEDIUM -> SignalYellow
                                        TrafficDensityLevel.HEAVY -> SignalRed
                                    },
                                    CircleShape
                                )
                        )
                        Text(
                            text = cvResult.overallDensity.label,
                            color = when (cvResult.overallDensity) {
                                TrafficDensityLevel.LOW -> SignalGreen
                                TrafficDensityLevel.MEDIUM -> SignalYellow
                                TrafficDensityLevel.HEAVY -> SignalRed
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = cvResult.overallCongestionStatus,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // 6. Vehicle Classification Breakdown Grid (Cars, Bikes, Buses, Trucks)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "VEHICLE CLASSIFICATION COUNTS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cars Count
                    ClassificationCountItem(
                        emoji = "🚗",
                        title = "Cars",
                        count = cvResult.carCount,
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    // Bikes Count
                    ClassificationCountItem(
                        emoji = "🏍️",
                        title = "Bikes",
                        count = cvResult.bikeCount,
                        color = SignalGreen,
                        modifier = Modifier.weight(1f)
                    )
                    // Buses Count
                    ClassificationCountItem(
                        emoji = "🚌",
                        title = "Buses",
                        count = cvResult.busCount,
                        color = Color(0xFFC084FC),
                        modifier = Modifier.weight(1f)
                    )
                    // Trucks Count
                    ClassificationCountItem(
                        emoji = "🚚",
                        title = "Trucks",
                        count = cvResult.truckCount,
                        color = Color(0xFFFB923C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7. Smart Traffic Signal Module (Density-Based Timing: 20s, 40s, 60s)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .testTag("smart_traffic_signal_module")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Traffic,
                            contentDescription = "Smart Signal",
                            tint = SignalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SMART TRAFFIC SIGNAL MODULE",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SignalGreen.copy(alpha = 0.2f))
                            .border(1.dp, SignalGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTO-ADAPTIVE ON",
                            color = SignalGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Recommended Timing Callout Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.1f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Signal: Priority Lane ${cvResult.activeSignalLane}",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${cvResult.recommendedSignalSeconds}s GREEN",
                                color = SignalGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "System gives more green time to higher density: Low → 20s | Medium → 40s | Heavy → 60s. Lane ${cvResult.activeSignalLane} has maximum vehicle queue.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Visual Timing Scale Reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimingRuleIndicator(
                        label = "Low Traffic",
                        timing = "20s",
                        active = cvResult.overallDensity == TrafficDensityLevel.LOW,
                        color = SignalGreen
                    )
                    TimingRuleIndicator(
                        label = "Medium Flow",
                        timing = "40s",
                        active = cvResult.overallDensity == TrafficDensityLevel.MEDIUM,
                        color = SignalYellow
                    )
                    TimingRuleIndicator(
                        label = "Heavy Congestion",
                        timing = "60s",
                        active = cvResult.overallDensity == TrafficDensityLevel.HEAVY,
                        color = SignalRed
                    )
                }
            }
        }

        // 8. Multi-Lane Detailed Breakdown (Lanes 1, 2, 3)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LANE TRAFFIC & PASSING BREAKDOWN",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                cvResult.lanes.forEach { lane ->
                    val isPriority = lane.laneId == cvResult.activeSignalLane
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPriority) CyberCyan.copy(alpha = 0.08f) else CityDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isPriority) CyberCyan.copy(alpha = 0.5f) else CityDarkOutline,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = lane.name,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isPriority) {
                                        Text(
                                            text = "PRIORITY 🟢",
                                            color = SignalGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Passed Total: ${lane.passedCount} vehicles • Queue: ${lane.vehicleCount}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = lane.density.label,
                                    color = when (lane.density) {
                                        TrafficDensityLevel.LOW -> SignalGreen
                                        TrafficDensityLevel.MEDIUM -> SignalYellow
                                        TrafficDensityLevel.HEAVY -> SignalRed
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${lane.recommendedGreenSeconds}s Green",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Alert System (Heavy Congestion, Stopped Vehicle Hazard, High Traffic, Flow Shifts)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
                .testTag("cv_alerts_section")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerts",
                            tint = if (cvResult.alerts.isNotEmpty()) SignalRed else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CONGESTION & HAZARD ALERTS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${cvResult.alerts.size} Active",
                        color = if (cvResult.alerts.isNotEmpty()) SignalRed else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cvResult.alerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Traffic flowing normally. No hazards detected.",
                            color = SignalGreen,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        cvResult.alerts.take(4).forEach { alert ->
                            val borderColor = when (alert.severity) {
                                AlertSeverity.CRITICAL -> SignalRed
                                AlertSeverity.WARNING -> SignalYellow
                                AlertSeverity.INFO -> CyberCyan
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = borderColor.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = when (alert.severity) {
                                            AlertSeverity.CRITICAL -> "🚨"
                                            AlertSeverity.WARNING -> "⚠️"
                                            AlertSeverity.INFO -> "ℹ️"
                                        },
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = alert.message,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ClassificationCountItem(
    emoji: String,
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = "$count",
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimingRuleIndicator(
    label: String,
    timing: String,
    active: Boolean,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) color.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (active) color else CityDarkOutline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = timing,
            color = if (active) color else TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = if (active) TextPrimary else TextMuted,
            fontSize = 10.sp
        )
    }
}
