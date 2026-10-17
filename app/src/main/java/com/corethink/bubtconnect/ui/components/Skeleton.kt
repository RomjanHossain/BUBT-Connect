package com.corethink.bubtconnect.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 1n · Shimmer sweep, left to right, repeating every 1.4s. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerSweep"
    )
    val base = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
    val highlight = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    drawBehind {
        // The highlight band travels from just off the left edge to just off the right one.
        val x = size.width * (sweep * 2f - 1f)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(x, 0f),
                end = Offset(x + size.width, 0f)
            )
        )
    }
}

/** One shimmer block (placeholder rectangle). */
@Composable
fun Shim(
    modifier: Modifier = Modifier,
    radius: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(radius))
            .shimmer()
    )
}

/** Shimmer base/entry colors exposed for hand-rolled sweeps (e.g. cards). */
@Composable
fun shimmerHighlight(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

@Composable
fun shimmerBase(): Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
