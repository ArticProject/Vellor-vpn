package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppTab
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard

@Composable
fun VellorSafariBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    modifier: Modifier = Modifier
) {
    val isRu = currentLanguage == AppLanguage.RUSSIAN

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(if (isDarkTheme) DarkSurfaceCard.copy(alpha = 0.95f) else Color(0xFF09090B).copy(alpha = 0.92f))
                .border(
                    1.dp,
                    if (isDarkTheme) DarkSurfaceBorder else Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = tab == currentTab
                    val icon = when (tab) {
                        AppTab.TUNNEL -> Icons.Filled.VpnKey
                        AppTab.NODES -> Icons.Filled.Dns
                        AppTab.PROFILE -> Icons.Filled.Person
                    }
                    val label = when (tab) {
                        AppTab.TUNNEL -> if (isRu) "Туннель" else "Tunnel"
                        AppTab.NODES -> if (isRu) "Узлы" else "Nodes"
                        AppTab.PROFILE -> if (isRu) "Профиль" else "Profile"
                    }

                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isDarkTheme) Color(0xFF4A4A52) else Color.White.copy(alpha = 0.22f)
                        } else Color.Transparent,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                        label = "tab_bg"
                    )

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
                        label = "tab_icon_scale"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0xFFA1A1AA),
                        animationSpec = spring(stiffness = 400f),
                        label = "tab_icon_tint"
                    )

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(tabBg)
                            .bounceClick(scaleDown = 0.92f, onClick = { onTabSelected(tab) })
                            .padding(horizontal = if (isSelected) 14.dp else 12.dp)
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(17.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(spring(stiffness = 400f)),
                                exit = shrinkHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut(spring(stiffness = 400f))
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
