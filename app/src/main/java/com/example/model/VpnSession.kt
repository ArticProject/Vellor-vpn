package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_sessions")
data class VpnSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverName: String,
    val country: String,
    val city: String,
    val flagEmoji: String,
    val protocol: String,
    val startTimeMillis: Long,
    val durationSeconds: Long,
    val bytesDownloaded: Long,
    val bytesUploaded: Long
)
