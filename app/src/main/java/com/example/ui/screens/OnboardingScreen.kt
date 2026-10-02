package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppLanguage
import com.example.ui.components.bounceClick
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2-Step Interactive Onboarding Experience:
 * Step 0: Welcome, personalized name and avatar selection.
 * Step 1: Cosmos orbital badges video animation highlighting the 5 key advantages (X-ray, Quick Tile, Profiles, Design, Stealth Reality).
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
    var enteredUsername by remember { mutableStateOf(currentUsername.ifBlank { "Mihail U." }) }
    var selectedAvatarIdx by remember { mutableIntStateOf(avatarIndex) }

    val isRu = currentLanguage == AppLanguage.RUSSIAN
    val bgColor = if (isDarkTheme) Color(0xFF09090B) else Color.White
    val cardBg = if (isDarkTheme) Color(0xFF14171A) else Color(0xFFF4F4F6)
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
                // STEP 0: Welcome & Profile Personalization
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 26.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VELLOR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = textPrimary
                        )

                        // Language toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "RU",
                                fontSize = 11.sp,
                                fontWeight = if (isRu) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRu) textPrimary else textSecondary,
                                modifier = Modifier
                                    .clickable { onSelectLanguage(AppLanguage.RUSSIAN) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "/",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            Text(
                                text = "EN",
                                fontSize = 11.sp,
                                fontWeight = if (!isRu) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isRu) textPrimary else textSecondary,
                                modifier = Modifier
                                    .clickable { onSelectLanguage(AppLanguage.ENGLISH) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Center Content: Avatar & Greeting & Name Input
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title & Subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isRu) "Добро пожаловать в Vellor" else "Welcome to Vellor",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isRu) "Настройте профиль перед началом работы" else "Set up your profile before we get started",
                                fontSize = 14.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Avatar container with tap-to-change
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(cardBg)
                                    .border(2.dp, borderColor, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (customAvatarPath != null && File(customAvatarPath).exists()) {
                                    AsyncImage(
                                        model = File(customAvatarPath),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (selectedAvatarIdx == 0) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_avatar_sculpture),
                                        contentDescription = "Sculpture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = getPresetAvatarIcon(selectedAvatarIdx),
                                        contentDescription = "Preset avatar",
                                        tint = textPrimary,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            // Camera pill badge
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Pick photo",
                                    tint = if (isDarkTheme) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Presets avatar picker
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (idx in 0..4) {
                                val isSelected = idx == selectedAvatarIdx && (customAvatarPath == null || !File(customAvatarPath).exists())
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(cardBg)
                                        .border(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) textPrimary else borderColor,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedAvatarIdx = idx
                                            onSelectAvatar(idx)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (idx == 0) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_avatar_sculpture),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = getPresetAvatarIcon(idx),
                                            contentDescription = null,
                                            tint = if (isSelected) textPrimary else textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // User Name Input Field
                        OutlinedTextField(
                            value = enteredUsername,
                            onValueChange = { enteredUsername = it },
                            label = { Text(if (isRu) "Имя пользователя" else "Username") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = textPrimary,
                                unfocusedBorderColor = borderColor,
                                focusedLabelColor = textPrimary,
                                unfocusedLabelColor = textSecondary,
                                cursorColor = textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_username_input")
                        )
                    }

                    // Bottom Pill Button: "Продолжить"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(27.dp))
                            .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                            .bounceClick(scaleDown = 0.96f) {
                                onSaveUsername(enteredUsername.ifBlank { "Mihail U." })
                                step = 1
                            }
                            .testTag("onboarding_continue_step0"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRu) "Продолжить" else "Continue",
                            color = if (isDarkTheme) Color.Black else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                // ==========================================
                // STEP 1: Cosmos Orbital Video Animation
                // ==========================================
                CosmosOrbitalAdvantagesScreen(
                    isDarkTheme = isDarkTheme,
                    isRu = isRu,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    onBack = { step = 0 },
                    onComplete = {
                        onSaveUsername(enteredUsername.ifBlank { "Mihail U." })
                        onFinishOnboarding()
                    }
                )
            }
        }
    }
}

/**
 * Cosmos Video Replication:
 * 6 circular icons rotating along an orbital track.
 * Showcases advantages:
 * 1. X-ray / VLESS
 * 2. Быстрое подключение в шторке
 * 3. Персональные профили
 * 4. Дизайн Cosmos
 * 5. Stealth & Reality
 * 6. Серверы Европы
 */
@Composable
private fun CosmosOrbitalAdvantagesScreen(
    isDarkTheme: Boolean,
    isRu: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    borderColor: Color,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val advantages = remember(isRu) {
        listOf(
            AdvantageItem(
                title = if (isRu) "Шифрование X-ray / VLESS" else "X-ray / VLESS Protocol",
                desc = if (isRu) "Надёжное ядро X-ray и протокол VLESS без блокировок и замедлений" else "Cutting-edge X-ray core with unthrottled VLESS encapsulation",
                icon = Icons.Filled.Security
            ),
            AdvantageItem(
                title = if (isRu) "Быстрое управление в шторке" else "Quick Settings Shade Tile",
                desc = if (isRu) "Мгновенное подключение и отключение в один тап прямо из шторки Android" else "Instant toggle and status check from your Android notification tile",
                icon = Icons.Filled.ElectricBolt
            ),
            AdvantageItem(
                title = if (isRu) "Персональные профили" else "Personal Identity & Profiles",
                desc = if (isRu) "Кастомизация аватара, индивидуальные ключи доступа и мониторинг" else "Custom avatar, subscription controls and encrypted usage logs",
                icon = Icons.Filled.Person
            ),
            AdvantageItem(
                title = if (isRu) "Минималистичный дизайн" else "Cosmos Minimalist Aesthetic",
                desc = if (isRu) "Лаконичные формы, живая физика шариков и тёмная/светлая темы" else "Pure monochrome palette, fluid physics and tactile micro-interactions",
                icon = Icons.Filled.Tune
            ),
            AdvantageItem(
                title = if (isRu) "Маскировка Stealth & Reality" else "Stealth & Reality Camouflage",
                desc = if (isRu) "Полная маскировка трафика под обычный веб-серфинг популярных сайтов" else "Direct disguise of VPN packets into legitimate TLS browser traffic",
                icon = Icons.Filled.VisibilityOff
            )
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // Auto-advance advantages every 3.8s
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3800)
            selectedIndex = (selectedIndex + 1) % advantages.size
        }
    }

    // Continuous orbital rotation transition
    val infiniteTransition = rememberInfiniteTransition(label = "cosmos_orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing)
        ),
        label = "orbit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }

            Text(
                text = if (isRu) "Преимущества" else "Core Features",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        // Center: Orbital ring of Cosmos badges
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Orbital Badges Canvas
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background subtle dashed orbital ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.38f
                    drawCircle(
                        color = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE4E4E7),
                        radius = radius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx()
                        )
                    )
                }

                // Center logo / dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(textPrimary)
                )

                // 6 Orbiting badges
                val numBadges = 6
                val orbitRadius = 90.dp
                val badgeIcons = listOf(
                    Icons.Filled.Security,
                    Icons.Filled.ElectricBolt,
                    Icons.Filled.Person,
                    Icons.Filled.Tune,
                    Icons.Filled.VisibilityOff,
                    Icons.Filled.Public
                )

                for (i in 0 until numBadges) {
                    val currentTheta = orbitAngle + (i * 2 * PI / numBadges).toFloat()
                    val x = (cos(currentTheta) * orbitRadius.value).dp
                    val y = (sin(currentTheta) * orbitRadius.value).dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(
                                start = if (x > 0.dp) x * 2 else 0.dp,
                                top = if (y > 0.dp) y * 2 else 0.dp,
                                end = if (x < 0.dp) -x * 2 else 0.dp,
                                bottom = if (y < 0.dp) -y * 2 else 0.dp
                            )
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF1E2226) else Color(0xFF181A1D))
                            .border(
                                1.5.dp,
                                if (i == selectedIndex % numBadges) textPrimary else Color.Transparent,
                                CircleShape
                            )
                            .clickable {
                                selectedIndex = i % advantages.size
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcons[i],
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Current Advantage description
            val item = advantages[selectedIndex]
            AnimatedContent(
                targetState = item,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "advantage_text"
            ) { targetItem ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = targetItem.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = targetItem.desc,
                        fontSize = 13.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Indicators dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                advantages.indices.forEach { index ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) textPrimary else textSecondary.copy(alpha = 0.35f)
                            )
                            .clickable { selectedIndex = index }
                    )
                }
            }
        }

        // Bottom Pill Button: "Начать использование"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                .bounceClick(scaleDown = 0.96f) {
                    onComplete()
                }
                .testTag("onboarding_complete_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRu) "Начать использование" else "Continue to setup",
                color = if (isDarkTheme) Color.Black else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private data class AdvantageItem(
    val title: String,
    val desc: String,
    val icon: ImageVector
)

private fun getPresetAvatarIcon(index: Int): ImageVector {
    return when (index) {
        1 -> Icons.Filled.Face
        2 -> Icons.Filled.Person
        3 -> Icons.Filled.Lock
        4 -> Icons.Filled.Shield
        else -> Icons.Filled.AccountCircle
    }
}
