package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1-in-1 replication matching user's exact specification:
 * - 6 solid black circular badges (NOT glassmorphic/glass style) with crisp white icons revolving in orbit.
 * - The badges shrink/fade away.
 * - The central 8-dot flower (Screenshot_20261003_132850.jpg) appears.
 * - It DOES NOT ROTATE (angle 0) and stays completely static for 2.0 seconds.
 * - Cycle restarts seamlessly.
 */
@Composable
fun CosmosVideoExactMorphingAnimation(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Total cycle duration: 5100 ms:
    // 0.00 - 0.47 (2400ms): 6 badges orbiting
    // 0.47 - 0.55 (400ms): badges shrink/disappear
    // 0.55 - 0.94 (2000ms): 8-dot flower stays completely still, no rotation!
    // 0.94 - 1.00 (300ms): flower fades out, badges return
    val infiniteTransition = rememberInfiniteTransition(label = "features_morph_anim")
    val masterProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "master_progress"
    )

    // Solid black circular badges - NOT glass style
    val ballColor = if (isDarkTheme) Color(0xFF14171A) else Color(0xFF101214)
    val ballBorder = if (isDarkTheme) Color(0xFF23272C) else Color(0xFF1E2125)
    val dotColor = if (isDarkTheme) Color.White else Color(0xFF09090B)

    val icons = listOf(
        Icons.Filled.Shield,
        Icons.Filled.FlashOn,
        Icons.Filled.Person,
        Icons.Filled.VpnKey,
        Icons.Filled.Dns,
        Icons.Filled.Speed
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        val p = masterProgress

        when {
            // PHASE 1: [0.00 .. 0.47] 6 solid black circular badges revolving clockwise
            p < 0.47f -> {
                val phaseP = p / 0.47f
                val baseRadius = 68.dp.value
                val rotationDeg = phaseP * 360f

                icons.forEachIndexed { index, icon ->
                    val angleDeg = (360f / icons.size) * index + rotationDeg
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val x = baseRadius * cos(angleRad)
                    val y = baseRadius * sin(angleRad)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = x * density
                                translationY = y * density
                            }
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ballColor)
                            .border(1.dp, ballBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // PHASE 2: [0.47 .. 0.55] Badges shrink into center & disappear
            p in 0.47f..<0.55f -> {
                val t = FastOutSlowInEasing.transform((p - 0.47f) / 0.08f)
                val startRadius = 68.dp.value
                val targetRadius = 16.dp.value
                val currentRadius = startRadius + (targetRadius - startRadius) * t

                val startSize = 46.dp.value
                val targetSize = 8.dp.value
                val currentSize = startSize + (targetSize - startSize) * t
                val alpha = (1f - t).coerceIn(0f, 1f)

                icons.forEachIndexed { index, icon ->
                    val angleDeg = (360f / icons.size) * index + 360f
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val x = currentRadius * cos(angleRad)
                    val y = currentRadius * sin(angleRad)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = x * density
                                translationY = y * density
                                this.alpha = alpha
                            }
                            .size(currentSize.dp)
                            .clip(CircleShape)
                            .background(ballColor)
                            .border(1.dp, ballBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (alpha > 0.3f) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = alpha),
                                modifier = Modifier.size((18f * (1f - t)).coerceAtLeast(6f).dp)
                            )
                        }
                    }
                }
            }

            // PHASE 3: [0.55 .. 0.94] EXACT 6-DOT HEXAGONAL FLOWER STANDS STILL FOR 2 SECONDS (DOES NOT ROTATE)
            // As user drew in Screenshot_20261003_141452.jpg:
            // Exactly 6 dots: 1 top (-90°), 1 bottom (90°), 2 left, 2 right, standing still for 2.0s!
            p in 0.55f..<0.94f -> {
                val ringRadius = 24.dp.value

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dotRadiusPx = 5.5.dp.toPx()
                    val numDots = 6
                    for (i in 0 until numDots) {
                        // Angle from -90° (top) in steps of 60°
                        val angleDeg = -90f + (360f / numDots) * i
                        val rad = (angleDeg * PI / 180f).toFloat()
                        val x = center.x + ringRadius * density * cos(rad)
                        val y = center.y + ringRadius * density * sin(rad)
                        drawCircle(
                            color = dotColor,
                            radius = dotRadiusPx,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // PHASE 4: [0.94 .. 1.00] 8 dots blossom back out into the 6 badges
            else -> {
                val expandP = FastOutSlowInEasing.transform((p - 0.94f) / 0.06f)
                val startRadius = 22.dp.value
                val targetRadius = 68.dp.value
                val currentRadius = startRadius + (targetRadius - startRadius) * expandP

                val startSize = 10.dp.value
                val targetSize = 46.dp.value
                val currentSize = startSize + (targetSize - startSize) * expandP
                val iconAlpha = expandP.coerceIn(0f, 1f)

                icons.forEachIndexed { index, icon ->
                    val angleDeg = (360f / icons.size) * index
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val x = currentRadius * cos(angleRad)
                    val y = currentRadius * sin(angleRad)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = x * density
                                translationY = y * density
                            }
                            .size(currentSize.dp)
                            .clip(CircleShape)
                            .background(ballColor)
                            .border(1.dp, ballBorder.copy(alpha = expandP), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconAlpha > 0.15f) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = iconAlpha),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
