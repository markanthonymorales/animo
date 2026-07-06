package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoriteItem
import com.example.data.MediaItem
import com.example.data.SanctuaryData
import com.example.ui.MainViewModel
import com.example.ui.Screen
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val morningTime by viewModel.morningTime.collectAsState()
    val nightTime by viewModel.nightTime.collectAsState()

    // Daily Verse based on selected mood
    val dailyVerse = viewModel.getDynamicDailyVerse()

    // Dynamic Prayers
    val morningPrayer = viewModel.getDynamicPrayer(isMorning = true)
    val nightPrayer = viewModel.getDynamicPrayer(isMorning = false)

    // Check time of day to highlight morning vs night prayer
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isMorningTime = currentHour in 6..12
    val isNightTime = currentHour >= 20 || currentHour < 4

    // Check if daily verse is favorited
    var isVerseFavorited by remember { mutableStateOf(false) }
    LaunchedEffect(dailyVerse, favorites) {
        isVerseFavorited = favorites.any { it.text == dailyVerse.text && it.language == language }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for ad banner
    ) {
        // Dynamic Mood Header Indicator
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_mood_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedMood) {
                            "Anxious" -> "🌪️"
                            "Sad" -> "🌧️"
                            "Lonely" -> "🍃"
                            "Grateful" -> "☀️"
                            else -> "⚓"
                        },
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                "es" -> "Santuario Personalizado"
                                "tl" -> "Personaladong Santuwaryo"
                                else -> "Personalized Sanctuary"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (language) {
                                "es" -> "Enfocado en: ${when(selectedMood){"Anxious"->"Ansiedad" "Sad"->"Tristeza" "Lonely"->"Soledad" "Grateful"->"Gratitud" else->"Estrés"}}"
                                "tl" -> "Nakatuon sa: ${when(selectedMood){"Anxious"->"Kabalisa" "Sad"->"Kalungkutan" "Lonely"->"Pangungulila" "Grateful"->"Pasasalamat" else->"Stress"}}"
                                else -> "Focused on: $selectedMood"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.Welcome) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = when (language) {
                                "es" -> "Cambiar"
                                "tl" -> "Baguhin"
                                else -> "Change"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Verse of the Day Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("verse_of_the_day_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (language) {
                                "es" -> "Verso del Día"
                                "tl" -> "Talata sa Araw na Ito"
                                else -> "Verse of the Day"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(
                                    dailyVerse.text,
                                    dailyVerse.reference,
                                    "verse"
                                )
                            },
                            modifier = Modifier.testTag("bookmark_verse_button")
                        ) {
                            Icon(
                                imageVector = if (isVerseFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Verse",
                                tint = if (isVerseFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "“${dailyVerse.text}”",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dailyVerse.reference,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // Find associated video item and play it
                                val mediaItem = SanctuaryData.mediaItems.firstOrNull { 
                                    it.type == "video" && it.language == language 
                                } ?: SanctuaryData.mediaItems.first { it.type == "video" }
                                viewModel.selectMedia(mediaItem)
                                viewModel.navigateTo(Screen.MediaPlayer)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("play_video_verse_button"),
                            elevation = null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (language) {
                                        "es" -> "Ver Video"
                                        "tl" -> "I-play ang Video"
                                        else -> "Play Video Verse"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                // Find associated audio prayer item and play it
                                val mediaItem = SanctuaryData.mediaItems.firstOrNull { 
                                    it.type == "audio_prayer" && it.language == language 
                                } ?: SanctuaryData.mediaItems.first { it.type == "audio_prayer" }
                                viewModel.selectMedia(mediaItem)
                                viewModel.navigateTo(Screen.MediaPlayer)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("listen_audio_verse_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (language) {
                                        "es" -> "Escuchar Audio"
                                        "tl" -> "Makinig sa Audio"
                                        else -> "Listen to Audio"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Morning & Night Prayer Modules
        item {
            Text(
                text = when (language) {
                    "es" -> "Oraciones Diarias"
                    "tl" -> "Panalangin sa Araw-araw"
                    else -> "Daily Prayers"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Morning Prayer Card
                PrayerCard(
                    prayer = morningPrayer,
                    isHighlighted = isMorningTime || (!isMorningTime && !isNightTime), // default highlight in morning
                    language = language,
                    isFavorited = favorites.any { it.text == morningPrayer.content && it.language == language },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(morningPrayer.content, morningPrayer.title, "prayer")
                    },
                    modifier = Modifier.testTag("morning_prayer_card")
                )

                // Night Prayer Card
                PrayerCard(
                    prayer = nightPrayer,
                    isHighlighted = isNightTime,
                    language = language,
                    isFavorited = favorites.any { it.text == nightPrayer.content && it.language == language },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(nightPrayer.content, nightPrayer.title, "prayer")
                    },
                    modifier = Modifier.testTag("night_prayer_card")
                )
            }
        }

        // Notification Schedules
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("notification_settings_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = when (language) {
                            "es" -> "Recordatorios del Santuario"
                            "tl" -> "Mga Paalala sa Santuwaryo"
                            else -> "Sanctuary Reminders"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (language) {
                            "es" -> "Recibe pasajes pacíficos al despertar y antes de dormir de forma local."
                            "tl" -> "Makatanggap ng payapang talata pagkagising at bago matulog."
                            else -> "Receive peaceful scriptures locally upon waking and before sleep."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotificationTimeSelector(
                            label = when (language) {
                                "es" -> "Mañana"
                                "tl" -> "Umaga"
                                else -> "Morning"
                            },
                            time = morningTime,
                            icon = Icons.Default.WbSunny,
                            onClick = {
                                showTimePicker(context, morningTime) { selectedTime ->
                                    viewModel.saveMorningTime(selectedTime)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        NotificationTimeSelector(
                            label = when (language) {
                                "es" -> "Noche"
                                "tl" -> "Gabi"
                                else -> "Night"
                            },
                            time = nightTime,
                            icon = Icons.Default.NightsStay,
                            onClick = {
                                showTimePicker(context, nightTime) { selectedTime ->
                                    viewModel.saveNightTime(selectedTime)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Favorites / Bookmarks List
        item {
            Text(
                text = when (language) {
                    "es" -> "Mis Favoritos Guardados"
                    "tl" -> "Aking mga Paborito"
                    else -> "My Saved Sanctuary"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (favorites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (language) {
                                "es" -> "Ningún elemento guardado aún."
                                "tl" -> "Wala pang naka-save na paborito."
                                else -> "No saved items yet."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = when (language) {
                                "es" -> "Toca el icono de marcador para guardar versos u oraciones."
                                "tl" -> "I-tap ang bookmark icon para mag-save."
                                else -> "Tap the bookmark icon to save comforting verses or prayers."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(favorites) { favorite ->
                FavoriteRow(
                    item = favorite,
                    onDelete = {
                        viewModel.toggleFavorite(favorite.text, favorite.referenceOrTitle, favorite.type)
                    }
                )
            }
        }
    }
}

@Composable
fun PrayerCard(
    prayer: com.example.data.Prayer,
    isHighlighted: Boolean,
    language: String,
    isFavorited: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isHighlighted) 1.5.dp else 1.dp,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (prayer.isMorning) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prayer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Prayer",
                        tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = prayer.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun NotificationTimeSelector(
    label: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FavoriteRow(
    item: FavoriteItem,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.type == "verse") Icons.Default.MenuBook else Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.referenceOrTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Bookmark",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Shows actual Android TimePickerDialog for scheduling notifications locally
private fun showTimePicker(
    context: android.content.Context,
    currentTimeString: String,
    onTimeSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    var initialHour = 7
    var initialMinute = 0

    try {
        // Parse current time string, e.g., "07:30 AM"
        val parts = currentTimeString.split(" ")
        val timeParts = parts[0].split(":")
        var h = timeParts[0].toInt()
        val m = timeParts[1].toInt()
        val isPm = parts.getOrNull(1)?.lowercase() == "pm"
        if (isPm && h < 12) h += 12
        if (!isPm && h == 12) h = 0
        initialHour = h
        initialMinute = m
    } catch (e: Exception) {
        // Fallback
    }

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val displayHour = when {
                hourOfDay == 0 -> 12
                hourOfDay > 12 -> hourOfDay - 12
                else -> hourOfDay
            }
            val formattedTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
            onTimeSelected(formattedTime)
        },
        initialHour,
        initialMinute,
        false // 12-hour format
    ).show()
}
