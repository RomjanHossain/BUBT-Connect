package com.corethink.bubtconnect.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Signature motion for BUBT Connect: springy press feedback, staggered entrances
 * and overshoot pops. All effects are graphicsLayer-only so they never re-layout.
 */

/** Springs down while pressed and bounces back on release. Share the interaction
 *  source with the surface/clickable that handles the tap. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
    dampingRatio: Float = 0.6f,
    stiffness: Float = Spring.StiffnessMedium,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Self-contained press bounce for raw clickables (no ripple). */
fun Modifier.bounceClick(
    onClick: () -> Unit,
    onClickLabel: String? = null,
    pressedScale: Float = 0.93f,
    dampingRatio: Float = 0.55f,
    stiffness: Float = Spring.StiffnessMedium,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    pressScale(source, pressedScale, dampingRatio, stiffness)
        .clickableNoRipple(source, onClickLabel, onClick)
}

/** One-shot entrance you can stagger: fades in and rises as data/cards appear. */
fun Modifier.riseIn(index: Int = 0, stepMillis: Int = 40, riseDistance: Float = 32f): Modifier =
    composed {
        var played by rememberSaveable { mutableStateOf(false) }
        val progress = remember { Animatable(if (played) 1f else 0f) }
        LaunchedEffect(Unit) {
            if (!played) {
                delay((index * stepMillis).coerceAtMost(320).toLong())
                progress.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                played = true
            }
        }
        graphicsLayer {
            alpha = progress.value.coerceIn(0f, 1f)
            translationY = (1f - progress.value) * riseDistance * density
        }
    }

/** Overshoot pop driven by a state change (e.g. heart like). */
@Composable
fun rememberPopScale(active: Boolean): Float {
    val scale by animateFloatAsState(
        targetValue = if (active) 1f else 0.55f,
        animationSpec = spring(dampingRatio = 0.32f, stiffness = 420f),
        label = "popScale"
    )
    return scale
}

/** Clickable without the default ripple, for places where motion carries the feedback. */
private fun Modifier.clickableNoRipple(
    source: MutableInteractionSource,
    onClickLabel: String?,
    onClick: () -> Unit
): Modifier = clickable(
    interactionSource = source,
    indication = null,
    onClickLabel = onClickLabel,
    onClick = onClick
)
