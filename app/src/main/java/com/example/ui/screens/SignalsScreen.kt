package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

@Composable
fun SignalsScreen(
    viewModel: TrafficViewModel,
    modifier: Modifier = Modifier
) {
    val junctions by viewModel.junctions.collectAsState()
    val selectedJunctionId by viewModel.selectedJunctionId.collectAsState()
    val junction by viewModel.selectedJunction.collectAsState()
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
        // 1. Header with Help Guide button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMART TRAFFIC LIGHTS",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = j.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            FilledTonalButton(
                onClick = { viewModel.toggleHelpDialog(true) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CyberCyan.copy(alpha = 0.2f),
                    contentColor = CyberCyan
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Help",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "How It Works", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Friendly Explanation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SignalGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "🚦", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Automatic Adaptive Timing",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Green lights automatically stay on longer whenever buses or long lines of vehicles build up, clearing queues without manual intervention.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Junction selector
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
                        .testTag("signal_tab_${item.id}")
                ) {
                    Text(
                        text = item.name.substringBefore(" &"),
                        color = if (isSel) CyberCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // 2. Central Countdown Ring & Traffic Lights
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ACTIVE GREEN PATH: ${if (j.activeDirection == LaneDirection.NORTH_SOUTH) "NORTH - SOUTH" else "EAST - WEST"}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Large Countdown Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(125.dp)
                ) {
                    val progress = (j.secondsRemaining.toFloat() / j.baseDurationSeconds.toFloat()).coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = when (if (j.activeDirection == LaneDirection.NORTH_SOUTH) j.nsSignal else j.ewSignal) {
                            SignalColor.GREEN -> SignalGreen
                            SignalColor.YELLOW -> SignalYellow
                            SignalColor.RED -> SignalRed
                        },
                        trackColor = CityDarkSurfaceVariant
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (j.isEmergencyCorridor) "PRIORITY" else "${j.secondsRemaining}",
                            color = if (j.isEmergencyCorridor) EmergencyPurple else TextPrimary,
                            fontSize = if (j.isEmergencyCorridor) 18.sp else 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (j.isEmergencyCorridor) "CORRIDOR" else "SECONDS LEFT",
                            color = if (j.isEmergencyCorridor) EmergencyPurple else TextMuted,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Dual Signal Heads: North-South & East-West
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SignalHeadBox(
                        title = "NORTH - SOUTH",
                        signal = j.nsSignal,
                        isActive = j.activeDirection == LaneDirection.NORTH_SOUTH
                    )
                    SignalHeadBox(
                        title = "EAST - WEST",
                        signal = j.ewSignal,
                        isActive = j.activeDirection == LaneDirection.EAST_WEST
                    )
                }
            }
        }

        // 3. AI Adaptive Decision Log
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TechBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Timing",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "AI DECISION REASON",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = j.adaptiveReason,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (j.adaptiveExtensionSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SignalGreen.copy(alpha = 0.2f))
                            .border(1.dp, SignalGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ +${j.adaptiveExtensionSeconds}s BONUS GREEN TIME EXTENDED",
                            color = SignalGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4. Manual Light Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "MANUAL CONTROLS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.forceSignalSwitch(j.id) },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Flip Green Light 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerEmergencyCorridor(j.id) },
                        enabled = !j.isEmergencyCorridor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignalRed,
                            disabledContainerColor = SignalRed.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (j.isEmergencyCorridor) "Ambulance Active 🚨" else "Ambulance Priority 🚨",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Approach Lane Queue Density Meters
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ROAD QUEUES & CONGESTION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                val nsVehicles = j.vehicles.filter { it.lane == LaneDirection.NORTH_SOUTH }
                val ewVehicles = j.vehicles.filter { it.lane == LaneDirection.EAST_WEST }

                QueueBar(
                    label = "North - South Road",
                    vehicleCount = nsVehicles.size,
                    maxVehicles = 8
                )

                QueueBar(
                    label = "East - West Road",
                    vehicleCount = ewVehicles.size,
                    maxVehicles = 8
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SignalHeadBox(
    title: String,
    signal: SignalColor,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = if (isActive) CyberCyan else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Traffic Light Housing
        Box(
            modifier = Modifier
                .width(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(Color(0xFF070B14))
                .border(1.dp, CityDarkOutline, RoundedCornerShape(23.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Red
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (signal == SignalColor.RED) SignalRed else SignalRed.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
                // Yellow
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (signal == SignalColor.YELLOW) SignalYellow else SignalYellow.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
                // Green
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (signal == SignalColor.GREEN) SignalGreen else SignalGreen.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun QueueBar(
    label: String,
    vehicleCount: Int,
    maxVehicles: Int
) {
    val fraction = (vehicleCount.toFloat() / maxVehicles.toFloat()).coerceIn(0.05f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "$vehicleCount Vehicles waiting",
                color = CyberCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (fraction > 0.75f) SignalRed else if (fraction > 0.45f) SignalYellow else SignalGreen,
            trackColor = CityDarkSurfaceVariant
        )
    }
}
