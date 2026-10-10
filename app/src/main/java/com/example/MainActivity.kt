package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HelpGuideDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.TrafficViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("CV Dashboard", Icons.Default.Analytics, "nav_dashboard"),
    CCTV("Live Camera", Icons.Default.Videocam, "nav_cctv"),
    SIGNALS("Smart Lights", Icons.Default.Traffic, "nav_signals"),
    ENFORCEMENT("Speed Tickets", Icons.Default.ReceiptLong, "nav_enforcement"),
    TOUR("Easy Guide", Icons.Default.School, "nav_tour")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val trafficViewModel: TrafficViewModel = viewModel()
                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
                val showHelpDialog by trafficViewModel.showHelpDialog.collectAsState()

                // BackHandler returns to CV Dashboard if on any secondary tab
                BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
                    currentTab = NavigationTab.DASHBOARD
                }

                if (showHelpDialog) {
                    HelpGuideDialog(onDismiss = { trafficViewModel.toggleHelpDialog(false) })
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CityDarkBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        NavigationBar(
                            containerColor = CityDarkSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("main_navigation_bar")
                        ) {
                            NavigationTab.entries.forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 10.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyberCyan,
                                        selectedTextColor = CyberCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = CyberCyan.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> CvDashboardScreen(
                                viewModel = trafficViewModel,
                                onOpenGuide = { currentTab = NavigationTab.TOUR }
                            )
                            NavigationTab.CCTV -> CctvScreen(
                                viewModel = trafficViewModel,
                                onOpenTour = { currentTab = NavigationTab.TOUR }
                            )
                            NavigationTab.SIGNALS -> SignalsScreen(viewModel = trafficViewModel)
                            NavigationTab.ENFORCEMENT -> EnforcementScreen(viewModel = trafficViewModel)
                            NavigationTab.TOUR -> QuickTourScreen(
                                viewModel = trafficViewModel,
                                onNavigateToCamera = { currentTab = NavigationTab.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}
