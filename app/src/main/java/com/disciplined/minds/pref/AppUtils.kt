import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.disciplined.minds.applist.AppInfo
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Square Infosoft.
 */


class AppUtils {

    companion object {
        @SuppressLint("QueryPermissionsNeeded")
        fun getInstalledApplications(context: Context): List<AppInfo> {
            val packageManager = context.packageManager
                    ?: return Collections.emptyList<AppInfo>()
            // Querying the PackageManager to get all the InstalledApplications by passing 0 flags
            val applications = packageManager.getInstalledApplications(0)
            val installedApplications = ArrayList<AppInfo>()
            for (applicationInfo in applications) {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null && applicationInfo.packageName != BuildConfig.PACKAGE_NAME) {
                    val applicationName =
                            packageManager.getApplicationLabel(applicationInfo).toString()
                    val applicationIcon = packageManager.getApplicationIcon(applicationInfo)
                    val applicationPackage = applicationInfo.packageName
                    val appInfo = AppInfo()
                    if (isSystemApplication(applicationInfo)) {
                        appInfo.isOpen = true
                    } else appInfo.isOpen = AppConstants.DefaultApp.contains(applicationName)
                    appInfo.applicationName = applicationName
                    appInfo.applicationIcon = applicationIcon
                    appInfo.applicationPackage = applicationPackage
                    installedApplications.add(appInfo)
                }
            }
            return installedApplications
        }

        private fun isSystemApplication(applicationInfo: ApplicationInfo): Boolean {
            if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
                return true
            return false
        }

        @SuppressLint("PrivateApi")
        fun isXiaomiDevice(): Boolean {
            val manufacturer = Build.MANUFACTURER
            if (manufacturer == AppConstants.XIAOMI) {
                val c = Class.forName("android.os.SystemProperties")
                val get = c.getMethod("get", String::class.java)
                val miui = get.invoke(c, "ro.miui.ui.version.name") as String?
                if (miui != null && (miui.contains("11") || miui.contains("10"))) {
                    return true
                }
                return false
            }
            return false
        }
    }
}