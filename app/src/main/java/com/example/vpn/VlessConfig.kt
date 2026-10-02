package com.example.vpn

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.util.UUID

/** Strictly supports the TCP + REALITY profiles used by this app. No direct fallback. */
object VlessConfig {
    fun build(link: String): String {
        // Never include the input or parser exceptions in diagnostics: the URI contains credentials.
        try {
            val uri = URI(link.trim())
            require(uri.scheme == "vless" && !uri.host.isNullOrBlank())
            require(uri.port in 1..65535)
            val id = UUID.fromString(uri.userInfo).toString()
            val params = linkedMapOf<String, String>()
            uri.rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.forEach {
                val pair = it.split('=', limit = 2)
                val key = decode(pair[0])
                require(key !in params)
                params[key] = decode(pair.getOrElse(1) { "" })
            }
            require(params.keys.all { it in setOf("type", "security", "encryption", "sni", "fp", "pbk", "sid", "spx", "flow", "headerType") })
            require(params["type"] in listOf(null, "tcp", "raw"))
            require(params["security"] == "reality")
            require(params["encryption"] in listOf(null, "none"))
            require(params["headerType"] in listOf(null, "none"))
            val flow = params["flow"].orEmpty()
            require(flow in listOf("", "xtls-rprx-vision"))
            val key = params["pbk"].orEmpty()
            require(key.matches(Regex("[A-Za-z0-9_-]{43,44}=*")))
            val sid = params["sid"].orEmpty()
            require(sid.length <= 16 && sid.length % 2 == 0 && sid.matches(Regex("[0-9a-fA-F]*")))
            val sni = params["sni"].orEmpty()
            require(sni.isNotBlank() && sni.length <= 253 && sni.none { it.isWhitespace() })
            val requested = params["fp"] ?: "chrome"
            require(requested in setOf("chrome", "firefox", "safari", "ios", "android", "edge", "random", "randomized"))
            // uTLS "android" (OkHttp/Android 11) has no TLS 1.3, which REALITY requires.
            val fingerprint = if (requested == "android") "chrome" else requested
            val user = JSONObject().put("id", id).put("encryption", "none").put("flow", flow)
            val reality = JSONObject().put("serverName", sni).put("fingerprint", fingerprint)
                .put("publicKey", key).put("shortId", sid).put("spiderX", params["spx"] ?: "/")
            val outbound = JSONObject().put("tag", "proxy").put("protocol", "vless")
                .put("settings", JSONObject().put("vnext", JSONArray().put(JSONObject()
                    .put("address", uri.host).put("port", uri.port).put("users", JSONArray().put(user)))))
                .put("streamSettings", JSONObject().put("network", "tcp").put("security", "reality")
                    .put("realitySettings", reality))
            val sniffing = JSONObject()
                .put("enabled", true)
                .put("destOverride", JSONArray().put("http").put("tls").put("quic"))
                .put("routeOnly", false)

            val inbound = JSONObject()
                .put("tag", "tun")
                .put("protocol", "tun")
                .put("settings", JSONObject().put("name", "vellor").put("mtu", 1500))
                .put("sniffing", sniffing)

            val dns = JSONObject()
                .put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8").put("https://dns.google/dns-query"))

            val routing = JSONObject()
                .put("domainStrategy", "IPIfNonMatch")
                .put("rules", JSONArray().put(
                    JSONObject()
                        .put("type", "field")
                        .put("inboundTag", JSONArray().put("tun"))
                        .put("outboundTag", "proxy")
                        .put("network", "tcp,udp")
                ))

            return JSONObject()
                .put("log", JSONObject().put("loglevel", "none"))
                .put("dns", dns)
                .put("inbounds", JSONArray().put(inbound))
                .put("outbounds", JSONArray().put(outbound))
                .put("routing", routing)
                .put("stats", JSONObject())
                .put("policy", JSONObject().put("system", JSONObject()
                    .put("statsOutboundUplink", true).put("statsOutboundDownlink", true)))
                .toString()
        } catch (_: Exception) {
            throw IllegalArgumentException("Unsupported or invalid VLESS/REALITY profile")
        }
    }

    private fun decode(value: String) = URLDecoder.decode(value, "UTF-8")
}
