package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.AppTab
import com.example.model.VpnState
import com.example.ui.components.MeshNetworkBackground
import com.example.ui.components.VellorSafariBar
import com.example.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

@Composable
fun VellorApp(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var isAppSplashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.verifyNetworkConnection(context)
    }

    if (isAppSplashVisible) {
        com.example.ui.components.CosmosAppSplashScreen(
            visible = true,
            isDarkTheme = isDarkTheme,
            currentLanguage = selectedLanguage,
            onFinished = {
                isAppSplashVisible = false
            }
        )
        return
    }

    val username by viewModel.username.collectAsStateWithLifecycle()
    val avatarIndex by viewModel.avatarIndex.collectAsStateWithLifecycle()
    val customAvatarPath by viewModel.customAvatarPath.collectAsStateWithLifecycle()

    // If onboarding is not completed yet, show the initial interactive cards flow
    if (!isOnboardingCompleted) {
        OnboardingScreen(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = { viewModel.setDarkTheme(it) },
            currentLanguage = selectedLanguage,
            onSelectLanguage = { viewModel.setLanguage(it) },
            currentUsername = username,
            avatarIndex = avatarIndex,
            customAvatarPath = customAvatarPath,
            onSelectAvatar = { viewModel.selectAvatar(it) },
            onPickCustomAvatar = { viewModel.pickCustomAvatar(it) },
            onSaveUsername = { viewModel.registerUser(it, "") },
            onFinishOnboarding = { viewModel.completeOnboarding() },
            modifier = modifier
        )
        return
    }

    // 3 Swipeable tabs: 0 = Tunnel, 1 = Nodes, 2 = Profile
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val currentTab = when (pagerState.currentPage) {
        0 -> AppTab.TUNNEL
        1 -> AppTab.NODES
        else -> AppTab.PROFILE
    }

    val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
    val connectionFailed by viewModel.connectionFailed.collectAsStateWithLifecycle()
    val connectionError by viewModel.connectionError.collectAsStateWithLifecycle()

    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val protocol by viewModel.protocol.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    val downloadSpeed by viewModel.downloadSpeedMb.collectAsStateWithLifecycle()
    val uploadSpeed by viewModel.uploadSpeedMb.collectAsStateWithLifecycle()
    val pingMs by viewModel.pingMs.collectAsStateWithLifecycle()
    val inspectingServer by viewModel.inspectingServer.collectAsStateWithLifecycle()

    val username by viewModel.username.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val sovereignId by viewModel.sovereignId.collectAsStateWithLifecycle()
    val avatarIndex by viewModel.avatarIndex.collectAsStateWithLifecycle()
    val customAvatarPath by viewModel.customAvatarPath.collectAsStateWithLifecycle()
    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
    val activatedKey by viewModel.activatedKey.collectAsStateWithLifecycle()
    val showActivationDialog by viewModel.showActivationDialog.collectAsStateWithLifecycle()
    val activationError by viewModel.activationError.collectAsStateWithLifecycle()
    val activationBusy by viewModel.activationBusy.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val isBatterySaverEnabled by viewModel.isBatterySaverEnabled.collectAsStateWithLifecycle()
    val isLowPowerMode by viewModel.isLowPowerMode.collectAsStateWithLifecycle()

    if (pagerState.currentPage != 0) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF09090B) else Color.White)
    ) {
        // Living animated floating constellation/astral mesh on background with high visibility
        MeshNetworkBackground(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // HorizontalPager enables native swipe left/right across the 3 main screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        TunnelScreen(
                            vpnState = vpnState,
                            selectedServer = selectedServer,
                            protocol = protocol,
                            allServers = servers,
                            downloadSpeedMb = downloadSpeed,
                            uploadSpeedMb = uploadSpeed,
                            durationFormatted = viewModel.formattedDuration,
                            pingMs = pingMs,
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                            currentLanguage = selectedLanguage,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            onToggleConnect = { viewModel.toggleConnect() },
                            onSelectServer = { server ->
                                viewModel.selectServer(server)
                            },
                            onOpenServerPicker = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            onInspectServer = { server -> viewModel.inspectServer(server) },
                            isBatterySaverEnabled = isBatterySaverEnabled,
                            onToggleBatterySaver = { viewModel.toggleBatterySaver(it) },
                            isLowPowerMode = isLowPowerMode,
                            onResetOnboarding = { viewModel.resetOnboarding() }
                        )
                    }
                    1 -> {
                        LocationsScreen(
                            servers = servers,
                            selectedServer = selectedServer,
                            isConnected = vpnState == VpnState.CONNECTED,
                            isDarkTheme = isDarkTheme,
                            currentLanguage = selectedLanguage,
                            onSelectServer = { server ->
                                viewModel.selectServer(server)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            onToggleFavorite = { serverId ->
                                viewModel.toggleFavorite(serverId)
                            },
                            onBack = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        )
                    }
                    2 -> {
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        ProfileScreen(
                            username = username,
                            userEmail = userEmail,
                            sovereignId = sovereignId,
                            avatarIndex = avatarIndex,
                            customAvatarPath = customAvatarPath,
                            isRegistered = isRegistered,
                            isActivated = isActivated,
                            activatedKey = activatedKey,
                            activationError = activationError,
                            activationBusy = activationBusy,
                            subscription = subscription,
                            activeServerName = selectedServer.country,
                            sessions = sessions,
                            onActivateKey = { key -> viewModel.activateKey(key) },
                            onDeactivateKey = { viewModel.deactivateKey() },
                            onRegisterUser = { name, email -> viewModel.registerUser(name, email) },
                            onSelectAvatar = { index -> viewModel.setAvatarIndex(index) },
                            onPickCustomAvatar = { uri -> viewModel.setCustomAvatar(localContext, uri) },
                            onClearHistory = { viewModel.clearHistory() },
                            isBatterySaverEnabled = isBatterySaverEnabled,
                            onToggleBatterySaver = { viewModel.toggleBatterySaver(it) },
                            isLowPowerMode = isLowPowerMode,
                            isDarkTheme = isDarkTheme,
                            currentLanguage = selectedLanguage
                        )
                    }
                }
            }

            VellorSafariBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    coroutineScope.launch {
                        val targetPage = when (tab) {
                            AppTab.TUNNEL -> 0
                            AppTab.NODES -> 1
                            AppTab.PROFILE -> 2
                        }
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                isDarkTheme = isDarkTheme,
                currentLanguage = selectedLanguage,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            inspectingServer?.let { server ->
                ServerInspectorDialog(
                    server = server,
                    isConnected = vpnState == VpnState.CONNECTED && server.id == selectedServer.id,
                    onDismiss = { viewModel.inspectServer(null) },
                    onConnect = {
                        viewModel.selectServer(server)
                        if (vpnState != VpnState.CONNECTED) {
                            viewModel.toggleConnect()
                        }
                    },
                    isDarkTheme = isDarkTheme,
                    currentLanguage = selectedLanguage
                )
            }

            if (showActivationDialog) {
                ActivationRequiredDialog(
                    onDismissRequest = { viewModel.dismissActivationDialog() },
                    onActivateKey = { key -> viewModel.activateKey(key) },
                    onNavigateToProfile = {
                        viewModel.dismissActivationDialog()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    activationError = activationError,
                    activationBusy = activationBusy,
                    isDarkTheme = isDarkTheme,
                    currentLanguage = selectedLanguage
                )
            }

            // Cosmos Error Overlay: triggers on network error / VPN error
            com.example.ui.components.CosmosErrorOverlay(
                visible = connectionFailed,
                title = if (selectedLanguage == AppLanguage.RUSSIAN) "Нет подключения к сети" else "No internet connection",
                subtitle = if (connectionError.isNotBlank()) connectionError
                    else if (selectedLanguage == AppLanguage.RUSSIAN) "Не удалось подключиться к серверу. Проверьте стабильное соединение для продолжения."
                    else "Reconnect to a stable network to continue.",
                buttonText = if (selectedLanguage == AppLanguage.RUSSIAN) "Попробовать снова" else "Try again",
                onRetry = {
                    viewModel.clearConnectionFailure()
                    viewModel.toggleConnect()
                },
                onDismiss = viewModel::clearConnectionFailure,
                isDarkTheme = isDarkTheme
            )

            // Cosmos Error Overlay: triggers when incorrect code is entered in profile
            com.example.ui.components.CosmosErrorOverlay(
                visible = activationError != null,
                title = if (selectedLanguage == AppLanguage.RUSSIAN) "Неверный код доступа" else "Invalid Access Key",
                subtitle = activationError ?: (if (selectedLanguage == AppLanguage.RUSSIAN) "Проверьте введённый ключ VLESS и повторите попытку." else "Check the entered VLESS key and try again."),
                buttonText = if (selectedLanguage == AppLanguage.RUSSIAN) "Ввести заново" else "Try again",
                onRetry = {
                    viewModel.clearActivationError()
                },
                onDismiss = {
                    viewModel.clearActivationError()
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}
