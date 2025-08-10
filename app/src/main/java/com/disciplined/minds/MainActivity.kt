package com.disciplined.minds

import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.disciplined.minds.applist.service.AppBlockService
import com.disciplined.minds.applist.views.AppListActivity
import com.disciplined.minds.databinding.ActivityMainBinding
import com.disciplined.minds.permission.PermissionActivity
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.timer.service.TimerService
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceDataHelper: PreferenceDataHelper
    private val handler = Handler(Looper.getMainLooper())
    private var timerUpdateTimer: Timer? = null
    private var selectedTimerDuration = 30 // Default 30 minutes
    
    // UI Elements
    private lateinit var tvBlockingStatus: TextView
    private lateinit var ivStatusIndicator: ImageView
    private lateinit var tvTimerDisplay: TextView
    private lateinit var layoutTimerControls: View
    private lateinit var layoutActiveTimer: View
    private lateinit var btnStartTimer: Button
    private lateinit var btnStopTimer: Button
    private lateinit var btnExtendTimer: Button
    private lateinit var btnStopActiveTimer: Button
    private lateinit var btnAppLock: Button
    
    // Timer duration buttons
    private lateinit var btn30Min: Button
    private lateinit var btn60Min: Button
    private lateinit var btn90Min: Button
    private lateinit var btn120Min: Button
    
    private val timerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when {
                intent?.getBooleanExtra("timer_started", false) == true -> {
                    updateTimerUI()
                }
                intent?.getBooleanExtra("timer_stopped", false) == true -> {
                    updateTimerUI()
                }
                intent?.getBooleanExtra("timer_completed", false) == true -> {
                    updateTimerUI()
                }
                intent?.hasExtra("remaining_time") == true -> {
                    updateTimerDisplay()
                }
            }
            updateBlockingStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        
        preferenceDataHelper = PreferenceDataHelper.getInstance(applicationContext)!!
        initViews()
        setupClickListeners()
        init()
    }
    
    override fun onResume() {
        super.onResume()
        updateBlockingStatus()
        updateTimerUI()
        startTimerUpdateLoop()
        
        // Register broadcast receiver with proper export flag for Android 13+
        val filter = IntentFilter("com.disciplined.minds.TIMER_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerBroadcastReceiver, filter)
        }
    }
    
    override fun onPause() {
        super.onPause()
        stopTimerUpdateLoop()
        
        // Unregister broadcast receiver
        try {
            unregisterReceiver(timerBroadcastReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    private fun initViews() {
        tvBlockingStatus = findViewById(R.id.tvBlockingStatus)
        ivStatusIndicator = findViewById(R.id.ivStatusIndicator)
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay)
        layoutTimerControls = findViewById(R.id.layoutTimerControls)
        layoutActiveTimer = findViewById(R.id.layoutActiveTimer)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStopTimer = findViewById(R.id.btnStopTimer)
        btnExtendTimer = findViewById(R.id.btnExtendTimer)
        btnStopActiveTimer = findViewById(R.id.btnStopActiveTimer)
        btnAppLock = findViewById(R.id.btnAppLock)
        
        btn30Min = findViewById(R.id.btn30Min)
        btn60Min = findViewById(R.id.btn60Min)
        btn90Min = findViewById(R.id.btn90Min)
        btn120Min = findViewById(R.id.btn120Min)
        
        // Set default selected timer duration
        updateTimerDurationButtons(30)
    }
    
    private fun setupClickListeners() {
        btnAppLock.setOnClickListener {
            val intent = Intent(this, AppListActivity::class.java)
            startActivity(intent)
        }
        
        // Timer duration buttons
        btn30Min.setOnClickListener { updateTimerDurationButtons(30) }
        btn60Min.setOnClickListener { updateTimerDurationButtons(60) }
        btn90Min.setOnClickListener { updateTimerDurationButtons(90) }
        btn120Min.setOnClickListener { updateTimerDurationButtons(120) }
        
        // Timer control buttons
        btnStartTimer.setOnClickListener {
            startFocusTimer(selectedTimerDuration)
        }
        
        btnStopTimer.setOnClickListener {
            stopFocusTimer()
        }
        
        btnExtendTimer.setOnClickListener {
            extendFocusTimer(15)
        }
        
        btnStopActiveTimer.setOnClickListener {
            stopFocusTimer()
        }
    }

    private fun init() {
        // Check permissions - redirect to permission activity if not granted
        if (!checkForPermission(this@MainActivity) || !Settings.canDrawOverlays(this)) {
            val intent = Intent(this, PermissionActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        startService(Intent(this@MainActivity, AppBlockService::class.java))
        updateBlockingStatus()
        updateTimerUI()
    }
    
    private fun updateTimerDurationButtons(selectedDuration: Int) {
        selectedTimerDuration = selectedDuration
        
        // Reset all button colors
        val buttons = listOf(btn30Min, btn60Min, btn90Min, btn120Min)
        val durations = listOf(30, 60, 90, 120)
        
        buttons.forEachIndexed { index, button ->
            if (durations[index] == selectedDuration) {
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_gray))
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }
    
    private fun startFocusTimer(durationMinutes: Int) {
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_START_TIMER
        intent.putExtra(TimerService.EXTRA_TIMER_DURATION, durationMinutes)
        startForegroundService(intent)
        
        updateTimerUI()
    }
    
    private fun stopFocusTimer() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_STOP_TIMER
        startService(intent)
        
        updateTimerUI()
    }
    
    private fun extendFocusTimer(extendMinutes: Int) {
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_EXTEND_TIMER
        intent.putExtra(TimerService.EXTRA_EXTEND_MINUTES, extendMinutes)
        startService(intent)
    }
    
    private fun updateTimerUI() {
        val isTimerActive = preferenceDataHelper.isTimerActive()
        
        if (isTimerActive) {
            layoutTimerControls.visibility = View.GONE
            layoutActiveTimer.visibility = View.VISIBLE
            updateTimerDisplay()
        } else {
            layoutTimerControls.visibility = View.VISIBLE
            layoutActiveTimer.visibility = View.GONE
            tvTimerDisplay.text = "00:00"
        }
    }
    
    private fun updateTimerDisplay() {
        val remainingTime = preferenceDataHelper.getRemainingTimerTime()
        val minutes = (remainingTime / 60000).toInt()
        val seconds = ((remainingTime % 60000) / 1000).toInt()
        
        tvTimerDisplay.text = String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun updateBlockingStatus() {
        val isStudyMode = preferenceDataHelper.isStudyMode()
        val isTimerActive = preferenceDataHelper.isTimerBlockingEnabled() && 
                            preferenceDataHelper.isTimerActive() &&
                            preferenceDataHelper.getRemainingTimerTime() > 0
        
        val isBlocking = isStudyMode || isTimerActive
        
        if (isBlocking) {
            if (isTimerActive) {
                tvBlockingStatus.text = "Timer Active"
                tvBlockingStatus.setTextColor(ContextCompat.getColor(this, R.color.color_yellow_dark))
                ivStatusIndicator.setImageResource(R.drawable.ic_timer)
                ivStatusIndicator.setColorFilter(ContextCompat.getColor(this, R.color.color_yellow_dark))
            } else {
                tvBlockingStatus.text = "Study Mode Active"
                tvBlockingStatus.setTextColor(ContextCompat.getColor(this, R.color.darkgreen))
                ivStatusIndicator.setImageResource(R.drawable.ic_check_circle)
                ivStatusIndicator.setColorFilter(ContextCompat.getColor(this, R.color.darkgreen))
            }
        } else {
            tvBlockingStatus.text = "Inactive"
            tvBlockingStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
            ivStatusIndicator.setImageResource(R.drawable.ic_error)
            ivStatusIndicator.setColorFilter(ContextCompat.getColor(this, R.color.red))
        }
    }
    
    private fun startTimerUpdateLoop() {
        timerUpdateTimer = Timer()
        timerUpdateTimer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    if (preferenceDataHelper.isTimerActive()) {
                        updateTimerDisplay()
                        updateBlockingStatus()
                    }
                }
            }
        }, 0, 1000) // Update every second
    }
    
    private fun stopTimerUpdateLoop() {
        timerUpdateTimer?.cancel()
        timerUpdateTimer = null
    }

    private fun checkForPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
