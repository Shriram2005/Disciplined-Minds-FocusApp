package com.disciplined.minds.pref

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
import java.text.ParseException
import java.text.SimpleDateFormat


class StringUtils {

    companion object {

        fun isAccessGranted(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
                val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                var mode = 0
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName)
                mode == AppOpsManager.MODE_ALLOWED
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun isNotificationServiceEnabled(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver,
                    AppConstants.ENABLE_NOTIFICATION_LISTENER)
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        @SuppressLint("SimpleDateFormat")
        fun getTimeFormat(time: Long): String? {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ")
            return simpleDateFormat.format(time)
        }

        @SuppressLint("SimpleDateFormat")
        fun getTimeMilliSecond(date: String): Long? {
            var timeInMilli = 0L
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ")
            try {
                val dateFormat = inputFormat.parse(date)
                timeInMilli = dateFormat.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return timeInMilli
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        fun getRecentApps(context: Context): String? {
            var topPackageName = ""
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val mUsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                    val time = System.currentTimeMillis()
                    
                    // Increase the time window for better detection
                    val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 60, time + 10 * 1000)
                    val event = UsageEvents.Event()
                    
                    var lastResumedPackage = ""
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            lastResumedPackage = event.packageName
                        }
                    }
                    
                    if (!TextUtils.isEmpty(lastResumedPackage)) {
                        return lastResumedPackage
                    }
                    
                    // Fallback: try to get the most recent usage stats
                    val usageStats = mUsageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 60 * 5, // Last 5 minutes
                        time
                    )
                    
                    if (usageStats.isNotEmpty()) {
                        val mostRecent = usageStats.maxByOrNull { it.lastTimeUsed }
                        if (mostRecent != null && !TextUtils.isEmpty(mostRecent.packageName)) {
                            return mostRecent.packageName
                        }
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Fallback to old method for older Android versions
            try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val taskInfo = am.getRunningTasks(1)
                if (taskInfo.isNotEmpty()) {
                    val componentInfo = taskInfo[0].topActivity
                    if (componentInfo != null) {
                        return componentInfo.packageName
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return ""
        }
    }
}