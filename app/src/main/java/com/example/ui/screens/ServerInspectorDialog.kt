package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.components.CapitalCityPhotoView
import com.example.ui.components.bounceClick
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceInner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerInspectorDialog(
    server: ServerLocation,
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onConnect: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM
) {
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkTheme) DarkSurfaceCard else Color.White)
                .border(
                    1.dp,
                    if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isRu) "ПАРАМЕТРЫ УЗЛА" else "NODE INSPECTOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "${server.city}, ${server.country}",
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

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F0F12))
                ) {
                    CapitalCityPhotoView(
                        server = server,
                        modifier = Modifier.fillMaxSize(),
                        darkenFactor = 0.25f,
                        showCoordinates = false
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${server.city.uppercase()} · ${server.countryCode}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InspectorSpecRow(
                        label = if (isRu) "Шлюз IP" else "Public Gateway IP",
                        value = server.ipAddress,
                        isDarkTheme = isDarkTheme
                    )
                    InspectorSpecRow(
                        label = if (isRu) "Задержка" else "Estimated Latency",
                        value = "${server.pingMs} ms",
                        isDarkTheme = isDarkTheme
                    )
                    InspectorSpecRow(
                        label = if (isRu) "Пропускная способность" else "Hardware Capacity",
                        value = "10 Gbps WireGuard Dedicated",
                        isDarkTheme = isDarkTheme
                    )
                    InspectorSpecRow(
                        label = if (isRu) "Загрузка сервера" else "Fleet Utilization",
                        value = "${server.loadPercent}%",
                        isDarkTheme = isDarkTheme
                    )
                    InspectorSpecRow(
                        label = "P2P BitTorrent",
                        value = if (server.isP2p) (if (isRu) "Разрешено" else "Permitted") else (if (isRu) "Ограничено" else "Restricted"),
                        isDarkTheme = isDarkTheme
                    )
                    InspectorSpecRow(
                        label = if (isRu) "Стриминг" else "Streaming Unblocker",
                        value = if (server.isStreaming) "4K Ultra HD" else "Standard",
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B))
                        .border(
                            1.dp,
                            if (isDarkTheme) DarkSurfaceBorder else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        )
                        .bounceClick(scaleDown = 0.95f) {
                            onConnect()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isConnected) (if (isRu) "Текущий активный узел" else "Currently Active Node")
                               else (if (isRu) "Подключить через ${server.city}" else "Connect Through ${server.city}"),
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
fun InspectorSpecRow(label: String, value: String, isDarkTheme: Boolean) {
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
