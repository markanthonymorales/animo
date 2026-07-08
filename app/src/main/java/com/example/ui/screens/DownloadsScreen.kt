package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadedResource
import com.example.ui.ActiveDownload
import com.example.ui.DownloadStatus
import com.example.ui.MainViewModel
import com.example.ui.Screen

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val syncOverWifiOnly by viewModel.syncOverWifiOnly.collectAsState()
    val simulateMobileData by viewModel.simulateMobileData.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val completedDownloads by viewModel.downloads.collectAsState()

    // Storage calculations
    val totalSizeMb = remember(activeDownloads, completedDownloads) {
        val completedSum = completedDownloads.sumOf { res ->
            when (res.type) {
                "video" -> 45.2
                "audio_worship", "audio_prayer" -> 8.4
                else -> 0.05
            }
        }
        val activeSum = activeDownloads.sumOf { dl ->
            (dl.downloadedSizeMb).toDouble()
        }
        completedSum + activeSum
    }

    // Limit set to 500 MB for demo Cache
    val cacheLimitMb = 500.0
    val storageRatio = (totalSizeMb / cacheLimitMb).toFloat().coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F4)) // Cohesive soft Cream/Beige Background
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        // 1. Title Header
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = when (language) {
                        "es" -> "Gestor de Descargas"
                        "tl" -> "Tagapamahala ng Download"
                        else -> "Downloads Manager"
                    },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26332A)
                    ),
                    modifier = Modifier.testTag("downloads_title")
                )
                Text(
                    text = when (language) {
                        "es" -> "Administra tu contenido offline y consumo de datos."
                        "tl" -> "Pamahalaan ang offline content at paggamit ng data."
                        else -> "Manage offline content and cellular data synchronizations."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF5D665E),
                        fontSize = 14.sp
                    )
                )
            }
        }

        // 2. Storage Usage Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFE9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_usage_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Storage Usage",
                                tint = Color(0xFF3F5E4D)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    "es" -> "Almacenamiento de Animo"
                                    "tl" -> "Imbakan ng Animo"
                                    else -> "Animo Cache Storage"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF26332A)
                                )
                            )
                        }

                        // Clear cache button
                        TextButton(
                            onClick = {
                                completedDownloads.forEach {
                                    viewModel.cancelOrDeleteDownload(it.resourceId)
                                }
                                activeDownloads.forEach {
                                    viewModel.cancelOrDeleteDownload(it.id)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                        ) {
                            Text(
                                text = when (language) {
                                    "es" -> "Limpiar"
                                    "tl" -> "Burahin"
                                    else -> "Clear"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { storageRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF3F5E4D),
                        trackColor = Color(0xFFE2DDD5)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.1f MB used", totalSizeMb),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5D665E),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = String.format("%.1f MB limit", cacheLimitMb),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D665E))
                        )
                    }
                }
            }
        }

        // 3. Wi-Fi & Simulated Connection Settings
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Sync Over Wi-Fi Only setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (language) {
                                    "es" -> "Sincronizar solo por Wi-Fi"
                                    "tl" -> "Sync sa Wi-Fi Lamang"
                                    else -> "Sync Over Wi-Fi Only"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF26332A)
                                )
                            )
                            Text(
                                text = when (language) {
                                    "es" -> "Evita descargar videos grandes en datos móviles."
                                    "tl" -> "Iwasang mag-download ng malalaking video gamit ang mobile data."
                                    else -> "Prevents heavy media transfers on mobile cellular networks."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF5D665E)
                                )
                            )
                        }
                        Switch(
                            checked = syncOverWifiOnly,
                            onCheckedChange = { viewModel.toggleSyncOverWifiOnly() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3F5E4D)
                            ),
                            modifier = Modifier.testTag("wifi_only_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF2EFE9))

                    // Simulated Mobile Connection Toggle (Very awesome for demonstration!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (language) {
                                    "es" -> "Simular Datos Móviles"
                                    "tl" -> "Simulate Mobile Data"
                                    else -> "Simulate Mobile Data"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF26332A)
                                )
                            )
                            Text(
                                text = when (language) {
                                    "es" -> "Fuerza la simulación de red celular para probar Wi-Fi Only."
                                    "tl" -> "Ipilit ang cellular mode para subukan ang Wi-Fi Only."
                                    else -> "Force a simulated cellular mode to test Wi-Fi restriction."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF5D665E)
                                )
                            )
                        }
                        Switch(
                            checked = simulateMobileData,
                            onCheckedChange = { viewModel.toggleSimulateMobileData() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFB86B42)
                            ),
                            modifier = Modifier.testTag("simulate_cellular_switch")
                        )
                    }
                }
            }
        }

        // 4. Section: Ongoing Downloads
        val activeItems = activeDownloads.filter { it.status != DownloadStatus.COMPLETED }
        if (activeItems.isNotEmpty()) {
            item {
                Text(
                    text = when (language) {
                        "es" -> "Descargas Activas"
                        "tl" -> "Kasalukuyang Downloads"
                        else -> "Ongoing Fetches"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26332A)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(activeItems, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF2EFE9), RoundedCornerShape(16.dp))
                        .testTag("active_dl_${item.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    "video" -> Icons.Default.PlayCircle
                                    "audio_worship", "audio_prayer" -> Icons.Default.MusicNote
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = Color(0xFF3F5E4D),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF26332A)
                                    )
                                )
                                Text(
                                    text = String.format("%.1f MB / %.1f MB", item.downloadedSizeMb, item.totalSizeMb),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D665E))
                                )
                            }

                            // Play/Pause Action
                            IconButton(
                                onClick = {
                                    if (item.status == DownloadStatus.DOWNLOADING) {
                                        viewModel.pauseDownload(item.id)
                                    } else {
                                        viewModel.startOrResumeDownload(
                                            id = item.id,
                                            title = item.title,
                                            subtitle = item.subtitle,
                                            type = item.type,
                                            content = item.content,
                                            duration = item.duration
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (item.status == DownloadStatus.DOWNLOADING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Download",
                                    tint = Color(0xFF3F5E4D)
                                )
                            }

                            // Cancel Action
                            IconButton(
                                onClick = { viewModel.cancelOrDeleteDownload(item.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Download",
                                    tint = Color(0xFFC62828)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status / Waiting banner
                        if (item.status == DownloadStatus.FAILED_WIFI_REQUIRED) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF3CD))
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = when (language) {
                                        "es" -> "⚠️ Esperando conexión Wi-Fi..."
                                        "tl" -> "⚠️ Naghihintay ng Wi-Fi..."
                                        else -> "⚠️ Waiting for Wi-Fi (Cellular Sync Restricted)..."
                                    },
                                    color = Color(0xFF856404),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF3F5E4D),
                                trackColor = Color(0xFFF2EFE9)
                            )
                        }
                    }
                }
            }
        }

        // 5. Section: Offline Cached Library (Completed)
        item {
            Text(
                text = when (language) {
                    "es" -> "Biblioteca Offline"
                    "tl" -> "Offline Library"
                    else -> "Cached Offline Library"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF26332A)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (completedDownloads.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF2EFE9), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Empty Downloads",
                            tint = Color(0xFFB8C0B9),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (language) {
                                    "es" -> "Sin Contenido Offline"
                                    "tl" -> "Walang Offline Content"
                                    else -> "No Offline Content Cached"
                                },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26332A),
                            fontSize = 15.sp
                        )
                        Text(
                            text = when (language) {
                                    "es" -> "Toca el icono de descarga en cualquier recurso del Santuario para sincronizarlo."
                                    "tl" -> "I-tap ang download icon sa kahit anong Sanctuary media para i-sync ito offline."
                                    else -> "Tap the download icon on any Sanctuary media or scriptures to sync them for offline access."
                                },
                            color = Color(0xFF5D665E),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            items(completedDownloads, key = { it.resourceId }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF2EFE9), RoundedCornerShape(16.dp))
                        .testTag("completed_dl_${item.resourceId}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                "video" -> Icons.Default.PlayCircle
                                "audio_worship", "audio_prayer" -> Icons.Default.Headset
                                else -> Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = Color(0xFF3F5E4D),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF26332A)
                                )
                            )
                            Text(
                                text = "${item.subtitle} • ${
                                    when (item.type) {
                                        "video" -> "45.2 MB"
                                        "audio_worship", "audio_prayer" -> "8.4 MB"
                                        else -> "0.05 MB"
                                    }
                                }",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D665E))
                            )
                        }

                        // Play/Use Button
                        IconButton(
                            onClick = {
                                if (item.type == "video" || item.type.startsWith("audio")) {
                                    // Navigate to media player
                                    viewModel.navigateTo(Screen.MediaPlayer)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Cached Media",
                                tint = Color(0xFF3F5E4D)
                            )
                        }

                        // Delete from offline library
                        IconButton(
                            onClick = { viewModel.cancelOrDeleteDownload(item.resourceId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete from Offline Cache",
                                tint = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }
    }
}
