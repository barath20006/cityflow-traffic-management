package com.example.ui.components

import android.graphics.RectF
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cv.model.*
import com.example.ui.theme.*

@Composable
fun CvVideoPlayerCanvas(
    result: CvProcessingResult,
    sourceTitle: String,
    isPlaying: Boolean,
    selectedVehicle: TrackedVehicle?,
    onSelectVehicle: (TrackedVehicle?) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cv_hud_anim")

    // Pulsing alpha for hazard bounding boxes & tripwire
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazard_pulse"
    )

    // Scanning laser sweep
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_sweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
            .border(1.5.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .testTag("cv_video_canvas_box")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(result.vehicles) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        // Check if tap hit any vehicle bounding box
                        val hit = result.vehicles.find { v ->
                            val left = v.normBounds.left * w
                            val top = v.normBounds.top * h
                            val right = v.normBounds.right * w
                            val bottom = v.normBounds.bottom * h
                            tapOffset.x in left..right && tapOffset.y in top..bottom
                        }
                        onSelectVehicle(hit)
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw Road Asphalt & Perspective
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B111E),
                        Color(0xFF131D31),
                        Color(0xFF1A2744)
                    )
                ),
                topLeft = Offset.Zero,
                size = size
            )

            // Road borders (outer shoulders)
            val leftShoulder = canvasW * 0.06f
            val rightShoulder = canvasW * 0.94f

            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(leftShoulder, 0f),
                end = Offset(leftShoulder, canvasH),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(rightShoulder, 0f),
                end = Offset(rightShoulder, canvasH),
                strokeWidth = 3.dp.toPx()
            )

            // 2. Lane Dividers (Dashed White Lines)
            val laneWidth = (rightShoulder - leftShoulder) / 3f
            val lane1DividerX = leftShoulder + laneWidth
            val lane2DividerX = leftShoulder + laneWidth * 2

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 20f), 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(lane1DividerX, 0f),
                end = Offset(lane1DividerX, canvasH),
                strokeWidth = 2.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(lane2DividerX, 0f),
                end = Offset(lane2DividerX, canvasH),
                strokeWidth = 2.dp.toPx(),
                pathEffect = dashEffect
            )

            // 3. Lane Detection Tripwire (Y = 0.75)
            val tripwireY = canvasH * 0.75f
            drawLine(
                color = SignalYellow.copy(alpha = 0.85f),
                start = Offset(leftShoulder, tripwireY),
                end = Offset(rightShoulder, tripwireY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )

            // 4. Subtle Optical Scanner Laser Line
            drawLine(
                color = CyberCyan.copy(alpha = 0.25f),
                start = Offset(0f, scanY * canvasH),
                end = Offset(canvasW, scanY * canvasH),
                strokeWidth = 1.5.dp.toPx()
            )

            // 5. Draw Tracked Vehicle Bounding Boxes & Tech Reticles
            for (v in result.vehicles) {
                val boxLeft = v.normBounds.left * canvasW
                val boxTop = v.normBounds.top * canvasH
                val boxRight = v.normBounds.right * canvasW
                val boxBottom = v.normBounds.bottom * canvasH
                val boxW = boxRight - boxLeft
                val boxH = boxBottom - boxTop

                val isSelected = selectedVehicle?.id == v.id

                val (strokeColor, fillColor) = when {
                    v.isStopped -> Pair(SignalRed, SignalRed.copy(alpha = 0.28f * pulseAlpha))
                    v.speedKmh < 18f -> Pair(SignalYellow, SignalYellow.copy(alpha = 0.16f))
                    v.classification == VehicleClass.CAR -> Pair(CyberCyan, CyberCyan.copy(alpha = 0.14f))
                    v.classification == VehicleClass.MOTORCYCLE -> Pair(SignalGreen, SignalGreen.copy(alpha = 0.14f))
                    v.classification == VehicleClass.BUS -> Pair(Color(0xFFC084FC), Color(0xFFC084FC).copy(alpha = 0.18f))
                    VehicleClass.TRUCK == v.classification -> Pair(Color(0xFFFB923C), Color(0xFFFB923C).copy(alpha = 0.16f))
                    else -> Pair(CyberCyan, CyberCyan.copy(alpha = 0.12f))
                }

                // Vehicle Box Fill
                drawRect(
                    color = fillColor,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxW, boxH)
                )

                // Vehicle Box Border
                drawRect(
                    color = if (isSelected) Color.White else strokeColor,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxW, boxH),
                    style = Stroke(width = if (isSelected) 2.8.dp.toPx() else 1.8.dp.toPx())
                )

                // Tech Corner Reticles
                val cornerLen = 8.dp.toPx()
                val reticleColor = if (isSelected) Color.White else strokeColor
                val reticleStroke = 2.dp.toPx()

                // Top-Left corner
                drawLine(reticleColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLen, boxTop), reticleStroke)
                drawLine(reticleColor, Offset(boxLeft, boxTop), Offset(boxLeft, boxTop + cornerLen), reticleStroke)
                // Top-Right corner
                drawLine(reticleColor, Offset(boxRight, boxTop), Offset(boxRight - cornerLen, boxTop), reticleStroke)
                drawLine(reticleColor, Offset(boxRight, boxTop), Offset(boxRight, boxTop + cornerLen), reticleStroke)
                // Bottom-Left corner
                drawLine(reticleColor, Offset(boxLeft, boxBottom), Offset(boxLeft + cornerLen, boxBottom), reticleStroke)
                drawLine(reticleColor, Offset(boxLeft, boxBottom), Offset(boxLeft, boxBottom - cornerLen), reticleStroke)
                // Bottom-Right corner
                drawLine(reticleColor, Offset(boxRight, boxBottom), Offset(boxRight - cornerLen, boxBottom), reticleStroke)
                drawLine(reticleColor, Offset(boxRight, boxBottom), Offset(boxRight, boxBottom - cornerLen), reticleStroke)

                // Label Tag Background
                val tagH = 14.dp.toPx()
                val tagW = (boxW * 0.95f).coerceAtLeast(48.dp.toPx())
                drawRect(
                    color = strokeColor.copy(alpha = 0.95f),
                    topLeft = Offset(boxLeft, (boxTop - tagH).coerceAtLeast(0f)),
                    size = Size(tagW, tagH)
                )
            }
        }

        // Top Optical HUD Overlay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isPlaying) SignalRed else SignalYellow, RoundedCornerShape(4.dp))
                )
                Text(
                    text = if (isPlaying) "● REC LIVE [OpenCV + YOLO]" else "⏸ PAUSED",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", result.fps)} FPS • ${result.inferenceLatencyMs}ms",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top-center video title pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = sourceTitle.take(34),
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Lane Info Bottom Badges Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            result.lanes.forEach { lane ->
                val isPriority = lane.laneId == result.activeSignalLane
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isPriority) SignalGreen.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.7f)
                        )
                        .border(
                            1.dp,
                            if (isPriority) SignalGreen else CityDarkOutline,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LANE ${lane.laneId}",
                            color = if (isPriority) SignalGreen else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lane.vehicleCount} cars • ${lane.recommendedGreenSeconds}s",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Detection tripwire label
        Text(
            text = "--- LANE PASSING TRIPWIRE (TOTAL PASSED: ${result.passedVehiclesTotal}) ---",
            color = SignalYellow.copy(alpha = 0.85f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 220.dp)
        )
    }
}
