package com.disciplined.minds

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.disciplined.minds.timer.service.TimerService
import com.disciplined.minds.ui.applock.AppLockScreen
import com.disciplined.minds.ui.applock.AppLockViewModel
import com.disciplined.minds.ui.home.HomeScreen
import com.disciplined.minds.ui.home.HomeViewModel
import com.disciplined.minds.ui.permission.PermissionScreen
import com.disciplined.minds.ui.permission.PermissionViewModel
import com.disciplined.minds.ui.theme.DisciplinedMindsTheme

class MainActivity : ComponentActivity() {

    private val permissionViewModel: PermissionViewModel by viewModels {
        PermissionViewModel.provideFactory(application)
    }
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.provideFactory(application)
    }
    private val appLockViewModel: AppLockViewModel by viewModels {
        AppLockViewModel.provideFactory(application)
    }

    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            homeViewModel.handleTimerBroadcast()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        setContent {
            DisciplinedMindsTheme {
                val permissionState = permissionViewModel.permissionState.collectAsStateWithLifecycle().value
                val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
                val selectedDuration = homeViewModel.selectedDuration.collectAsStateWithLifecycle().value
                val appLockState = appLockViewModel.uiState.collectAsStateWithLifecycle().value
                val navController = rememberNavController()

                LaunchedEffect(permissionState.allGranted) {
                    if (permissionState.allGranted) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Permission.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Permission.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Permission.route
                    ) {
                        composable(Screen.Permission.route) {
                            PermissionScreen(
                                state = permissionState,
                                onRequestUsageAccess = { openUsageAccessSettings() },
                                onRequestOverlayPermission = { openOverlaySettings() },
                                onRequestNotificationAccess = { openNotificationListenerSettings() },
                                onContinue = {
                                    permissionViewModel.refreshPermissions()
                                    if (permissionState.allGranted) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Permission.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                uiState = homeUiState,
                                selectedDuration = selectedDuration,
                                onDurationSelected = { homeViewModel.updateSelectedDuration(it) },
                                onStartTimer = { homeViewModel.startFocusTimer() },
                                onStopTimer = { homeViewModel.stopFocusTimer() },
                                onExtendTimer = { homeViewModel.extendFocusTimer() },
                                onToggleStudyMode = { homeViewModel.toggleStudyMode(it) },
                                onManageApps = {
                                    appLockViewModel.loadApplications()
                                    navController.navigate(Screen.AppLock.route)
                                },
                                onManualRefresh = { homeViewModel.refreshState() }
                            )
                        }
                        composable(Screen.AppLock.route) {
                            AppLockScreen(
                                state = appLockState,
                                onToggleLock = { appLockViewModel.toggleLock(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(TimerService.ACTION_TIMER_UPDATE)
        localBroadcastManager.registerReceiver(timerUpdateReceiver, filter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            localBroadcastManager.unregisterReceiver(timerUpdateReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        permissionViewModel.refreshPermissions()
        homeViewModel.refreshState()
    }

    private fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        ContextCompat.startActivity(this, intent, null)
    }

    private enum class Screen(val route: String) {
        Permission("permission"),
        Home("home"),
        AppLock("applock")
    }
}
