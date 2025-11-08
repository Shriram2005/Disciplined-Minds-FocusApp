package com.disciplined.minds.timer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.disciplined.minds.MainActivity
import com.disciplined.minds.R
import com.disciplined.minds.pref.PreferenceDataHelper
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max

class TimerService : Service() {

    private lateinit var notificationManager: NotificationManager
    private var builder: NotificationCompat.Builder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private lateinit var preferenceDataHelper: PreferenceDataHelper
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val channelId = "timer-service-channel"
    private val channelName = "Timer Service"
    private val notificationId = 10125

    override fun onCreate() {
        super.onCreate()
        preferenceDataHelper = PreferenceDataHelper.getInstance(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val duration = intent.getIntExtra(EXTRA_TIMER_DURATION, 30)
                startTimer(duration)
            }
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_EXTEND_TIMER -> {
                val extendMinutes = intent.getIntExtra(EXTRA_EXTEND_MINUTES, 15)
                extendTimer(extendMinutes)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer(durationMinutes: Int) {
        val startTime = System.currentTimeMillis()
        preferenceDataHelper.setTimerDuration(durationMinutes)
        preferenceDataHelper.setTimerStartTime(startTime)
        preferenceDataHelper.setTimerActive(true)
        preferenceDataHelper.setTimerBlockingEnabled(true)

        broadcastUpdate("timer_started")
        startForegroundNotification()
        startTimerCountdown()
    }

    private fun stopTimer() {
        android.util.Log.d("TimerService", "stopTimer called - disabling blocking")
        timer?.cancel()
        timer = null
        preferenceDataHelper.setTimerActive(false)
        preferenceDataHelper.setTimerBlockingEnabled(false)
        android.util.Log.d(
            "TimerService",
            "Timer state updated: isActive=${preferenceDataHelper.isTimerActive()}, isBlocking=${preferenceDataHelper.isTimerBlockingEnabled()}"
        )
        broadcastUpdate("timer_stopped")
        stopForeground(true)
        stopSelf()
    }

    private fun extendTimer(extendMinutes: Int) {
        if (preferenceDataHelper.isTimerActive()) {
            val currentDuration = preferenceDataHelper.getTimerDuration()
            preferenceDataHelper.setTimerDuration(currentDuration + extendMinutes)
            broadcastUpdate("timer_extended")
            updateNotification()
        }
    }

    private fun startTimerCountdown() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val remainingTime = preferenceDataHelper.getRemainingTimerTime()
                if (remainingTime <= 0) {
                    handler.post { timerExpired() }
                    cancel()
                } else {
                    handler.post {
                        updateNotification()
                        broadcastUpdate("remaining_time", remainingTime)
                    }
                }
            }
        }, 0, 1000)
    }

    private fun timerExpired() {
        android.util.Log.d("TimerService", "Timer expired - disabling blocking")
        preferenceDataHelper.setTimerActive(false)
        preferenceDataHelper.setTimerBlockingEnabled(false)
        android.util.Log.d(
            "TimerService",
            "Timer state updated: isActive=${preferenceDataHelper.isTimerActive()}, isBlocking=${preferenceDataHelper.isTimerBlockingEnabled()}"
        )
        notificationManager.notify(notificationId + 1, createCompletionNotification())
        broadcastUpdate("timer_completed")
        stopForeground(true)
        stopSelf()
    }

    private fun broadcastUpdate(event: String, remaining: Long? = null) {
        val intent = Intent(ACTION_TIMER_UPDATE).apply {
            when (event) {
                "timer_started" -> putExtra("timer_started", true)
                "timer_stopped" -> putExtra("timer_stopped", true)
                "timer_completed" -> putExtra("timer_completed", true)
                "timer_extended" -> putExtra("timer_extended", true)
                "remaining_time" -> putExtra("remaining_time", remaining ?: 0L)
            }
        }
        // Send to both local and system broadcast receivers for reliability
        localBroadcastManager.sendBroadcast(intent)
        sendBroadcast(intent)
    }

    private fun startForegroundNotification() {
        val notification = createTimerNotification()
        startForeground(notificationId, notification)
    }

    private fun updateNotification() {
        val notification = createTimerNotification()
        notificationManager.notify(notificationId, notification)
    }

    private fun createTimerNotification(): Notification {
        val remainingTime = preferenceDataHelper.getRemainingTimerTime()
        val remainingMinutes = max(0, (remainingTime / 60000).toInt())
        val remainingSeconds = max(0, ((remainingTime % 60000) / 1000).toInt())
        val timeText = String.format("%02d:%02d", remainingMinutes, remainingSeconds)

        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP_TIMER }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val extendIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_EXTEND_TIMER
            putExtra(EXTRA_EXTEND_MINUTES, 15)
        }
        val extendPendingIntent = PendingIntent.getService(this, 1, extendIntent, PendingIntent.FLAG_IMMUTABLE)

        builder = NotificationCompat.Builder(this, channelId)
        return builder!!
            .setContentTitle(getString(R.string.focus_timer_running))
            .setContentText(getString(R.string.time_remaining_format, timeText))
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_close, getString(R.string.stop), stopPendingIntent)
            .addAction(android.R.drawable.ic_input_add, getString(R.string.extend_fifteen), extendPendingIntent)
            .build()
    }

    private fun createCompletionNotification(): Notification {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.focus_session_complete_title))
            .setContentText(getString(R.string.focus_session_complete_message))
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_TIMER_UPDATE = "com.disciplined.minds.TIMER_UPDATE"
        const val ACTION_START_TIMER = "com.disciplined.minds.timer.START_TIMER"
        const val ACTION_STOP_TIMER = "com.disciplined.minds.timer.STOP_TIMER"
        const val ACTION_EXTEND_TIMER = "com.disciplined.minds.timer.EXTEND_TIMER"
        const val EXTRA_TIMER_DURATION = "timer_duration"
        const val EXTRA_EXTEND_MINUTES = "extend_minutes"
    }
}
