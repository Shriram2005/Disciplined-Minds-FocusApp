package com.disciplinedminds.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import com.disciplinedminds.applist.AppInfo
import com.disciplinedminds.pref.AppConstants
import java.util.Collections
import kotlin.collections.ArrayList

/**
 * Utility helpers for working with installed applications.
 */
object AppUtils {

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApplications(context: Context): List<AppInfo> {
        val packageManager = context.packageManager ?: return Collections.emptyList()
        val applications = packageManager.getInstalledApplications(0)
        val installedApplications = ArrayList<AppInfo>(applications.size)
        for (applicationInfo in applications) {
            val launchIntent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
            if (launchIntent != null && applicationInfo.packageName != context.packageName) {
                val applicationName = packageManager.getApplicationLabel(applicationInfo).toString()
                val applicationIcon = packageManager.getApplicationIcon(applicationInfo)
                val appInfo = AppInfo().apply {
                    this.applicationName = applicationName
                    this.applicationIcon = applicationIcon
                    this.applicationPackage = applicationInfo.packageName
                    this.isOpen = if (isSystemApplication(applicationInfo)) {
                        true
                    } else {
                        AppConstants.DefaultApp.contains(applicationName)
                    }
                }
                installedApplications.add(appInfo)
            }
        }
        return installedApplications
    }

    private fun isSystemApplication(applicationInfo: ApplicationInfo): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}
