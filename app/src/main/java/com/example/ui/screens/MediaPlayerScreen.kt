package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItem
import com.example.ui.MainViewModel
import com.example.ui.Screen
import kotlinx.coroutines.delay

@Composable
fun MediaPlayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val currentItemState by viewModel.currentMediaItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playProgress.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isCurrentItemFavorited by viewModel.isCurrentItemFavorited.collectAsState()

    // Filter media items based on current active language
    val filteredMediaList = remember(language) {
        viewModel.bibleProjectMediaItems.filter { it.language == language }
    }

    // Ensure we have a default selected media item
    val currentItem = currentItemState ?: filteredMediaList.firstOrNull() ?: viewModel.bibleProjectMediaItems.first()

    // Simulated progress tick when media is playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(500)
                if (progress < 1f) {
                    viewModel.updateProgress(progress + 0.01f)
                } else {
                    viewModel.updateProgress(0f)
                }
            }
        }
    }

    // Rotating vinyl animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing aura pulse animation
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    // Keep bookmark state synced for active current item
    LaunchedEffect(currentItem, favorites, language) {
        viewModel.checkCurrentItemFavorited(currentItem.lyricOrScripture)
    }

    // Function to skip forward/backward
    val playNextTrack = {
        val currentIndex = filteredMediaList.indexOfFirst { it.id == currentItem.id }
        if (currentIndex != -1 && currentIndex < filteredMediaList.size - 1) {
            viewModel.selectMedia(filteredMediaList[currentIndex + 1])
        } else if (filteredMediaList.isNotEmpty()) {
            viewModel.selectMedia(filteredMediaList.first())
        }
        viewModel.updateProgress(0f)
    }

    val playPreviousTrack = {
        val currentIndex = filteredMediaList.indexOfFirst { it.id == currentItem.id }
        if (currentIndex > 0) {
            viewModel.selectMedia(filteredMediaList[currentIndex - 1])
        } else if (filteredMediaList.isNotEmpty()) {
            viewModel.selectMedia(filteredMediaList.last())
        }
        viewModel.updateProgress(0f)
    }

    // Track player presentation mode (Native vs React web component)
    var isReactPlayerMode by remember { mutableStateOf(false) }

    // Main layout container (Deep Dark Slate Blue Background)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121C24)) // Dark Meditative Blue
    ) {
        if (isReactPlayerMode) {
            com.example.ui.components.ReactPlayerWebView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Float button to switch back to native player view
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 110.dp, end = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF3F5E4D))
                    .clickable { isReactPlayerMode = false }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("switch_to_native_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Switch to Native",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Native Mode",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // 1. Top Custom Navigation Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Dashboard) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (language) {
                                    "es" -> "Reproductor"
                                    "tl" -> "Tugtugan"
                                    else -> "Media Player"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            )
                            
                            // Switcher row
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Language indicator pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF3F5E4D))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (language) {
                                            "es" -> "ES"
                                            "tl" -> "TL"
                                            else -> "EN"
                                        },
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // React player selector pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable { isReactPlayerMode = true }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("switch_to_react_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "REACT PLAYER",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { /* Share functionality placeholder */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }

            // 2. Active Music Controller Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2B35)), // Translucent Navy
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .testTag("active_player_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title header inside player
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3F5E4D))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentItem.type.uppercase().replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(currentItem.lyricOrScripture, currentItem.title, "verse")
                                },
                                modifier = Modifier.testTag("bookmark_active_item_button")
                            ) {
                                Icon(
                                    imageVector = if (isCurrentItemFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark Lyric",
                                    tint = if (isCurrentItemFavorited) Color(0xFF81C784) else Color.White.copy(alpha = 0.6f)
                                )
                            }

                            val downloadsList by viewModel.downloads.collectAsState()
                            val isDownloaded = downloadsList.any { it.resourceId == currentItem.id }

                            IconButton(
                                onClick = {
                                    viewModel.toggleDownload(
                                        id = currentItem.id,
                                        title = currentItem.title,
                                        subtitle = currentItem.subtitle,
                                        type = currentItem.type,
                                        content = currentItem.lyricOrScripture,
                                        duration = currentItem.duration
                                    )
                                },
                                modifier = Modifier.testTag("download_media_button")
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                    contentDescription = "Download media offline",
                                    tint = if (isDownloaded) Color(0xFF81C784) else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Breathing Aura Disk or Widescreen Video Player Viewport
                        if (currentItem.type == "video") {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2B35)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.77f) // 16:9 widescreen
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .testTag("video_player_viewport")
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SunsetThumbnailCanvas(modifier = Modifier.fillMaxSize())

                                    // Watermark Logo
                                    Text(
                                        text = "The Bible Project HD",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )

                                    // Real-time advancing subtitles / captions
                                    val subtitleText = remember(progress) {
                                        val percent = progress
                                        when {
                                            percent < 0.2f -> when (language) {
                                                "es" -> "El Pacto es el compromiso fiel de Dios..."
                                                "tl" -> "Ang Tipan ay ang tapat na pangako ng Diyos..."
                                                else -> "A Covenant is God's sacred promise to be with us..."
                                            }
                                            percent < 0.4f -> when (language) {
                                                "es" -> "De principio a fin, la historia bíblica trata de restauración."
                                                "tl" -> "Mula sa simula, ang kwento ay tungkol sa pagpapanumbalik."
                                                else -> "From beginning to end, the biblical story is about restoring union."
                                            }
                                            percent < 0.6f -> when (language) {
                                                "es" -> "Jesús es la máxima expresión de la verdad divina."
                                                "tl" -> "Si Hesus ang pinakadakilang katuparan ng tapat na pag-ibig."
                                                else -> "Jesus acts as the ultimate Covenant-keeper and source of life."
                                            }
                                            percent < 0.8f -> when (language) {
                                                "es" -> "Inhala y exhala Su gracia tranquila en este momento."
                                                "tl" -> "Huminga nang malalim at tanggapin ang Kanyang biyaya."
                                                else -> "Take a breath, and meditate on this beautiful path of peace."
                                            }
                                            else -> when (language) {
                                                "es" -> "Descansa en la promesa eterna de Su presencia."
                                                "tl" -> "Mamahinga sa walang-hanggang kapayapaan ng Diyos."
                                                else -> "Rest secure in the absolute certainty of His eternal love."
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 10.dp)
                                            .padding(horizontal = 14.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = subtitleText,
                                            color = Color(0xFFFEF08A),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    // Overlay Playback Indicator
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clickable { viewModel.togglePlay() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Toggle Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    if (isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .align(Alignment.TopCenter)
                                                .offset(y = (75 * auraScale).dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(150.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF3F5E4D).copy(alpha = 0.3f * auraScale),
                                                    Color.Transparent
                                                )
                                            ),
                                            CircleShape
                                        )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .rotate(if (isPlaying) rotationAngle else 0f)
                                        .background(Color(0xFF121C24), CircleShape)
                                        .border(2.dp, Color(0xFF3F5E4D), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(Color(0xFF1E2B35), CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Titles
                        Text(
                            text = currentItem.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentItem.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable lyric lyric display
                        Text(
                            text = "“${currentItem.lyricOrScripture}”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 20.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Seekbar slider
                        Slider(
                            value = progress,
                            onValueChange = { viewModel.updateProgress(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3F5E4D),
                                activeTrackColor = Color(0xFF3F5E4D),
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Timers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val totalSecs = 180 // Default to 3 min
                            val currentSecs = (progress * totalSecs).toInt()
                            val min = currentSecs / 60
                            val sec = currentSecs % 60
                            Text(
                                text = String.format("%02d:%02d", min, sec),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = currentItem.duration,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Player controls (Skip backward, Play/Pause, Skip forward)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = playPreviousTrack,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            IconButton(
                                onClick = { viewModel.togglePlay() },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFF3F5E4D), CircleShape)
                                    .testTag("play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play / Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            IconButton(
                                onClick = playNextTrack,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Worship Song & Explore Section Title
            item {
                Text(
                    text = when (language) {
                        "es" -> "Explorar Meditaciones"
                        "tl" -> "Iba pang Meditasyon"
                        else -> "Explore Quiet Tracks"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 4. Scrolling track items
            items(filteredMediaList) { item ->
                val isCurrent = currentItem.id == item.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isCurrent) Color(0xFF1E2B35) else Color.White.copy(alpha = 0.03f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) Color(0xFF3F5E4D) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectMedia(item) }
                        .padding(14.dp)
                        .testTag("track_item_${item.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom micro album thumbnail canvas!
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (item.type == "video") {
                            SunsetThumbnailCanvas(modifier = Modifier.fillMaxSize())
                        } else {
                            NightThumbnailCanvas(modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color(0xFF81C784) else Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        )
                    }

                    Text(
                        text = item.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
    }
}

// Custom Language Selection dropdown for player
@Composable
fun LanguageDropDownMenu(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val languages = listOf(
        Pair("en", "English"),
        Pair("es", "Español"),
        Pair("tl", "Tagalog")
    )

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("language_dropdown_trigger")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = languages.firstOrNull { it.first == currentLanguage }?.second ?: "English",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(text = name, fontWeight = if (code == currentLanguage) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    },
                    modifier = Modifier.testTag("lang_option_$code")
                )
            }
        }
    }
}

// Gorgeous Vector-drawn micro thumbnails for tracks
@Composable
fun SunsetThumbnailCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val skyGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFE07A5F), Color(0xFFF2CC8F))
        )
        drawRect(brush = skyGradient)
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = size.height * 0.25f,
            center = Offset(size.width * 0.5f, size.height * 0.7f)
        )
    }
}

@Composable
fun NightThumbnailCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val nightGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
        drawRect(brush = nightGradient)
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = size.height * 0.2f,
            center = Offset(size.width * 0.7f, size.height * 0.4f)
        )
    }
}
