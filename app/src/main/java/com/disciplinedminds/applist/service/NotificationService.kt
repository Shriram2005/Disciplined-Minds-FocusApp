package com.disciplinedminds.applist.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.disciplinedminds.pref.PreferenceDataHelper

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val helper = PreferenceDataHelper.getInstance(applicationContext)
            val appList = helper.getAppList()
            val isStudyMode = helper.isStudyMode()
            val isTimerBlockingActive = helper.isTimerBlockingEnabled() && helper.isTimerActive() && helper.getRemainingTimerTime() > 0
            val shouldBlock = (isStudyMode || isTimerBlockingActive)

            Log.d(TAG, "Notification from: ${sbn.packageName}, StudyMode: $isStudyMode, TimerBlocking: $isTimerBlockingActive, ShouldBlock: $shouldBlock")

            // Only block if blocking mode is active AND the app is in the blocked list
            if (shouldBlock && appList?.get(sbn.packageName) == true) {
                Log.d(TAG, "Blocking notification from: ${sbn.packageName}")
                cancelNotification(sbn.key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onNotificationPosted", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "NotificationService disconnected")
    }

    private companion object {
        private const val TAG = "NotificationService"
    }
}
