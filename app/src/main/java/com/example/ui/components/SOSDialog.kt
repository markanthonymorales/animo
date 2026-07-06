package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SOSColor

@Composable
fun SOSCrisisDialog(
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val title = when (language) {
        "es" -> "No estás solo. Por favor, busca ayuda."
        "tl" -> "Hindi ka nag-iisa. Mangyaring humingi ng tulong."
        else -> "You are not alone. Please reach out."
    }

    val body = when (language) {
        "es" -> "Hemos detectado que estás pasando por un momento sumamente difícil. Tu vida es increíblemente valiosa. Por favor, comunícate con profesionales de apoyo de inmediato."
        "tl" -> "Naramdaman namin na ikaw ay dumaranas ng matinding pagsubok ngayon. Napakahalaga ng iyong buhay. Mangyaring makipag-ugnayan sa mga propesyonal na handang tumulong sa iyo."
        else -> "We detected that you are going through an extremely heavy moment. Your life has infinite value and beauty. Please reach out to professional support systems immediately."
    }

    Dialog(
        onDismissRequest = { /* Non-dismissible by tapping outside */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, SOSColor, RoundedCornerShape(24.dp))
                .testTag("crisis_sos_modal"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Crisis Help",
                    tint = SOSColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SOSColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(16.dp))

                // Hotline List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HotlineItem(
                        region = "US / Canada",
                        number = "Call or Text 988",
                        description = "Suicide & Crisis Lifeline (24/7)",
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                            context.startActivity(intent)
                        }
                    )
                    HotlineItem(
                        region = "Crisis Text Line",
                        number = "Text HOME to 741741",
                        description = "Free crisis support via text",
                        onCall = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:741741?body=HOME"))
                            context.startActivity(intent)
                        }
                    )
                    HotlineItem(
                        region = "United Kingdom",
                        number = "Call 111 or 116 123",
                        description = "NHS & Samaritans help",
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:116123"))
                            context.startActivity(intent)
                        }
                    )
                    HotlineItem(
                        region = "Philippines",
                        number = "Call 1553 / 0917-899-8727",
                        description = "NCMH Crisis Helpline",
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1553"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SOSColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("crisis_sos_dismiss_button")
                ) {
                    Text(
                        text = when (language) {
                            "es" -> "Reconozco esto, estoy a salvo ahora"
                            "tl" -> "Naintindihan ko, ligtas na ako ngayon"
                            else -> "I acknowledge this, I am safe now"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HotlineItem(
    region: String,
    number: String,
    description: String,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = region,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = number,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        IconButton(
            onClick = onCall,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Dial",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SOSGeneralDialog(
    language: String,
    onDismiss: () -> Unit
) {
    var phase by remember { mutableStateOf("Inhale peace...") }
    var secondsRemaining by remember { mutableStateOf(4) }

    // Diaphragmatic breathing core animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 8000
                0.7f at 0
                1.3f at 4000 // Inhale peaks at 4s
                1.3f at 5000 // Hold for 1s
                0.7f at 8000 // Exhale peaks at 8s
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    // Sync breathing text and timer
    LaunchedEffect(scaleFactor) {
        // Approximate phase mapping based on scale cycle
        if (scaleFactor < 1.0f && phase != "Exhale anxiety...") {
            phase = when (language) {
                "es" -> "Exhala la ansiedad..."
                "tl" -> "Ibuga ang pangamba..."
                else -> "Exhale anxiety..."
            }
        } else if (scaleFactor >= 1.25f && phase != "Hold...") {
            phase = when (language) {
                "es" -> "Retén..."
                "tl" -> "Pigilan..."
                else -> "Hold..."
            }
        } else if (scaleFactor >= 0.7f && scaleFactor < 1.25f && scaleFactor > 0.8f && phase != "Inhale peace...") {
            phase = when (language) {
                "es" -> "Inhala paz..."
                "tl" -> "Huminga ng kapayapaan..."
                else -> "Inhale peace..."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("general_sos_modal"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            "es" -> "SOS Calma"
                            "tl" -> "SOS Kalmado"
                            else -> "SOS Calm"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (language) {
                        "es" -> "Sigue el círculo rítmico para ralentizar tu respiración y calmar tu sistema nervioso."
                        "tl" -> "Sundan ang bilog upang mapabagal ang paghinga at pakalmahin ang iyong katawan."
                        else -> "Follow the expanding circle to slow your breathing and soothe your nervous system."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Beautiful Meditative Pulsing Breathing Circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scaleFactor)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = phase,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("general_sos_dismiss_button")
                ) {
                    Text(
                        text = when (language) {
                            "es" -> "Me siento mejor, gracias"
                            "tl" -> "Mabuti na ako, salamat"
                            else -> "I feel better, thank you"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
