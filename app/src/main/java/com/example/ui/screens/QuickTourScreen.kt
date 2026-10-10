package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cv.engine.TrafficScenario
import com.example.model.VehicleType
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

@Composable
fun QuickTourScreen(
    viewModel: TrafficViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var lastFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎓", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "Easy Guide: OpenCV & Smart Traffic",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Understand all features in under 1 minute",
                            color = CyberCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "This Smart City AI solves traffic congestion using 4 automated modules:\n\n" +
                            "1. 📷 OpenCV & YOLO Video Detection: Identifies Cars, Bikes, Buses, and Trucks with bounding boxes.\n" +
                            "2. 🚦 Smart Traffic Signals: Gives more green-light time to busy lanes (Low: 20s, Medium: 40s, Heavy: 60s).\n" +
                            "3. ⚠️ Hazard & Congestion Alerts: Immediately notifies when a car stops or traffic gridlock occurs.\n" +
                            "4. 🏎️ Optical Speed Radar: Flags speeding vehicles and issues digital tickets automatically.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Live Action Feedback banner (if user pressed a button)
        AnimatedVisibility(visible = lastFeedbackMessage != null) {
            lastFeedbackMessage?.let { feedback ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SignalGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SignalGreen, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "✅", fontSize = 20.sp)
                        Text(
                            text = feedback,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Interactive Hands-On Playground: "Try These Controls Now"
        Text(
            text = "TAP TO TEST KEY OPENCV & SIGNAL FEATURES",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // Experiment 1: Heavy Congestion (60s Signal Timing)
        InteractiveExperimentCard(
            emoji = "🔴",
            title = "1. Test Heavy Congestion (60s Green Signal)",
            description = "Simulates a rush hour traffic jam. Watch the system calculate Heavy Traffic density and automatically allocate 60 seconds green light!",
            buttonLabel = "Simulate Heavy Congestion (60s)",
            buttonColor = SignalRed,
            onClick = {
                viewModel.setTrafficScenario(TrafficScenario.HEAVY_CONGESTION)
                lastFeedbackMessage = "Heavy Traffic simulated! System recommended 60s Green Signal and raised Congestion Alert."
            }
        )

        // Experiment 2: Stopped Vehicle Hazard Alert
        InteractiveExperimentCard(
            emoji = "⚠️",
            title = "2. Test Stopped Vehicle Hazard Alert",
            description = "Simulates a vehicle broken down in Lane 2 stationary for over 3 seconds. Triggers a Critical Hazard alert and haptic alert!",
            buttonLabel = "Simulate Stopped Vehicle Hazard",
            buttonColor = SignalYellow.copy(alpha = 0.9f),
            onClick = {
                viewModel.injectCvStoppedHazard()
                lastFeedbackMessage = "Stopped Vehicle Hazard triggered in Lane 2! Alert raised on the dashboard."
            }
        )

        // Experiment 3: Low Traffic (20s Signal Timing)
        InteractiveExperimentCard(
            emoji = "🟢",
            title = "3. Test Low Traffic (20s Green Signal)",
            description = "Simulates light off-peak traffic. The system detects Low density and keeps green time efficient at 20 seconds.",
            buttonLabel = "Simulate Low Traffic (20s)",
            buttonColor = SignalGreen,
            onClick = {
                viewModel.setTrafficScenario(TrafficScenario.LOW_TRAFFIC)
                lastFeedbackMessage = "Low Traffic set! System calculated 20s Green Timing for smooth flow."
            }
        )

        // Experiment 4: Catch a Speeder
        InteractiveExperimentCard(
            emoji = "🏎️",
            title = "4. Catch a Speeding Car",
            description = "Spawns a vehicle over the speed limit. The radar turns red and writes an automated speed ticket to the database.",
            buttonLabel = "Catch Speeder",
            buttonColor = TechBlue,
            onClick = {
                viewModel.spawnVehicle(VehicleType.CAR)
                lastFeedbackMessage = "Speeding vehicle spawned! Check 'Speed Tickets' to view ticket."
            }
        )

        // 3. Visual Legend: What do the box colors mean?
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "WHAT DO THE BOUNDING BOX COLORS MEAN?",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                ColorLegendRow(
                    color = CyberCyan,
                    title = "Cyan Blue Box",
                    description = "Detected Passenger Car (automobile)."
                )
                ColorLegendRow(
                    color = SignalGreen,
                    title = "Green Box",
                    description = "Detected Motorcycle / Bike."
                )
                ColorLegendRow(
                    color = Color(0xFFC084FC),
                    title = "Purple Box",
                    description = "Detected City Transit Bus."
                )
                ColorLegendRow(
                    color = Color(0xFFFB923C),
                    title = "Orange Box",
                    description = "Detected Heavy Cargo Truck."
                )
                ColorLegendRow(
                    color = SignalRed,
                    title = "Pulsing Red Box",
                    description = "Stopped Vehicle Hazard or Speeding Violator."
                )
            }
        }

        // 4. Common Questions (FAQ)
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "FREQUENTLY ASKED QUESTIONS",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                FaqItem(
                    q = "Can I upload my own traffic video?",
                    a = "Yes! On the 'CV Dashboard' screen, tap the 'Upload Video' button at the top to select any video from your phone. OpenCV will detect vehicles frame-by-frame."
                )

                FaqItem(
                    q = "How does the Smart Signal timing work?",
                    a = "The AI counts vehicles in each lane:\n• Low Traffic (≤2) → 20 seconds green\n• Medium Traffic (3-5) → 40 seconds green\n• Heavy Traffic (≥6) → 60 seconds green\nThe lane with the highest density is given the longer green light automatically!"
                )

                FaqItem(
                    q = "What triggers the Hazard Alert?",
                    a = "When any vehicle remains stationary (speed 0 km/h) for more than 3.0 seconds, or when overall traffic density reaches Heavy gridlock."
                )
            }
        }

        // Go to CV Dashboard Button
        Button(
            onClick = onNavigateToCamera,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("tour_go_dashboard_btn")
        ) {
            Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Open CV Dashboard & Live Feed", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun InteractiveExperimentCard(
    emoji: String,
    title: String,
    description: String,
    buttonLabel: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, buttonColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = emoji, fontSize = 22.sp)
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(text = buttonLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ColorLegendRow(
    color: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = "$title: ",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FaqItem(q: String, a: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = q, color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = a, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
