package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnProtocol
import com.example.model.VpnState
import com.example.ui.theme.VellorEmerald

@Composable
fun GlassmorphismSpeedProtocolPanel(
    vpnState: VpnState,
    protocol: VpnProtocol,
    downloadSpeedMb: Float,
    uploadSpeedMb: Float,
    durationFormatted: String,
    pingMs: Int,
    isDarkTheme: Boolean = false,
    onProtocolClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val cardBg = if (isDarkTheme) Color(0xFF18181B) else Color(0xFFF4F4F6)
    val cardBorder = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val textSecondary = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpeedMetricGlassCard(
                icon = Icons.Filled.ArrowDownward,
                label = "DOWN",
                value = if (isConnected) String.format("%.1f", downloadSpeedMb) else "0.0",
                unit = "MB/S",
                accentColor = VellorEmerald,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            SpeedMetricGlassCard(
                icon = Icons.Filled.ArrowUpward,
                label = "UP",
                value = if (isConnected) String.format("%.1f", uploadSpeedMb) else "0.0",
                unit = "MB/S",
                accentColor = Color(0xFF60A5FA),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            SpeedMetricGlassCard(
                icon = Icons.Filled.Timer,
                label = "UPTIME",
                value = if (isConnected) durationFormatted else "00:00",
                unit = "ACTIVE",
                accentColor = textSecondary,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            SpeedMetricGlassCard(
                icon = Icons.Filled.Speed,
                label = "LATENCY",
                value = if (pingMs > 0) "$pingMs" else if (isConnected) "..." else "28",
                unit = "MS",
                accentColor = if (pingMs in 1..85) VellorEmerald else if (pingMs in 86..150) Color(0xFFFBBF24) else textSecondary,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                .bounceClick(scaleDown = 0.96f, onClick = onProtocolClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TUNNEL CIPHER",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${protocol.displayName} · ${protocol.cipher}",
                        style = MaterialTheme.typography.titleSmall,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color.White else Color(0xFF09090B))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = protocol.badge,
                        color = if (isDarkTheme) Color(0xFF09090B) else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedMetricGlassCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) Color(0xFF18181B) else Color(0xFFF4F4F6)
    val cardBorder = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF09090B)
    val textSecondary = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = unit,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary
            )
        }
    }
}
