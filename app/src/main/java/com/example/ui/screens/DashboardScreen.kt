package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoriteItem
import com.example.data.MediaItem
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.components.DashboardSkeleton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        isInitialLoading = false
    }

    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val downloadsList by viewModel.downloads.collectAsState()

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

    val dailyAffirmationText by viewModel.dailyAffirmationText.collectAsState()
    val isFetchingAffirmation by viewModel.isFetchingAffirmation.collectAsState()

    // Expand state for prayers to show full text when clicked
    var showMorningPrayerSheet by remember { mutableStateOf(false) }
    var showNightPrayerSheet by remember { mutableStateOf(false) }

    // Date format for May 15 style
    val formattedDate = remember {
        val sdf = SimpleDateFormat("MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..11 -> when (language) {
                "es" -> "Buenos días,"
                "tl" -> "Magandang umaga,"
                else -> "Good morning,"
            }
            hour in 12..17 -> when (language) {
                "es" -> "Buenas tardes,"
                "tl" -> "Magandang hapon,"
                else -> "Good afternoon,"
            }
            else -> when (language) {
                "es" -> "Buenas noches,"
                "tl" -> "Magandang gabi,"
                else -> "Good evening,"
            }
        }
    }

    if (isInitialLoading) {
        DashboardSkeleton(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFFBF9F4)) // Soft Cream/Beige Background matching Screen 2
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp) // Space for bottom components
        ) {
        // 1. Top Header: Greeting, Date & Bell Icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF5D665E)
                        )
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26332A)
                        )
                    )
                }

                // Notification Bell Icon with Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2EFE9))
                        .clickable { /* Notification settings click */ }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF3F5E4D)
                    )
                    // Notification badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE07A5F), CircleShape) // Warm orange-red badge
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        // 2. Personalized Mood Shield (Quick toggle or information pill)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFE9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_mood_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                        fontSize = 24.sp,
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
                            color = Color(0xFF3F5E4D),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (language) {
                                "es" -> "Enfocado en: ${when(selectedMood){"Anxious"->"Ansiedad" "Sad"->"Tristeza" "Lonely"->"Soledad" "Grateful"->"Gratitud" else->"Estrés"}}"
                                "tl" -> "Nakatuon sa: ${when(selectedMood){"Anxious"->"Kabalisa" "Sad"->"Kalungkutan" "Lonely"->"Pangungulila" "Grateful"->"Pasasalamat" else->"Stress"}}"
                                else -> "Focused on: $selectedMood"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26332A)
                        )
                    }
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.Welcome) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3F5E4D))
                    ) {
                        Text(
                            text = when (language) {
                                "es" -> "Cambiar"
                                "tl" -> "Baguhin"
                                else -> "Change"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2a. Daily Affirmation Widget (Stored locally, refreshes every 24 hours or manually)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F3)), // Gentle sunset peach
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF5E4D7),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("daily_affirmation_widget")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = "Affirmation Icon",
                                tint = Color(0xFFB86B42),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    "es" -> "Afirmación Diaria"
                                    "tl" -> "Araw-araw na Pagpapatibay"
                                    else -> "Daily Affirmation"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8A4620),
                                    fontSize = 16.sp
                                )
                            )
                        }

                        // Refresh button
                        IconButton(
                            onClick = { viewModel.fetchDailyAffirmation(forceRefresh = true) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Affirmation",
                                tint = Color(0xFFB86B42),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isFetchingAffirmation) {
                        Column {
                            ShimmerPlaceholder(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ShimmerPlaceholder(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    } else {
                        Text(
                            text = dailyAffirmationText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF4A3525),
                                fontStyle = FontStyle.Italic
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2b. Daily Scripture (The Bible Project Theme Reflections)
        item {
            val dailyScriptureText by viewModel.dailyScriptureText.collectAsState()
            val dailyScriptureReference by viewModel.dailyScriptureReference.collectAsState()
            val dailyScriptureExplanation by viewModel.dailyScriptureExplanation.collectAsState()
            val isFetchingScripture by viewModel.isFetchingScripture.collectAsState()
            val selectedTheme by viewModel.selectedScriptureTheme.collectAsState()
            var themeExpanded by remember { mutableStateOf(false) }

            val themes = listOf("Covenant", "Tree of Life", "Grace & Peace", "Wisdom Literature", "Justice")

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0EC)), // Gentle sage
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFC8DBD0),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("daily_scripture_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THE BIBLE PROJECT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E4E3F),
                                    letterSpacing = 1.5.sp
                                )
                            )
                            Text(
                                text = when (language) {
                                    "es" -> "Escritura Diaria"
                                    "tl" -> "Araw-araw na Kasulatan"
                                    else -> "Daily Scripture Reflection"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3529)
                                )
                            )
                        }

                        Box {
                            Button(
                                onClick = { themeExpanded = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3F5E4D),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("scripture_theme_selector")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedTheme, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = themeExpanded,
                                onDismissRequest = { themeExpanded = false }
                            ) {
                                themes.forEach { themeName ->
                                    DropdownMenuItem(
                                        text = { Text(themeName, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.setScriptureTheme(themeName)
                                            themeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isFetchingScripture) {
                        Column {
                            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(16.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            ShimmerPlaceholder(modifier = Modifier.width(90.dp).height(12.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFC8DBD0)))
                            Spacer(modifier = Modifier.height(16.dp))
                            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(12.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.95f).height(12.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        Column {
                            AnimatedContent(
                                targetState = Triple(dailyScriptureText, dailyScriptureReference, dailyScriptureExplanation),
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                                },
                                label = "daily_scripture_transition"
                            ) { (scriptureText, scriptureRef, scriptureExpl) ->
                                Column {
                                    Text(
                                        text = "“$scriptureText”",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 22.sp
                                        ),
                                        color = Color(0xFF132A13)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "— $scriptureRef",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3F5E4D)
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFC8DBD0))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = scriptureExpl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = Color(0xFF2E4E3F)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.fetchDailyScripture(selectedTheme) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3F5E4D)),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.testTag("fetch_scripture_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (language) {
                                                "es" -> "Actualizar de IA"
                                                "tl" -> "I-refresh mula sa IA"
                                                else -> "Fetch Live from AI"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val isFav = favorites.any { it.text == dailyScriptureText && it.language == language }
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFavorite(
                                                dailyScriptureText,
                                                dailyScriptureReference,
                                                "verse"
                                            )
                                        },
                                        modifier = Modifier.size(36.dp).testTag("bookmark_scripture_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark Scripture",
                                            tint = Color(0xFF3F5E4D),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    val downloadsList by viewModel.downloads.collectAsState()
                                    val isDownloaded = downloadsList.any { it.resourceId == dailyScriptureReference }
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleDownload(
                                                id = dailyScriptureReference,
                                                title = "Daily Scripture: $selectedTheme",
                                                subtitle = dailyScriptureReference,
                                                type = "scripture",
                                                content = dailyScriptureText,
                                                duration = "Offline"
                                            )
                                        },
                                        modifier = Modifier.size(36.dp).testTag("download_scripture_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                            contentDescription = "Download Scripture for Offline",
                                            tint = if (isDownloaded) Color(0xFF3F5E4D) else Color(0xFF8C968E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Verse of the Day Card (Split Columns with Sunset Canvas)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE2DDD5),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("verse_of_the_day_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title Header + Bookmark
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
                            color = Color(0xFF3F5E4D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(
                                    dailyVerse.text,
                                    dailyVerse.reference,
                                    "verse"
                                )
                            },
                            modifier = Modifier.size(24.dp).testTag("bookmark_verse_button")
                        ) {
                            Icon(
                                imageVector = if (isVerseFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Verse",
                                tint = if (isVerseFavorited) Color(0xFF3F5E4D) else Color(0xFF8C968E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Column / Row Content with Custom Sunset Canvas on the right side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Text Area with Meditative Fade transition
                        AnimatedContent(
                            targetState = dailyVerse,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                            },
                            modifier = Modifier.weight(1.3f),
                            label = "daily_verse_animation"
                        ) { verse ->
                            Column {
                                Text(
                                    text = "“${verse.text}”",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 22.sp
                                    ),
                                    color = Color(0xFF26332A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "- ${verse.reference}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D7565)
                                    )
                                )
                            }
                        }

                        // Right Sunset Art Box
                        SunsetCanvas(
                            modifier = Modifier
                                .size(width = 90.dp, height = 75.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons Row: Play Video Verse & Listen to Audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val mediaItem = viewModel.bibleProjectMediaItems.firstOrNull { 
                                    it.type == "video" && it.language == language 
                                } ?: viewModel.bibleProjectMediaItems.first { it.type == "video" }
                                viewModel.selectMedia(mediaItem)
                                viewModel.navigateTo(Screen.MediaPlayer)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF26332A)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE2DDD5)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("play_video_verse_button"),
                            shape = RoundedCornerShape(20.dp),
                            elevation = null,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    "es" -> "Ver Video"
                                    "tl" -> "I-play ang Video"
                                    else -> "Play Video Verse"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val mediaItem = viewModel.bibleProjectMediaItems.firstOrNull { 
                                    it.type == "audio_prayer" && it.language == language 
                                } ?: viewModel.bibleProjectMediaItems.first { it.type == "audio_prayer" }
                                viewModel.selectMedia(mediaItem)
                                viewModel.navigateTo(Screen.MediaPlayer)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF26332A)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE2DDD5)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("listen_audio_verse_button"),
                            shape = RoundedCornerShape(20.dp),
                            elevation = null,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    "es" -> "Escuchar Audio"
                                    "tl" -> "Makinig sa Audio"
                                    else -> "Listen to Audio"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Morning Prayer Section (Custom Forest Road Canvas)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (language) {
                        "es" -> "Oración de la Mañana"
                        "tl" -> "Panalangin sa Umaga"
                        else -> "Morning Prayer"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF26332A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Forest Canvas clickable card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showMorningPrayerSheet = !showMorningPrayerSheet }
                ) {
                    ForestPathCanvas(modifier = Modifier.fillMaxSize())
                    
                    // Small "Click to view full prayer" indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (language) {
                                "es" -> "Ver Oración 📖"
                                "tl" -> "Tingnan ang Panalangin 📖"
                                else -> "View Prayer 📖"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expandable prayer content
                AnimatedVisibility(visible = showMorningPrayerSheet) {
                    Card(
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFE9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = morningPrayer.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F5E4D)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.toggleFavorite(morningPrayer.content, morningPrayer.title, "prayer")
                                    }
                                ) {
                                    val isFav = favorites.any { it.text == morningPrayer.content && it.language == language }
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark Morning Prayer",
                                        tint = if (isFav) Color(0xFF3F5E4D) else Color(0xFF8C968E)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = morningPrayer.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF26332A),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Evening Prayer Section (Custom Starry Night Canvas)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (language) {
                        "es" -> "Oración de la Tarde"
                        "tl" -> "Panalangin sa Gabi"
                        else -> "Evening Prayer"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF26332A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Night sky clickable card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showNightPrayerSheet = !showNightPrayerSheet }
                ) {
                    StarryNightCanvas(modifier = Modifier.fillMaxSize())
                    
                    // Small click indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (language) {
                                "es" -> "Ver Oración 📖"
                                "tl" -> "Tingnan ang Panalangin 📖"
                                else -> "View Prayer 📖"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expandable prayer content
                AnimatedVisibility(visible = showNightPrayerSheet) {
                    Card(
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFE9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nightPrayer.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F5E4D)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.toggleFavorite(nightPrayer.content, nightPrayer.title, "prayer")
                                    }
                                ) {
                                    val isFav = favorites.any { it.text == nightPrayer.content && it.language == language }
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark Night Prayer",
                                        tint = if (isFav) Color(0xFF3F5E4D) else Color(0xFF8C968E)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = nightPrayer.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF26332A),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Notification Schedules Reminders
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE2DDD5),
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
                        color = Color(0xFF3F5E4D)
                    )
                    Text(
                        text = when (language) {
                            "es" -> "Recibe pasajes pacíficos al despertar y antes de dormir de forma local."
                            "tl" -> "Makatanggap ng payapang talata pagkagising at bago matulog."
                            else -> "Receive peaceful scriptures locally upon waking and before sleep."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D665E)
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

        // 6b. Offline Sanctuary Library (Downloaded resources)
        item {
            Text(
                text = when (language) {
                    "es" -> "Santuario Fuera de Línea"
                    "tl" -> "Offline na Santuwaryo"
                    else -> "Offline Sanctuary Library"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26332A),
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (downloadsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color(0xFF8C968E).copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (language) {
                                "es" -> "No hay descargas aún"
                                "tl" -> "Wala pang downloaded na file"
                                else -> "No offline downloads yet"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D665E)
                        )
                        Text(
                            text = when (language) {
                                "es" -> "Descarga audios, videos o escrituras para usar sin internet."
                                "tl" -> "Mag-download ng bidyo o audio para sa offline."
                                else -> "Download scriptures, prayers, or audio tracks for offline use."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8C968E),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(downloadsList) { resource ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F0EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (resource.type) {
                                    "video" -> Icons.Default.PlayCircle
                                    "audio_worship", "audio_prayer" -> Icons.Default.MusicNote
                                    "scripture" -> Icons.Default.MenuBook
                                    else -> Icons.Default.SelfImprovement
                                },
                                contentDescription = null,
                                tint = Color(0xFF3F5E4D),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resource.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF26332A)
                            )
                            Text(
                                text = resource.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D665E)
                            )
                            if (resource.content.isNotEmpty() && resource.type == "scripture") {
                                Text(
                                    text = "“${resource.content}”",
                                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                    color = Color(0xFF3F5E4D),
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (resource.type == "video" || resource.type.startsWith("audio")) {
                                IconButton(
                                    onClick = {
                                        val mediaItem = MediaItem(
                                            id = resource.resourceId,
                                            title = resource.title,
                                            subtitle = resource.subtitle,
                                            type = resource.type,
                                            url = "",
                                            duration = resource.duration,
                                            language = resource.language,
                                            lyricOrScripture = resource.content
                                        )
                                        viewModel.selectMedia(mediaItem)
                                        viewModel.navigateTo(Screen.MediaPlayer)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play offline track",
                                        tint = Color(0xFF3F5E4D)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleDownload(
                                        resource.resourceId,
                                        resource.title,
                                        resource.subtitle,
                                        resource.type,
                                        resource.content,
                                        resource.duration
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete download",
                                    tint = Color(0xFFCC5E5E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Favorites / Bookmarks List
        item {
            Text(
                text = when (language) {
                    "es" -> "Mis Favoritos Guardados"
                    "tl" -> "Aking mga Paborito"
                    else -> "My Saved Sanctuary"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26332A)
            )
        }

        if (favorites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Color(0xFF8C968E).copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (language) {
                                "es" -> "Ningún elemento guardado aún."
                                "tl" -> "Wala pang naka-save na paborito."
                                else -> "No saved items yet."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D665E)
                        )
                        Text(
                            text = when (language) {
                                "es" -> "Toca el icono de marcador para guardar versos u oraciones."
                                "tl" -> "I-tap ang bookmark icon para mag-save."
                                else -> "Tap the bookmark icon to save comforting verses or prayers."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8C968E),
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
}

// Beautiful Sunset Canvas Component
@Composable
fun SunsetCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Sky sunset gradient
        val skyGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE07A5F), // Coral Red
                Color(0xFFF2CC8F), // Glowing Peach
                Color(0xFF3F5E4D)  // Peaceful Sage Water
            )
        )
        drawRect(brush = skyGradient)

        // Golden Sun
        drawCircle(
            color = Color(0xFFFDF0CD),
            radius = height * 0.3f,
            center = Offset(width * 0.55f, height * 0.65f)
        )

        // Wave lines
        val waveColor = Color.White.copy(alpha = 0.35f)
        drawLine(
            color = waveColor,
            start = Offset(0f, height * 0.7f),
            end = Offset(width, height * 0.7f),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = waveColor,
            start = Offset(width * 0.15f, height * 0.8f),
            end = Offset(width * 0.85f, height * 0.8f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = waveColor,
            start = Offset(width * 0.3f, height * 0.9f),
            end = Offset(width * 0.7f, height * 0.9f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// Beautiful Forest Path Canvas
@Composable
fun ForestPathCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Trees background gradient
        val forestGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4F772D), // Peaceful Forest Top
                Color(0xFF31572C), // Deep Pine
                Color(0xFF132A13)  // Dark Ground
            )
        )
        drawRect(brush = forestGradient)

        // Path Perspective
        val path = Path().apply {
            moveTo(width * 0.45f, height * 0.35f)
            lineTo(width * 0.55f, height * 0.35f)
            lineTo(width * 0.8f, height)
            lineTo(width * 0.2f, height)
            close()
        }
        drawPath(
            path = path,
            color = Color(0xFF90A955).copy(alpha = 0.35f)
        )

        // Translucent Sunbeams
        val rayColor = Color.White.copy(alpha = 0.12f)
        val ray1 = Path().apply {
            moveTo(width * 0.65f, 0f)
            lineTo(width * 0.8f, 0f)
            lineTo(width * 0.45f, height)
            lineTo(width * 0.3f, height)
            close()
        }
        drawPath(path = ray1, color = rayColor)

        val ray2 = Path().apply {
            moveTo(width * 0.35f, 0f)
            lineTo(width * 0.45f, 0f)
            lineTo(width * 0.15f, height)
            lineTo(width * 0.05f, height)
            close()
        }
        drawPath(path = ray2, color = rayColor)
    }
}

// Starry Night Canvas Component
@Composable
fun StarryNightCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Dark deep sky
        val nightGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Dark Midnight
                Color(0xFF1E293B), // Deep Blue Slate
                Color(0xFF3F5E4D).copy(alpha = 0.4f) // Merging with peace green ground
            )
        )
        drawRect(brush = nightGradient)

        // Crescent moon
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = height * 0.16f,
            center = Offset(width * 0.78f, height * 0.32f)
        )
        drawCircle(
            color = Color(0xFF0F172A), // Matches sky background to create crescent
            radius = height * 0.16f,
            center = Offset(width * 0.74f, height * 0.28f)
        )

        // Glowing white/gold stars
        val stars = listOf(
            Offset(width * 0.12f, height * 0.22f),
            Offset(width * 0.28f, height * 0.18f),
            Offset(width * 0.44f, height * 0.32f),
            Offset(width * 0.58f, height * 0.14f),
            Offset(width * 0.2f, height * 0.6f),
            Offset(width * 0.38f, height * 0.5f),
            Offset(width * 0.88f, height * 0.55f),
            Offset(width * 0.62f, height * 0.7f)
        )
        for (star in stars) {
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 2.dp.toPx(),
                center = star
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
            .background(Color(0xFFF2EFE9))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3F5E4D),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF5D665E)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26332A)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(14.dp))
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
                        tint = Color(0xFF3F5E4D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.referenceOrTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F5E4D)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF26332A),
                    lineHeight = 18.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Bookmark",
                    tint = Color(0xFFCC5E5E),
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
    var initialHour = 7
    var initialMinute = 0

    try {
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
            val amPm = if (hourOfDay < 12) AM_PM_LABEL(false) else AM_PM_LABEL(true)
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

private fun AM_PM_LABEL(isPm: Boolean): String {
    return if (isPm) "PM" else "AM"
}
