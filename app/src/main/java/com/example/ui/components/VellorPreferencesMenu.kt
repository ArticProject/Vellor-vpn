package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage

@Composable
fun VellorPreferencesMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    isBatterySaverEnabled: Boolean = true,
    onToggleBatterySaver: (Boolean) -> Unit = {},
    isLowPowerMode: Boolean = false,
    onResetOnboarding: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = (-16).dp, y = 8.dp),
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDarkTheme) Color(0xFF18181B) else Color.White)
            .border(
                1.dp,
                if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA),
                RoundedCornerShape(20.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        // Section: Appearance Theme
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = if (currentLanguage == AppLanguage.RUSSIAN) "ТЕМА ОФОРМЛЕНИЯ" else "APPEARANCE",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) Color(0xFF27272A) else Color(0xFFF4F4F6))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Light Mode Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (!isDarkTheme) {
                                if (isDarkTheme) Color(0xFF3F3F46) else Color.White
                            } else Color.Transparent
                        )
                        .clickable { onToggleDarkTheme(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = if (!isDarkTheme) Color(0xFF09090B) else Color(0xFF71717A),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.RUSSIAN) "Светлая" else "Light",
                            fontSize = 12.sp,
                            fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isDarkTheme) Color(0xFF09090B) else Color(0xFF71717A)
                        )
                    }
                }

                // Dark Mode Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkTheme) Color(0xFF09090B) else Color.Transparent
                        )
                        .clickable { onToggleDarkTheme(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color(0xFF71717A),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.RUSSIAN) "Тёмная" else "Dark",
                            fontSize = 12.sp,
                            fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDarkTheme) Color.White else Color(0xFF71717A)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Section: System Language
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLanguage == AppLanguage.RUSSIAN) "ЯЗЫК СИСТЕМЫ" else "SYSTEM LANGUAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
            }
        }

        AppLanguage.values().forEach { lang ->
            val isSelected = lang == currentLanguage
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = lang.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lang.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                onClick = {
                    onSelectLanguage(lang)
                    onDismissRequest()
                },
                colors = MenuDefaults.itemColors(
                    textColor = if (isDarkTheme) Color.White else Color(0xFF09090B)
                )
            )
        }

        HorizontalDivider(
            color = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Section: Battery Saver
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Power,
                        contentDescription = null,
                        tint = if (isBatterySaverEnabled) (if (isDarkTheme) Color.White else Color(0xFF09090B)) else Color(0xFF71717A),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.RUSSIAN) "Энергосбережение" else "Battery Saver",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                        Text(
                            text = if (isBatterySaverEnabled && isLowPowerMode) {
                                (if (currentLanguage == AppLanguage.RUSSIAN) "Эко-режим (15с пинг)" else "Eco active (15s ping)")
                            } else {
                                (if (currentLanguage == AppLanguage.RUSSIAN) "Снижает пинг при <20%" else "Saves ping in low power")
                            },
                            fontSize = 10.sp,
                            color = if (isBatterySaverEnabled && isLowPowerMode) Color(0xFF10B981) else (if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A))
                        )
                    }
                }

                Switch(
                    checked = isBatterySaverEnabled,
                    onCheckedChange = onToggleBatterySaver,
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        HorizontalDivider(
            color = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = if (currentLanguage == AppLanguage.RUSSIAN) "✦ Вводный тур (Onboarding)" else "✦ Introduction Tour",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                )
            },
            onClick = {
                onDismissRequest()
                onResetOnboarding()
            }
        )
    }
}
