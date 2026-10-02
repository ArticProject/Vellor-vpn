package com.example.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val subtitle: String = ""
) {
    SYSTEM("system", "System Default", "🌐", "Auto-detect · System Locale"),
    ENGLISH("en", "English", "🇺🇸", "International Sovereign Protocol"),
    RUSSIAN("ru", "Русский", "🇷🇺", "Суверенный зашифрованный шлюз"),
    JAPANESE("ja", "日本語", "🇯🇵", "東京 10G 暗号化ゲートウェイ"),
    GERMAN("de", "Deutsch", "🇩🇪", "Frankfurt Datenschutz Standard")
}
