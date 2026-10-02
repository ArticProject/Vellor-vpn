package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom open/uncompleted infinity loop symbol:
 * Inspired by Meta/Moebius topological ribbon, but with an open / uncompleted aesthetic breach
 * symbolizing the sovereign bypass gateway.
 */
@Composable
fun OpenInfinityLogo(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    color: Color = Color.White,
    strokeWidth: Float = 3.2f
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            // Left loop: center around (0.30w, 0.50h), radius ~ 0.22w
            // Start from uncompleted gap at top-center (0.46w, 0.42h)
            moveTo(w * 0.46f, h * 0.44f)
            // Cross diagonally down-left towards left bottom
            cubicTo(
                w * 0.38f, h * 0.58f,
                w * 0.28f, h * 0.82f,
                w * 0.16f, h * 0.72f
            )
            // Left loop round arc
            cubicTo(
                w * 0.04f, h * 0.62f,
                w * 0.04f, h * 0.38f,
                w * 0.16f, h * 0.28f
            )
            // Left top curve crossing back over center
            cubicTo(
                w * 0.28f, h * 0.18f,
                w * 0.40f, h * 0.42f,
                w * 0.50f, h * 0.50f
            )
            // Cross over to right loop
            cubicTo(
                w * 0.60f, h * 0.58f,
                w * 0.72f, h * 0.82f,
                w * 0.84f, h * 0.72f
            )
            // Right round arc
            cubicTo(
                w * 0.96f, h * 0.62f,
                w * 0.96f, h * 0.38f,
                w * 0.84f, h * 0.28f
            )
            // Curve back towards top-right, but LEAVING IT OPEN / UNCOMPLETED (breaks before connecting back)
            cubicTo(
                w * 0.76f, h * 0.21f,
                w * 0.66f, h * 0.28f,
                w * 0.58f, h * 0.36f
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Accent terminal node dot on the open edge
        drawCircle(
            color = color,
            radius = strokeWidth * 0.85f,
            center = Offset(w * 0.58f, h * 0.36f)
        )
    }
}
