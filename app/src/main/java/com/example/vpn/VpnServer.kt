package com.example.vpn

enum class VpnProtocol(val displayName: String) {
    VLESS_REALITY("VLESS Reality"),
    WIREGUARD("WireGuard")
}

data class VpnServer(
    val id: String,
    val country: String,
    val city: String,
    val flagEmoji: String,
    val pingMs: Int,
    val protocol: VpnProtocol,
    val loadPercentage: Int,
    val ipAddress: String,
    val isRecommended: Boolean = false
) {
    val title: String get() = "$city, $country"
}

object ServerCatalog {
    val defaultServers = listOf(
        VpnServer(
            id = "fin_hel_01",
            country = "Финляндия",
            city = "Хельсинки",
            flagEmoji = "🇫🇮",
            pingMs = 28,
            protocol = VpnProtocol.VLESS_REALITY,
            loadPercentage = 24,
            ipAddress = "95.217.34.112",
            isRecommended = true
        ),
        VpnServer(
            id = "de_fra_01",
            country = "Германия",
            city = "Франкфурт",
            flagEmoji = "🇩🇪",
            pingMs = 35,
            protocol = VpnProtocol.VLESS_REALITY,
            loadPercentage = 42,
            ipAddress = "159.69.88.201"
        ),
        VpnServer(
            id = "nl_ams_01",
            country = "Нидерланды",
            city = "Амстердам",
            flagEmoji = "🇳🇱",
            pingMs = 32,
            protocol = VpnProtocol.WIREGUARD,
            loadPercentage = 31,
            ipAddress = "185.193.125.44"
        ),
        VpnServer(
            id = "gb_lon_01",
            country = "Великобритания",
            city = "Лондон",
            flagEmoji = "🇬🇧",
            pingMs = 41,
            protocol = VpnProtocol.VLESS_REALITY,
            loadPercentage = 55,
            ipAddress = "51.159.22.9"
        ),
        VpnServer(
            id = "jp_tok_01",
            country = "Япония",
            city = "Токио",
            flagEmoji = "🇯🇵",
            pingMs = 110,
            protocol = VpnProtocol.WIREGUARD,
            loadPercentage = 18,
            ipAddress = "133.130.100.5"
        ),
        VpnServer(
            id = "sg_sin_01",
            country = "Сингапур",
            city = "Сингапур",
            flagEmoji = "🇸🇬",
            pingMs = 135,
            protocol = VpnProtocol.VLESS_REALITY,
            loadPercentage = 29,
            ipAddress = "139.180.201.78"
        ),
        VpnServer(
            id = "us_nyc_01",
            country = "США",
            city = "Нью-Йорк",
            flagEmoji = "🇺🇸",
            pingMs = 86,
            protocol = VpnProtocol.VLESS_REALITY,
            loadPercentage = 68,
            ipAddress = "198.51.100.42"
        )
    )
}
