package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.VellorApp
import com.example.ui.theme.VellorTheme
import com.example.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VpnViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not */ }

    private val vpnLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = com.example.subscription.SubscriptionStorage(this)
        if (storage.key.isBlank()) {
            storage.key = com.example.subscription.H1Access.FINLAND_CODE
            viewModel.activateKey(com.example.subscription.H1Access.FINLAND_CODE, restoring = true)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val vpnPermissionIntent by viewModel.vpnPermissionIntent.collectAsStateWithLifecycle()

            LaunchedEffect(vpnPermissionIntent) {
                vpnPermissionIntent?.let { intent ->
                    try {
                        vpnLauncher.launch(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        viewModel.onVpnPermissionFailed()
                    }
                    viewModel.onVpnPermissionHandled()
                }
            }

            VellorTheme(darkTheme = isDarkTheme) {
                VellorApp(viewModel = viewModel)
            }
        }
    }
}
