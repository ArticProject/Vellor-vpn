package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import java.util.Random
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class PlexusNode(
    val baseX: Float,
    val baseY: Float,
    val ampX: Float,
    val ampY: Float,
    val speedX: Float,
    val speedY: Float,
    val phaseX: Float,
    val phaseY: Float
)

/**
 * Geometric Plexus / Network Constellation Background.
 * Exactly matches the web example from screenshot:
 * 56 crisp drifting nodes forming clean triangulated geometric facets.
 * Strictly limits connections per node to 3 so it never clusters into "snowflakes",
 * but forms a wide, elegant polygonal mesh across the entire screen.
 */
@Composable
fun MeshNetworkBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    // 56 nodes evenly distributed across the entire screen layout
    val nodes = remember {
        val rand = Random(42) // Fixed seed for stable, aesthetic layout
        val list = mutableListOf<PlexusNode>()
        val cols = 7
        val rows = 8

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Stratified jittered grid to avoid clumping
                val cellX = (c + 0.15f + rand.nextFloat() * 0.7f) / cols
                val cellY = (r + 0.15f + rand.nextFloat() * 0.7f) / rows

                list.add(
                    PlexusNode(
                        baseX = cellX.coerceIn(0.04f, 0.96f),
                        baseY = cellY.coerceIn(0.03f, 0.97f),
                        ampX = 0.020f + rand.nextFloat() * 0.025f,
                        ampY = 0.020f + rand.nextFloat() * 0.025f,
                        speedX = 0.35f + rand.nextFloat() * 0.45f,
                        speedY = 0.35f + rand.nextFloat() * 0.45f,
                        phaseX = rand.nextFloat() * 6.28f,
                        phaseY = rand.nextFloat() * 6.28f
                    )
                )
            }
        }
        list
    }

    val transition = rememberInfiniteTransition(label = "plexus_transition")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 62.8318f, // 10 * 2PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plexus_time"
    )

    // Crisp high-contrast geometric colors
    val lineColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF09090B)
    val dotColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF09090B)
    val lineMaxAlpha = if (isDarkTheme) 0.22f else 0.18f
    val dotAlpha = if (isDarkTheme) 0.65f else 0.50f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val maxConnectDist = 95.dp.toPx()
        val maxDistSq = maxConnectDist * maxConnectDist

        // Compute current drifting positions
        val count = nodes.size
        val pts = Array(count) { i ->
            val n = nodes[i]
            val px = (n.baseX + sin(time * n.speedX + n.phaseX) * n.ampX).coerceIn(0.02f, 0.98f)
            val py = (n.baseY + cos(time * n.speedY + n.phaseY) * n.ampY).coerceIn(0.02f, 0.98f)
            Offset(px * w, py * h)
        }

        // Connection degree tracker to guarantee clean geometric mesh (no starbursts/snowflakes)
        val degrees = IntArray(count)

        // 1. Draw connecting geometric network lines
        for (i in 0 until count) {
            if (degrees[i] >= 3) continue
            val p1 = pts[i]

            for (j in (i + 1) until count) {
                if (degrees[i] >= 3) break
                if (degrees[j] >= 3) continue

                val p2 = pts[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = (dx * dx) + (dy * dy)

                if (distSq < maxDistSq) {
                    val dist = hypot(dx, dy)
                    val proximity = 1f - (dist / maxConnectDist)
                    val alpha = proximity * lineMaxAlpha

                    drawLine(
                        color = lineColor.copy(alpha = alpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 0.9.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    degrees[i]++
                    degrees[j]++
                }
            }
        }

        // 2. Draw crisp sharp nodes (small distinct points, no fuzzy glow)
        val dotRadius = 1.8.dp.toPx()
        for (i in 0 until count) {
            drawCircle(
                color = dotColor.copy(alpha = dotAlpha),
                radius = dotRadius,
                center = pts[i]
            )
        }
    }
}
