package com.disciplinedminds

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.disciplinedminds.timer.service.TimerService
import com.disciplinedminds.ui.applock.AppLockScreen
import com.disciplinedminds.ui.applock.AppLockViewModel
import com.disciplinedminds.ui.home.HomeScreen
import com.disciplinedminds.ui.home.HomeViewModel
import com.disciplinedminds.ui.permission.PermissionScreen
import com.disciplinedminds.ui.permission.PermissionViewModel
import com.disciplinedminds.ui.theme.DisciplinedMindsTheme
import com.disciplinedminds.ui.settings.SettingsScreen
import com.disciplinedminds.ui.settings.SettingsViewModel
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.ui.res.painterResource
import com.disciplinedminds.R

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
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.provideFactory(application)
    }

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            homeViewModel.handleTimerBroadcast()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkThemeEnabled by settingsViewModel.darkTheme.collectAsStateWithLifecycle()
            DisciplinedMindsTheme(useDarkTheme = darkThemeEnabled) {
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
                    Scaffold(
                        bottomBar = {
                            if (permissionState.allGranted) {
                                NavigationBar {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = navBackStackEntry?.destination?.route
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.Home.route,
                                        onClick = { navController.navigate(Screen.Home.route) },
                                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                                        label = { Text("Home") }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.AppLock.route,
                                        onClick = {
                                            appLockViewModel.loadApplications()
                                            navController.navigate(Screen.AppLock.route)
                                        },
                                        icon = { Icon(painter = painterResource(R.drawable.ic_lock), contentDescription = "Locks") },
                                        label = { Text("Locks") }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.Settings.route,
                                        onClick = { navController.navigate(Screen.Settings.route) },
                                        icon = { Icon(painter = painterResource(R.drawable.ic_timer), contentDescription = "Settings") },
                                        label = { Text("Settings") }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
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
                                    onBack = { /* Bottom nav handles navigation */ navController.navigate(Screen.Home.route) }
                                )
                            }
                            composable(Screen.Settings.route) {
                                val dark by settingsViewModel.darkTheme.collectAsStateWithLifecycle()
                                SettingsScreen(
                                    darkTheme = dark,
                                    onToggleDarkTheme = { settingsViewModel.toggleDarkTheme(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(TimerService.ACTION_TIMER_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerUpdateReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(timerUpdateReceiver)
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
        AppLock("applock"),
        Settings("settings")
    }
}
