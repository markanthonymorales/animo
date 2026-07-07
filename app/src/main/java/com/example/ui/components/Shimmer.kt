package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    // Warm soft colors matching the cream and sage green aesthetic of Animo
    val shimmerColors = listOf(
        Color(0xFFE6E2D8).copy(alpha = 0.6f),
        Color(0xFFF2EFE9).copy(alpha = 0.9f),
        Color(0xFFE6E2D8).copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun DailyScriptureSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8F0EC)) // Gentle sage bg of Scripture card
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                ShimmerPlaceholder(modifier = Modifier.width(110.dp).height(12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerPlaceholder(modifier = Modifier.width(160.dp).height(20.dp))
            }
            ShimmerPlaceholder(modifier = Modifier.width(80.dp).height(32.dp), shape = RoundedCornerShape(12.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(18.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.85f).height(18.dp))
        Spacer(modifier = Modifier.height(14.dp))
        ShimmerPlaceholder(modifier = Modifier.width(90.dp).height(14.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFC8DBD0)))
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(14.dp))
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.95f).height(14.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerPlaceholder(modifier = Modifier.width(100.dp).height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerPlaceholder(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(18.dp))
                ShimmerPlaceholder(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(18.dp))
            }
        }
    }
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F4)) // Soft cream bg
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                ShimmerPlaceholder(modifier = Modifier.width(100.dp).height(18.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(modifier = Modifier.width(160.dp).height(28.dp))
            }
            ShimmerPlaceholder(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(22.dp))
        }

        // Mood shield
        ShimmerPlaceholder(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp)
        )

        // Scripture card skeleton
        DailyScriptureSkeleton()

        // Verse of the Day skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            ShimmerPlaceholder(modifier = Modifier.width(130.dp).height(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                }
                ShimmerPlaceholder(modifier = Modifier.size(width = 90.dp, height = 75.dp), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(20.dp))
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(20.dp))
            }
        }
    }
}
