package com.disciplined.minds.applist.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.disciplined.minds.MainActivity
import com.disciplined.minds.R
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.pref.StringUtils
import java.util.*
import kotlin.collections.ArrayList


class AppBlockService : Service() {

    private lateinit var notificationManager: NotificationManager
    private var builder: NotificationCompat.Builder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var previousPackage = ""
    val ACTION_STOP = "playback.service.action.STOP"
    private var isServiceRunning = false
    var appList: HashMap<String, Boolean>? = null
    var windowManager: WindowManager? = null

    var channelId ="genius-me-study-mode"
    var channelName ="Study Mode"
    var notificationServiceId = 10124

    override fun onCreate() {
        super.onCreate()
        notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startServiceWithNotification()
    }

    private fun startServiceWithNotification() {
        isServiceRunning = true
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.action = "App Block"// A string containing the action name
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val stopIntent = Intent(this, AppBlockService::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent: PendingIntent =
                PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val contentPendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val icon = BitmapFactory.decodeResource(resources, R.drawable.app_logo)
        builder =
                NotificationCompat.Builder(this, channelId)
        
        // Update notification text based on current blocking status
        val preferenceHelper = PreferenceDataHelper.getInstance(applicationContext)!!
        val isTimerActive = preferenceHelper.isTimerBlockingEnabled() && 
                           preferenceHelper.isTimerActive() &&
                           preferenceHelper.getRemainingTimerTime() > 0
        
        val notificationTitle = if (isTimerActive) {
            "Focus Timer Active"
        } else {
            resources.getString(R.string.non_essential_app_locked)
        }
        
        val notificationText = if (isTimerActive) {
            val remainingTime = preferenceHelper.getRemainingTimerTime()
            val minutes = (remainingTime / 60000).toInt()
            val seconds = ((remainingTime % 60000) / 1000).toInt()
            "Time remaining: ${String.format("%02d:%02d", minutes, seconds)}"
        } else {
            resources.getString(R.string.stay_focused)
        }
        
        val notification =
                builder!!.setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setSmallIcon(R.drawable.app_logo)
                        .setContentIntent(contentPendingIntent)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_close, getString(R.string.stop), stopPendingIntent)
                        .build()
        notification.flags =
                notification.flags or Notification.FLAG_NO_CLEAR
        startForeground(notificationServiceId, notification)

        appList = PreferenceDataHelper.getInstance(applicationContext)!!.getAppList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkActivityState()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun checkActivityState() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (isServiceRunning) {



                   /* if(isAppRunning(applicationContext!!,"com.bettride.driver")){
                        Log.e(">>>>>>>>","krishna = if");
                    }else{
                        Log.e(">>>>>>>>","krishna = else");
                    }
                    */


                    //appList?.put("com.bettride.driver",true);
                    val mPackageName: String? =
                                StringUtils.getRecentApps(applicationContext!!)

                        // Check if timer blocking is enabled and timer is active
                        val preferenceDataHelper = PreferenceDataHelper.getInstance(applicationContext)!!
                        val isTimerBlockingActive = preferenceDataHelper.isTimerBlockingEnabled() && 
                                                   preferenceDataHelper.isTimerActive() &&
                                                   preferenceDataHelper.getRemainingTimerTime() > 0

                        // Block apps if either study mode is on OR timer blocking is active
                        val shouldBlockApps = preferenceDataHelper.isStudyMode() || isTimerBlockingActive

                        if (appList != null && mPackageName != null && appList!![mPackageName] != null && 
                            appList!![mPackageName]!! && shouldBlockApps &&
                            previousPackage != mPackageName && windowManager == null) {
                            handler.post {
                                Log.e("krishna","$$$$$"+mPackageName);
                                openScreen()
                            }
                        }
                        if (mPackageName != null) {
                            previousPackage = mPackageName
                        }
                } else {
                    cancel()
                }
            }
        }, 0, 50)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        var IMPORTANCE = NotificationManager.IMPORTANCE_NONE
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
        }

        val chan = NotificationChannel(
                channelId,
                channelName,
                IMPORTANCE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)
    }

    private fun openScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Settings.canDrawOverlays(applicationContext)) {

            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            windowManager = this
                    .getSystemService(Context.WINDOW_SERVICE) as WindowManager

            var testView: View? = null

            val wrapper: ViewGroup = object : FrameLayout(this) {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                        Log.v("Back", "Back Key")
                        removeTestView(testView!!)
                        return true
                    } else if (event.keyCode == KeyEvent.KEYCODE_HOME) {
                        return true
                    }
                    return super.dispatchKeyEvent(event)
                }
            }

            val para = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }
            val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    para,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            )


            testView = LayoutInflater.from(applicationContext).inflate(
                    R.layout.activity_block,
                    wrapper
            )

            val tvClose = testView.findViewById<TextView>(R.id.tvClose) as TextView
            tvClose.setOnClickListener {
                removeTestView(testView)
            }
            windowManager!!.addView(testView, params)
        }
    }

    private fun removeTestView(testView: View) {
        if (windowManager != null) {
            windowManager!!.removeView(testView)
            windowManager = null
            previousPackage = ""
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var action: String? = null
        if (intent != null) {
            action = intent.action
        }
        if (action != null && action == ACTION_STOP) {
            stopMyService()
        }
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopMyService()
        super.onDestroy()
    }

    fun stopMyService() {
        stopForeground(true)
        isServiceRunning = false
        stopSelf()
       /*
        TODO
        val studyModeList: List<Mode> = listAll(Mode::class.java)
        for (i in studyModeList.size - 1 downTo 0) {
            if (studyModeList[i].mode != null && studyModeList[i].mode == AppConstants.STUDY_MODE) {
                updateStudyMode(studyModeList[i])
                PreferenceDataHelper.getInstance(applicationContext)!!.setStudyMode(false)
                stopSelf()
                break
            }
        }*/
    }

    private fun updateStudyMode(/*studyMode: Mode*/) {
      /*  val calendar = Calendar.getInstance()
        val endDate = StringUtils.getTimeFormat(calendar.timeInMillis)
        val startTime = StringUtils.getTimeMilliSecond(studyMode.startTime!!)
        val difference = ((calendar.timeInMillis - startTime!!) / (1000 * 60)).toInt()
        val appList = PreferenceDataHelper.getInstance(this)!!.getAppList()
        val openAppList = getOpenAppList(appList)
        studyMode.endTime = endDate
        studyMode.duration = difference
        studyMode.openAppId = openAppList
        studyMode.save()

       */
    }

    private fun getOpenAppList(appList: HashMap<String, Boolean>?): String {
        val openAppList = ArrayList<String>()
        if (appList != null) {
            for (element in appList.keys) {
                if (!appList[element]!!) {
                    openAppList.add(element)
                }
            }
        }
        val openApp = openAppList.toString()
        return openApp
    }

    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses


        if (procInfos != null) {
            for (processInfo in procInfos) {
                Log.e(">>>>>>>>","$"+processInfo.processName );
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }
}