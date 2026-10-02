package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.VpnState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VellorQuickSettingsTileService : TileService() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var stateJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(VellorVpnService.connectionState.value)
        stateJob?.cancel()
        stateJob = scope.launch {
            VellorVpnService.connectionState.collectLatest { state ->
                updateTileState(state)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stateJob?.cancel()
    }

    override fun onClick() {
        super.onClick()
        val isRunning = VellorVpnService.isRunning || 
            VellorVpnService.connectionState.value == VpnState.CONNECTED ||
            VellorVpnService.connectionState.value == VpnState.CONNECTING

        if (isRunning) {
            VellorVpnService.requestStop(this)
            updateTileState(VpnState.DISCONNECTED)
        } else {
            // Check if Android VPN permission has been granted
            val vpnPermissionIntent = VpnService.prepare(this)
            if (vpnPermissionIntent != null) {
                // Must open the app to request VPN permission from the user
                val appIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        appIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(appIntent)
                }
            } else {
                // Permission is already granted! Connect directly!
                val intent = Intent(this, VellorVpnService::class.java).apply {
                    action = VellorVpnService.ACTION_CONNECT
                }
                ContextCompat.startForegroundService(this, intent)
                updateTileState(VpnState.CONNECTING)
            }
        }
    }

    private fun updateTileState(state: VpnState) {
        val tile = qsTile ?: return
        when (state) {
            VpnState.CONNECTED -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Vellor VPN"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Подключено"
                }
            }
            VpnState.CONNECTING -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Vellor VPN"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Подключение..."
                }
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Vellor VPN"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Отключено"
                }
            }
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_vpn_stat)
        tile.updateTile()
    }
}
