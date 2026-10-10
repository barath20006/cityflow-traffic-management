package com.example.ui.components

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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun HelpGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CityDarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .border(1.5.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("help_guide_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help Guide",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "How CityFlow Works",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quick User-Friendly Guide",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Divider(color = CityDarkOutline)

                // Feature 1: Live Traffic Camera
                GuideFeatureItem(
                    icon = Icons.Default.Videocam,
                    iconTint = CyberCyan,
                    title = "1. Live Traffic Camera",
                    description = "Watch real-time vehicles moving across the intersection.",
                    actionTip = "💡 Tip: Tap any moving car, bike, or bus on the screen to view its live speedometer and details!"
                )

                // Feature 2: Smart Traffic Signals
                GuideFeatureItem(
                    icon = Icons.Default.Traffic,
                    iconTint = SignalGreen,
                    title = "2. Smart Adaptive Signals",
                    description = "Traffic lights adapt automatically. When many vehicles or transit buses are waiting, the AI extends green lights (+6s to +12s) so queues clear faster.",
                    actionTip = "💡 Tip: Go to the 'Smart Lights' tab to see countdown timers and lane density meters."
                )

                // Feature 3: Emergency Ambulance Priority
                GuideFeatureItem(
                    icon = Icons.Default.Emergency,
                    iconTint = SignalRed,
                    title = "3. Emergency Green Corridor",
                    description = "Tap '🚨 Send Ambulance' to dispatch an emergency vehicle. All conflicting lights turn RED instantly to give the ambulance a clear green path!",
                    actionTip = "💡 Tip: Try the red 'Send Ambulance' button at the top of the screen."
                )

                // Feature 4: AI Speed Radar & Tickets
                GuideFeatureItem(
                    icon = Icons.Default.Speed,
                    iconTint = SignalYellow,
                    title = "4. Automated Speed Tickets",
                    description = "The AI radar scans vehicle speeds. Vehicles exceeding the speed limit get highlighted in RED and a digital ticket (E-Challan) is automatically issued.",
                    actionTip = "💡 Tip: Move the speed slider down to catch more speeders in action!"
                )

                // Feature 5: Spoken Voice Announcements
                GuideFeatureItem(
                    icon = Icons.Default.Campaign,
                    iconTint = EmergencyPurple,
                    title = "5. Loudspeaker Voice Alerts",
                    description = "Real spoken voice alerts announce speed violations and emergency warnings out loud on your device speakers.",
                    actionTip = "💡 Tip: Open the 'Voice Alerts' tab to type any message and hear the phone speak it!"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Got It Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_guide_btn")
                ) {
                    Text(
                        text = "Got It! Let's Explore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideFeatureItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    actionTip: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CityDarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionTip,
                    color = CyberCyan,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
