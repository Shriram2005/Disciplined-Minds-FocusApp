package com.disciplined.minds.timer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.timer.service.TimerService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferenceDataHelper = PreferenceDataHelper.getInstance(context)!!
            
            // Check if timer was active before reboot
            if (preferenceDataHelper.isTimerActive()) {
                val remainingTime = preferenceDataHelper.getRemainingTimerTime()
                
                if (remainingTime > 0) {
                    // Restart timer service
                    val timerIntent = Intent(context, TimerService::class.java)
                    timerIntent.action = TimerService.ACTION_START_TIMER
                    timerIntent.putExtra(TimerService.EXTRA_TIMER_DURATION, (remainingTime / 60000).toInt() + 1)
                    context.startForegroundService(timerIntent)
                } else {
                    // Timer expired during reboot, clean up
                    preferenceDataHelper.setTimerActive(false)
                    preferenceDataHelper.setTimerBlockingEnabled(false)
                }
            }
        }
    }
}
