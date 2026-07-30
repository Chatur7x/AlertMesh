package com.alertmesh

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alertmesh.service.MeshService
import com.alertmesh.ui.emergency.EmergencyScreen
import com.alertmesh.ui.emergency.EmergencyViewModel
import com.alertmesh.ui.emergency.EmergencyViewModelFactory
import com.alertmesh.ui.terminal.TerminalScreen
import com.alertmesh.ui.terminal.TerminalViewModel
import com.alertmesh.ui.terminal.TerminalViewModelFactory
import com.alertmesh.ui.theme.AlertMeshTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as AlertMeshApp
        val container = app.container

        requestPermissions()
        startMeshService()

        setContent {
            AlertMeshTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "terminal") {
                        composable("terminal") {
                            val viewModel: TerminalViewModel = viewModel(
                                factory = TerminalViewModelFactory(container)
                            )
                            TerminalScreen(
                                viewModel = viewModel,
                                onNavigateToEmergency = { navController.navigate("emergency") }
                            )
                        }
                        composable("emergency") {
                            val viewModel: EmergencyViewModel = viewModel(
                                factory = EmergencyViewModelFactory(container)
                            )
                            EmergencyScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startMeshService() {
        val intent = Intent(this, MeshService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
