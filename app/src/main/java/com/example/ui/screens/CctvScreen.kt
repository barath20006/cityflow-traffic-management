package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.model.*
import com.example.ui.components.CctvCanvas
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

@Composable
fun CctvScreen(
    viewModel: TrafficViewModel,
    onOpenTour: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val junctions by viewModel.junctions.collectAsState()
    val selectedJunctionId by viewModel.selectedJunctionId.collectAsState()
    val junction by viewModel.selectedJunction.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val rushHourMode by viewModel.rushHourMode.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val scrollState = rememberScrollState()

    if (junction == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberCyan)
        }
        return
    }

    val j = junction!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header with App Title & Quick Utility Buttons
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
                        text = "CITYFLOW SMART TRAFFIC",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = j.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top action buttons: Help Guide, Rush hour, Mute
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Help Guide Button
                FilledTonalButton(
                    onClick = onOpenTour,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyberCyan.copy(alpha = 0.2f),
                        contentColor = CyberCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("help_guide_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Easy Guide",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Easy Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Rush hour toggle
                IconButton(
                    onClick = { viewModel.toggleRushHour() },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (rushHourMode) SignalYellow.copy(alpha = 0.2f) else CityDarkSurfaceVariant,
                            CircleShape
                        )
                        .testTag("rush_hour_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Rush Hour",
                        tint = if (rushHourMode) SignalYellow else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Audio Mute toggle
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isMuted) SignalRed.copy(alpha = 0.2f) else CityDarkSurfaceVariant,
                            CircleShape
                        )
                        .testTag("mute_audio_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isMuted) SignalRed else CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Friendly Easy Guide Banner (Direct Access for Beginners)
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCyan.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { onOpenTour() }
                .testTag("easy_guide_banner")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "👋", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Confused? Tap here for 1-Minute Easy Guide",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clear explanations & interactive buttons to test all features!",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. Interactive Quick Playground Card (User-Friendly Controls)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎮 INTERACTIVE CONTROLS (TAP TO TEST)",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Live Actions",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Spawn Vehicle Button
                    Button(
                        onClick = { viewModel.spawnVehicle() },
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("add_vehicle_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(text = "➕ Add Car", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Test Speeder Button
                    Button(
                        onClick = { viewModel.spawnVehicle(VehicleType.CAR) },
                        colors = ButtonDefaults.buttonColors(containerColor = SignalYellow.copy(alpha = 0.85f), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(42.dp)
                            .testTag("test_speeder_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(text = "🏎️ Test Speeder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Emergency Ambulance Button
                    Button(
                        onClick = { viewModel.triggerEmergencyCorridor(j.id) },
                        enabled = !j.isEmergencyCorridor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignalRed,
                            disabledContainerColor = SignalRed.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                            .testTag("emergency_corridor_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (j.isEmergencyCorridor) "🚨 Path Open" else "🚨 Ambulance",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Switch Lights Button
                    OutlinedButton(
                        onClick = { viewModel.forceSignalSwitch(j.id) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(42.dp)
                            .testTag("force_signal_btn"),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Text(text = "Flip 🚦", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Junction Selection Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(junctions) { item ->
                val isSel = item.id == selectedJunctionId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) CyberCyan.copy(alpha = 0.2f) else CityDarkSurface)
                        .border(
                            1.dp,
                            if (isSel) CyberCyan else CityDarkOutline,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectJunction(item.id) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("junction_chip_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (item.nsSignal == SignalColor.GREEN || item.ewSignal == SignalColor.GREEN) SignalGreen else SignalRed,
                                    CircleShape
                                )
                        )
                        Text(
                            text = item.name.substringBefore(" &"),
                            color = if (isSel) CyberCyan else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // 4. Interactive CCTV Camera Viewport
        CctvCanvas(
            junction = j,
            selectedVehicle = selectedVehicle,
            onVehicleSelected = { viewModel.selectVehicle(it) }
        )

        // Friendly Tap Hint Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CityDarkSurfaceVariant.copy(alpha = 0.7f))
                .border(1.dp, CityDarkOutline, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Tip: Tap any moving vehicle on the camera to see its live speed!",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }

        // 5. Vehicle Telemetry Inspector Card (When vehicle is clicked)
        AnimatedVisibility(visible = selectedVehicle != null) {
            selectedVehicle?.let { v ->
                VehicleInspectorCard(
                    vehicle = v,
                    speedLimit = j.speedLimitKmh,
                    onClose = { viewModel.selectVehicle(null) }
                )
            }
        }

        // 6. Vehicle Classification Real-Time Breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VEHICLES ON ROAD (TAP TO ADD)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${j.vehicles.size} Driving Now",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val counts = VehicleType.entries.filter { it != VehicleType.AMBULANCE }.map { type ->
                        Pair(type, j.vehicles.count { it.type == type })
                    }

                    counts.forEach { (type, count) ->
                        ClassificationBadge(
                            type = type,
                            count = count,
                            onClick = { viewModel.spawnVehicle(type) }
                        )
                    }
                }
            }
        }

        // 7. Speed Limit Threshold Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Radar Limit",
                            tint = SignalYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Speed Limit Setting",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${j.speedLimitKmh} km/h",
                        color = CyberCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = j.speedLimitKmh.toFloat(),
                    onValueChange = { viewModel.setSpeedLimit(j.id, it.toInt()) },
                    valueRange = 30f..80f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = CityDarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("speed_limit_slider")
                )

                Text(
                    text = "Try dragging the slider down to 40 km/h: faster vehicles will be highlighted in RED and get ticketed automatically!",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ClassificationBadge(
    type: VehicleType,
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CityDarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = type.emoji, fontSize = 16.sp)
            Column {
                Text(
                    text = type.label.substringBefore(" /"),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "$count (+add)",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun VehicleInspectorCard(
    vehicle: DetectedVehicle,
    speedLimit: Int,
    onClose: () -> Unit
) {
    val isOver = vehicle.speedKmh > speedLimit

    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isOver) SignalRed else CyberCyan,
                RoundedCornerShape(12.dp)
            )
            .testTag("vehicle_inspector_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = vehicle.type.emoji, fontSize = 26.sp)
                    Column {
                        Text(
                            text = "${vehicle.type.label} • ${vehicle.plateNumber}",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOver) "⚠️ Speeding! Drive safely." else "✅ Safe Driving Velocity",
                            color = if (isOver) SignalRed else SignalGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Inspector",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = CityDarkOutline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "CURRENT SPEED", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${vehicle.speedKmh.toInt()} km/h",
                        color = if (isOver) SignalRed else SignalGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(text = "SPEED LIMIT", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "$speedLimit km/h",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(text = "RADAR STATUS", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = if (isOver) "TICKETED 🎫" else "LEGAL ✓",
                        color = if (isOver) SignalRed else SignalGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
