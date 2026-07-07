package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import java.util.*

@Composable
fun WelcomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when {
        hour in 5..11 -> when (language) {
            "es" -> "Buenos días."
            "tl" -> "Magandang umaga."
            else -> "Good morning."
        }
        hour in 12..17 -> when (language) {
            "es" -> "Buenas tardes."
            "tl" -> "Magandang hapon."
            else -> "Good afternoon."
        }
        else -> when (language) {
            "es" -> "Buenas noches."
            "tl" -> "Magandang gabi."
            else -> "Good evening."
        }
    }

    val heartQuestion = when (language) {
        "es" -> "¿Qué hay en tu corazón hoy?"
        "tl" -> "Ano ang laman ng iyong puso ngayon?"
        else -> "What is on your heart right now?"
    }

    // Sage green theme color
    val sageBgColor = Color(0xFF8CA693)
    val creamTextColor = Color(0xFFFBF9F4)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sageBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Language Toggle Button (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val nextLang = when (language) {
                        "en" -> "es"
                        "es" -> "tl"
                        else -> "en"
                    }
                    viewModel.setLanguage(nextLang)
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("welcome_language_toggle")
        ) {
            Text(
                text = "[Language: ${language.uppercase(Locale.ROOT)}]",
                color = creamTextColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        // 2. Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(110.dp))

            // Dynamic Time-based Greeting
            Text(
                text = timeGreeting,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                ),
                color = creamTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // "What is on your heart right now?"
            Text(
                text = heartQuestion,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 36.sp
                ),
                color = creamTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pill-shaped mood choices
            // Row 1: Anxious / Overwhelmed
            MoodPillItem(
                label = when (language) {
                    "es" -> "Ansioso / Agobiado"
                    "tl" -> "Mabalisa / Punô"
                    else -> "Anxious / Overwhelmed"
                },
                isSelected = selectedMood == "Anxious",
                onClick = {
                    viewModel.setMood("Anxious")
                    viewModel.navigateTo(Screen.Dashboard)
                },
                creamColor = creamTextColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Sad / Low Energy
            MoodPillItem(
                label = when (language) {
                    "es" -> "Triste / Desanimado"
                    "tl" -> "Malungkot / Pagod"
                    else -> "Sad / Low Energy"
                },
                isSelected = selectedMood == "Sad",
                onClick = {
                    viewModel.setMood("Sad")
                    viewModel.navigateTo(Screen.Dashboard)
                },
                creamColor = creamTextColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row 3: Lonely & Grateful side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MoodPillItem(
                        label = when (language) {
                            "es" -> "Solitario"
                            "tl" -> "Nag-iisa"
                            else -> "Lonely"
                        },
                        isSelected = selectedMood == "Lonely",
                        onClick = {
                            viewModel.setMood("Lonely")
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        creamColor = creamTextColor
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MoodPillItem(
                        label = when (language) {
                            "es" -> "Agradecido"
                            "tl" -> "Nagpapasalamat"
                            else -> "Grateful"
                        },
                        isSelected = selectedMood == "Grateful",
                        onClick = {
                            viewModel.setMood("Grateful")
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        creamColor = creamTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 4: Healing & Stressed side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MoodPillItem(
                        label = when (language) {
                            "es" -> "Sanación"
                            "tl" -> "Paggaling"
                            else -> "Healing"
                        },
                        isSelected = selectedMood == "Grateful", // Maps to Grateful for scripture/prayer peace!
                        onClick = {
                            viewModel.setMood("Grateful")
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        creamColor = creamTextColor
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MoodPillItem(
                        label = when (language) {
                            "es" -> "Estresado"
                            "tl" -> "Na-stress"
                            else -> "Stressed"
                        },
                        isSelected = selectedMood == "Struggling", // Maps to Struggling for comforting scripture!
                        onClick = {
                            viewModel.setMood("Struggling")
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        creamColor = creamTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Space for bottom anchored items
        }

        // 3. Anchored Bottom Components
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(sageBgColor)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Ad Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(creamTextColor.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = when (language) {
                        "es" -> "ANUNCIO: Aplicación de Asesoría Local (Banner)"
                        "tl" -> "AD: Lokal na Counseling App (Banner)"
                        else -> "AD: Local Counseling App (Banner)"
                    },
                    color = creamTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom-most static navigation visual helper
            Text(
                text = "[Home - Media - AI Chat]",
                color = creamTextColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun MoodPillItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    creamColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) creamColor.copy(alpha = 0.25f)
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = creamColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("mood_pill_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = creamColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
