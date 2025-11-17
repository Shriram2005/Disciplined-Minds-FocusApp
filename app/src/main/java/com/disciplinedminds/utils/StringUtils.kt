package com.disciplinedminds.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi

/**
 * Collection of helper methods used by the background services for permissions and recent apps.
 */
object StringUtils {

    fun isAccessGranted(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            com.disciplinedminds.pref.AppConstants.ENABLE_NOTIFICATION_LISTENER
        )
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun getRecentApps(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val usageEvents = usageStatsManager.queryEvents(time - 60_000, time + 10_000)
                val event = UsageEvents.Event()
                var lastResumedPackage = ""
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        lastResumedPackage = event.packageName
                    }
                }
                if (!lastResumedPackage.isNullOrEmpty()) {
                    return lastResumedPackage
                }
            } catch (_: Exception) {
                // ignore and fall back
            }
        }

        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)
            if (taskInfo.isNotEmpty()) {
                val componentInfo = taskInfo[0].topActivity
                componentInfo?.packageName ?: ""
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }
}
