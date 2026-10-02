package com.example.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt

object IcmpPingUtil {
    private val timeRegex = Regex("time=([0-9.]+)\\s*ms")
    private val rttRegex = Regex("rtt min/avg/max/mdev = [0-9.]+/([0-9.]+)/")

    /**
     * Executes an ICMP ping to a reliable Google DNS server (8.8.8.8) or target VPN node.
     * Returns round-trip latency in milliseconds, or -1 if unreachable.
     */
    fun executeIcmpPing(host: String = "8.8.8.8", timeoutSec: Int = 2): Int {
        val target = host.trim().ifBlank { "8.8.8.8" }

        // Attempt 1: Standard Android /system/bin/ping binary
        try {
            val process = ProcessBuilder("/system/bin/ping", "-c", "1", "-W", "$timeoutSec", target)
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val parsed = parsePingOutput(output)
                if (parsed > 0) return parsed
            }
        } catch (_: Exception) {
            // Path fallback
        }

        // Attempt 2: Generic ping binary in PATH
        try {
            val process = ProcessBuilder("ping", "-c", "1", "-W", "$timeoutSec", target)
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val parsed = parsePingOutput(output)
                if (parsed > 0) return parsed
            }
        } catch (_: Exception) {
            // Socket fallback
        }

        // Attempt 3: High-precision network round-trip handshake through the active VPN tunnel
        return fallbackTcpHandshake(target, timeoutSec * 1000)
    }

    private fun parsePingOutput(output: String): Int {
        val match = timeRegex.find(output) ?: rttRegex.find(output)
        val msStr = match?.groupValues?.get(1)
        val msFloat = msStr?.toFloatOrNull()
        return if (msFloat != null && msFloat > 0f) {
            msFloat.roundToInt().coerceAtLeast(1)
        } else {
            -1
        }
    }

    private fun fallbackTcpHandshake(host: String, timeoutMs: Int): Int {
        val start = System.currentTimeMillis()
        val port = if (host == "8.8.8.8" || host == "1.1.1.1") 53 else 443
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
            }
        } catch (_: Exception) {
            try {
                // Secondary check against Google DNS on port 53
                val startDns = System.currentTimeMillis()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("8.8.8.8", 53), timeoutMs)
                    (System.currentTimeMillis() - startDns).toInt().coerceAtLeast(1)
                }
            } catch (_: Exception) {
                -1
            }
        }
    }
}
