package com.example.data

import android.content.Context
import android.content.SharedPreferences

class VellorPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vellor_vpn_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BATTERY_SAVER = "battery_saver_enabled"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_SELECTED_SERVER = "selected_server_id"
        private const val KEY_AVATAR_TYPE = "avatar_type" // "preset_sculpture", "preset_geometric", "custom_uri"
        private const val KEY_AVATAR_CUSTOM_URI = "avatar_custom_uri"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    var isBatterySaverEnabled: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_SAVER, false)
        set(value) = prefs.edit().putBoolean(KEY_BATTERY_SAVER, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "ru") ?: "ru"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var selectedServerId: String
        get() = prefs.getString(KEY_SELECTED_SERVER, "fin_hel_01") ?: "fin_hel_01"
        set(value) = prefs.edit().putString(KEY_SELECTED_SERVER, value).apply()

    var avatarType: String
        get() = prefs.getString(KEY_AVATAR_TYPE, "preset_sculpture") ?: "preset_sculpture"
        set(value) = prefs.edit().putString(KEY_AVATAR_TYPE, value).apply()

    var customAvatarUri: String?
        get() = prefs.getString(KEY_AVATAR_CUSTOM_URI, null)
        set(value) = prefs.edit().putString(KEY_AVATAR_CUSTOM_URI, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Mihail U.") ?: "Mihail U."
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()
}
