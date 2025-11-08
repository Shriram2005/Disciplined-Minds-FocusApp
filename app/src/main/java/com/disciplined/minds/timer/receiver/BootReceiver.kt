package com.disciplined.minds.timer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.disciplined.minds.applist.service.AppBlockService
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.timer.service.TimerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val helper = PreferenceDataHelper.getInstance(context)
            if (helper.isTimerActive()) {
                val remainingTime = helper.getRemainingTimerTime()
                if (remainingTime > 0) {
                    val timerIntent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START_TIMER
                        putExtra(TimerService.EXTRA_TIMER_DURATION, (remainingTime / 60000).toInt() + 1)
                    }
                    context.startForegroundService(timerIntent)
                    context.startService(Intent(context, AppBlockService::class.java))
                } else {
                    helper.setTimerActive(false)
                    helper.setTimerBlockingEnabled(false)
                }
            }
            if (helper.isStudyMode()) {
                context.startService(Intent(context, AppBlockService::class.java))
            }
        }
    }
}
