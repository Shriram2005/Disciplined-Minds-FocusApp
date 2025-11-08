package com.disciplined.minds.applist.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.disciplined.minds.MainActivity
import com.disciplined.minds.R
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.timer.service.TimerService
import com.disciplined.minds.utils.StringUtils
import java.util.Timer
import java.util.TimerTask
import kotlin.collections.HashMap

class AppBlockService : Service() {

    private lateinit var notificationManager: NotificationManager
    private var builder: NotificationCompat.Builder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var previousPackage = ""
    private var isServiceRunning = false
    private var appList: HashMap<String, Boolean>? = null
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val channelId = "genius-me-study-mode"
    private val channelName = "Study Mode"
    private val notificationServiceId = 10124

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when {
                intent?.getBooleanExtra("timer_started", false) == true -> {
                    Log.d(TAG, "Timer started - enabling blocking")
                    updateNotification()
                    forceRefreshBlockingState()
                }
                intent?.getBooleanExtra("timer_stopped", false) == true -> {
                    Log.d(TAG, "Timer stopped - disabling blocking and removing overlay")
                    // Immediately remove overlay when timer is stopped
                    handler.post { removeOverlayIfPresent() }
                    updateNotification()
                    forceRefreshBlockingState()
                }
                intent?.getBooleanExtra("timer_completed", false) == true -> {
                    Log.d(TAG, "Timer completed - disabling blocking and removing overlay")
                    // Immediately remove overlay when timer completes
                    handler.post { removeOverlayIfPresent() }
                    updateNotification()
                    forceRefreshBlockingState()
                }
                intent?.hasExtra("remaining_time") == true -> updateNotification()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val filter = IntentFilter(TimerService.ACTION_TIMER_UPDATE)
        localBroadcastManager.registerReceiver(timerUpdateReceiver, filter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerUpdateReceiver, filter)
        }
        startServiceWithNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (ACTION_STOP == action) {
            stopMyService()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try {
            localBroadcastManager.unregisterReceiver(timerUpdateReceiver)
        } catch (_: Exception) {
        }
        try {
            unregisterReceiver(timerUpdateReceiver)
        } catch (_: Exception) {
        }
        stopMyService()
        super.onDestroy()
    }

    private fun startServiceWithNotification() {
        isServiceRunning = true
        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = "App Block"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        createNotificationChannel()

        builder = NotificationCompat.Builder(this, channelId)
        val notification = builder!!
            .setContentTitle(getString(R.string.app_block_service_active))
            .setContentText(getString(R.string.monitoring_apps))
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_close, getString(R.string.stop), createStopPendingIntent())
            .build()

        startForeground(notificationServiceId, notification)
        appList = PreferenceDataHelper.getInstance(applicationContext).getAppList()
        checkActivityState()
    }

    private fun createStopPendingIntent(): PendingIntent {
        val stopIntent = Intent(this, AppBlockService::class.java).apply { action = ACTION_STOP }
        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun updateNotification() {
        val preferenceHelper = PreferenceDataHelper.getInstance(applicationContext)
        val isTimerActive = preferenceHelper.isTimerBlockingEnabled() && preferenceHelper.isTimerActive() && preferenceHelper.getRemainingTimerTime() > 0

        val notificationTitle = if (isTimerActive) {
            getString(R.string.focus_timer_active)
        } else {
            getString(R.string.non_essential_app_locked)
        }

        val notificationText = if (isTimerActive) {
            val remainingTime = preferenceHelper.getRemainingTimerTime()
            val minutes = (remainingTime / 60000).toInt()
            val seconds = ((remainingTime % 60000) / 1000).toInt()
            getString(R.string.time_remaining_format, String.format("%02d:%02d", minutes, seconds))
        } else {
            getString(R.string.stay_focused)
        }

        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = "App Block"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = builder!!
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_close, getString(R.string.stop), createStopPendingIntent())
            .build().apply { flags = flags or Notification.FLAG_NO_CLEAR }

        notificationManager.notify(notificationServiceId, notification)
    }

    private fun forceRefreshBlockingState() {
        try {
            appList = PreferenceDataHelper.getInstance(applicationContext).getAppList()
            val currentPackage = StringUtils.getRecentApps(applicationContext)
            if (currentPackage.isNotEmpty()) {
                val helper = PreferenceDataHelper.getInstance(applicationContext)
                val isTimerBlockingActive = helper.isTimerBlockingEnabled() && helper.isTimerActive() && helper.getRemainingTimerTime() > 0
                val shouldBlockApps = helper.isStudyMode() || isTimerBlockingActive

                Log.d(
                    TAG,
                    "forceRefreshBlockingState: currentPackage=$currentPackage, isTimerBlocking=$isTimerBlockingActive, shouldBlock=$shouldBlockApps, overlayVisible=${overlayView != null}"
                )

                // If blocking conditions are no longer met, immediately remove the overlay
                if (!shouldBlockApps) {
                    Log.d(TAG, "Blocking conditions not met - removing overlay")
                    handler.post { removeOverlayIfPresent() }
                    return
                }

                // Only show overlay if blocking is active and app is blocked
                if (appList?.get(currentPackage) == true) {
                    Log.d(TAG, "App is blocked and blocking is active - ensuring overlay")
                    handler.post { ensureOverlayVisible() }
                } else {
                    // Current app is not blocked, remove overlay if present
                    Log.d(TAG, "Current app not in block list - removing overlay if present")
                    handler.post { removeOverlayIfPresent() }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in forceRefreshBlockingState", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun checkActivityState() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!isServiceRunning) {
                    cancel()
                    return
                }
                try {
                    val packageName = StringUtils.getRecentApps(applicationContext)
                    val preferenceHelper = PreferenceDataHelper.getInstance(applicationContext)
                    val isTimerBlockingActive = preferenceHelper.isTimerBlockingEnabled() && preferenceHelper.isTimerActive() && preferenceHelper.getRemainingTimerTime() > 0
                    val shouldBlockApps = preferenceHelper.isStudyMode() || isTimerBlockingActive

                    if (appList != null && packageName.isNotEmpty() && appList!![packageName] == true && shouldBlockApps && previousPackage != packageName) {
                        if (validateBlockingConditions(preferenceHelper, packageName)) {
                            handler.post { ensureOverlayVisible() }
                        }
                    }

                    if (packageName.isNotEmpty()) {
                        previousPackage = packageName
                    }

                    // Only remove overlay if blocking conditions are no longer met (study mode off AND timer not active)
                    // Do NOT remove overlay just because user is on home screen or another app
                    appList = preferenceHelper.getAppList()
                    if (overlayView != null && !shouldBlockApps) {
                        handler.post { removeOverlayIfPresent() }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in checkActivityState", e)
                }
            }
        }, 0, 100)
    }

    private fun validateBlockingConditions(helper: PreferenceDataHelper, packageName: String): Boolean {
        return try {
            val isTimerBlockingActive = helper.isTimerBlockingEnabled() && helper.isTimerActive() && helper.getRemainingTimerTime() > 0
            val shouldBlockApps = helper.isStudyMode() || isTimerBlockingActive
            val currentList = helper.getAppList()
            val isAppBlocked = currentList?.get(packageName) == true
            shouldBlockApps && isAppBlocked
        } catch (e: Exception) {
            Log.e(TAG, "Error validating blocking conditions", e)
            false
        }
    }

    private fun ensureOverlayVisible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                applicationContext
            )
        ) {
            removeOverlayIfPresent()
            return
        }

        val helper = PreferenceDataHelper.getInstance(applicationContext)
        val currentPackage = StringUtils.getRecentApps(applicationContext)
        val isTimerBlockingActive =
            helper.isTimerBlockingEnabled() && helper.isTimerActive() && helper.getRemainingTimerTime() > 0
        val shouldBlockApps = helper.isStudyMode() || isTimerBlockingActive
        val isPackageBlocked =
            currentPackage.isNotEmpty() && (helper.getAppList()?.get(currentPackage) == true)
        if (!shouldBlockApps || !isPackageBlocked) {
            removeOverlayIfPresent()
            return
        }

        if (overlayView != null) {
            return
        }

        if (windowManager == null) {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or 
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            PixelFormat.TRANSLUCENT
        )
        
        // Make the overlay draw behind status bar and navigation bar for true fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val wrapper: ViewGroup = object : FrameLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_HOME) {
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }

        overlayView =
            LayoutInflater.from(applicationContext).inflate(R.layout.activity_block, wrapper)
        
        // Set system UI visibility to hide status bar and navigation bar for true fullscreen
        overlayView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        
        overlayView?.findViewById<TextView>(R.id.tvClose)?.setOnClickListener {
            removeOverlayIfPresent()
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to add overlay view", e)
            overlayView = null
            return
        }

        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(startMain)
    }

    private fun removeOverlayIfPresent() {
        if (overlayView != null) {
            Log.d(TAG, "Removing overlay view")
        }
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
                Log.d(TAG, "Overlay view removed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay", e)
            }
        }
        overlayView = null
        windowManager = null
        previousPackage = ""
    }

    fun stopMyService() {
        removeOverlayIfPresent()
        stopForeground(true)
        isServiceRunning = false
        stopSelf()
    }

    private fun createNotificationChannel() {
        val importance = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            NotificationManager.IMPORTANCE_NONE
        }
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        notificationManager.createNotificationChannel(channel)
    }

    private companion object {
        private const val TAG = "AppBlockService"
        private const val ACTION_STOP = "playback.service.action.STOP"
    }
}
