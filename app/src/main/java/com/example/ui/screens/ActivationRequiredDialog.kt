package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.components.bounceClick
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceInner

@Composable
fun ActivationRequiredDialog(
    onDismissRequest: () -> Unit,
    onActivateKey: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    activationError: String?,
    activationBusy: Boolean,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isRu = currentLanguage == AppLanguage.RUSSIAN
    var inputKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = if (isDarkTheme) Color(0xFF141418) else Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (isRu) "Требуется активация" else "Activation Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isRu)
                        "Введите личный код или ссылку подписки. TEST открывает общий тестовый доступ со сроком и трафиком на всех."
                    else
                        "Enter your code or subscription link. TEST provides shared trial access with a shared expiry and traffic allowance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = {
                        Text(
                            text = if (isRu) "Код или ссылка подписки" else "Code or subscription link",
                            color = if (isDarkTheme) Color(0xFF52525B) else Color(0xFFA1A1AA),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.VpnKey,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color(0xFF09090B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    enabled = !activationBusy,
                    isError = activationError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF09090B),
                        unfocusedBorderColor = if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                        focusedContainerColor = if (isDarkTheme) DarkSurfaceInner else Color(0xFFF9F9FB),
                        unfocusedContainerColor = if (isDarkTheme) DarkSurfaceInner else Color(0xFFF9F9FB),
                        errorBorderColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (inputKey.isNotBlank() && !activationBusy) {
                                onActivateKey(inputKey)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_key_input")
                )

                if (activationBusy) { Text(if (isRu) "Проверяем подписку…" else "Checking subscription…") }

                if (activationError != null) {
                    Text(
                        text = activationError,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Quick test key chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isRu) "Ключ:" else "Trial key:",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6))
                            .border(0.8.dp, if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA), RoundedCornerShape(6.dp))
                            .bounceClick(scaleDown = 0.94f) {
                                inputKey = "TEST"
                                onActivateKey("TEST")
                            }
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TEST",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                    .bounceClick(scaleDown = 0.94f) {
                        keyboardController?.hide()
                        if (inputKey.isNotBlank() && !activationBusy) {
                            onActivateKey(inputKey)
                        } else {
                            onDismissRequest()
                            onNavigateToProfile()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("dialog_activate_btn")
            ) {
                Text(
                    text = if (inputKey.isNotBlank() && !activationBusy) {
                        if (isRu) "Активировать" else "Activate"
                    } else {
                        if (isRu) "В Профиль" else "Open Profile"
                    },
                    color = if (isDarkTheme) Color(0xFF09090B) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = if (isRu) "Закрыть" else "Close",
                    color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    fontSize = 13.sp
                )
            }
        }
    )
}
