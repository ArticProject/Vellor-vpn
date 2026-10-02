package com.example.subscription

import android.content.Context
import android.util.Base64
import com.example.model.ServerLocation
import com.example.vpn.VlessConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.UUID

/** Public subscription access only. Never include a panel administrator token here. */
object H1Access {
    const val HOST = "de1.h1cloud.net"
    const val PORT = 25557
    const val TRIAL_CODE = "TEST"
    const val TRIAL_ID = "e1b667c1-2438-471a-b1cd-9ba90d557e9d"
    const val FINLAND_CODE = "FINLAND"

    val FINLAND_SERVER = ServerLocation(
        id = "finland_helsinki",
        country = "Финляндия",
        countryCode = "FI",
        city = "Хельсинки",
        cityCode = "HEL",
        flagEmoji = "🇫🇮",
        pingMs = 28,
        loadPercent = 14,
        ipAddress = "78.17.187.7",
        vlessUrl = "vless://6a294168-a8f8-4be7-aa00-52ef37dfa8bc@78.17.187.7:48021?type=tcp&security=reality&pbk=lFEZaA1TkoZIw4KPwlFGmowr12hjZGLo3Fmxr5BNxWg&fp=chrome&sni=www.apple.com&sid=337f5c2742305c7a&spx=%2F#Finland%20%F0%9F%87%AB%F0%9F%87%AE",
        isLiveServer = true,
        isP2p = true,
        isStreaming = true,
        isStealth = true,
        isFavorite = true
    )

    val GERMANY_SERVER = ServerLocation(
        id = "germany_frankfurt",
        country = "Германия",
        countryCode = "DE",
        city = "Франкфурт",
        cityCode = "FRA",
        flagEmoji = "🇩🇪",
        pingMs = 38,
        loadPercent = 24,
        ipAddress = "de1.h1cloud.net",
        vlessUrl = "vless://e1b667c1-2438-471a-b1cd-9ba90d557e9d@de1.h1cloud.net:25562?type=tcp&security=reality&sni=www.samsung.com&fp=android&pbk=IaWM7egEriDsIBixWjUN1i5FWBpOhVfVRBa2edgR9HI&sid=3758385544d9dc53&spx=%2F&encryption=none#Germany%20%F0%9F%87%A9%F0%9F%87%AA",
        isLiveServer = true,
        isP2p = true,
        isStreaming = true,
        isStealth = true,
        isFavorite = false
    )

    val POLAND_SERVER = ServerLocation(
        id = "poland_warsaw",
        country = "Польша",
        countryCode = "PL",
        city = "Варшава",
        cityCode = "WAW",
        flagEmoji = "🇵🇱",
        pingMs = 32,
        loadPercent = 18,
        ipAddress = "pl4.h1cloud.net",
        vlessUrl = "vless://dfe208e8-5ab5-42f2-a962-cfb56e5f092a@pl4.h1cloud.net:25435?type=tcp&security=reality&sni=www.samsung.com&fp=android&pbk=mMc2xFftyOh4hvo8Tojv6CzmTLz8t1TWCI6AcfUmoFk&sid=0e5e881c33a9b0d1&spx=%2F&encryption=none#Poland%20%F0%9F%87%B5%F0%9F%87%B1",
        isLiveServer = true,
        isP2p = true,
        isStreaming = true,
        isStealth = true,
        isFavorite = false
    )

    val DEFAULT_SERVERS = listOf(FINLAND_SERVER, GERMANY_SERVER, POLAND_SERVER)

    private val uuidPattern = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

    fun normalize(raw: String): String {
        val input = raw.trim()
        if (input.equals(FINLAND_CODE, ignoreCase = true) || input.equals("FINLAND_DEFAULT", ignoreCase = true)) return FINLAND_CODE
        if (input.equals(TRIAL_CODE, ignoreCase = true)) return TRIAL_CODE
        if (uuidPattern.matches(input)) return input.lowercase()
        try {
            val uri = URI(input)
            require(uri.scheme == "http" && uri.host == HOST && uri.port == PORT)
            require(uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null)
            val id = uri.path.removePrefix("/sub/")
            require(uri.path == "/sub/$id" && uuidPattern.matches(id))
            return id.lowercase()
        } catch (_: Exception) {
            throw SubscriptionException("Введите личный код или ссылку подписки.")
        }
    }

    fun url(key: String): String {
        val code = normalize(key)
        return "http://$HOST:$PORT/sub/${if (code == TRIAL_CODE) TRIAL_ID else code}"
    }
}

class SubscriptionException(message: String) : Exception(message)

data class Subscription(
    val servers: List<ServerLocation>,
    val expiresAt: Long?,
    val usedBytes: Long,
    val limitBytes: Long?
) {
    fun requireUsable(nowSeconds: Long = System.currentTimeMillis() / 1000) {
        if (expiresAt != null && expiresAt <= nowSeconds) throw SubscriptionException("Срок подписки истёк. Обратитесь к владельцу VPN.")
        if (limitBytes != null && usedBytes >= limitBytes) throw SubscriptionException("Трафик подписки закончился.")
        if (servers.isEmpty()) throw SubscriptionException("В подписке нет поддерживаемых серверов VLESS/REALITY.")
    }
}

fun interface SubscriptionProvider {
    suspend fun load(key: String, deviceId: String): Subscription
}

class H1SubscriptionClient(
    private val openConnection: (java.net.URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) : SubscriptionProvider {
    override suspend fun load(key: String, deviceId: String): Subscription = withContext(Dispatchers.IO) {
        val normalized = H1Access.normalize(key)
        if (normalized == H1Access.FINLAND_CODE) {
            return@withContext Subscription(
                servers = H1Access.DEFAULT_SERVERS,
                expiresAt = null,
                usedBytes = 0,
                limitBytes = null
            )
        }
        val url = H1Access.url(key)
        val connection = openConnection(URI(url).toURL())
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.useCaches = false
            connection.setRequestProperty("User-Agent", "Vellor/1.2")
            connection.setRequestProperty("X-HWID", deviceId)
            connection.setRequestProperty("Accept", "text/plain")
            when (connection.responseCode) {
                200 -> Unit
                403 -> throw SubscriptionException("Доступ запрещён: проверьте срок, трафик и привязку устройства в панели. Для переноса доступа владелец может сбросить устройства.")
                404 -> throw SubscriptionException("Подписка не найдена. Проверьте личный код.")
                else -> throw SubscriptionException("Панель подписок недоступна. Повторите попытку позже.")
            }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (output.size() + count > MAX_BYTES) throw SubscriptionException("Слишком большой ответ подписки.")
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            parse(bytes.toString(Charsets.UTF_8), connection.getHeaderField("Subscription-Userinfo"))
        } catch (e: SubscriptionException) {
            throw e
        } catch (_: Exception) {
            // URL, response and underlying exceptions can contain private access credentials.
            throw SubscriptionException("Не удалось проверить подписку. Проверьте интернет и повторите попытку.")
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val MAX_BYTES = 64 * 1024

        fun parse(body: String, userInfo: String?, nowSeconds: Long = System.currentTimeMillis() / 1000): Subscription {
            try {
                require(body.length <= MAX_BYTES)
                val trimmed = body.trim()
                val text = if (trimmed.startsWith("vless://")) trimmed
                    else Base64.decode(trimmed, Base64.DEFAULT).toString(Charsets.UTF_8)
                val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
                require(lines.size <= 64)
                val servers = lines.mapNotNull { line ->
                    val link = line.trim()
                    try {
                        VlessConfig.build(link)
                        val uri = URI(link)
                        val label = uri.fragment?.take(60)?.takeIf { it.isNotBlank() } ?: uri.host
                        if (label.contains("Испан", ignoreCase = true) || label.contains("Spain", ignoreCase = true)) null
                        else ServerLocation(
                            id = MessageDigest.getInstance("SHA-256").digest(link.toByteArray()).take(8).joinToString("") { "%02x".format(it) },
                            country = label, countryCode = "", city = uri.host, cityCode = "VPN", flagEmoji = "🌐",
                            pingMs = 0, loadPercent = 0, ipAddress = uri.host,
                            vlessUrl = link, isLiveServer = true, isStealth = true
                        )
                    } catch (_: IllegalArgumentException) { null }
                }.distinctBy { it.vlessUrl }
                val values = userInfo.orEmpty().split(';').filter { it.isNotBlank() }.associate { item ->
                    val parts = item.trim().split('=', limit = 2)
                    require(parts.size == 2)
                    parts[0].trim() to parts[1].trim()
                }
                fun number(name: String): Long? = values[name]?.let { raw -> raw.toLong().also { require(it >= 0) } }
                val upload = number("upload") ?: 0L
                val download = number("download") ?: 0L
                require(upload <= Long.MAX_VALUE - download)
                return Subscription(servers, number("expire")?.takeIf { it > 0 }, upload + download,
                    number("total")?.takeIf { it > 0 }).also { it.requireUsable(nowSeconds) }
            } catch (e: SubscriptionException) {
                throw e
            } catch (_: Exception) {
                throw SubscriptionException("Панель вернула неподдерживаемый формат подписки.")
            }
        }
    }
}

/** Installation identity and access code must not migrate to another phone via Android backup. */
class SubscriptionStorage(context: Context) {
    private val prefs = context.getSharedPreferences("vellor_access", Context.MODE_PRIVATE)
    val deviceId: String = synchronized(SubscriptionStorage::class.java) {
        val file = java.io.File(context.noBackupFilesDir, "vellor-device-id")
        val saved = if (file.exists()) file.readText().trim() else ""
        if (runCatching { UUID.fromString(saved).toString() == saved }.getOrDefault(false)) saved
        else UUID.randomUUID().toString().also { file.writeText(it) }
    }
    var selectedServerId: String
        get() = prefs.getString("server_id", "").orEmpty()
        set(value) { prefs.edit().putString("server_id", value).apply() }
    var key: String
        get() = prefs.getString("key", "").orEmpty()
        set(value) { prefs.edit().putString("key", value).apply() }
}
