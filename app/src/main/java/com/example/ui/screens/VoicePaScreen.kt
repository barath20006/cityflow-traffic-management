package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.VoiceLog
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoicePaScreen(
    viewModel: TrafficViewModel,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val lastAnnounced by viewModel.voiceManager.lastAnnouncedText.collectAsState()
    val logs by viewModel.voiceLogs.collectAsState()
    val isAutoVoice by viewModel.isAutoVoiceEnabled.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    var customMessage by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityDarkBg)
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
                    text = "LOUDSPEAKER VOICE BROADCAST",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Traffic Public Address",
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

        // 2. Active Audio Visualizer & Last Message Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isSpeaking) CyberCyan else CityDarkOutline, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isSpeaking) CyberCyan.copy(alpha = 0.2f) else CityDarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.Campaign,
                        contentDescription = "Speaker",
                        tint = if (isSpeaking) CyberCyan else TextSecondary,
                        modifier = Modifier.size(if (isSpeaking) (28 * pulseScale).dp else 26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSpeaking) "LOUDSPEAKER BROADCASTING NOW 🔊" else "LAST SPOKEN ANNOUNCEMENT",
                        color = if (isSpeaking) SignalGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "\"$lastAnnounced\"",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 3. Custom Broadcast Input
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CityDarkOutline, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Type any custom announcement to speak:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    placeholder = { Text("e.g., Heavy rain on MG Road. Please maintain safe distance.", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_broadcast_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CityDarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )

                Button(
                    onClick = {
                        viewModel.broadcastAnnouncement(customMessage, "CUSTOM")
                        customMessage = ""
                    },
                    enabled = customMessage.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("broadcast_submit_btn")
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "📢 Speak This Announcement Out Loud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Quick Smart City Soundboard Presets
        Text(
            text = "ONE-TAP VOICE PRESETS (TAP TO SPEAK)",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetTile(
                title = "🚨 Emergency",
                desc = "Clear path for ambulance",
                onClick = {
                    viewModel.broadcastAnnouncement(
                        "Attention all vehicles: Emergency vehicle approaching. Please clear the center lane immediately.",
                        "EMERGENCY"
                    )
                },
                modifier = Modifier.weight(1f)
            )
            PresetTile(
                title = "⚠️ Speed Limit",
                desc = "Slow down to 50 km/h",
                onClick = {
                    viewModel.broadcastAnnouncement(
                        "Speed radar active. All vehicles please observe the legal fifty kilometer per hour speed limit.",
                        "SPEED"
                    )
                },
                modifier = Modifier.weight(1f)
            )
            PresetTile(
                title = "🚌 Bus Priority",
                desc = "Green light extended",
                onClick = {
                    viewModel.broadcastAnnouncement(
                        "Green light extended for transit buses. Thank you for your patience.",
                        "SIGNAL"
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        // 5. User-Friendly Switches
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-Speak Speed Violations",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Phone speaks automatically when a vehicle speeds",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isAutoVoice,
                        onCheckedChange = { viewModel.toggleAutoVoice() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("auto_voice_switch")
                    )
                }

                Divider(color = CityDarkOutline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mute Loudspeaker",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isMuted) "Audio is muted" else "Audio is playing through speakers",
                            color = if (isMuted) SignalRed else SignalGreen,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isMuted,
                        onCheckedChange = { viewModel.toggleMute() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SignalRed,
                            checkedTrackColor = SignalRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("mute_voice_switch")
                    )
                }
            }
        }

        Text(
            text = "RECENT AUDIO LOGS",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // History list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                VoiceLogItem(log = log, onReplay = { viewModel.broadcastAnnouncement(log.message, log.category) })
            }
        }
    }
}

@Composable
private fun PresetTile(
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.dp, CityDarkOutline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun VoiceLogItem(
    log: VoiceLog,
    onReplay: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CityDarkOutline, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = dateStr,
                color = CyberCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = log.message,
                color = TextPrimary,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            IconButton(onClick = onReplay, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Replay",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
