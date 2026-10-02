package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class AstralParticle(
    val initialX: Float, // 0f..1f
    val initialY: Float, // 0f..1f
    val radius: Float,
    val speed: Float,
    val phase: Float,
    val baseAlpha: Float
)

/**
 * Astral Mesh Background:
 * Deep ethereal cosmic canvas with drifting astral stars, constellation links,
 * and soft ambient nebula gradients.
 */
@Composable
fun AstralBackground(
    modifier: Modifier = Modifier,
    isConnected: Boolean = false,
    isConnecting: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "astral_drift")

    // Slow, serene astral drift
    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnecting) 12000 else 24000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "astral_phase"
    )

    // Gentle nebula breathing pulse
    val nebulaPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nebula_pulse"
    )

    // Pre-calculated deterministic astral star nodes
    val particles = remember {
        val list = mutableListOf<AstralParticle>()
        val count = 28
        for (i in 0 until count) {
            val px = ((i * 137.5f) % 100) / 100f
            val py = ((i * 83.3f) % 100) / 100f
            val rad = 1.2f + (i % 3) * 0.8f
            val spd = 0.4f + (i % 4) * 0.25f
            val ph = i * 0.45f
            val alp = 0.25f + (i % 5) * 0.12f
            list.add(AstralParticle(px, py, rad, spd, ph, alp))
        }
        list
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Deep astral background canvas with nebula radial glows
            if (isDarkTheme) {
                // Dark void canvas
                drawRect(color = Color(0xFF090A0F))

                // Top-right cosmic violet nebula
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x334F46E5), // Indigo
                            Color(0x187C3AED), // Violet
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.15f),
                        radius = width * 0.8f * nebulaPulse
                    ),
                    radius = width * 0.8f * nebulaPulse,
                    center = Offset(width * 0.85f, height * 0.15f)
                )

                // Center-left aurora / emerald glow
                val centerAuraColor = when {
                    isConnected -> Color(0x3310B981) // Emerald
                    isConnecting -> Color(0x336366F1) // Indigo
                    else -> Color(0x183B82F6)        // Astral Blue
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            centerAuraColor,
                            Color.Transparent
                        ),
                        center = Offset(width * 0.2f, height * 0.5f),
                        radius = width * 0.75f * nebulaPulse
                    ),
                    radius = width * 0.75f * nebulaPulse,
                    center = Offset(width * 0.2f, height * 0.5f)
                )
            } else {
                // Light crystalline astral canvas
                drawRect(color = Color(0xFFF7F8FC))

                // Soft lavender ambient glow at top
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x22818CF8),
                            Color(0x10C7D2FE),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.8f, height * 0.15f),
                        radius = width * 0.75f * nebulaPulse
                    ),
                    radius = width * 0.75f * nebulaPulse,
                    center = Offset(width * 0.8f, height * 0.15f)
                )

                // Subtle emerald / cyan aura
                val lightAuraColor = when {
                    isConnected -> Color(0x1E10B981)
                    isConnecting -> Color(0x1E6366F1)
                    else -> Color(0x123B82F6)
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            lightAuraColor,
                            Color.Transparent
                        ),
                        center = Offset(width * 0.25f, height * 0.55f),
                        radius = width * 0.7f * nebulaPulse
                    ),
                    radius = width * 0.7f * nebulaPulse,
                    center = Offset(width * 0.25f, height * 0.55f)
                )
            }

            // 2. Compute current positions of all astral stars
            val maxLinkDist = 85.dp.toPx()
            val nodePositions = ArrayList<Offset>(particles.size)

            for (p in particles) {
                val dx = sin(driftPhase * p.speed + p.phase) * 14.dp.toPx()
                val dy = cos(driftPhase * p.speed * 0.8f + p.phase) * 14.dp.toPx()
                val x = (p.initialX * width + dx).mod(width)
                val y = (p.initialY * height + dy).mod(height)
                nodePositions.add(Offset(x, y))
            }

            // 3. Draw ethereal constellation mesh lines between close astral nodes
            val lineColor = if (isDarkTheme) {
                if (isConnected) Color(0xFF34D399) else Color(0xFF818CF8)
            } else {
                if (isConnected) Color(0xFF10B981) else Color(0xFF6366F1)
            }

            for (i in 0 until particles.size) {
                val p1 = nodePositions[i]
                for (j in (i + 1) until particles.size) {
                    val p2 = nodePositions[j]
                    val dX = p1.x - p2.x
                    val dY = p1.y - p2.y
                    val dist = sqrt(dX * dX + dY * dY)
                    if (dist < maxLinkDist) {
                        val alpha = (1f - dist / maxLinkDist) * 0.18f
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 0.8.dp.toPx(),
                            alpha = alpha
                        )
                    }
                }
            }

            // 4. Draw astral stars / nodes
            val starColor = if (isDarkTheme) {
                if (isConnected) Color(0xFF6EE7B7) else Color(0xFFA5B4FC)
            } else {
                if (isConnected) Color(0xFF059669) else Color(0xFF4F46E5)
            }

            for (i in 0 until particles.size) {
                val pos = nodePositions[i]
                val p = particles[i]
                val twinkle = 0.75f + 0.35f * sin(driftPhase * 1.5f + p.phase)

                // Soft star glow
                drawCircle(
                    color = starColor,
                    radius = p.radius.dp.toPx() * 1.6f,
                    center = pos,
                    alpha = p.baseAlpha * twinkle * 0.35f
                )

                // Star core
                drawCircle(
                    color = starColor,
                    radius = p.radius.dp.toPx(),
                    center = pos,
                    alpha = p.baseAlpha * twinkle
                )
            }
        }
    }
}
