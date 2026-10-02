package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ServerLocation
import com.example.model.VpnState
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceInner
import com.example.ui.theme.VellorEmerald
import java.util.Locale

/**
 * Editorial Session Manifest Dialog modeled directly after the "SHOPPING BAG / YOUR CART"
 * bottom-sheet modal in the vellorshoping.com reference video.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionManifestDialog(
    vpnState: VpnState,
    selectedServer: ServerLocation,
    downloadSpeedMb: Float,
    uploadSpeedMb: Float,
    durationFormatted: String,
    pingMs: Int,
    onDismiss: () -> Unit,
    onToggleConnect: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(if (isDarkTheme) DarkSurfaceCard else Color.White)
                .border(
                    1.dp,
                    if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                    RoundedCornerShape(26.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                // Top Bar: Label & Close "X"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isRu) "АКТИВНЫЙ ТУННЕЛЬ" else "YOUR SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                            fontSize = 10.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRu) "Криптографический Манифест" else "Session Manifest",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Node Card with status pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) VellorEmerald else Color(0xFF71717A))
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = "${selectedServer.city}, ${selectedServer.country}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "IP: ${selectedServer.ipAddress} · ${selectedServer.cityCode}",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isConnected) VellorEmerald.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isConnected) VellorEmerald.copy(alpha = 0.4f) else (if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isConnected) (if (isRu) "В СЕТИ" else "ACTIVE") else (if (isRu) "ПАУЗА" else "IDLE"),
                                color = if (isConnected) VellorEmerald else (if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Technical breakdown table (like subtotal / total in shopping bag)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ManifestRow(
                        label = if (isRu) "Шифрование" else "Cipher Suite",
                        value = "ChaCha20-Poly1305 / Blake2s",
                        isDarkTheme = isDarkTheme
                    )
                    ManifestRow(
                        label = if (isRu) "Время в сети" else "Connection Uptime",
                        value = if (isConnected) durationFormatted else "00:00:00",
                        isDarkTheme = isDarkTheme
                    )
                    ManifestRow(
                        label = if (isRu) "Скорость загрузки" else "Downlink Speed",
                        value = String.format(Locale.US, "%.1f Mbps", if (isConnected) downloadSpeedMb else 0f),
                        isDarkTheme = isDarkTheme
                    )
                    ManifestRow(
                        label = if (isRu) "Скорость отдачи" else "Uplink Speed",
                        value = String.format(Locale.US, "%.1f Mbps", if (isConnected) uploadSpeedMb else 0f),
                        isDarkTheme = isDarkTheme
                    )
                    ManifestRow(
                        label = if (isRu) "Задержка шлюза" else "Roundtrip Ping",
                        value = "$pingMs ms",
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Zero-Log Attestation Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color.White)
                        .border(1.dp, if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VellorEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRu) "Аудит пройден: нулевое логирование IP и DNS"
                                   else "Audited & Verified: Zero IP or DNS logging",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Pill Button (like "Proceed to checkout" in the video)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B))
                        .border(1.dp, if (isDarkTheme) DarkSurfaceBorder else Color.Transparent, RoundedCornerShape(24.dp))
                        .clickable {
                            onToggleConnect()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isConnected) (if (isRu) "Отключить туннель" else "Disconnect Tunnel")
                               else (if (isRu) "Активировать туннель" else "Activate Tunnel"),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ManifestRow(label: String, value: String, isDarkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
        )
    }
}
