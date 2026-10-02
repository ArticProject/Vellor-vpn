package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.model.ServerLocation

@Composable
fun CitySkylineView(
    server: ServerLocation,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F0F12))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF27272A),
                        Color(0xFF141416),
                        Color(0xFF09090B)
                    )
                )
            )

            when (server.cityCode) {
                "ZRH" -> drawZurichLandmarks(w, h)
                "RKV", "HEL" -> drawReykjavikLandmarks(w, h)
                "TYO" -> drawTokyoLandmarks(w, h)
                "AMS" -> drawAmsterdamLandmarks(w, h)
                "LON" -> drawLondonLandmarks(w, h)
                "FRA" -> drawFrankfurtLandmarks(w, h)
                "BER" -> drawFrankfurtLandmarks(w, h)
                else -> drawGenericMonoliths(w, h)
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF09090B).copy(alpha = 0.85f),
                        Color(0xFF09090B)
                    ),
                    startY = h * 0.72f,
                    endY = h
                )
            )
        }
    }
}

private fun DrawScope.drawZurichLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    val darkTone = Color(0xFF3F3F46)
    val towerWidth = w * 0.11f
    val towerHeight = h * 0.52f
    val t1X = w * 0.38f
    val t2X = w * 0.54f
    val baseY = h * 0.78f

    drawRect(color = midTone, topLeft = Offset(t1X, baseY - towerHeight), size = Size(towerWidth, towerHeight))
    drawRect(color = darkTone, topLeft = Offset(t2X, baseY - towerHeight), size = Size(towerWidth, towerHeight))
}

private fun DrawScope.drawTokyoLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    val towerX = w * 0.50f
    val baseY = h * 0.78f
    val p = Path().apply {
        moveTo(towerX - w * 0.12f, baseY)
        lineTo(towerX - w * 0.02f, baseY - h * 0.55f)
        lineTo(towerX, baseY - h * 0.70f)
        lineTo(towerX + w * 0.02f, baseY - h * 0.55f)
        lineTo(towerX + w * 0.12f, baseY)
        close()
    }
    drawPath(p, color = midTone)
}

private fun DrawScope.drawLondonLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    drawRect(color = midTone, topLeft = Offset(w * 0.45f, h * 0.25f), size = Size(w * 0.12f, h * 0.53f))
}

private fun DrawScope.drawAmsterdamLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    for (i in 0..4) {
        val x = w * (0.20f + i * 0.12f)
        drawRect(color = midTone, topLeft = Offset(x, h * 0.35f), size = Size(w * 0.10f, h * 0.43f))
    }
}

private fun DrawScope.drawReykjavikLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    val p = Path().apply {
        moveTo(w * 0.50f, h * 0.20f)
        lineTo(w * 0.35f, h * 0.78f)
        lineTo(w * 0.65f, h * 0.78f)
        close()
    }
    drawPath(p, color = midTone)
}

private fun DrawScope.drawFrankfurtLandmarks(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    drawRect(color = midTone, topLeft = Offset(w * 0.30f, h * 0.25f), size = Size(w * 0.15f, h * 0.53f))
    drawRect(color = midTone, topLeft = Offset(w * 0.55f, h * 0.20f), size = Size(w * 0.18f, h * 0.58f))
}

private fun DrawScope.drawGenericMonoliths(w: Float, h: Float) {
    val midTone = Color(0xFFA1A1AA)
    drawRect(color = midTone, topLeft = Offset(w * 0.35f, h * 0.30f), size = Size(w * 0.30f, h * 0.48f))
}
