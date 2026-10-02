package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1-in-1 replication of the 8-dots animation from Cosmos / "No internet connection" dialog:
 * Exactly 8 clean, solid, flat black dots (no 3D, no extra lines):
 * 1) A rotating circular loader (8 dots arranged in a circle, spinning smoothly)
 * 2) Smoothly morphs without cross-collision into a horizontal chain of beads jumping in a wave
 * 3) Smoothly curls back into the rotating circular loader
 */
@Composable
fun VellorMorphingDotsAnimation(
    modifier: Modifier = Modifier,
    dotColor: Color = Color(0xFF000000),
    size: Dp = 130.dp,
    dotRadiusDp: Dp = 5.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmos_dots_transition")

    // Full cycle progress: 0f..1f (4.8 seconds total cycle)
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle_progress"
    )

    // Continuous wave phase for the bouncing beads
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Base angles for the 8 dots on the circle, ordered from left to right
    // to guarantee zero dot cross-collision when opening into a line:
    val baseAngles = floatArrayOf(
        PI.toFloat(),                     // Index 0: leftmost (-R, 0)
        3f * PI.toFloat() / 4f,           // Index 1: bottom-left
        5f * PI.toFloat() / 4f,           // Index 2: top-left
        PI.toFloat() / 2f,                // Index 3: bottom
        3f * PI.toFloat() / 2f,           // Index 4: top
        PI.toFloat() / 4f,                // Index 5: bottom-right
        7f * PI.toFloat() / 4f,           // Index 6: top-right
        0f                                // Index 7: rightmost (+R, 0)
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val dotCount = 8
            val circleRadiusPx = 28.dp.toPx()
            val dotRadiusPx = dotRadiusDp.toPx()
            val spacingPx = 13.dp.toPx()
            val maxWaveAmplitudePx = 12.dp.toPx()

            // Cycle timeline:
            // 0.00..0.38 (0 - 1824ms): Circular spinner rotating smoothly
            // 0.38..0.52 (1824 - 2496ms): Morphing from Circle into Horizontal Wave
            // 0.52..0.85 (2496 - 4080ms): Horizontal Wave bouncing
            // 0.85..1.00 (4080 - 4800ms): Morphing back from Horizontal Wave into Circle
            val t = cycleProgress

            val morphProgress: Float // 0f = Circle, 1f = Wave
            val waveAmp: Float
            val rotation: Float

            when {
                // Phase 1: Pure Circle Spinner (rotates 1 full turn)
                t < 0.38f -> {
                    val p = t / 0.38f
                    morphProgress = 0f
                    waveAmp = 0f
                    rotation = p * 2f * PI.toFloat()
                }

                // Phase 2: Morphing from Circle into Horizontal Wave
                t < 0.52f -> {
                    val sub = (t - 0.38f) / 0.14f
                    val eased = FastOutSlowInEasing.transform(sub)
                    morphProgress = eased
                    waveAmp = maxWaveAmplitudePx * eased
                    // Settles rotation cleanly to 2*PI (0)
                    rotation = 2f * PI.toFloat()
                }

                // Phase 3: Pure Horizontal Wave
                t < 0.85f -> {
                    morphProgress = 1f
                    waveAmp = maxWaveAmplitudePx
                    rotation = 0f
                }

                // Phase 4: Morphing back from Horizontal Wave into Circle
                else -> {
                    val sub = (t - 0.85f) / 0.15f
                    val eased = FastOutSlowInEasing.transform(sub)
                    morphProgress = 1f - eased
                    waveAmp = maxWaveAmplitudePx * (1f - eased)
                    rotation = eased * 0.5f * PI.toFloat()
                }
            }

            // Draw the 8 pristine, solid dots
            for (i in 0 until dotCount) {
                // Circle position:
                val currentAngle = baseAngles[i] + rotation
                val circleX = center.x + circleRadiusPx * cos(currentAngle)
                val circleY = center.y + circleRadiusPx * sin(currentAngle)

                // Horizontal line position:
                val waveX = center.x + (i - (dotCount - 1) / 2f) * spacingPx
                val waveY = center.y + waveAmp * sin(wavePhase - i * 0.72f)

                // Smooth non-colliding morph between circle and wave
                val currentX = circleX + (waveX - circleX) * morphProgress
                val currentY = circleY + (waveY - circleY) * morphProgress

                drawCircle(
                    color = dotColor,
                    radius = dotRadiusPx,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
