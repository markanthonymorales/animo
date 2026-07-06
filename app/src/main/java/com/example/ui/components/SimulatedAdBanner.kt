package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimulatedAdBanner(
    language: String
) {
    val sponsorLabel = when (language) {
        "es" -> "PATROCINADO"
        "tl" -> "SPONSOR"
        else -> "SPONSORED"
    }

    val sponsorMessage = when (language) {
        "es" -> "Apoyado por la Red de Lecturas Pacíficas. Descubre libros de paz y meditación."
        "tl" -> "Suportado ng Peaceful Readings Network. Tuklasin ang mga libro ng kapayapaan."
        else -> "Supported by Peaceful Readings Network. Discover silent guides & meditative books."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .clickable { /* Simulated navigation to peaceful books */ }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("static_ad_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Elegant minor "AD" indicator that looks extremely peaceful
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = sponsorLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = sponsorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
    }
}
