package com.disciplined.minds.applist.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Created by Square Infosoft.
 */


class NotificationService : NotificationListenerService() {
    private val TAG = "NotificationListener"

    override fun onListenerConnected() {
        Log.i(TAG, "Notification Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val appList = PreferenceDataHelper.getInstance(applicationContext)!!.getAppList()
        val isStudyMode = PreferenceDataHelper.getInstance(applicationContext)!!.isStudyMode()

        if (isStudyMode) {
            if (appList != null && appList[sbn.packageName] != null && appList[sbn.packageName]!!) {
                cancelNotification(sbn.key)
            }
        }
    }
}