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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.VellorAmber
import com.example.ui.theme.VellorEmerald

@Composable
fun LocationsScreen(
    servers: List<ServerLocation>,
    selectedServer: ServerLocation,
    isConnected: Boolean,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onSelectServer: (ServerLocation) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val isRu = currentLanguage == AppLanguage.RUSSIAN

    val filteredServers = remember(servers, searchQuery, selectedFilter) {
        servers.filter { server ->
            val matchesQuery = searchQuery.isBlank() ||
                server.country.contains(searchQuery, ignoreCase = true) ||
                server.city.contains(searchQuery, ignoreCase = true) ||
                server.cityCode.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "FAVORITES" -> server.isFavorite
                "P2P" -> server.isP2p
                "STREAMING" -> server.isStreaming
                "STEALTH" -> server.isStealth
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkTheme) Color.White else Color(0xFF09090B)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isRu) "Глобальные узлы" else "Global Nodes",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRu) "${servers.size} суверенных узлов онлайн" else "${servers.size} sovereign server nodes online",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (isRu) "Поиск страны, города, кода..." else "Search country, city, code...",
                        color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    unfocusedBorderColor = if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                    focusedContainerColor = if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6),
                    unfocusedContainerColor = if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6),
                    focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF09090B),
                    unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF09090B)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL", "FAVORITES", "P2P", "STREAMING")
                filters.forEach { filter ->
                    val isFilterSelected = filter == selectedFilter
                    val filterLabel = when (filter) {
                        "ALL" -> if (isRu) "ВСЕ" else "ALL"
                        "FAVORITES" -> if (isRu) "ИЗБРАННОЕ" else "FAVORITES"
                        "P2P" -> "P2P"
                        "STREAMING" -> if (isRu) "СТРИМИНГ" else "STREAMING"
                        else -> filter
                    }
                    val chipBg by animateColorAsState(
                        targetValue = if (isFilterSelected) {
                            if (isDarkTheme) DarkSurfaceBorder else Color(0xFF09090B)
                        } else {
                            if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6)
                        },
                        animationSpec = spring(stiffness = 400f),
                        label = "filter_chip_bg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                                CircleShape
                            )
                            .bounceClick(scaleDown = 0.92f) { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filterLabel,
                            color = if (isFilterSelected) Color.White else if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF09090B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        items(filteredServers, key = { it.id }) { server ->
            val isSelected = server.id == selectedServer.id
            val itemBorderCol by animateColorAsState(
                targetValue = if (isSelected) {
                    if (isDarkTheme) Color.White else Color(0xFF09090B)
                } else {
                    if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA)
                },
                animationSpec = spring(stiffness = 400f),
                label = "loc_item_border"
            )
            val itemBorderW by animateDpAsState(
                targetValue = if (isSelected) 1.6.dp else 1.dp,
                animationSpec = spring(stiffness = 400f),
                label = "loc_item_border_w"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDarkTheme) DarkSurfaceCard else Color.White)
                    .border(
                        width = itemBorderW,
                        color = itemBorderCol,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .bounceClick(scaleDown = 0.97f) { onSelectServer(server) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF09090B))
                    ) {
                        CapitalCityPhotoView(
                            server = server,
                            modifier = Modifier.fillMaxSize(),
                            darkenFactor = 0.25f,
                            showCoordinates = false
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) VellorEmerald else if (isDarkTheme) Color.White else Color(0xFF09090B))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${server.cityCode} · ${server.pingMs}ms",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                            )
                            Text(
                                text = "Load ${server.loadPercent}%",
                                fontSize = 12.sp,
                                color = if (server.loadPercent < 40) VellorEmerald else if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                            )
                        }
                    }

                    IconButton(onClick = { onToggleFavorite(server.id) }) {
                        Icon(
                            imageVector = if (server.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (server.isFavorite) VellorAmber else if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFFA1A1AA),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFF09090B))
                                .border(1.dp, if (isDarkTheme) DarkSurfaceBorder else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Active",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
