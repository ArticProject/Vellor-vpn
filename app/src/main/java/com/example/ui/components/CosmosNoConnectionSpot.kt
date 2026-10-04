package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Single-run physical Cosmos animation:
 * 8 dots launch up into an orbital ring, spin at peak, fall back to baseline,
 * bounce with squash-damping, and rest on the baseline without repeating.
 */
@Composable
fun CosmosOneShotDotsAnimation(
    playTrigger: Int,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    dotColor: Color = if (isDarkTheme) Color.White else Color(0xFF14171A)
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(playTrigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    val currentT = progress.value

    Canvas(modifier = modifier.size(width = 160.dp, height = 90.dp)) {
        val centerX = size.width / 2f
        val baselineY = size.height * 0.78f
        val dotRadius = 4.2.dp.toPx()
        val numDots = 8

        val liftY: Float
        val ringRadius: Float
        val rotationAngle: Float
        val horizontalSpread: Float

        when {
            currentT < 0.12f -> {
                // Initial resting state on baseline
                liftY = 0f
                ringRadius = 0f
                rotationAngle = 0f
                horizontalSpread = 1f
            }
            currentT < 0.45f -> {
                // Spring ascent into circular constellation
                val t = (currentT - 0.12f) / 0.33f
                val springT = sin(t * (PI.toFloat() / 2f))
                val overshoot = if (t > 0.8f) sin((t - 0.8f) / 0.2f * PI.toFloat()) * 4f else 0f
                liftY = (springT * 38.dp.toPx()) + overshoot
                ringRadius = springT * 22.dp.toPx()
                rotationAngle = t * 45f
                horizontalSpread = 1f - springT
            }
            currentT < 0.70f -> {
                // Orbital spin at apex
                val t = (currentT - 0.45f) / 0.25f
                liftY = 38.dp.toPx() + sin(t * PI.toFloat() * 2f) * 1.5f
                ringRadius = 22.dp.toPx()
                rotationAngle = 45f + (t * 180f)
                horizontalSpread = 0f
            }
            currentT < 0.88f -> {
                // Fall back down to baseline with gravity
                val t = (currentT - 0.70f) / 0.18f
                val fallT = t * t
                liftY = (1f - fallT) * 38.dp.toPx()
                ringRadius = (1f - fallT) * 22.dp.toPx()
                rotationAngle = 225f + (t * 45f)
                horizontalSpread = fallT
            }
            currentT < 1.0f -> {
                // Impact squash & settle on baseline
                val t = (currentT - 0.88f) / 0.12f
                val squash = sin(t * PI.toFloat()) * 5.dp.toPx()
                liftY = 0f
                ringRadius = 0f
                rotationAngle = 270f
                horizontalSpread = 1f + (squash / 10f)
            }
            else -> {
                // Completed: dots lie and rest calmly on baseline
                liftY = 0f
                ringRadius = 0f
                rotationAngle = 270f
                horizontalSpread = 1f
            }
        }

        // Draw 8 dots
        for (i in 0 until numDots) {
            val angleDeg = (i * (360f / numDots)) + rotationAngle
            val angleRad = angleDeg * (PI.toFloat() / 180f)

            val px: Float
            val py: Float

            if (ringRadius > 1f) {
                px = centerX + cos(angleRad) * ringRadius
                py = baselineY - liftY + sin(angleRad) * ringRadius
            } else {
                val offsetIndex = i - (numDots - 1) / 2f
                val spacing = 7.dp.toPx() * horizontalSpread
                px = centerX + (offsetIndex * spacing)
                val bounceJitter = if (currentT in 0.88f..0.98f) sin((i + currentT * 10f)) * 1.5f else 0f
                py = baselineY + bounceJitter
            }

            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * Full-screen modal overlay with blurred backdrop.
 * Any tap on the blurred backdrop or outside dismisses the overlay.
 */
@Composable
fun CosmosErrorOverlay(
    visible: Boolean,
    title: String,
    subtitle: String,
    buttonText: String = "Попробовать снова",
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = true
) {
    if (!visible) return

    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            kotlinx.coroutines.delay(2200)
            isSpinning = false
        }
    }

    val cardBg = if (isDarkTheme) Color(0xFF141210) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkTheme) Color(0xFF2C2723) else Color(0xFFE5E2DC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val textSecondary = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val btnBg = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val btnText = if (isDarkTheme) Color(0xFF09090B) else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cosmos_error_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Blurred backdrop layer - tapping anywhere dismisses the overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .blur(18.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Center card with spring animation
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume taps on the card so it doesn't dismiss */ }
                .padding(24.dp)
        ) {
            // Dismiss icon in top right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1-in-1 video accurate morphing 8-dots spinner
                CosmosMorphingDotsSpinner(
                    isSpinning = isSpinning,
                    isDarkTheme = isDarkTheme,
                    dotColor = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = title,
                    color = textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = subtitle,
                    color = textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Action button (morphs dots into spinning flower wheel and executes retry)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(btnBg)
                        .bounceClick(scaleDown = 0.93f) {
                            isSpinning = true
                            onRetry()
                        }
                        .padding(horizontal = 30.dp, vertical = 13.dp)
                        .testTag("cosmos_overlay_retry_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        color = btnText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

/**
 * Cosmos App Launch Splash Screen:
 * Features undulating 8-dot snake wave, "connecting" text, and bottom architectural "VELLOR" branding.
 * If offline, dots smoothly scatter into the "No internet connection" dialog as drawn by the user.
 */
@Composable
fun CosmosAppSplashScreen(
    visible: Boolean,
    isDarkTheme: Boolean = true,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onFinished: () -> Unit
) {
    if (!visible) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val isRu = currentLanguage == AppLanguage.RUSSIAN
    val bgColor = if (isDarkTheme) Color(0xFF09090B) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val textSecondary = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)

    var hasInternet by remember { mutableStateOf(true) }
    var isChecking by remember { mutableStateOf(true) }
    var retryTrigger by remember { mutableStateOf(0) }

    fun checkNet(): Boolean {
        return try {
            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val net = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(net)
            caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Exception) {
            true
        }
    }

    LaunchedEffect(retryTrigger) {
        isChecking = true
        kotlinx.coroutines.delay(350)
        val ok = checkNet()
        hasInternet = ok
        isChecking = false
        if (ok) {
            kotlinx.coroutines.delay(1800)
            onFinished()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snake_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "phase"
    )

    // Scatter animation progress for offline state
    val scatterProgress = remember { Animatable(0f) }
    LaunchedEffect(hasInternet) {
        if (!hasInternet) {
            scatterProgress.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scatterProgress.animateTo(0f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // 1. Center: Snake wave or Scattered dots + connecting text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            // 8 dots Canvas
            Canvas(
                modifier = Modifier
                    .size(width = 220.dp, height = 90.dp)
            ) {
                val numDots = 8
                val spacing = 20.dp.toPx()
                val totalWidth = (numDots - 1) * spacing
                val startX = (size.width - totalWidth) / 2f
                val centerY = size.height / 2f
                val dotRadius = 4.5.dp.toPx()
                val tScatter = scatterProgress.value

                // Scatter offsets matching handwritten diagram
                val scatterOffsets = listOf(
                    Offset(-42f, -32f),
                    Offset(-22f, 28f),
                    Offset(-10f, -40f),
                    Offset(14f, 34f),
                    Offset(32f, -24f),
                    Offset(48f, 26f),
                    Offset(62f, -18f),
                    Offset(78f, 15f)
                )

                for (i in 0 until numDots) {
                    val angle = wavePhase + i * 0.72f
                    val waveY = (sin(angle.toDouble()) * 11.dp.toPx()).toFloat()
                    val waveX = (cos(angle.toDouble()) * 3.dp.toPx()).toFloat()

                    val basePos = Offset(startX + i * spacing + waveX, centerY + waveY)
                    val scatterPos = basePos + (scatterOffsets.getOrElse(i) { Offset(0f, 0f) } * 1.6f)

                    val currentPos = Offset(
                        x = basePos.x + (scatterPos.x - basePos.x) * tScatter,
                        y = basePos.y + (scatterPos.y - basePos.y) * tScatter
                    )

                    val currentRadius = if (tScatter > 0.5f) {
                        dotRadius * (1f + (i % 3) * 0.15f)
                    } else {
                        (dotRadius * (0.9f + 0.2f * sin(angle.toDouble()))).toFloat()
                    }

                    drawCircle(
                        color = textPrimary,
                        radius = currentRadius,
                        center = currentPos
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-dot text "connecting"
            AnimatedVisibility(
                visible = hasInternet,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "connecting",
                    color = textSecondary.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        }

        // 2. Offline Dialog Overlay when no internet
        AnimatedVisibility(
            visible = !hasInternet,
            enter = fadeIn() + scaleIn(initialScale = 0.94f),
            exit = fadeOut() + scaleOut(targetScale = 0.94f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (isDarkTheme) Color(0xFF14171A) else Color(0xFFF4F4F6))
                    .border(
                        1.dp,
                        if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE4E4E7),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CosmosMorphingDotsSpinner(
                        isSpinning = isChecking,
                        isDarkTheme = isDarkTheme,
                        dotColor = textPrimary
                    )

                    // Title
                    Text(
                        text = if (isRu) "No internet connection" else "No internet connection",
                        color = textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = if (isRu) "Reconnect to a stable network to continue" else "Reconnect to a stable network to continue",
                        color = textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // "Try again" black pill button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                            .clickable {
                                retryTrigger++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRu) "Try again" else "Try again",
                            color = if (isDarkTheme) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
