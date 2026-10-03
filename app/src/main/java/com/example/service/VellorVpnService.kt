package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.model.VpnState
import kotlinx.coroutines.flow.MutableStateFlow

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.vpn.VlessConfig
import com.example.vpn.VpnEngine
import com.example.vpn.XrayEngine
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

data class VpnTelemetry(
    val downloaded: Long = 0, val uploaded: Long = 0,
    val downloadMbps: Float = 0f, val uploadMbps: Float = 0f,
    val pingMs: Int = 0, val startedAt: Long = 0
)

class VellorVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var engine: VpnEngine? = null
    private var trafficTask: ScheduledFuture<*>? = null
    private val generation = AtomicLong()
    private val main = Handler(Looper.getMainLooper())
    private var currentServerName: String = "Vellor"
    private var currentServerCountry: String = ""
    private var currentPingMs: Int = 0

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_VLESS_URL = "extra_vless_url"
        const val EXTRA_SERVER_COUNTRY = "extra_server_country"
        const val CHANNEL_ID = "vellor_vpn_channel"
        const val NOTIFICATION_ID = 1001
        @Volatile
        private var worker = Executors.newSingleThreadScheduledExecutor { task ->
            Thread(task, "astral-vpn").apply { isDaemon = true }
        }

        private fun getWorker(): ScheduledExecutorService {
            var w = worker
            if (w.isShutdown || w.isTerminated) {
                synchronized(this) {
                    w = worker
                    if (w.isShutdown || w.isTerminated) {
                        w = Executors.newSingleThreadScheduledExecutor { task ->
                            Thread(task, "astral-vpn").apply { isDaemon = true }
                        }
                        worker = w
                    }
                }
            }
            return w
        }
        internal var engineFactory: (Context) -> VpnEngine = { XrayEngine(it) }
        internal val connectionState = MutableStateFlow(VpnState.DISCONNECTED)
        internal val connectionFailed = MutableStateFlow(false)
        internal val errorMessage = MutableStateFlow("")
        internal val telemetry = MutableStateFlow(VpnTelemetry())
        @Volatile private var owner: VellorVpnService? = null
        @Volatile var isRunning = false

        fun requestStop(context: Context) {
            val current = owner
            if (current != null) {
                current.stopVpn()
            } else {
                try {
                    val intent = Intent(context, VellorVpnService::class.java).apply {
                        action = ACTION_DISCONNECT
                    }
                    context.startService(intent)
                } catch (_: Exception) {
                    try {
                        androidx.core.content.ContextCompat.startForegroundService(
                            context,
                            Intent(context, VellorVpnService::class.java).apply {
                                action = ACTION_DISCONNECT
                            }
                        )
                    } catch (_: Exception) {}
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CONNECT) {
            val serverName = intent.getStringExtra(EXTRA_SERVER_NAME)
            val vlessUrl = intent.getStringExtra(EXTRA_VLESS_URL)
            val serverCountry = intent.getStringExtra(EXTRA_SERVER_COUNTRY) ?: ""
            if (vlessUrl.isNullOrBlank()) {
                val storage = com.example.subscription.SubscriptionStorage(this)
                val server = com.example.subscription.H1Access.DEFAULT_SERVERS.firstOrNull { it.id == storage.selectedServerId }
                    ?: com.example.subscription.H1Access.FINLAND_SERVER
                startVpn(server.fullName, server.country, server.vlessUrl)
            } else {
                startVpn(serverName ?: "Vellor", serverCountry, vlessUrl)
            }
        } else stopVpn()
        return START_NOT_STICKY
    }

    private fun startVpn(serverName: String, serverCountry: String, profile: String) {
        val request = generation.incrementAndGet()
        owner = this
        currentServerName = serverName
        currentServerCountry = serverCountry
        currentPingMs = 0
        connectionFailed.value = false
        errorMessage.value = ""
        connectionState.value = VpnState.CONNECTING
        try {
            createNotificationChannel()
            val notification = createNotification("ASTRAL VPN", "Подключение к $serverCountry...", 0)
            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
                } catch (_: Exception) {
                    try {
                        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } catch (_: Exception) {
                        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
                    }
                }
            } else {
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
            }
        } catch (_: Exception) {
            fail(request, "Не удалось запустить VPN. Проверьте разрешение Android.")
            return
        }
        getWorker().execute {
            releaseTunnel()
            if (!isCurrent(request)) return@execute
            try {
                val config = VlessConfig.build(profile)
                val builder = Builder().setSession("ASTRAL ($serverName)")
                    .addAddress("10.8.0.2", 24)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1")
                    .addDnsServer("8.8.8.8")
                    .setMtu(1500)
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (_: Exception) {}
                if (Build.VERSION.SDK_INT >= 29) builder.setMetered(false)
                
                var fd = try { builder.establish() } catch (e: Exception) { null }
                if (fd == null) {
                    try { Thread.sleep(120) } catch (_: Exception) {}
                    fd = try { builder.establish() } catch (e: Exception) { null }
                }
                vpnInterface = fd
                if (vpnInterface == null) {
                    fail(request, "Не удалось создать VPN интерфейс. Предоставьте разрешение VPN.")
                    return@execute
                }
                if (!isCurrent(request)) { releaseTunnel(); return@execute }
                val core = engineFactory(this).also { engine = it }
                core.start(config, vpnInterface?.fd ?: -1)
                val ping = try { core.probe() } catch (_: Exception) { 28L }
                if (!isCurrent(request)) { releaseTunnel(); return@execute }
                try { core.readTraffic() } catch (_: Exception) {}
                currentPingMs = ping.coerceAtMost(Int.MAX_VALUE.toLong()).toInt().coerceAtLeast(1)
                telemetry.value = VpnTelemetry(pingMs = currentPingMs,
                    startedAt = System.currentTimeMillis())
                isRunning = true
                connectionState.value = VpnState.CONNECTED
                main.post {
                    if (isCurrent(request)) {
                        getSystemService(NotificationManager::class.java)?.notify(
                            NOTIFICATION_ID,
                            createNotification(
                                title = "ASTRAL VPN • $currentServerName",
                                statusText = "$serverCountry • ${currentPingMs}ms",
                                pingMs = currentPingMs
                            )
                        )
                    }
                }
                var lastSample = SystemClock.elapsedRealtime()
                val sessionStartTime = System.currentTimeMillis()
                var ticks = 0
                trafficTask = getWorker().scheduleWithFixedDelay({
                    if (isCurrent(request)) {
                        try {
                            val bytes = core.readTraffic()
                            val now = SystemClock.elapsedRealtime()
                            val elapsed = (now - lastSample).coerceAtLeast(1)
                            lastSample = now
                            ticks++
                            
                            // Измеряем пинг каждые 2 секунды
                            if (ticks % 2 == 0) {
                                try {
                                    val measured = core.probe().coerceAtMost(9999).toInt()
                                    if (measured in 1..2500) {
                                        currentPingMs = measured
                                    }
                                } catch (_: Exception) {
                                    // оставляем текущий
                                }
                            }
                            
                            val elapsedSec = (System.currentTimeMillis() - sessionStartTime) / 1000
                            val mm = elapsedSec / 60
                            val ss = elapsedSec % 60
                            val durationStr = String.format("%02d:%02d", mm, ss)
                            
                            main.post {
                                if (isCurrent(request)) {
                                    getSystemService(NotificationManager::class.java)?.notify(
                                        NOTIFICATION_ID,
                                        createNotification(
                                            title = "Vellor VPN • $currentServerName",
                                            statusText = "$currentServerCountry • ${currentPingMs}ms • $durationStr",
                                            pingMs = currentPingMs
                                        )
                                    )
                                }
                            }
                            
                            val previous = telemetry.value
                            telemetry.value = previous.copy(
                                downloaded = previous.downloaded + bytes.downloaded,
                                uploaded = previous.uploaded + bytes.uploaded,
                                downloadMbps = bytes.downloaded * 8f / (elapsed * 1000f),
                                uploadMbps = bytes.uploaded * 8f / (elapsed * 1000f),
                                pingMs = currentPingMs
                            )
                        } catch (_: Exception) {
                            fail(request, "VPN остановлен из-за ошибки ядра. Подключитесь повторно.")
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS)
            } catch (e: Exception) {
                Log.e("ASTRAL_VPN", "Start VPN failed", e)
                val msg = e.message?.takeIf { it.isNotBlank() } ?: "Не удалось подключиться. Проверьте интернет и VLESS-ключ сервера."
                fail(request, msg)
            } catch (e: LinkageError) {
                Log.e("ASTRAL_VPN", "Native library error", e)
                fail(request, "Не удалось загрузить VPN-ядро для этого устройства.")
            }
        }
    }

    private fun isCurrent(request: Long) = owner === this && generation.get() == request

    private fun fail(request: Long, message: String) {
        main.post {
            if (isCurrent(request)) {
                errorMessage.value = message
                connectionFailed.value = true
                stopVpn()
            }
        }
    }

    private fun releaseTunnel() {
        trafficTask?.cancel(false)
        trafficTask = null
        val oldEngine = engine
        engine = null
        if (oldEngine != null) {
            try { oldEngine.close() } catch (_: Exception) {}
            try { Thread.sleep(100) } catch (_: Exception) {}
        }
        try { vpnInterface?.close() } catch (_: Exception) { }
        vpnInterface = null
        isRunning = false
    }

    private fun stopVpn() {
        val request = generation.incrementAndGet()
        connectionState.value = VpnState.DISCONNECTING
        getWorker().execute {
            releaseTunnel()
            main.post {
                if (generation.get() == request) {
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    telemetry.value = VpnTelemetry()
                    connectionState.value = VpnState.DISCONNECTED
                }
            }
        }
    }

    override fun onRevoke() = stopVpn()

    override fun onDestroy() {
        getWorker().execute {
            releaseTunnel()
        }
        if (owner === this) {
            owner = null
            telemetry.value = VpnTelemetry()
            connectionState.value = VpnState.DISCONNECTED
        }
        super.onDestroy()
    }

    private fun createNotification(title: String, statusText: String, pingMs: Int): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, VellorVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pingBadge = if (pingMs > 0) " • ${pingMs}ms" else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(statusText)
            .setSubText(if (pingMs > 0) "${pingMs}ms" else "Vellor")
            .setSmallIcon(R.drawable.ic_vpn_stat)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отключить", disconnectPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$statusText$pingBadge\nЗащищённый VLESS / REALITY туннель"))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Vellor VPN", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Активное подключение Vellor VPN"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
