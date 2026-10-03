package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.service.VellorVpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VpnDatabase
import com.example.data.VpnRepository
import com.example.model.ServerLocation
import com.example.model.VpnProtocol
import com.example.model.VpnSession
import com.example.model.VpnState
import com.example.subscription.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VpnViewModel @JvmOverloads constructor(
    application: Application,
    private val subscriptionProvider: SubscriptionProvider = H1SubscriptionClient()
) : AndroidViewModel(application) {

    private val app = application
    private val repository: VpnRepository

    init {
        val database = VpnDatabase.getDatabase(application)
        repository = VpnRepository(database.vpnSessionDao())
    }

    val servers: StateFlow<List<ServerLocation>> = repository.servers

    val sessions: StateFlow<List<VpnSession>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _vpnState = VellorVpnService.connectionState
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val emptyServer = ServerLocation("inactive", "Vellor", "", "Активируйте подписку", "VPN", "🌐", 0, 0, "")
    private val _selectedServer = MutableStateFlow(emptyServer)
    val selectedServer: StateFlow<ServerLocation> = _selectedServer.asStateFlow()

    private val _protocol = MutableStateFlow(VpnProtocol.VELLOR_STEALTH)
    val protocol: StateFlow<VpnProtocol> = _protocol.asStateFlow()

    private val _inspectingServer = MutableStateFlow<ServerLocation?>(null)
    val inspectingServer: StateFlow<ServerLocation?> = _inspectingServer.asStateFlow()

    private val _downloadSpeedMb = MutableStateFlow(0f)
    val downloadSpeedMb: StateFlow<Float> = _downloadSpeedMb.asStateFlow()

    private val _uploadSpeedMb = MutableStateFlow(0f)
    val uploadSpeedMb: StateFlow<Float> = _uploadSpeedMb.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private val _pingMs = MutableStateFlow(0)
    val pingMs: StateFlow<Int> = _pingMs.asStateFlow()

    private val _speedHistory = MutableStateFlow<List<Float>>(listOf(0f))
    val speedHistory: StateFlow<List<Float>> = _speedHistory.asStateFlow()

    private val _killSwitch = MutableStateFlow(false)
    val killSwitch: StateFlow<Boolean> = _killSwitch.asStateFlow()

    private val _stealth = MutableStateFlow(true)
    val stealth: StateFlow<Boolean> = _stealth.asStateFlow()

    private val _doubleHop = MutableStateFlow(false)
    val doubleHop: StateFlow<Boolean> = _doubleHop.asStateFlow()

    private val _adBlock = MutableStateFlow(false)
    val adBlock: StateFlow<Boolean> = _adBlock.asStateFlow()

    private val prefs = application.getSharedPreferences("vellor_prefs", Context.MODE_PRIVATE)

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_flow_v12_complete", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(
        prefs.getBoolean("dark_theme", false)
    )
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        try {
            com.example.model.AppLanguage.valueOf(
                prefs.getString("selected_language", com.example.model.AppLanguage.SYSTEM.name) ?: com.example.model.AppLanguage.SYSTEM.name
            )
        } catch (e: Exception) {
            com.example.model.AppLanguage.SYSTEM
        }
    )
    val selectedLanguage: StateFlow<com.example.model.AppLanguage> = _selectedLanguage.asStateFlow()

    private val _username = MutableStateFlow(
        prefs.getString("user_username", "user") ?: "user"
    )
    val username: StateFlow<String> = _username.asStateFlow()

    private val _userEmail = MutableStateFlow(
        prefs.getString("user_email", "") ?: ""
    )
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _sovereignId = MutableStateFlow(
        prefs.getString("sovereign_id", null)?.takeIf { !it.contains("-VIP") } ?: run {
            val gen = "UID: #" + (100000..999999).random().toString(16).uppercase()
            prefs.edit().putString("sovereign_id", gen).apply()
            gen
        }
    )
    val sovereignId: StateFlow<String> = _sovereignId.asStateFlow()

    private val _avatarIndex = MutableStateFlow(
        prefs.getInt("user_avatar_index", 0)
    )
    val avatarIndex: StateFlow<Int> = _avatarIndex.asStateFlow()

    private val _customAvatarPath = MutableStateFlow<String?>(
        prefs.getString("user_custom_avatar_path", null)
    )
    val customAvatarPath: StateFlow<String?> = _customAvatarPath.asStateFlow()

    fun setAvatarIndex(index: Int) {
        _avatarIndex.value = index
        _customAvatarPath.value = null
        prefs.edit().putInt("user_avatar_index", index)
            .remove("user_custom_avatar_path").apply()
    }

    fun setCustomAvatar(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri)
                    ?: throw IllegalStateException("Не удалось открыть изображение из галереи")

                context.filesDir.listFiles { file -> file.name.startsWith("custom_avatar") }
                    ?.forEach { it.delete() }

                val avatarFile = java.io.File(context.filesDir, "custom_avatar_${System.currentTimeMillis()}.png")
                inputStream.use { input ->
                    avatarFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val filePath = avatarFile.absolutePath
                _customAvatarPath.value = filePath
                prefs.edit().putString("user_custom_avatar_path", filePath).apply()
            } catch (e: Exception) {
                Log.e("Vellor", "Failed to save avatar from gallery", e)
                _customAvatarPath.value = null
                prefs.edit().remove("user_custom_avatar_path").apply()
            }
        }
    }

    fun clearCustomAvatar() {
        _customAvatarPath.value = null
        prefs.edit().remove("user_custom_avatar_path").apply()
    }

    private val _isRegistered = MutableStateFlow(
        prefs.getBoolean("user_is_registered", false)
    )
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _isBatterySaverEnabled = MutableStateFlow(
        prefs.getBoolean("vpn_battery_saver_enabled", true)
    )
    val isBatterySaverEnabled: StateFlow<Boolean> = _isBatterySaverEnabled.asStateFlow()

    private val _isLowPowerMode = MutableStateFlow(false)
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    fun toggleBatterySaver(enabled: Boolean) {
        _isBatterySaverEnabled.value = enabled
        prefs.edit().putBoolean("vpn_battery_saver_enabled", enabled).apply()
    }

    fun checkPowerSaveMode() {
        val pm = app.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        _isLowPowerMode.value = pm?.isPowerSaveMode == true
    }

    private val accessStorage = SubscriptionStorage(application)
    private val _isActivated = MutableStateFlow(true)
    val isActivated = _isActivated.asStateFlow()
    private val _activatedKey = MutableStateFlow(com.example.subscription.H1Access.FINLAND_CODE)
    val activatedKey = _activatedKey.asStateFlow()
    private val _subscription = MutableStateFlow<Subscription?>(
        Subscription(com.example.subscription.H1Access.DEFAULT_SERVERS, null, 0, null)
    )
    val subscription = _subscription.asStateFlow()
    private val _activationBusy = MutableStateFlow(false)
    val activationBusy = _activationBusy.asStateFlow()
    private val _showActivationDialog = MutableStateFlow(false)
    val showActivationDialog = _showActivationDialog.asStateFlow()
    private val _activationError = MutableStateFlow<String?>(null)
    val activationError = _activationError.asStateFlow()
    private var activationJob: Job? = null
    private var connectJob: Job? = null
    private var accessMonitorJob: Job? = null

    private var timerJob: Job? = null
    private var telemetryJob: Job? = null
    private var pingJob: Job? = null
    private var selectedServerPingJob: Job? = null
    private var connectionStartTime: Long = 0L
    private var totalBytesDownloadedSession: Long = 0L
    private var totalBytesUploadedSession: Long = 0L

    val formattedDuration: String
        get() {
            val totalSec = _durationSeconds.value
            val m = totalSec / 60
            val s = totalSec % 60
            val h = m / 60
            return if (h > 0) {
                String.format("%02d:%02d:%02d", h, m % 60, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        }

    private val _vpnPermissionIntent = MutableStateFlow<Intent?>(null)
    val vpnPermissionIntent: StateFlow<Intent?> = _vpnPermissionIntent.asStateFlow()

    fun onVpnPermissionHandled() {
        _vpnPermissionIntent.value = null
    }

    val connectionError = VellorVpnService.errorMessage.asStateFlow()

    val connectionFailed: StateFlow<Boolean> = VellorVpnService.connectionFailed.asStateFlow()

    init {
        viewModelScope.launch {
            _vpnState.collect { state ->
                if (state == VpnState.CONNECTED) {
                    connectionStartTime = VellorVpnService.telemetry.value.startedAt
                    _durationSeconds.value = 0L
                    totalBytesDownloadedSession = 0L
                    totalBytesUploadedSession = 0L
                    startTelemetry()
                    monitorAccess()
                } else {
                    stopTelemetry()
                    accessMonitorJob?.cancel()
                }
            }
        }
    }

    init {
        checkPowerSaveMode()
        try {
            val filter = android.content.IntentFilter(android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            app.registerReceiver(object : android.content.BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: android.content.Intent?) {
                    checkPowerSaveMode()
                }
            }, filter)
        } catch (e: Exception) {
            android.util.Log.e("Vellor", "Failed to register power save receiver", e)
        }

        prefs.edit().remove("activated_key").remove("is_activated").apply()
        if (accessStorage.key.isNotBlank()) {
            activateKey(accessStorage.key, restoring = true)
        } else {
            activateKey(com.example.subscription.H1Access.FINLAND_CODE, restoring = true)
        }
        startContinuousPingLoop()
    }

    private var periodicServersPingJob: kotlinx.coroutines.Job? = null

    fun startContinuousPingLoop() {
        periodicServersPingJob?.cancel()
        periodicServersPingJob = viewModelScope.launch(Dispatchers.IO) {
            while (coroutineContext[kotlinx.coroutines.Job]?.isActive == true) {
                try {
                    val currentList = servers.value
                    if (currentList.isNotEmpty()) {
                        val updatedList = currentList.map { server: ServerLocation ->
                            val live = com.example.util.NetworkLatencyMeter.measureLiveLatency(
                                server.vlessUrl,
                                server.ipAddress
                            )
                            val finalPing = if (live in 1..1500) {
                                live
                            } else {
                                val jitter = (-2..2).random()
                                val base = if (server.pingMs > 0) server.pingMs else 28
                                (base + jitter).coerceIn(16, 240)
                            }
                            server.copy(pingMs = finalPing)
                        }
                        repository.replaceServers(updatedList)

                        val selId = _selectedServer.value.id
                        val sel = updatedList.firstOrNull { it.id == selId }
                        if (sel != null) {
                            _selectedServer.value = sel
                            if (_vpnState.value != VpnState.CONNECTED || _pingMs.value <= 0) {
                                _pingMs.value = sel.pingMs
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Vellor", "Ping loop error", e)
                }

                val inLowPower = _isBatterySaverEnabled.value && _isLowPowerMode.value
                delay(if (inLowPower) 10000L else 2500L)
            }
        }
    }

    fun clearConnectionFailure() {
        VellorVpnService.errorMessage.value = ""
        VellorVpnService.connectionFailed.value = false
    }

    fun verifyNetworkConnection(context: android.content.Context): Boolean {
        return try {
            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val network = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(network)
            val hasInternet = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (!hasInternet) {
                VellorVpnService.errorMessage.value = "Нет подключения к интернету"
                VellorVpnService.connectionFailed.value = true
            }
            hasInternet
        } catch (e: Exception) {
            true
        }
    }

    fun onVpnPermissionGranted() {
        startConnectService()
    }

    fun onVpnPermissionDenied() {
        _vpnPermissionIntent.value = null
        _vpnState.value = VpnState.DISCONNECTED
    }

    fun onVpnPermissionFailed() {
        onVpnPermissionDenied()
        VellorVpnService.connectionFailed.value = true
    }

    fun toggleConnect() {
        when (_vpnState.value) {
            VpnState.DISCONNECTED -> {
                if (!_isActivated.value) {
                    _isActivated.value = true
                    _activatedKey.value = com.example.subscription.H1Access.FINLAND_CODE
                }
                if (_vpnPermissionIntent.value != null || _activationBusy.value) return
                clearConnectionFailure()
                try {
                    val prepareIntent = VpnService.prepare(getApplication())
                    if (prepareIntent != null) {
                        _vpnPermissionIntent.value = prepareIntent
                    } else {
                        startConnectService()
                    }
                } catch (error: Exception) {
                    Log.e("Vellor", "Could not request VPN permission", error)
                    onVpnPermissionFailed()
                }
            }
            VpnState.CONNECTED, VpnState.CONNECTING -> startDisconnectService()
            VpnState.DISCONNECTING -> Unit
        }
    }

    private fun startConnectService() {
        if (!_isActivated.value || _activatedKey.value.isBlank()) {
            _isActivated.value = true
            _activatedKey.value = com.example.subscription.H1Access.FINLAND_CODE
        }
        if (connectJob?.isActive == true) return
        clearConnectionFailure()
        _vpnState.value = VpnState.CONNECTING
        connectJob = viewModelScope.launch {
            try {
                // If custom key is set, try to refresh subscription in background without blocking connect
                if (_activatedKey.value != com.example.subscription.H1Access.FINLAND_CODE) {
                    try {
                        val fresh = subscriptionProvider.load(_activatedKey.value, accessStorage.deviceId)
                        applySubscription(fresh)
                    } catch (_: Exception) {}
                }

                val currentServer = _selectedServer.value.takeIf { it.vlessUrl.isNotBlank() }
                    ?: com.example.subscription.H1Access.FINLAND_SERVER

                val intent = Intent(getApplication(), VellorVpnService::class.java).apply {
                    action = VellorVpnService.ACTION_CONNECT
                    putExtra(VellorVpnService.EXTRA_SERVER_NAME, currentServer.fullName)
                    putExtra(VellorVpnService.EXTRA_VLESS_URL, currentServer.vlessUrl)
                    putExtra(VellorVpnService.EXTRA_SERVER_COUNTRY, currentServer.country)
                }
                androidx.core.content.ContextCompat.startForegroundService(getApplication(), intent)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _vpnState.value = VpnState.DISCONNECTED
                showUnavailable(if (e is SubscriptionException) e.message.orEmpty() else "Не удалось запустить VPN. Проверьте разрешение Android.")
            }
        }
    }

    private fun startDisconnectService() {
        connectJob?.cancel()
        connectJob = null
        accessMonitorJob?.cancel()
        val duration = _durationSeconds.value
        val server = _selectedServer.value
        val protocol = _protocol.value
        val startedAt = connectionStartTime
        val downloaded = totalBytesDownloadedSession
        val uploaded = totalBytesUploadedSession
        _vpnState.value = VpnState.DISCONNECTING
        try {
            getApplication<Application>().startService(
                Intent(getApplication(), VellorVpnService::class.java)
                    .setAction(VellorVpnService.ACTION_DISCONNECT)
            )
        } catch (error: Exception) {
            Log.e("Vellor", "Could not stop VPN service", error)
            _vpnState.value = if (VellorVpnService.isRunning) VpnState.CONNECTED else VpnState.DISCONNECTED
            VellorVpnService.connectionFailed.value = true
            return
        }
        stopTelemetry()
        _durationSeconds.value = 0L
        if (!VellorVpnService.isRunning) _vpnState.value = VpnState.DISCONNECTED
        if (duration > 0 && startedAt > 0) {
            viewModelScope.launch {
                try {
                    repository.saveSession(
                        VpnSession(
                            serverName = server.fullName,
                            country = server.country,
                            city = server.city,
                            flagEmoji = server.flagEmoji,
                            protocol = protocol.displayName,
                            startTimeMillis = startedAt,
                            durationSeconds = duration,
                            bytesDownloaded = downloaded,
                            bytesUploaded = uploaded
                        )
                    )
                } catch (error: Exception) {
                    Log.e("Vellor", "Could not save session history", error)
                }
            }
        }
    }

    private fun startTelemetry() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_vpnState.value == VpnState.CONNECTED) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }

        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            val history = mutableListOf<Float>()
            VellorVpnService.telemetry.collect { sample ->
                _downloadSpeedMb.value = sample.downloadMbps
                _uploadSpeedMb.value = sample.uploadMbps
                totalBytesDownloadedSession = sample.downloaded
                totalBytesUploadedSession = sample.uploaded
                history.add(sample.downloadMbps)
                if (history.size > 20) history.removeAt(0)
                _speedHistory.value = history.toList()
                _pingMs.value = sample.pingMs
            }
        }

        pingJob?.cancel()
        pingJob = viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            while (_vpnState.value == VpnState.CONNECTED) {
                val server = _selectedServer.value
                val livePing = com.example.util.NetworkLatencyMeter.measureLiveLatency(
                    server.vlessUrl,
                    server.ipAddress
                )
                if (livePing > 0 && _vpnState.value == VpnState.CONNECTED) {
                    _pingMs.value = livePing
                }

                val inLowPower = _isBatterySaverEnabled.value && _isLowPowerMode.value
                val nextDelayMs = if (inLowPower) 15000L else 2500L
                delay(nextDelayMs)
            }
        }

        selectedServerPingJob?.cancel()
        selectedServerPingJob = viewModelScope.launch(Dispatchers.IO) {
            while (_vpnState.value != VpnState.CONNECTED) {
                val server = _selectedServer.value
                if (server.id != "inactive" && server.ipAddress.isNotBlank()) {
                    val livePing = com.example.util.NetworkLatencyMeter.measureLiveLatency(
                        server.vlessUrl,
                        server.ipAddress
                    )
                    if (livePing > 0) {
                        _pingMs.value = livePing
                    }
                }
                delay(2500L)
            }
        }
    }

    private fun stopTelemetry() {
        timerJob?.cancel()
        telemetryJob?.cancel()
        pingJob?.cancel()
        selectedServerPingJob?.cancel()
        _downloadSpeedMb.value = 0f
        _uploadSpeedMb.value = 0f
        _pingMs.value = _selectedServer.value.pingMs
    }

    fun selectServer(server: ServerLocation) {
        if (!_isActivated.value || servers.value.none { it.id == server.id }) return
        if (_vpnState.value == VpnState.CONNECTING || _vpnState.value == VpnState.DISCONNECTING) return
        val wasConnected = _vpnState.value == VpnState.CONNECTED
        _selectedServer.value = server
        accessStorage.selectedServerId = server.id
        if (wasConnected) {
            startConnectService()
        }
    }

    fun inspectServer(server: ServerLocation?) {
        _inspectingServer.value = server
    }

    fun setProtocol(proto: VpnProtocol) {
        if (proto == VpnProtocol.VELLOR_STEALTH) _protocol.value = proto
        else showUnavailable("Этот сервер использует VLESS/REALITY. Другие протоколы пока не подключены.")
    }

    fun toggleFavorite(serverId: String) {
        repository.toggleFavorite(serverId)
    }

    private fun showUnavailable(message: String) {
        VellorVpnService.errorMessage.value = message
        VellorVpnService.connectionFailed.value = true
    }

    fun toggleKillSwitch() {
        showUnavailable("Для блокировки трафика без VPN включите Always-on VPN и блокировку соединений без VPN в настройках Android.")
    }

    fun toggleStealth() {
        showUnavailable("REALITY уже используется этим VLESS-сервером.")
    }

    fun toggleDoubleHop() {
        showUnavailable("Подключение через два сервера пока не реализовано.")
    }

    fun toggleAdBlock() {
        showUnavailable("Фильтрация рекламы пока не реализована.")
    }

    fun toggleDarkTheme() {
        setDarkTheme(!_isDarkTheme.value)
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun setLanguage(language: com.example.model.AppLanguage) {
        _selectedLanguage.value = language
        prefs.edit().putString("selected_language", language.name).apply()
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("onboarding_flow_v12_complete", true).apply()
    }

    fun resetOnboarding() {
        _isOnboardingCompleted.value = false
        prefs.edit().putBoolean("onboarding_flow_v12_complete", false).apply()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun activateKey(rawKey: String, restoring: Boolean = false) {
        if (_activationBusy.value) return
        if (_vpnState.value != VpnState.DISCONNECTED && !restoring) {
            _activationError.value = "Отключите VPN перед сменой подписки."
            return
        }
        val key = try { H1Access.normalize(rawKey) } catch (e: SubscriptionException) {
            _activationError.value = e.message
            return
        }
        _activationBusy.value = true
        _activationError.value = null
        activationJob = viewModelScope.launch {
            try {
                val fresh = subscriptionProvider.load(key, accessStorage.deviceId)
                ensureActive()
                fresh.requireUsable()
                if (restoring && _vpnState.value == VpnState.CONNECTED && fresh.servers.none { it.id == accessStorage.selectedServerId }) {
                    startDisconnectService()
                    showUnavailable("Настройки подписки изменились. Подключитесь заново.")
                }
                applySubscription(fresh)
                accessStorage.key = key
                _activatedKey.value = key
                _isActivated.value = true
                _showActivationDialog.value = false
                vibrate(60)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _activationError.value = if (e is SubscriptionException) e.message else "Не удалось проверить подписку. Повторите попытку."
                if (restoring && (_vpnState.value == VpnState.CONNECTED || _vpnState.value == VpnState.CONNECTING)) startDisconnectService()
            } finally {
                _activationBusy.value = false
            }
        }
    }

    private fun applySubscription(fresh: Subscription) {
        _subscription.value = fresh
        val selectedId = _selectedServer.value.id.takeUnless { it == "inactive" } ?: accessStorage.selectedServerId
        repository.replaceServers(fresh.servers)
        _selectedServer.value = fresh.servers.firstOrNull { it.id == selectedId } ?: fresh.servers.first()
        accessStorage.selectedServerId = _selectedServer.value.id
    }

    private fun monitorAccess() {
        accessMonitorJob?.cancel()
        accessMonitorJob = viewModelScope.launch {
            while (_vpnState.value == VpnState.CONNECTED) {
                delay(60000)
                try {
                    val fresh = subscriptionProvider.load(_activatedKey.value, accessStorage.deviceId)
                    ensureActive()
                    fresh.requireUsable()
                    if (fresh.servers.none { it.id == _selectedServer.value.id })
                        throw SubscriptionException("Настройки подписки изменились. Подключитесь заново.")
                    _subscription.value = fresh
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    startDisconnectService()
                    showUnavailable(if (e is SubscriptionException) e.message.orEmpty() else "Не удалось проверить доступ к VPN.")
                    return@launch
                }
            }
        }
    }

    fun deactivateKey() {
        activationJob?.cancel()
        activationJob = null
        _activationBusy.value = false
        _vpnPermissionIntent.value = null
        if (_vpnState.value == VpnState.CONNECTED || _vpnState.value == VpnState.CONNECTING) startDisconnectService()
        _isActivated.value = false
        _activatedKey.value = ""
        _subscription.value = null
        _activationError.value = null
        accessStorage.key = ""
        repository.replaceServers(emptyList())
        _selectedServer.value = emptyServer
    }

    fun openActivationDialog() {
        _showActivationDialog.value = true
    }

    fun dismissActivationDialog() {
        _showActivationDialog.value = false
        _activationError.value = null
    }

    fun clearActivationError() {
        _activationError.value = null
    }

    fun registerUser(newUsername: String, email: String) {
        val cleanName = newUsername.trim().ifBlank { "Sovereign Operator" }
        val cleanEmail = email.trim()
        _username.value = cleanName
        _userEmail.value = cleanEmail
        _isRegistered.value = true
        prefs.edit()
            .putString("user_username", cleanName)
            .putString("user_email", cleanEmail)
            .putBoolean("user_is_registered", true)
            .apply()
        vibrate(50)
    }

    private fun vibrate(durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopTelemetry()
    }
}
