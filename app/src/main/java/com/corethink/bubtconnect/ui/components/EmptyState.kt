package com.corethink.bubtconnect.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess

enum class EmptyIllustration { NoExams, NoNotes }

/** 1w / 1x motion note · Gentle vertical drift on the float loop. */
private fun Modifier.floatDrift(
    durationMillis: Int,
    startDelayMillis: Int = 0,
    amplitude: Dp = 6.dp
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "emptyStateFloat")
    val t by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis = startDelayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyStateFloatY"
    )
    graphicsLayer { translationY = t * amplitude.toPx() }
}

/** 1w / 1x · Illustrated empty state built from basic shapes + a Material icon. */
@Composable
fun EmptyState(
    illustration: EmptyIllustration,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(200.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(172.dp, 142.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = CircleShape
                    )
            )
            when (illustration) {
                EmptyIllustration.NoExams -> NoExamsArt()
                EmptyIllustration.NoNotes -> NoNotesArt()
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = BubtSpacing.Xl)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = BubtSpacing.Sm, bottom = BubtSpacing.Lg)
        )
        action()
    }
}

/** 1w · Floating event card with drifting confetti bits. */
@Composable
private fun NoExamsArt() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(88.dp, 96.dp)
                .floatDrift(3000, amplitude = 4.dp)
                .shadow(4.dp, MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(88.dp, 22.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Icon(
                imageVector = Icons.Filled.EventAvailable,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 8.dp)
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 28.dp, top = 30.dp)
                .size(12.dp)
                .floatDrift(2400, startDelayMillis = 200)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(end = 30.dp, top = 24.dp)
                .size(10.dp)
                .floatDrift(2900, startDelayMillis = 600)
                .background(MaterialTheme.colorScheme.error, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(end = 22.dp, bottom = 40.dp)
                .size(14.dp, 6.dp)
                .floatDrift(2600, startDelayMillis = 400)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 34.dp, bottom = 30.dp)
                .size(8.dp)
                .floatDrift(2200, startDelayMillis = 800)
                .background(BubtSuccess, CircleShape)
        )
    }
}

/** 1x · Note page on a back sheet with an add FAB. */
@Composable
private fun NoNotesArt() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 52.dp)
                .size(72.dp, 92.dp)
                .floatDrift(3200, startDelayMillis = 300, amplitude = 3.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.medium
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 68.dp)
                .size(76.dp, 96.dp)
                .floatDrift(2800, amplitude = 4.dp)
                .shadow(4.dp, MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp, 6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(52.dp, 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp, 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp, 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 34.dp, bottom = 34.dp)
                .size(44.dp)
                .floatDrift(2600, startDelayMillis = 500, amplitude = 5.dp)
                .shadow(6.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun EmptyStateNoExamsPreview() {
    BUBTConnectMaterial3Theme {
        EmptyState(
            illustration = EmptyIllustration.NoExams,
            title = "No upcoming exams 🎉",
            message = "No supplementary exams for you this term. We'll notify you if one is scheduled."
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun EmptyStateNoNotesPreview() {
    BUBTConnectMaterial3Theme {
        EmptyState(
            illustration = EmptyIllustration.NoNotes,
            title = "No shared notes yet. Be the first!",
            message = "Nobody has shared an internship report yet. Yours could help the next intake."
        )
    }
}
