package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ServerLocation
import com.example.model.VpnState
import com.example.ui.theme.VellorEmerald

@Composable
fun VellorHeadlineSection(
    vpnState: VpnState,
    selectedServer: ServerLocation,
    onToggleConnect: () -> Unit,
    onInspectClick: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = if (isRu) "НОВЫЙ ПРОТОКОЛ" else "NEW PROTOCOL",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Headline formatted on 3 lines strictly matching user image
        Text(
            text = if (isRu) "Повседневная приватность с\nчетким\nсилуэтом."
                   else "Everyday privacy with\na\nsharp silhouette.",
            style = MaterialTheme.typography.displayMedium,
            color = if (isDarkTheme) Color.White else Color(0xFF09090B),
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            letterSpacing = (-1.2).sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isRu) "Минималистичный суверенный туннель, направляющий трафик через ${selectedServer.city} и мировые узлы."
                   else "Minimal sovereign tunnel routing device traffic through ${selectedServer.city} and global nodes.",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
            lineHeight = 22.sp,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Two signature pill buttons with modern semi-transparent frosted background
        val connectBg = if (isDarkTheme) Color.White.copy(alpha = 0.16f) else Color(0xFF09090B).copy(alpha = 0.12f)
        val connectBorder = if (isDarkTheme) Color.White.copy(alpha = 0.35f) else Color(0xFF09090B).copy(alpha = 0.25f)
        val connectTextColor = if (isDarkTheme) Color.White else Color(0xFF09090B)

        val inspectBg = if (isDarkTheme) Color.White.copy(alpha = 0.07f) else Color(0xFF09090B).copy(alpha = 0.05f)
        val inspectBorder = if (isDarkTheme) Color.White.copy(alpha = 0.20f) else Color(0xFF09090B).copy(alpha = 0.15f)
        val inspectTextColor = if (isDarkTheme) Color.White else Color(0xFF09090B)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(connectBg)
                    .border(1.dp, connectBorder, RoundedCornerShape(24.dp))
                    .bounceClick(scaleDown = 0.94f, onClick = onToggleConnect)
                    .testTag("action_toggle_tunnel"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) VellorEmerald
                                else if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF09090B).copy(alpha = 0.7f)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) (if (isRu) "Отключить" else "Disconnect")
                               else (if (isRu) "Подключить туннель" else "Connect Tunnel"),
                        color = connectTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(inspectBg)
                    .border(
                        1.dp,
                        inspectBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .bounceClick(scaleDown = 0.94f, onClick = onInspectClick)
                    .testTag("action_inspect_node"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRu) "Инфо (${selectedServer.cityCode})" else "Inspect (${selectedServer.cityCode})",
                    color = inspectTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun VellorFeaturedNodesSection(
    servers: List<ServerLocation>,
    selectedServer: ServerLocation,
    isConnected: Boolean,
    onSelectServer: (ServerLocation) -> Unit,
    onInspectServer: (ServerLocation) -> Unit,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    isDarkTheme: Boolean = false,
    selectedServerPing: Int = 0,
    modifier: Modifier = Modifier
) {
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = if (isRu) "ИЗБРАННОЕ" else "FEATURED",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isRu) "Выбранные узлы" else "Selected nodes",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDarkTheme) Color.White else Color(0xFF09090B),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            letterSpacing = (-0.8).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isRu) "Быстрый выбор из всей сети серверов." else "A quick edit from the full network.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        val chunkedServers = servers.chunked(2)
        chunkedServers.forEach { rowServers ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowServers.forEach { server ->
                    val isSelected = server.id == selectedServer.id
                    val livePing = if (isSelected && selectedServerPing > 0) selectedServerPing else server.pingMs
                    VellorNodeCard(
                        server = server,
                        isSelected = isSelected,
                        isConnected = isConnected && isSelected,
                        livePing = livePing,
                        onConnect = { onSelectServer(server) },
                        onInspect = { onInspectServer(server) },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowServers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun VellorNodeCard(
    server: ServerLocation,
    isSelected: Boolean,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onInspect: () -> Unit,
    isDarkTheme: Boolean = false,
    livePing: Int = 0,
    modifier: Modifier = Modifier
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDarkTheme) Color.White else Color(0xFF09090B)
        } else {
            if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE5E5EA)
        },
        animationSpec = spring(stiffness = 400f),
        label = "node_card_border_col"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.6.dp else 1.dp,
        animationSpec = spring(stiffness = 400f),
        label = "node_card_border_w"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDarkTheme) Color(0xFF141416) else Color.White)
            .border(
                width = animatedBorderWidth,
                color = animatedBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .clip(RoundedCornerShape(14.dp))
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
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(0.8.dp, Color(0xFFE5E5EA), CircleShape)
                    .bounceClick(scaleDown = 0.90f, onClick = onInspect)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Inspect",
                    color = Color(0xFF09090B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${server.city.uppercase()} · ${server.countryCode}",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = server.city,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = server.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected && isConnected) VellorEmerald.copy(alpha = 0.15f)
                        else if (isDarkTheme) Color(0xFF27272A)
                        else Color(0xFFF4F4F6)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected && isConnected) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(VellorEmerald)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    val pingVal = if (livePing > 0) livePing else server.pingMs
                    Text(
                        text = "${pingVal}ms",
                        color = if (isSelected && isConnected) VellorEmerald
                                else if (isDarkTheme) Color(0xFFA1A1AA)
                                else Color(0xFF71717A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val btnBg by animateColorAsState(
            targetValue = if (isSelected) {
                if (isDarkTheme) Color.White else Color(0xFF09090B)
            } else {
                if (isDarkTheme) Color(0xFF27272A) else Color(0xFFF4F4F6)
            },
            animationSpec = spring(stiffness = 400f),
            label = "node_btn_bg"
        )
        val btnTextCol by animateColorAsState(
            targetValue = if (isSelected) {
                if (isDarkTheme) Color(0xFF09090B) else Color.White
            } else {
                if (isDarkTheme) Color.White else Color(0xFF09090B)
            },
            animationSpec = spring(stiffness = 400f),
            label = "node_btn_txt"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(btnBg)
                .bounceClick(scaleDown = 0.94f, onClick = onConnect),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSelected && isConnected) "Connected" else if (isSelected) "Selected" else "Route Node",
                color = btnTextCol,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
