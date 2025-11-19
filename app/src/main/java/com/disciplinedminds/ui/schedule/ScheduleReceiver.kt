package com.disciplinedminds.ui.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.disciplinedminds.timer.service.TimerService
import java.text.SimpleDateFormat
import java.util.*

class ScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_TIMER -> {
                val scheduleName = intent.getStringExtra("schedule_name") ?: "Scheduled Focus"
                val startTime = intent.getStringExtra("start_time") ?: ""
                val endTime = intent.getStringExtra("end_time") ?: ""
                
                // Calculate duration in minutes
                val duration = calculateDuration(startTime, endTime)
                
                if (duration > 0) {
                    val timerIntent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START_TIMER
                        putExtra(TimerService.EXTRA_TIMER_DURATION, duration)
                    }
                    ContextCompat.startForegroundService(context, timerIntent)
                }
            }
            ACTION_STOP_TIMER -> {
                val timerIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP_TIMER
                }
                context.startService(timerIntent)
            }
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): Int {
        try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = format.parse(startTime)
            val end = format.parse(endTime)
            
            if (start != null && end != null) {
                var diff = end.time - start.time
                
                // If end time is before start time, it's the next day
                if (diff < 0) {
                    diff += 24 * 60 * 60 * 1000
                }
                
                return (diff / (60 * 1000)).toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    companion object {
        const val ACTION_START_TIMER = "com.disciplinedminds.ACTION_START_SCHEDULED_TIMER"
        const val ACTION_STOP_TIMER = "com.disciplinedminds.ACTION_STOP_SCHEDULED_TIMER"
    }
}
