package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Shield
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
import com.example.model.VpnSession
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceInner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(
    sessions: List<VpnSession>,
    onClearHistory: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    modifier: Modifier = Modifier
) {
    val totalSeconds = sessions.sumOf { it.durationSeconds }
    val totalHours = totalSeconds / 3600f
    val totalBytes = sessions.sumOf { it.bytesDownloaded + it.bytesUploaded }
    val totalGb = totalBytes / (1024f * 1024f * 1024f)

    val isRu = currentLanguage == AppLanguage.RUSSIAN

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRu) "История сессий" else "Session History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRu) "Локальные записи прошлых подключений" else "Local cryptographic records of past tunnels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                }

                if (sessions.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6))
                        .border(
                            1.dp,
                            if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (isRu) "ВРЕМЯ В СЕТИ" else "TOTAL TUNNEL TIME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, if (isRu) "%.1f ч" else "%.1f hrs", totalHours),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                        Text(
                            text = if (isRu) "${sessions.size} сессий" else "${sessions.size} connections",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) Color(0xFF71717A) else Color(0xFFA1A1AA)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6))
                        .border(
                            1.dp,
                            if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE5E5EA),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (isRu) "ЗАШИФРОВАНО" else "DATA ENCRYPTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, if (isRu) "%.2f ГБ" else "%.2f GB", totalGb),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B)
                        )
                        Text(
                            text = if (isRu) "Без логов" else "Zero logs stored",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) Color(0xFF71717A) else Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRu) "ПРОШЛЫЕ СЕССИИ" else "PAST SESSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) DarkSurfaceCard else Color(0xFFF4F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFFA1A1AA),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isRu) "Нет записанных сессий" else "No recorded sessions",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRu) "Подключитесь к узлу для начала зашифрованного трафика."
                                   else "Connect to a node to begin encrypted routing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    .format(Date(session.startTimeMillis))

                val durationMin = session.durationSeconds / 60
                val durationSec = session.durationSeconds % 60
                val durationDisplay = if (durationMin > 0) "${durationMin}m ${durationSec}s" else "${durationSec}s"

                val totalMb = (session.bytesDownloaded + session.bytesUploaded) / (1024f * 1024f)

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
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${session.city}, ${session.country}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) DarkSurfaceInner else Color(0xFFF4F4F6))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = session.protocol,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) Color.White else Color(0xFF71717A)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color(0xFF71717A) else Color(0xFFA1A1AA)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = durationDisplay,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isDarkTheme) Color.White else Color(0xFF09090B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f MB", totalMb),
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color(0xFFA1A1AA) else Color(0xFF71717A)
                            )
                        }
                    }
                }
            }
        }
    }
}
