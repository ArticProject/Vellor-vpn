package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.VpnProtocol
import com.example.ui.components.bounceClick
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceInner

@Composable
fun SecurityScreen(
    currentProtocol: VpnProtocol,
    killSwitch: Boolean,
    stealth: Boolean,
    doubleHop: Boolean,
    adBlock: Boolean,
    onSelectProtocol: (VpnProtocol) -> Unit,
    onToggleKillSwitch: () -> Unit,
    onToggleStealth: () -> Unit,
    onToggleDoubleHop: () -> Unit,
    onToggleAdBlock: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    modifier: Modifier = Modifier
) {
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        item {
            Text(
                text = if (isRu) "Защита и Протоколы" else "Shield & Protocols",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isRu) "Криптографическая стойкость и защита без компромиссов"
                       else "Cryptographic integrity and zero-compromise protections",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRu) "ПРОТОКОЛ ТУННЕЛЯ" else "TUNNEL PROTOCOL",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(VpnProtocol.values().size) { index ->
            val proto = VpnProtocol.values()[index]
            val isSelected = proto == currentProtocol

            val protoBorderCol by animateColorAsState(
                targetValue = if (isSelected) {
                    if (isDarkTheme) Color.White else Color(0xFF09090B)
                } else {
                    if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA)
                },
                animationSpec = spring(stiffness = 400f),
                label = "proto_border_col"
            )
            val protoBorderW by animateDpAsState(
                targetValue = if (isSelected) 1.6.dp else 1.dp,
                animationSpec = spring(stiffness = 400f),
                label = "proto_border_w"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDarkTheme) DarkSurfaceCard else Color.White)
                    .border(
                        width = protoBorderW,
                        color = protoBorderCol,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .bounceClick(scaleDown = 0.96f) { onSelectProtocol(proto) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proto.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B))
                                    .border(0.8.dp, if (isDarkTheme) DarkSurfaceBorder else Color.Transparent, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = proto.badge,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = proto.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cipher: ${proto.cipher}",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) Color(0xFF71717A) else Color(0xFFA1A1AA)
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B))
                                .border(1.dp, if (isDarkTheme) DarkSurfaceBorder else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRu) "ПАКЕТ БЕЗОПАСНОСТИ" else "SECURITY SUITE",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                icon = Icons.Filled.Lock,
                title = if (isRu) "Аварийная блокировка (Kill Switch)" else "Hardware Kill Switch",
                description = if (isRu) "Мгновенно блокирует трафик при разрыве соединения." else "Block all non-tunnel traffic immediately if connection drops.",
                checked = killSwitch,
                isDarkTheme = isDarkTheme,
                onCheckedChange = { onToggleKillSwitch() }
            )

            SecurityToggleRow(
                icon = Icons.Filled.VisibilityOff,
                title = if (isRu) "Маскировка (Stealth Mode)" else "Stealth Mode (Obfuscation)",
                description = if (isRu) "Маскирует пакеты VPN под обычный HTTPS трафик." else "Disguise VPN packets as standard HTTPS traffic to bypass deep packet inspection.",
                checked = stealth,
                isDarkTheme = isDarkTheme,
                onCheckedChange = { onToggleStealth() }
            )

            SecurityToggleRow(
                icon = Icons.Filled.Security,
                title = if (isRu) "Двойное шифрование (Double Hop)" else "Double Hop Encryption",
                description = if (isRu) "Маршрутизация через два независимых сервера в разных юрисдикциях." else "Route traffic through two consecutive sovereign servers in different jurisdictions.",
                checked = doubleHop,
                isDarkTheme = isDarkTheme,
                onCheckedChange = { onToggleDoubleHop() }
            )

            SecurityToggleRow(
                icon = Icons.Filled.Block,
                title = if (isRu) "Фильтрация трекеров и рекламы" else "Zero-Trace Tracker Sinkhole",
                description = if (isRu) "Перехват телеметрии и вредоносных доменов на уровне DNS." else "DNS-level interception of malware, telemetry, and surveillance domains.",
                checked = adBlock,
                isDarkTheme = isDarkTheme,
                onCheckedChange = { onToggleAdBlock() }
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun SecurityToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    isDarkTheme: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDarkTheme) DarkSurfaceCard else Color.White)
            .border(
                1.dp,
                if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                RoundedCornerShape(18.dp)
            )
            .bounceClick(scaleDown = 0.98f) { onCheckedChange(!checked) }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B),
                    uncheckedThumbColor = if (isDarkTheme) Color(0xFFA1A1AA) else Color.White,
                    uncheckedTrackColor = if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA)
                )
            )
        }
    }
}
