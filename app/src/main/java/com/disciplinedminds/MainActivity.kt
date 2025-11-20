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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.disciplinedminds.ui.schedule.ScheduleScreen
import com.disciplinedminds.ui.schedule.ScheduleViewModel
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
    private val scheduleViewModel: ScheduleViewModel by viewModels {
        ScheduleViewModel.provideFactory(application)
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
                // Set status bar color based on theme
                val statusBarColor = if (darkThemeEnabled) Color.Black else Color.White
                val darkIcons = !darkThemeEnabled
                
                SideEffect {
                    val window = this.window
                    window.statusBarColor = statusBarColor.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = darkIcons
                    }
                }
                
                val permissionState = permissionViewModel.permissionState.collectAsStateWithLifecycle().value
                val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
                val selectedDuration = homeViewModel.selectedDuration.collectAsStateWithLifecycle().value
                val appLockState = appLockViewModel.uiState.collectAsStateWithLifecycle().value
                val schedules by scheduleViewModel.schedules.collectAsStateWithLifecycle()
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
                        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
                        bottomBar = {
                            if (permissionState.allGranted) {
                                NavigationBar(
                                    containerColor = Color(0xFF0f172a),
                                    contentColor = Color.White
                                ) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = navBackStackEntry?.destination?.route
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.Home.route,
                                        onClick = { navController.navigate(Screen.Home.route) },
                                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home", tint = Color.White) },
                                        label = { Text("Home", color = Color.White) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.AppLock.route,
                                        onClick = {
                                            appLockViewModel.loadApplications()
                                            navController.navigate(Screen.AppLock.route)
                                        },
                                        icon = { Icon(painter = painterResource(R.drawable.ic_lock), contentDescription = "Locks", tint = Color.White) },
                                        label = { Text("Locks", color = Color.White) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.Schedule.route,
                                        onClick = { navController.navigate(Screen.Schedule.route) },
                                        icon = { Icon(painter = painterResource(R.drawable.ic_schedule), contentDescription = "Schedule", tint = Color.White) },
                                        label = { Text("Schedule", color = Color.White) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == Screen.Settings.route,
                                        onClick = { navController.navigate(Screen.Settings.route) },
                                        icon = { Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Settings", tint = Color.White) },
                                        label = { Text("Settings", color = Color.White) }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Permission.route,
                            modifier = Modifier.padding(paddingValues)
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
                            composable(Screen.Schedule.route) {
                                ScheduleScreen(
                                    schedules = schedules,
                                    onAddSchedule = { scheduleViewModel.addSchedule(it) },
                                    onUpdateSchedule = { scheduleViewModel.updateSchedule(it) },
                                    onDeleteSchedule = { scheduleViewModel.deleteSchedule(it) },
                                    onToggleSchedule = { scheduleViewModel.toggleSchedule(it) }
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
        Schedule("schedule"),
        Settings("settings")
    }
}
