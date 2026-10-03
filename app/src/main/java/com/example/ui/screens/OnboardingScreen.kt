package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppLanguage
import com.example.ui.components.CosmosVideoExactMorphingAnimation
import com.example.ui.components.bounceClick
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * 2-Step Pristine Onboarding Flow:
 *
 * Screen 1 (Welcome):
 * - Top: "ASTRAL ∞" + Language Toggle
 * - Center: Giant bold "WELCOME" + Subtitle
 * - Bottom: Rotating Arch of Balls flowing clockwise over the "START" button
 *
 * Screen 2 (Advantages / Features):
 * - Floating Viewport Card in Cosmos.so style
 * - Top back arrow "<" + "Advantages"
 * - Center: 6 orbiting icon badges with smooth collapse to center (1.2s pause) and expansion cycle matching main (3).mp4
 * - Bottom: "Sovereign X-Ray Core" + Description
 * - "Continue" Button: disabled/gray for 3 seconds, then transitions to pure active white (no countdown numbers).
 */
@Composable
fun OnboardingScreen(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    currentUsername: String = "Mihail U.",
    avatarIndex: Int = 0,
    customAvatarPath: String? = null,
    onSelectAvatar: (Int) -> Unit = {},
    onPickCustomAvatar: (Uri) -> Unit = {},
    onSaveUsername: (String) -> Unit = {},
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }

    val isRu = currentLanguage == AppLanguage.RUSSIAN
    val bgColor = if (isDarkTheme) Color(0xFF09090B) else Color(0xFFF9F9FB)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val textSecondary = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val borderColor = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE4E4E7)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickCustomAvatar(uri)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "onboarding_steps",
            modifier = Modifier.fillMaxSize()
        ) { currentStep ->
            if (currentStep == 0) {
                // ==========================================
                // SCREEN 1: ASTRAL BACKGROUND & CENTERED BUTTON
                // ==========================================
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Ethereal Astral Background with drifting stars & constellations
                    com.example.ui.components.AstralBackground(
                        modifier = Modifier.fillMaxSize(),
                        isDarkTheme = isDarkTheme
                    )

                    // 2. Subtle architectural ASTRAL watermark
                    Text(
                        text = "ASTRAL",
                        fontSize = 86.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 16.sp,
                        color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.05f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header: ASTRAL ∞ & Language switcher
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ASTRAL ∞",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = textPrimary
                            )

                            // Compact Language pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isDarkTheme) Color(0xFF14171A) else Color(0xFFEDEDF0))
                                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RU",
                                    fontSize = 11.sp,
                                    fontWeight = if (isRu) FontWeight.Black else FontWeight.Normal,
                                    color = if (isRu) textPrimary else textSecondary,
                                    modifier = Modifier
                                        .clickable { onSelectLanguage(AppLanguage.RUSSIAN) }
                                        .padding(horizontal = 2.dp)
                                )
                                Text(
                                    text = "/",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Text(
                                    text = "EN",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isRu) FontWeight.Black else FontWeight.Normal,
                                    color = if (!isRu) textPrimary else textSecondary,
                                    modifier = Modifier
                                        .clickable { onSelectLanguage(AppLanguage.ENGLISH) }
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }

                        // Center: Giant WELCOME title & Aesthetic Subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "WELCOME",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (isRu) {
                                    "Цифровая эстетика суверенной криптографии.\nБесшумная маршрутизация персонального трафика через изолированные анклавы."
                                } else {
                                    "Handcrafted digital aesthetics with sovereign cryptography.\nSilent routing of personal traffic through isolated enclaves."
                                },
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Bottom: Conveyor arch of balls with button positioned slightly left and lower
                        ClockwiseArcStartSection(
                            isDarkTheme = isDarkTheme,
                            isRu = isRu,
                            textPrimary = textPrimary,
                            onStartClick = { step = 1 }
                        )
                    }
                }
            } else {
                // ====================================================
                // SCREEN 2: COSMOS FLOATING CARD & COLLAPSING ORBITING BADGES
                // ====================================================
                CosmosFeaturesScreen(
                    isDarkTheme = isDarkTheme,
                    isRu = isRu,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onBack = { step = 0 },
                    onComplete = onFinishOnboarding
                )
            }
        }
    }
}

/**
 * Screen 1 Bottom:
 * Arch of large circles continuously traveling clockwise over the top of the button.
 * The button is positioned slightly to the left and lower so it looks visually level and straight.
 */
@Composable
private fun ClockwiseArcStartSection(
    isDarkTheme: Boolean,
    isRu: Boolean,
    textPrimary: Color,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_ball_rotation")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_flow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw the moving conveyor arch of balls
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.94f
            val archRadiusX = 168.dp.toPx()
            val archRadiusY = 145.dp.toPx()
            val dotRadius = 24.dp.toPx()

            val startAngleDeg = 215.0
            val endAngleDeg = -35.0
            val angleSpanDeg = startAngleDeg - endAngleDeg // 250 degrees clockwise

            val numBalls = 8
            val ballSpacingFraction = 1.0 / numBalls

            for (i in 0 until numBalls) {
                val ballFrac = ((i * ballSpacingFraction) + (flowProgress * ballSpacingFraction)) % 1.0
                val currentDeg = startAngleDeg - (ballFrac * angleSpanDeg)
                val rad = Math.toRadians(currentDeg)

                val bx = centerX + (cos(rad) * archRadiusX).toFloat()
                val by = centerY - (sin(rad) * archRadiusY).toFloat()

                drawCircle(
                    color = textPrimary,
                    radius = dotRadius,
                    center = Offset(bx, by)
                )
            }
        }

        // Pill button: shifted slightly left (-10.dp) and lower (+38.dp) so it stands perfectly straight and centered
        Box(
            modifier = Modifier
                .offset(x = (-10).dp, y = 38.dp)
                .width(160.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                .bounceClick(scaleDown = 0.94f, onClick = onStartClick)
                .testTag("onboarding_start_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRu) "ПРОДОЛЖИТЬ" else "START",
                color = if (isDarkTheme) Color.Black else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

/**
 * Screen 2: Cosmos Floating Viewport Card with:
 * - Back button "<"
 * - Title "Advantages"
 * - 6 Orbiting badges with smooth collapse to center (2.0s pause without rotation) and expand back out
 * - Title "Sovereign X-Ray Core" and description
 * - "Continue" button: appears smoothly after 2 seconds, immediately white and enabled!
 */
@Composable
private fun CosmosFeaturesScreen(
    isDarkTheme: Boolean,
    isRu: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF14171A) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE4E4E7)

    // Button simply appears after 2 seconds
    var isButtonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        isButtonVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Floating Viewport Card (Cosmos.so style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(32.dp))
                .padding(20.dp)
        ) {
            // Card Header: Back Arrow & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color(0xFF1F2327) else Color(0xFFEDEDF0))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (isRu) "Преимущества" else "Advantages",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(38.dp))
            }

            // Card Center: Orbiting and collapsing 6 badges with 2.0s 8-dot flower pause (no rotation)
            CosmosVideoExactMorphingAnimation(
                isDarkTheme = isDarkTheme,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Below Card: Typography & Description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Sovereign X-Ray Core",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isRu) {
                    "Протокол VLESS REALITY нового поколения, мгновенное переключение в шторке Android, суверенные профили и аппаратное нулевое логирование."
                } else {
                    "Next-gen VLESS REALITY protocol, instant Android shade quick-toggle, sovereign profiles, and hardware-grade zero logging."
                },
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Continue Button: Appears smoothly after 2 seconds, immediately white and enabled!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isButtonVisible,
                enter = fadeIn(tween(400)) + androidx.compose.animation.scaleIn(initialScale = 0.95f, animationSpec = tween(400)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                        .bounceClick(scaleDown = 0.96f) { onComplete() }
                        .testTag("onboarding_continue_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRu) "Продолжить" else "Continue",
                        color = if (isDarkTheme) Color.Black else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
