package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.Screen
import kotlinx.coroutines.launch

@Composable
fun CounselScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val showCrisisModal by viewModel.showSOSCrisisModal.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to latest message when history changes
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F4)) // Soft cream background matching Screen 2 & 4
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Top Custom Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF2EFE9), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF26332A)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (language) {
                            "es" -> "Consejería"
                            "tl" -> "Payo"
                            else -> "Counsel"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26332A),
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = when (language) {
                            "es" -> "Estamos aquí para apoyarte"
                            "tl" -> "Nandito kami para sa iyo"
                            else -> "We are here to support you"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF5D665E),
                            fontSize = 11.sp
                        )
                    )
                }

                IconButton(
                    onClick = { viewModel.resetChat() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF2EFE9), CircleShape)
                        .testTag("reset_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Counsel",
                        tint = Color(0xFF3F5E4D)
                    )
                }
            }

            // 2. SOS Critical Alert Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0ED)), // Soft Warning Peach/Rose
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE07A5F).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE07A5F).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency Warning",
                                tint = Color(0xFFE07A5F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (language) {
                                "es" -> "Si estás en crisis, llama al 988 o busca ayuda inmediata."
                                "tl" -> "Kung may krisis, tumawag sa 988 para sa agarang tulong."
                                else -> "If you are experiencing a crisis, please call 988 or seek immediate help."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5A312B),
                                lineHeight = 16.sp
                            )
                        )
                    }

                    // Simulated Immediate Call Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE07A5F))
                            .clickable { viewModel.triggerCrisisExplicitly() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Call,
                            contentDescription = "Call 988 Hotline",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3. Message Logs History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chat_messages_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chatHistory) { message ->
                    ChatBubble(message = message)
                }

                if (isSending) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // 4. Subtle Typing companion indicator
            if (isSending) {
                Text(
                    text = when (language) {
                        "es" -> "Tu Guía del Santuario está escribiendo..."
                        "tl" -> "Sumusulat ang iyong Tagagabay..."
                        else -> "Your Sanctuary Guide is typing..."
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF5D665E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 4.dp)
                )
            }

            // 5. Bottom Text Input Row
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            if (showCrisisModal) Color(0xFFE07A5F) else Color(0xFFE2DDD5),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { if (!showCrisisModal) textInput = it },
                        placeholder = {
                            Text(
                                text = if (showCrisisModal) {
                                    when (language) {
                                        "es" -> "Conversación pausada por seguridad..."
                                        "tl" -> "Naka-pause para sa kaligtasan..."
                                        else -> "Conversation paused for safety..."
                                    }
                                } else {
                                    when (language) {
                                        "es" -> "Escribe lo que pesa en tu corazón..."
                                        "tl" -> "Isulat ang laman ng iyong puso..."
                                        else -> "Share what is on your heart..."
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = Color(0xFF8C968E)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = 4,
                        enabled = !showCrisisModal && !isSending
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank() && !showCrisisModal && !isSending) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(
                                if (textInput.isBlank() || showCrisisModal || isSending) Color(0xFFF2EFE9)
                                else Color(0xFF3F5E4D),
                                CircleShape
                            )
                            .size(36.dp)
                            .testTag("chat_send_button"),
                        enabled = textInput.isNotBlank() && !showCrisisModal && !isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isBlank() || showCrisisModal || isSending) Color(0xFF8C968E)
                            else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        Color(0xFFD3E7D6) // Soft pastel green for user
    } else {
        Color(0xFFF2EFE9) // Warm pastel gray for Guide
    }
    val contentColor = if (message.isUser) {
        Color(0xFF132A13) // Dark deep forest text
    } else {
        Color(0xFF26332A) // Slate green dark text
    }
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2DDD5).copy(alpha = 0.5f),
                    shape = shape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.2f at 0
                1f at 400
                0.2f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.2f at 0
                1f at 800
                0.2f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.2f at 0
                1f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .padding(start = 4.dp)
            .widthIn(max = 80.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
            .background(Color(0xFFF2EFE9))
            .border(1.dp, Color(0xFFE2DDD5).copy(alpha = 0.5f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).alpha(alpha1).background(Color(0xFF3F5E4D), CircleShape))
        Box(modifier = Modifier.size(6.dp).alpha(alpha2).background(Color(0xFF3F5E4D), CircleShape))
        Box(modifier = Modifier.size(6.dp).alpha(alpha3).background(Color(0xFF3F5E4D), CircleShape))
    }
}
