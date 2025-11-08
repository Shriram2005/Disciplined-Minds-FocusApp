package com.disciplined.minds.applist.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.disciplined.minds.pref.PreferenceDataHelper

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val helper = PreferenceDataHelper.getInstance(applicationContext)
        val appList = helper.getAppList()
        val isStudyMode = helper.isStudyMode()
        val isTimerBlockingActive = helper.isTimerBlockingEnabled() && helper.isTimerActive() && helper.getRemainingTimerTime() > 0

        if ((isStudyMode || isTimerBlockingActive) && appList?.get(sbn.packageName) == true) {
            cancelNotification(sbn.key)
        }
    }
}
