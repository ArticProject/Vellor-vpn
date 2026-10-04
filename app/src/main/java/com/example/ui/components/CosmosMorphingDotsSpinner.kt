package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1-in-1 recreation of the video loading animation:
 * 1. Rest: 8 dots in a horizontal row (● ● ● ● ● ● ● ●)
 * 2. Morph: The outer dots lift and curl into a circular ring
 * 3. Stretch: Each dot stretches radially into a rounded spoke/petal
 * 4. Spin: 8-spoke radial flower spinner rotates continuously with trailing opacity
 * 5. Return: Smoothly slows down, contracts back to round dots, and settles onto the horizontal baseline
 */
@Composable
fun CosmosMorphingDotsSpinner(
    isSpinning: Boolean,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    dotColor: Color = if (isDarkTheme) Color.White else Color(0xFF09090B)
) {
    // Morph progress: 0f = horizontal line of dots, 1f = circular radial spinner
    val morphProgress by animateFloatAsState(
        targetValue = if (isSpinning) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dots_morph_progress"
    )

    // Continuous rotation when spinning
    val infiniteTransition = rememberInfiniteTransition(label = "spinner_rotation")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_spin"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val numDots = 8

        val dotSpacing = 7.5.dp.toPx()
        val restingDotRadius = 3.6.dp.toPx()
        val ringRadius = 19.dp.toPx()
        val spokeFullLength = 11.5.dp.toPx()
        val spokeWidth = 3.8.dp.toPx()

        val morph = morphProgress.coerceIn(0f, 1f)

        // Interpolation stages
        // 0f..0.6f: Curl from horizontal line into circle
        // 0.4f..1.0f: Stretch dots into radial capsules
        // 0.6f..1.0f: Opacity trail and rotation
        val curlProgress = (morph / 0.65f).coerceIn(0f, 1f)
        val stretchProgress = ((morph - 0.35f) / 0.65f).coerceIn(0f, 1f)

        val currentRotation = continuousRotation * morph

        for (i in 0 until numDots) {
            // Resting position on horizontal row
            val restOffsetX = (i - (numDots - 1) / 2f) * dotSpacing
            val restX = centerX + restOffsetX
            val restY = centerY + 4.dp.toPx()

            // Circular target position
            val baseAngleDeg = i * (360f / numDots) - 90f
            val totalAngleDeg = baseAngleDeg + currentRotation
            val rad = Math.toRadians(totalAngleDeg.toDouble())

            val targetCenterX = centerX + (cos(rad) * ringRadius).toFloat()
            val targetCenterY = centerY + (sin(rad) * ringRadius).toFloat()

            // Smoothly move each dot from rest line to circle
            // Outer dots curl up first, creating the smile-arch seen in frame 00:01
            val dotDistFromCenter = abs(i - 3.5f) / 3.5f
            val curlCurve = ((curlProgress * 1.3f) - (1f - dotDistFromCenter) * 0.3f).coerceIn(0f, 1f)
            val smoothedCurl = sin(curlCurve * (PI.toFloat() / 2f))

            val currentCenterX = restX + (targetCenterX - restX) * smoothedCurl
            val currentCenterY = restY + (targetCenterY - restY) * smoothedCurl

            // Stretch into radial petal / spoke
            val spokeLen = (restingDotRadius * 2f) + (stretchProgress * (spokeFullLength - restingDotRadius * 2f))
            val halfLen = spokeLen / 2f

            // Trailing opacity when rotating in circular spoke mode
            val leadOffset = (totalAngleDeg % 360f + 360f) % 360f
            val spokeFrac = ((baseAngleDeg % 360f + 360f) % 360f) / 360f
            val spinAlpha = if (morph > 0.4f) {
                // Classic iOS / flower radial loader trailing alpha (lead is bright, trailing fades)
                val diff = (spokeFrac - (leadOffset / 360f) + 1f) % 1f
                0.18f + (0.82f * (1f - diff))
            } else {
                1.0f
            }
            val finalAlpha = 1.0f - (1.0f - spinAlpha) * morph

            if (stretchProgress > 0.05f) {
                // Draw rounded radial capsule
                val p1X = currentCenterX - (cos(rad) * halfLen).toFloat()
                val p1Y = currentCenterY - (sin(rad) * halfLen).toFloat()
                val p2X = currentCenterX + (cos(rad) * halfLen).toFloat()
                val p2Y = currentCenterY + (sin(rad) * halfLen).toFloat()

                drawLine(
                    color = dotColor.copy(alpha = finalAlpha),
                    start = Offset(p1X, p1Y),
                    end = Offset(p2X, p2Y),
                    strokeWidth = spokeWidth,
                    cap = StrokeCap.Round
                )
            } else {
                // Draw round dot
                drawCircle(
                    color = dotColor.copy(alpha = finalAlpha),
                    radius = restingDotRadius,
                    center = Offset(currentCenterX, currentCenterY)
                )
            }
        }
    }
}
