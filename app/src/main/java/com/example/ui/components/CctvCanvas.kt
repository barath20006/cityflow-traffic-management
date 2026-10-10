package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun CctvCanvas(
    junction: JunctionData,
    selectedVehicle: DetectedVehicle?,
    onVehicleSelected: (DetectedVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val roadWidthPx = with(LocalDensity.current) { 96.dp.toPx() }

    // Blinking recording dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "cctv_anim")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    // Radar scan wave sweep
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scan"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF090E17))
            .border(1.5.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("cctv_canvas_box")
    ) {
        // Main CCTV Interactive Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(junction.vehicles) {
                    detectTapGestures { tapOffset ->
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        // Check if any vehicle was tapped
                        val tappedVehicle = junction.vehicles.firstOrNull { v ->
                            val bounds = computeVehicleBounds(v, canvasWidth, canvasHeight, roadWidthPx, density)
                            bounds.contains(tapOffset)
                        }
                        if (tappedVehicle != null) {
                            onVehicleSelected(tappedVehicle)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Road Crossroad Infrastructure
            drawRoads(w, h, roadWidthPx)

            // 2. Draw Stop Lines and Pedestrian Crossings
            drawCrosswalks(w, h, roadWidthPx)

            // 3. Draw Traffic Signal Lights at Intersection
            drawSignalsOnMap(w, h, roadWidthPx, junction.nsSignal, junction.ewSignal)

            // 4. Draw Radar Scanline Effect
            drawRadarScan(w, h, scanProgress)

            // 5. Draw All Detected Moving Vehicles with Bounding Boxes & Telemetry
            junction.vehicles.forEach { vehicle ->
                val bounds = computeVehicleBounds(vehicle, w, h, roadWidthPx, density)
                val isSelected = vehicle.id == selectedVehicle?.id
                drawVehicleWithBoundingBox(
                    vehicle = vehicle,
                    bounds = bounds,
                    isSelected = isSelected,
                    textMeasurer = textMeasurer,
                    speedLimit = junction.speedLimitKmh
                )
            }
        }

        // CCTV Camera HUD Overlays
        // Top Left: Camera ID & Rec Indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .background(Color(0xCC050811), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red.copy(alpha = recAlpha), RoundedCornerShape(4.dp))
            )
            Text(
                text = "CAM-01 • ${junction.code}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Top Right: AI Vision Engine Stats
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .background(Color(0xCC050811), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "AI Vision",
                tint = CyberCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "YOLO-V11 • 30 FPS",
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Left: Active Lane & Speed Limit
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color(0xCC050811), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Speed Radar",
                tint = SignalYellow,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "LIMIT: ${junction.speedLimitKmh} KM/H • ${junction.activeDirection.displayName.take(13)}",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Right: Vehicle Count in Frame
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(Color(0xCC050811), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "TRACKED: ${junction.vehicles.size}",
                color = SignalGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun DrawScope.drawRoads(w: Float, h: Float, roadWidth: Float) {
    val roadColor = Color(0xFF161F2E)
    val grassColor = Color(0xFF0A101D)

    // Background terrain
    drawRect(color = grassColor, size = Size(w, h))

    // Vertical Road (North-South)
    val vLeft = (w - roadWidth) / 2f
    drawRect(color = roadColor, topLeft = Offset(vLeft, 0f), size = Size(roadWidth, h))

    // Horizontal Road (East-West)
    val hTop = (h - roadWidth) / 2f
    drawRect(color = roadColor, topLeft = Offset(0f, hTop), size = Size(w, roadWidth))

    // Lane divider dashed lines (Vertical)
    val vCenter = w / 2f
    var y = 0f
    while (y < h) {
        if (y < hTop - 10f || y > hTop + roadWidth + 10f) {
            drawLine(
                color = Color(0xFF4B5563),
                start = Offset(vCenter, y),
                end = Offset(vCenter, (y + 14f).coerceAtMost(h)),
                strokeWidth = 2.dp.toPx()
            )
        }
        y += 28f
    }

    // Lane divider dashed lines (Horizontal)
    val hCenter = h / 2f
    var x = 0f
    while (x < w) {
        if (x < vLeft - 10f || x > vLeft + roadWidth + 10f) {
            drawLine(
                color = Color(0xFF4B5563),
                start = Offset(x, hCenter),
                end = Offset((x + 14f).coerceAtMost(w), hCenter),
                strokeWidth = 2.dp.toPx()
            )
        }
        x += 28f
    }
}

private fun DrawScope.drawCrosswalks(w: Float, h: Float, roadWidth: Float) {
    val vLeft = (w - roadWidth) / 2f
    val vRight = vLeft + roadWidth
    val hTop = (h - roadWidth) / 2f
    val hBottom = hTop + roadWidth
    val zebraColor = Color(0x66FFFFFF)

    // North Stop Line & Zebra
    drawLine(Color.White, Offset(vLeft, hTop - 12f), Offset(vRight, hTop - 12f), 3.dp.toPx())
    var x = vLeft
    while (x < vRight) {
        drawRect(zebraColor, Offset(x, hTop - 8f), Size(7f, 6f))
        x += 12f
    }

    // South Stop Line & Zebra
    drawLine(Color.White, Offset(vLeft, hBottom + 12f), Offset(vRight, hBottom + 12f), 3.dp.toPx())
    x = vLeft
    while (x < vRight) {
        drawRect(zebraColor, Offset(x, hBottom + 2f), Size(7f, 6f))
        x += 12f
    }

    // West Stop Line & Zebra
    drawLine(Color.White, Offset(vLeft - 12f, hTop), Offset(vLeft - 12f, hBottom), 3.dp.toPx())
    var y = hTop
    while (y < hBottom) {
        drawRect(zebraColor, Offset(vLeft - 8f, y), Size(6f, 7f))
        y += 12f
    }

    // East Stop Line & Zebra
    drawLine(Color.White, Offset(vRight + 12f, hTop), Offset(vRight + 12f, hBottom), 3.dp.toPx())
    y = hTop
    while (y < hBottom) {
        drawRect(zebraColor, Offset(vRight + 2f, y), Size(6f, 7f))
        y += 12f
    }
}

private fun DrawScope.drawSignalsOnMap(
    w: Float,
    h: Float,
    roadWidth: Float,
    nsSignal: SignalColor,
    ewSignal: SignalColor
) {
    val vLeft = (w - roadWidth) / 2f
    val vRight = vLeft + roadWidth
    val hTop = (h - roadWidth) / 2f
    val hBottom = hTop + roadWidth

    val nsColor = when (nsSignal) {
        SignalColor.GREEN -> SignalGreen
        SignalColor.YELLOW -> SignalYellow
        SignalColor.RED -> SignalRed
    }

    val ewColor = when (ewSignal) {
        SignalColor.GREEN -> SignalGreen
        SignalColor.YELLOW -> SignalYellow
        SignalColor.RED -> SignalRed
    }

    // Top Right corner: North-South Signal Light
    drawCircle(color = nsColor, radius = 6.dp.toPx(), center = Offset(vRight + 14f, hTop - 14f))
    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(vRight + 14f, hTop - 14f))

    // Bottom Left corner: East-West Signal Light
    drawCircle(color = ewColor, radius = 6.dp.toPx(), center = Offset(vLeft - 14f, hBottom + 14f))
    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(vLeft - 14f, hBottom + 14f))
}

private fun DrawScope.drawRadarScan(w: Float, h: Float, progress: Float) {
    val scanY = h * progress
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, CyberCyan.copy(alpha = 0.35f), Color.Transparent)
        ),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 2.dp.toPx()
    )
}

private fun computeVehicleBounds(
    vehicle: DetectedVehicle,
    w: Float,
    h: Float,
    roadWidth: Float,
    density: Float
): Rect {
    val vLeft = (w - roadWidth) / 2f
    val hTop = (h - roadWidth) / 2f

    val (vehWDp, vehHDp) = when (vehicle.type) {
        VehicleType.BIKE -> Pair(12f, 20f)
        VehicleType.CAR -> Pair(20f, 32f)
        VehicleType.AUTO -> Pair(18f, 24f)
        VehicleType.BUS -> Pair(24f, 48f)
        VehicleType.LORRY -> Pair(22f, 42f)
        VehicleType.TRUCK -> Pair(24f, 52f)
        VehicleType.AMBULANCE -> Pair(22f, 34f)
    }

    val vehW = vehWDp * density
    val vehH = vehHDp * density

    return if (vehicle.lane == LaneDirection.NORTH_SOUTH) {
        val laneX = if (vehicle.laneIndex == 0) vLeft + (roadWidth * 0.25f) else vLeft + (roadWidth * 0.75f)
        val posY = h * vehicle.progress - (vehH / 2f)
        Rect(
            left = laneX - (vehW / 2f),
            top = posY,
            right = laneX + (vehW / 2f),
            bottom = posY + vehH
        )
    } else {
        val laneY = if (vehicle.laneIndex == 0) hTop + (roadWidth * 0.25f) else hTop + (roadWidth * 0.75f)
        val posX = w * vehicle.progress - (vehH / 2f)
        Rect(
            left = posX,
            top = laneY - (vehW / 2f),
            right = posX + vehH,
            bottom = laneY + (vehW / 2f)
        )
    }
}

private fun DrawScope.drawVehicleWithBoundingBox(
    vehicle: DetectedVehicle,
    bounds: Rect,
    isSelected: Boolean,
    textMeasurer: TextMeasurer,
    speedLimit: Int
) {
    val isSpeeding = vehicle.speedKmh > speedLimit
    val isEmergency = vehicle.type == VehicleType.AMBULANCE

    val boxColor = when {
        isEmergency -> EmergencyPurple
        isSpeeding -> SignalRed
        isSelected -> CyberCyan
        else -> SignalGreen
    }

    val bodyColor = when (vehicle.type) {
        VehicleType.BIKE -> ColorBike
        VehicleType.CAR -> ColorCar
        VehicleType.AUTO -> ColorAuto
        VehicleType.BUS -> ColorBus
        VehicleType.LORRY -> ColorLorry
        VehicleType.TRUCK -> ColorTruck
        VehicleType.AMBULANCE -> ColorAmbulance
    }

    // 1. Draw vehicle body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(bounds.left, bounds.top),
        size = Size(bounds.width, bounds.height),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    // Cabin / Windshield detail
    val innerMargin = 3.dp.toPx()
    if (bounds.width > innerMargin * 2 && bounds.height > innerMargin * 2) {
        drawRoundRect(
            color = Color(0x55000000),
            topLeft = Offset(bounds.left + innerMargin, bounds.top + innerMargin),
            size = Size(bounds.width - innerMargin * 2, (bounds.height - innerMargin * 2) * 0.45f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }

    // 2. Draw AI Optical Bounding Box
    drawRoundRect(
        color = boxColor,
        topLeft = Offset(bounds.left - 4f, bounds.top - 4f),
        size = Size(bounds.width + 8f, bounds.height + 8f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = if (isSelected || isSpeeding) 2.dp.toPx() else 1.2.dp.toPx())
    )

    // Corner bracket accents
    val cornerLen = 6.dp.toPx()
    val strokeW = 2.dp.toPx()
    // Top-Left corner
    drawLine(boxColor, Offset(bounds.left - 5f, bounds.top - 5f), Offset(bounds.left - 5f + cornerLen, bounds.top - 5f), strokeW)
    drawLine(boxColor, Offset(bounds.left - 5f, bounds.top - 5f), Offset(bounds.left - 5f, bounds.top - 5f + cornerLen), strokeW)
    // Bottom-Right corner
    drawLine(boxColor, Offset(bounds.right + 5f, bounds.bottom + 5f), Offset(bounds.right + 5f - cornerLen, bounds.bottom + 5f), strokeW)
    drawLine(boxColor, Offset(bounds.right + 5f, bounds.bottom + 5f), Offset(bounds.right + 5f, bounds.bottom + 5f - cornerLen), strokeW)

    // 3. Draw Telemetry HUD Tag Above Vehicle
    val tagText = if (isEmergency) {
        "AMBULANCE 🚨"
    } else {
        "${vehicle.type.name.take(3)} ${vehicle.speedKmh.toInt()}km/h"
    }

    val textResult = textMeasurer.measure(
        text = AnnotatedString(tagText),
        style = TextStyle(
            color = if (isSpeeding) SignalRed else if (isEmergency) EmergencyPurple else Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )

    val labelX = (bounds.left - 2f).coerceIn(0f, (size.width - textResult.size.width - 10f).coerceAtLeast(0f))
    val labelY = (bounds.top - 18f).coerceAtLeast(4f)

    // Label background pill
    drawRoundRect(
        color = Color(0xEE050A14),
        topLeft = Offset(labelX - 4f, labelY - 2f),
        size = Size(textResult.size.width + 8f, textResult.size.height + 4f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    drawText(
        textMeasurer = textMeasurer,
        text = tagText,
        topLeft = Offset(labelX, labelY),
        style = TextStyle(
            color = if (isSpeeding) SignalRed else if (isEmergency) EmergencyPurple else CyberCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}
