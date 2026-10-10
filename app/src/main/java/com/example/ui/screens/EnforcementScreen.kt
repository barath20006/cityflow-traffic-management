package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EnforcementScreen(
    viewModel: TrafficViewModel,
    modifier: Modifier = Modifier
) {
    val violations by viewModel.violations.collectAsState()
    var selectedFilter by remember { mutableStateOf<VehicleType?>(null) }
    val filteredList = remember(violations, selectedFilter) {
        if (selectedFilter == null) violations
        else violations.filter { it.vehicleType == selectedFilter }
    }

    val totalFines = remember(violations) {
        violations.sumOf { it.fineAmount }
    }
    val highestSpeed = remember(violations) {
        violations.maxOfOrNull { it.speedKmh } ?: 0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Help Guide and Clear Records button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTOMATIC RADAR ENFORCEMENT",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Speed Tickets & Challans",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                if (violations.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearViolations() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CityDarkSurfaceVariant, CircleShape)
                            .testTag("clear_violations_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Records",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Friendly Tip Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SignalYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "🎫", fontSize = 22.sp)
                Column {
                    Text(
                        text = "How E-Challans Work",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Whenever a vehicle exceeds the corridor speed limit, the optical camera snaps the vehicle, records the license plate, and generates a digital fine.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 2. Summary KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                title = "TICKETS ISSUED",
                value = "${violations.size}",
                accentColor = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "TOP SPEED CAUGHT",
                value = if (highestSpeed > 0) "${highestSpeed.toInt()} km/h" else "--",
                accentColor = SignalRed,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "TOTAL FINES",
                value = "₹$totalFines",
                accentColor = SignalYellow,
                modifier = Modifier.weight(1.2f)
            )
        }

        // 3. Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChipItem(
                    label = "All Vehicles (${violations.size})",
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            items(VehicleType.entries.filter { it != VehicleType.AMBULANCE }) { type ->
                val typeCount = violations.count { it.vehicleType == type }
                FilterChipItem(
                    label = "${type.emoji} ${type.label.substringBefore(" /")} ($typeCount)",
                    isSelected = selectedFilter == type,
                    onClick = { selectedFilter = type }
                )
            }
        }

        // 4. Violations List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CityDarkSurface)
                    .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Zero Violations",
                        tint = SignalGreen,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "No Speeders Caught Yet!",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All moving vehicles are currently operating within the speed limit.\nWant to test it? Go to Live Camera and lower the speed slider to 30 or 40 km/h!",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { record ->
                    ViolationItemCard(
                        record = record,
                        onPay = { viewModel.payViolation(record.id) },
                        onDismiss = { viewModel.dismissViolation(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, CityDarkOutline, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = accentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CityDarkSurface)
            .border(
                1.dp,
                if (isSelected) CyberCyan else CityDarkOutline,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyberCyan else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ViolationItemCard(
    record: SpeedViolationRecord,
    onPay: () -> Unit,
    onDismiss: () -> Unit
) {
    val overSpeed = (record.speedKmh - record.speedLimitKmh).toInt()
    val isPaid = record.status == "PAID"
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()).format(Date(record.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isPaid) SignalGreen.copy(alpha = 0.5f) else SignalRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("violation_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = record.vehicleType.emoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = "${record.vehicleType.label} • ${record.plateNumber}",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Captured at ${record.junctionName}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaid) SignalGreen.copy(alpha = 0.2f) else SignalRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPaid) "PAID ✅" else "+$overSpeed KM/H OVER",
                        color = if (isPaid) SignalGreen else SignalRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Divider(color = CityDarkOutline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Speed: ${record.speedKmh.toInt()} km/h (Limit was ${record.speedLimitKmh} km/h)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = dateStr,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "Fine: ₹${record.fineAmount}",
                    color = SignalYellow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Actions
            if (!isPaid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Dismiss", color = TextMuted, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Mark Paid ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
