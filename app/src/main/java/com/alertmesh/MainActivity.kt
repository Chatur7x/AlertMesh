package com.alertmesh

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.compose.animation.AnimatedContentTransitionScope
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
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        var bluetoothScan = true
        var bluetoothConnect = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothScan = permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false
            bluetoothConnect = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        }
        
        if (!fineLocation || !bluetoothScan || !bluetoothConnect) {
            Toast.makeText(this, "AlertMesh requires Location and Bluetooth permissions to form the mesh network", Toast.LENGTH_LONG).show()
        } else {
            startMeshService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        val app = application as AlertMeshApp
        val container = app.container
        val motionEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()

        requestPermissions()

        setContent {
            AlertMeshTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "terminal",
                        enterTransition = {
                            if (motionEnabled) slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(tween(180)) else EnterTransition.None
                        },
                        exitTransition = {
                            if (motionEnabled) slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(tween(150)) else ExitTransition.None
                        },
                        popEnterTransition = {
                            if (motionEnabled) slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(tween(180)) else EnterTransition.None
                        },
                        popExitTransition = {
                            if (motionEnabled) slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(tween(150)) else ExitTransition.None
                        }
                    ) {
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

    override fun onResume() {
        super.onResume()
        if (requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }) startMeshService()
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions().toTypedArray())
    }

    private fun requiredPermissions(): List<String> {
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
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        return permissions
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
