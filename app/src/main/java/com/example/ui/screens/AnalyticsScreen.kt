package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleType
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

@Composable
fun AnalyticsScreen(
    viewModel: TrafficViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val junctions by viewModel.junctions.collectAsState()
    val violations by viewModel.violations.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Guide Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CITY TRAFFIC REPORT",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Mobility & Eco Impact",
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
                Text(text = "Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Friendly Summary Banner
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
                Text(text = "🌱", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Clean Air & Faster Travel",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "By keeping lights green longer for buses and heavy queues, vehicles spend less time idling in traffic, saving fuel and reducing smog.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Main KPI Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsKpiCard(
                title = "VEHICLES OBSERVED",
                value = "${stats.totalVehiclesTracked}",
                sub = "Tracked on cameras",
                icon = Icons.Default.DirectionsCar,
                color = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            AnalyticsKpiCard(
                title = "CO2 SMOG SAVED",
                value = "${stats.carbonSavedKg} kg",
                sub = "From less idling",
                icon = Icons.Default.Eco,
                color = SignalGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsKpiCard(
                title = "WAIT TIME SAVED",
                value = "${stats.adaptiveCycleSavingsSeconds / 60} mins",
                sub = "Faster commute",
                icon = Icons.Default.Timer,
                color = SignalYellow,
                modifier = Modifier.weight(1f)
            )
            AnalyticsKpiCard(
                title = "SPEED TICKETS",
                value = "${violations.size} Issued",
                sub = "Automated camera fines",
                icon = Icons.Default.ReceiptLong,
                color = SignalRed,
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Vehicle Classification Composition Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "WHAT KINDS OF VEHICLES USE THE ROADS?",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                val total = stats.breakdown.values.sum().toFloat()
                val entries = stats.breakdown.toList()

                entries.forEach { (type, count) ->
                    val percentage = if (total > 0) ((count / total) * 100).toInt() else 0
                    val barColor = when (type) {
                        VehicleType.CAR -> ColorCar
                        VehicleType.BIKE -> ColorBike
                        VehicleType.AUTO -> ColorAuto
                        VehicleType.BUS -> ColorBus
                        VehicleType.LORRY -> ColorLorry
                        VehicleType.TRUCK -> ColorTruck
                        VehicleType.AMBULANCE -> ColorAmbulance
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${type.emoji} ${type.label}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$count vehicles ($percentage%)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = barColor,
                            trackColor = CityDarkSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Live Intersections Status
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ALL INTERSECTIONS AT A GLANCE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                junctions.forEach { j ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CityDarkSurfaceVariant)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = j.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${j.district} • Limit: ${j.speedLimitKmh} km/h", color = TextMuted, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (j.congestionIndex < 40) SignalGreen else if (j.congestionIndex < 70) SignalYellow else SignalRed, CircleShape)
                            )
                            Text(
                                text = if (j.congestionIndex < 40) "Smooth Flow" else if (j.congestionIndex < 70) "Normal Flow" else "Busy",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AnalyticsKpiCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = sub,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
