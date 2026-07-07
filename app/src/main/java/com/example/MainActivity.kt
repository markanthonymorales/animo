package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SimulatedAdBanner
import com.example.ui.components.SOSCrisisDialog
import com.example.ui.components.SOSGeneralDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MediaPlayerScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.CounselScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SOSColor

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val language by viewModel.language.collectAsState()
                val showCrisisModal by viewModel.showSOSCrisisModal.collectAsState()
                val showSOSGeneralModal by viewModel.showSOSGeneralModal.collectAsState()

                // Crisis overlay (Non-dismissible)
                if (showCrisisModal) {
                    SOSCrisisDialog(
                        language = language,
                        onDismiss = { viewModel.dismissCrisisModal() }
                    )
                }

                // General SOS Calm Breathing Overlay
                if (showSOSGeneralModal) {
                    SOSGeneralDialog(
                        language = language,
                        onDismiss = { viewModel.toggleSOSGeneral(false) }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Display TopAppBar only when we are NOT on the Welcome screen to keep Welcome screen completely pure and deep
                        if (currentScreen != Screen.Welcome) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "Animo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                actions = {
                                    // SOS Calm Button
                                    Button(
                                        onClick = { viewModel.toggleSOSGeneral(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SOSColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .height(36.dp)
                                            .testTag("sos_calm_appbar_button")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = when (language) {
                                                    "es" -> "SOS"
                                                    "tl" -> "SOS"
                                                    else -> "SOS Calm"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    },
                    bottomBar = {
                        // Display Bottom Bar + Static non-intrusive Ad Banner when entered
                        if (currentScreen != Screen.Welcome) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .navigationBarsPadding()
                            ) {
                                // Static, padded Non-Intrusive Ad Banner anchored exactly above Bottom Menu!
                                SimulatedAdBanner(language = language)

                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Dashboard,
                                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen == Screen.Dashboard) Icons.Default.Home else Icons.Default.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (language) {
                                                    "es" -> "Inicio"
                                                    "tl" -> "Tahanan"
                                                    else -> "Home"
                                                }
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.testTag("nav_home")
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.MediaPlayer,
                                        onClick = { viewModel.navigateTo(Screen.MediaPlayer) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen == Screen.MediaPlayer) Icons.Default.Headset else Icons.Default.Headset,
                                                contentDescription = "Sanctuary"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (language) {
                                                    "es" -> "Santuario"
                                                    "tl" -> "Santuwaryo"
                                                    else -> "Sanctuary"
                                                }
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.testTag("nav_media")
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Counsel,
                                        onClick = { viewModel.navigateTo(Screen.Counsel) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen == Screen.Counsel) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Counsel"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = when (language) {
                                                    "es" -> "Consejo"
                                                    "tl" -> "Gabay"
                                                    else -> "Counsel"
                                                }
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.testTag("nav_counsel")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(
                                top = if (currentScreen == Screen.Welcome) 0.dp else innerPadding.calculateTopPadding(),
                                bottom = if (currentScreen == Screen.Welcome) 0.dp else innerPadding.calculateBottomPadding()
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                            },
                            label = "screen_navigation_animation"
                        ) { targetScreen ->
                            when (targetScreen) {
                                is Screen.Welcome -> WelcomeScreen(viewModel = viewModel)
                                is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                                is Screen.MediaPlayer -> MediaPlayerScreen(viewModel = viewModel)
                                is Screen.Counsel -> CounselScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
