package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppLanguage
import com.example.model.VpnSession
import com.example.ui.components.bounceClick
import java.io.File

data class AvatarOption(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val bgColors: List<Color>
)

val AVATAR_OPTIONS = listOf(
    AvatarOption(0, "Sculpture", Icons.Filled.Person, listOf(Color(0xFF272422), Color(0xFF141210))),
    AvatarOption(1, "Enclave", Icons.Filled.Security, listOf(Color(0xFF2D2824), Color(0xFF1B1714))),
    AvatarOption(2, "Cipher Key", Icons.Filled.VpnKey, listOf(Color(0xFF332D27), Color(0xFF1E1A16))),
    AvatarOption(3, "Telemetry", Icons.Filled.Speed, listOf(Color(0xFF282522), Color(0xFF161412))),
    AvatarOption(4, "Shield", Icons.Filled.Shield, listOf(Color(0xFF302B26), Color(0xFF1A1714))),
    AvatarOption(5, "Agent", Icons.Filled.Person, listOf(Color(0xFF262220), Color(0xFF13110F)))
)

@Composable
fun ProfileScreen(
    username: String,
    userEmail: String,
    sovereignId: String,
    avatarIndex: Int = 0,
    customAvatarPath: String? = null,
    isRegistered: Boolean,
    isActivated: Boolean,
    activatedKey: String,
    activationError: String?,
    activationBusy: Boolean,
    subscription: com.example.subscription.Subscription?,
    activeServerName: String,
    sessions: List<VpnSession>,
    onActivateKey: (String) -> Unit,
    onDeactivateKey: () -> Unit,
    onRegisterUser: (String, String) -> Unit,
    onSelectAvatar: (Int) -> Unit = {},
    onPickCustomAvatar: (Uri) -> Unit = {},
    onClearHistory: () -> Unit,
    isBatterySaverEnabled: Boolean = true,
    onToggleBatterySaver: (Boolean) -> Unit = {},
    isLowPowerMode: Boolean = false,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    var keyInputText by remember { mutableStateOf("") }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showKeyInputDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickCustomAvatar(uri)
        }
    }

    val currentAvatar = AVATAR_OPTIONS.getOrElse(avatarIndex.coerceIn(0, AVATAR_OPTIONS.lastIndex)) { AVATAR_OPTIONS[0] }
    val hasCustomAvatar = customAvatarPath != null && File(customAvatarPath).exists()

    // Palette: rich warm charcoal/espresso tones (NO GREEN)
    val cardBg = if (isDarkTheme) Color(0xFF141210) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkTheme) Color(0xFF2C2723) else Color(0xFFE5E2DC)
    val innerBg = if (isDarkTheme) Color(0xFF1D1916) else Color(0xFFF7F5F2)
    val textPrimary = if (isDarkTheme) Color(0xFFEDE9E5) else Color(0xFF1A1715)
    val textSecondary = if (isDarkTheme) Color(0xFFA69E96) else Color(0xFF756E67)
    val accentWarm = if (isDarkTheme) Color(0xFFD6C7B7) else Color(0xFF4A4036)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Header
        item {
            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                Text(
                    text = if (isRu) "ПРОФИЛЬ" else "PROFILE",
                    style = MaterialTheme.typography.titleSmall,
                    color = accentWarm,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isRu) "Суверенный оператор" else "Sovereign Operator",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // 2. User Identity Card (Avatar + Nickname + UID)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Interactive Avatar (Tap to change from gallery)
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(currentAvatar.bgColors))
                                    .border(1.2.dp, cardBorder, CircleShape)
                                    .bounceClick(scaleDown = 0.92f) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasCustomAvatar) {
                                    AsyncImage(
                                        model = File(customAvatarPath!!),
                                        contentDescription = "Custom User Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else if (currentAvatar.id == 0) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_avatar_sculpture),
                                        contentDescription = "Sculpture Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = currentAvatar.icon,
                                        contentDescription = "Avatar",
                                        tint = textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Camera / Gallery badge overlay
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(textPrimary)
                                    .border(1.5.dp, cardBg, CircleShape)
                                    .bounceClick(scaleDown = 0.88f) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Pick from gallery",
                                    tint = cardBg,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = username,
                                    color = textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(innerBg)
                                        .bounceClick(scaleDown = 0.90f) { showRegisterDialog = true }
                                        .padding(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit Name",
                                        tint = textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Clean Unique UID badge (Tap to copy)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(innerBg)
                                    .bounceClick(scaleDown = 0.95f) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("UID", sovereignId))
                                        Toast.makeText(context, if (isRu) "UID скопирован" else "UID copied", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = sovereignId,
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy UID",
                                    tint = textSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Direct Pick from Gallery button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(innerBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .bounceClick(scaleDown = 0.92f) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isRu) "Галерея" else "Gallery",
                                    color = textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Developer / Creator Card ("Делал: @23") moved up directly under Profile Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isRu) "АВТОРСТВО" else "AUTHORSHIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Делал: @23",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(innerBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "@23",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accentWarm
                        )
                    }
                }
            }
        }

        // 3. Compact Key / Subscription Window (NO GREEN, Warm Dark/White)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(innerBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VpnKey,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (isRu) "КЛЮЧ ДОСТУПА" else "ACCESS KEY",
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                        }

                        // Status Pill (Monochrome)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(innerBg)
                                .border(0.8.dp, cardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isActivated) {
                                    if (isRu) "АКТИВЕН" else "ACTIVE"
                                } else {
                                    if (isRu) "НЕ АКТИВЕН" else "INACTIVE"
                                },
                                color = if (isActivated) textPrimary else textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Key info block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(innerBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isRu) "Текущий профиль" else "Active Profile",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isActivated) {
                                        if (activatedKey == "TEST") "H1Cloud Trial" else "VLESS Reality · $activeServerName"
                                    } else {
                                        if (isRu) "Требуется ввод ключа" else "Key required"
                                    },
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = if (isActivated) {
                                    if (isRu) "Сменить" else "Change"
                                } else {
                                    if (isRu) "Ввести" else "Enter"
                                },
                                color = accentWarm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .bounceClick(scaleDown = 0.94f) { showKeyInputDialog = true }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom padding to clear floating navigation bar
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Avatar Selection Dialog (with Gallery button + presets)
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = {
                Text(
                    text = if (isRu) "Аватар профиля" else "Profile Avatar",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Pick from gallery action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(innerBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                            .bounceClick(scaleDown = 0.95f) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                                showAvatarDialog = false
                            }
                            .padding(vertical = 13.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = if (isRu) "Выбрать из галереи" else "Choose from gallery",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (isRu) "Или выберите минималистичный стиль:" else "Or choose minimalist preset:",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AVATAR_OPTIONS.take(3).forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(opt.bgColors))
                                    .border(
                                        if (opt.id == avatarIndex && !hasCustomAvatar) 2.dp else 1.dp,
                                        if (opt.id == avatarIndex && !hasCustomAvatar) textPrimary else cardBorder,
                                        CircleShape
                                    )
                                    .bounceClick(scaleDown = 0.90f) {
                                        onSelectAvatar(opt.id)
                                        showAvatarDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (opt.id == 0) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_avatar_sculpture),
                                        contentDescription = "Sculpture Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = opt.icon,
                                        contentDescription = opt.title,
                                        tint = textPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AVATAR_OPTIONS.drop(3).forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(opt.bgColors))
                                    .border(
                                        if (opt.id == avatarIndex && !hasCustomAvatar) 2.dp else 1.dp,
                                        if (opt.id == avatarIndex && !hasCustomAvatar) textPrimary else cardBorder,
                                        CircleShape
                                    )
                                    .bounceClick(scaleDown = 0.90f) {
                                        onSelectAvatar(opt.id)
                                        showAvatarDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.title,
                                    tint = textPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text(if (isRu) "Закрыть" else "Close", color = textPrimary)
                }
            }
        )
    }

    // Key Input Dialog
    if (showKeyInputDialog) {
        var tempKey by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showKeyInputDialog = false },
            title = {
                Text(
                    text = if (isRu) "Ввод ключа доступа" else "Access Key Input",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isRu) "Вставьте VLESS-код или ссылку подписки:" else "Paste VLESS code or subscription URL:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        placeholder = { Text("vless://...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isActivated) {
                        TextButton(
                            onClick = {
                                onDeactivateKey()
                                showKeyInputDialog = false
                            }
                        ) {
                            Text(if (isRu) "Отключить текущий ключ" else "Sign out of key", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempKey.isNotBlank()) {
                            onActivateKey(tempKey.trim())
                            showKeyInputDialog = false
                        }
                    }
                ) {
                    Text(if (isRu) "Применить" else "Apply", fontWeight = FontWeight.Bold, color = textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyInputDialog = false }) {
                    Text(if (isRu) "Отмена" else "Cancel", color = textSecondary)
                }
            }
        )
    }

    // Registration / Edit Profile Dialog
    if (showRegisterDialog) {
        var tempName by remember { mutableStateOf(username) }
        var tempEmail by remember { mutableStateOf(userEmail) }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = {
                Text(
                    text = if (isRu) "Имя оператора" else "Operator Name",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isRu) "Укажите имя профиля:" else "Enter profile moniker:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(if (isRu) "Никнейм" else "Moniker") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRegisterUser(tempName.ifBlank { "user" }, tempEmail)
                        showRegisterDialog = false
                    }
                ) {
                    Text(
                        text = if (isRu) "Сохранить" else "Save",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text(
                        text = if (isRu) "Отмена" else "Cancel",
                        color = textSecondary
                    )
                }
            }
        )
    }
}
