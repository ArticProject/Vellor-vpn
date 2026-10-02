package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border

/**
 * Modern fluid bounce / tactile feedback modifier for buttons, cards and dials.
 * Creates an elastic, physics-based scale compression on touch down and spring-back on release.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounce_scale"
    )

    this
        .graphicsLayer {
            // Read state value ONLY in the graphics/draw phase to bypass recomposition
            val s = scaleState.value
            scaleX = s
            scaleY = s
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Harmonic multi-layer pulsing halo rings for the central connection dial.
 * Ethereal, elegant and fluid without visual clutter.
 */
@Composable
fun HarmonicPulseRings(
    modifier: Modifier = Modifier,
    baseSize: Dp = 96.dp,
    ringColor: Color = Color.White,
    active: Boolean = true
) {
    if (!active) return

    val infiniteTransition = rememberInfiniteTransition(label = "harmonic_rings")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_phase_1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, delayMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_phase_2"
    )

    Box(
        modifier = modifier.size(baseSize + 56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Wave ring 1
        val scale1 = 1.0f + phase1 * 0.48f
        val alpha1 = ((1f - phase1) * 0.55f).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .size(baseSize)
                .scale(scale1)
                .clip(CircleShape)
                .border(1.4.dp, ringColor.copy(alpha = alpha1), CircleShape)
        )

        // Wave ring 2 (staggered)
        val scale2 = 1.0f + phase2 * 0.48f
        val alpha2 = ((1f - phase2) * 0.55f).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .size(baseSize)
                .scale(scale2)
                .clip(CircleShape)
                .border(1.4.dp, ringColor.copy(alpha = alpha2), CircleShape)
        )
    }
}
