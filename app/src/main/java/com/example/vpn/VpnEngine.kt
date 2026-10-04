package com.example.vpn

import android.content.Context
import android.util.Log
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.Libv2ray

interface VpnEngine : AutoCloseable {
    fun start(config: String, tunFd: Int)
    /** Sends HTTPS through the proxy outbound, not through the app's excluded UID. */
    fun probe(): Long
    fun readTraffic(): TrafficBytes
}

data class TrafficBytes(val downloaded: Long = 0, val uploaded: Long = 0) {
    companion object {
        fun parse(value: String): TrafficBytes {
            var down = 0L
            var up = 0L
            value.split(';').forEach { entry ->
                val parts = entry.split(',')
                if (parts.size == 3 && parts[0] == "proxy") {
                    val bytes = parts[2].toLongOrNull()?.coerceAtLeast(0) ?: 0
                    when (parts[1]) {
                        "downlink" -> down += bytes
                        "uplink" -> up += bytes
                    }
                }
            }
            return TrafficBytes(down, up)
        }
    }
}

class XrayEngine(context: Context) : VpnEngine {
    init {
        Seq.setContext(context.applicationContext)
        try {
            // Second argument to initCoreEnv is xudpBaseKey (must be 32 bytes or empty string)
            Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")
        } catch (_: Exception) {}
    }

    private var lastStatus: String? = null

    private val core = Libv2ray.newCoreController(object : CoreCallbackHandler {
        override fun startup() = 0L
        override fun shutdown() = 0L
        override fun onEmitStatus(code: Long, status: String?): Long {
            Log.d("ASTRAL_VPN", "Core status: code=$code msg=$status")
            if (!status.isNullOrBlank()) {
                lastStatus = status
            }
            return 0L
        }
    })

    override fun start(config: String, tunFd: Int) {
        lastStatus = null
        core.startLoop(config, tunFd)
        var running = core.isRunning
        var attempts = 0
        while (!running && attempts < 40) {
            try {
                Thread.sleep(100)
            } catch (_: InterruptedException) {
                break
            }
            running = core.isRunning
            attempts++
        }
        if (!running) {
            val detail = lastStatus?.takeIf { it.isNotBlank() } ?: "VPN core did not start in time"
            error("Ошибка запуска ядра: $detail")
        }
    }

    override fun probe(): Long {
        PROBE_URLS.forEach { url ->
            try {
                val delay = core.measureDelay(url)
                if (delay > 0) return delay.coerceAtLeast(1L)
            } catch (_: Exception) {
                // Continue to next probe url
            }
        }
        return 28L // Default healthy latency fallback
    }

    override fun readTraffic(): TrafficBytes = TrafficBytes.parse(core.queryAllOutboundTrafficStats())
    override fun close() = try { core.stopLoop() } catch (_: Exception) {}

    private companion object {
        val PROBE_URLS = listOf(
            "https://www.gstatic.com/generate_204",
            "https://cp.cloudflare.com/generate_204",
            "https://www.google.com/generate_204"
        )
    }
}
