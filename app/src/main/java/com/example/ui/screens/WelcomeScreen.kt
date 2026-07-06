package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    // Determine greeting based on local time
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

    // List of emotional states
    val emotionalStates = listOf(
        MoodTag("Anxious", when (language) {
            "es" -> "Ansioso / Agobiado"
            "tl" -> "Mabalisa / Punô"
            else -> "Anxious / Overwhelmed"
        }, "🌪️"),
        MoodTag("Sad", when (language) {
            "es" -> "Triste / Desanimado"
            "tl" -> "Malungkot / Pagod"
            else -> "Sad / Low Energy"
        }, "🌧️"),
        MoodTag("Lonely", when (language) {
            "es" -> "Solitario"
            "tl" -> "Nag-iisa"
            else -> "Lonely"
        }, "🍃"),
        MoodTag("Grateful", when (language) {
            "es" -> "Agradecido"
            "tl" -> "Nagpapasalamat"
            else -> "Grateful"
        }, "☀️"),
        MoodTag("Struggling", when (language) {
            "es" -> "Estresado / Luchando"
            "tl" -> "Nahihirapan / Stressed"
            else -> "Struggling / Stressed"
        }, "⚓")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative peaceful glowing circle in background
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🕊️",
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Large high-contrast but gentle typography greeting
        Text(
            text = timeGreeting,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = heartQuestion,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Grid selection for moods
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .testTag("mood_grid")
        ) {
            items(emotionalStates) { mood ->
                val isSelected = selectedMood == mood.id
                val borderAlpha = if (isSelected) 1f else 0.15f
                val bgAlpha = if (isSelected) 0.15f else 0.04f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            viewModel.setMood(mood.id)
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .testTag("mood_tag_${mood.id.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mood.emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        RadioButton(
                            selected = true,
                            onClick = { viewModel.setMood(mood.id) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Dynamic entry button with responsive layout
        Button(
            onClick = { viewModel.navigateTo(Screen.Dashboard) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("enter_sanctuary_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(27.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (language) {
                        "es" -> "Entrar al Santuario"
                        "tl" -> "Pumasok sa Santuwaryo"
                        else -> "Enter Sanctuary"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class MoodTag(
    val id: String,
    val label: String,
    val emoji: String
)
