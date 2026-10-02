package com.example.util

import android.os.SystemClock
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.URL

object NetworkLatencyMeter {

    /**
     * Measures live, genuine round-trip latency (in ms) to the active VPN server endpoint
     * or Google/Cloudflare edge reference.
     */
    fun measureLiveLatency(vlessUrl: String, fallbackIp: String): Int {
        // 1. Direct socket connection to the active VPN server endpoint
        val endpoint = parseEndpoint(vlessUrl, fallbackIp)
        if (endpoint != null) {
            val ping = measureSocketTime(endpoint.first, endpoint.second, timeoutMs = 1500)
            if (ping > 0) return ping
        }

        // 2. HTTP 204 probe to Google captive portal reference via active connection
        val gPing = measureHttp204("https://www.gstatic.com/generate_204", timeoutMs = 1800)
        if (gPing > 0) return gPing

        val cfPing = measureHttp204("https://cp.cloudflare.com/generate_204", timeoutMs = 1800)
        if (cfPing > 0) return cfPing

        // 3. DNS TCP connection to 1.1.1.1 or 8.8.8.8
        val dnsPing = measureSocketTime("1.1.1.1", 53, timeoutMs = 1500)
        if (dnsPing > 0) return dnsPing

        val gDnsPing = measureSocketTime("8.8.8.8", 53, timeoutMs = 1500)
        if (gDnsPing > 0) return gDnsPing

        // 4. ICMP ping fallback
        return IcmpPingUtil.executeIcmpPing(fallbackIp.ifBlank { "8.8.8.8" }, timeoutSec = 2)
    }

    private fun parseEndpoint(vlessUrl: String, fallbackIp: String): Pair<String, Int>? {
        if (vlessUrl.isBlank()) {
            return if (fallbackIp.isNotBlank()) Pair(fallbackIp, 443) else null
        }
        return try {
            val uri = URI(vlessUrl)
            val host = uri.host ?: fallbackIp
            val port = if (uri.port > 0) uri.port else 443
            Pair(host, port)
        } catch (_: Exception) {
            if (fallbackIp.isNotBlank()) Pair(fallbackIp, 443) else null
        }
    }

    private fun measureSocketTime(host: String, port: Int, timeoutMs: Int): Int {
        val start = System.currentTimeMillis()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                val elapsed = (System.currentTimeMillis() - start).toInt()
                elapsed.coerceAtLeast(1)
            }
        } catch (_: Exception) {
            -1
        }
    }

    private fun measureHttp204(targetUrl: String, timeoutMs: Int): Int {
        val start = System.currentTimeMillis()
        return try {
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = false
                useCaches = false
                setRequestProperty("Connection", "close")
            }
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            if (code in 200..399) {
                (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
            } else {
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }
}
