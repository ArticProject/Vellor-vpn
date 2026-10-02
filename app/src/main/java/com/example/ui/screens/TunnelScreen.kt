package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.ServerLocation
import com.example.model.VpnProtocol
import com.example.model.VpnState
import com.example.ui.components.VellorFeaturedNodesSection
import com.example.ui.components.VellorHeader
import com.example.ui.components.VellorHeadlineSection
import com.example.ui.components.VellorHeroCard

@Composable
fun TunnelScreen(
    vpnState: VpnState,
    selectedServer: ServerLocation,
    protocol: VpnProtocol,
    allServers: List<ServerLocation>,
    downloadSpeedMb: Float,
    uploadSpeedMb: Float,
    durationFormatted: String,
    pingMs: Int,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleConnect: () -> Unit,
    onSelectServer: (ServerLocation) -> Unit,
    onOpenServerPicker: () -> Unit,
    onInspectServer: (ServerLocation) -> Unit,
    isBatterySaverEnabled: Boolean = true,
    onToggleBatterySaver: (Boolean) -> Unit = {},
    isLowPowerMode: Boolean = false,
    onResetOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item(key = "header") {
            VellorHeader(
                activeCityCode = selectedServer.cityCode,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                currentLanguage = currentLanguage,
                onSelectLanguage = onSelectLanguage,
                isBatterySaverEnabled = isBatterySaverEnabled,
                onToggleBatterySaver = onToggleBatterySaver,
                isLowPowerMode = isLowPowerMode,
                onResetOnboarding = onResetOnboarding
            )
        }

        item(key = "hero_card") {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                VellorHeroCard(
                    vpnState = vpnState,
                    selectedServer = selectedServer,
                    onToggleConnect = onToggleConnect,
                    pingMs = pingMs
                )
            }
        }

        item(key = "headline") {
            Spacer(modifier = Modifier.height(18.dp))
            VellorHeadlineSection(
                vpnState = vpnState,
                selectedServer = selectedServer,
                onToggleConnect = onToggleConnect,
                onInspectClick = { onInspectServer(selectedServer) },
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme
            )
        }

        item(key = "featured_nodes") {
            Spacer(modifier = Modifier.height(26.dp))
            VellorFeaturedNodesSection(
                servers = allServers,
                selectedServer = selectedServer,
                isConnected = isConnected,
                onSelectServer = onSelectServer,
                onInspectServer = onInspectServer,
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                selectedServerPing = pingMs
            )
        }
    }
}
