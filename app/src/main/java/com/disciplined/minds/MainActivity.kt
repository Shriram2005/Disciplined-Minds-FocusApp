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
import android.app.ActivityManager
import android.util.Log


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
                    Log.d("MainActivity", "Timer started broadcast received")
                    // Re-enable start button and update UI
                    btnStartTimer.isEnabled = true
                    btnStartTimer.text = "Start Timer"
                    forceRefreshUI()
                }
                intent?.getBooleanExtra("timer_stopped", false) == true -> {
                    Log.d("MainActivity", "Timer stopped broadcast received")
                    // Re-enable stop buttons and update UI
                    btnStopTimer.isEnabled = true
                    btnStopTimer.text = "Stop Timer"
                    btnStopActiveTimer.isEnabled = true
                    btnStopActiveTimer.text = "Stop Timer"
                    forceRefreshUI()
                }
                intent?.getBooleanExtra("timer_completed", false) == true -> {
                    Log.d("MainActivity", "Timer completed broadcast received")
                    // Re-enable all buttons and update UI
                    btnStartTimer.isEnabled = true
                    btnStartTimer.text = "Start Timer"
                    btnStopTimer.isEnabled = true
                    btnStopTimer.text = "Stop Timer"
                    btnStopActiveTimer.isEnabled = true
                    btnStopActiveTimer.text = "Stop Timer"
                    forceRefreshUI()
                }
                intent?.hasExtra("remaining_time") == true -> {
                    updateTimerDisplay()
                }
                intent?.getBooleanExtra("timer_extended", false) == true -> {
                    Log.d("MainActivity", "Timer extended broadcast received")
                    forceRefreshUI()
                }
            }
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
    
    private fun forceRefreshUI() {
        // Force refresh all UI elements
        updateTimerUI()
        updateBlockingStatus()
        updateTimerDisplay()
        
        // Log current state for debugging
        try {
            val isTimerActive = preferenceDataHelper.isTimerActive()
            val remainingTime = preferenceDataHelper.getRemainingTimerTime()
            val isTimerBlocking = preferenceDataHelper.isTimerBlockingEnabled()
            
            Log.d("MainActivity", "Force refresh - Timer active: $isTimerActive, Remaining: $remainingTime, Blocking: $isTimerBlocking")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in forceRefreshUI", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        forceRefreshUI()
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
        
        // Add long press on timer display to refresh state
        tvTimerDisplay.setOnLongClickListener {
            refreshTimerState()
            true
        }
        
        // Add long press on blocking status to refresh
        tvBlockingStatus.setOnLongClickListener {
            refreshTimerState()
            true
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
        // Provide immediate visual feedback
        btnStartTimer.isEnabled = false
        btnStartTimer.text = "Starting..."
        
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_START_TIMER
        intent.putExtra(TimerService.EXTRA_TIMER_DURATION, durationMinutes)
        startForegroundService(intent)
        
        // Ensure AppBlockService is running to handle blocking
        ensureAppBlockServiceRunning()
        
        // Update UI immediately to show timer is starting
        showTimerStartingState()
        
        // Schedule a delayed UI update to ensure synchronization
        handler.postDelayed({
            updateTimerUI()
            updateBlockingStatus()
        }, 500) // 500ms delay to allow service to update preferences
    }
    
    private fun stopFocusTimer() {
        // Provide immediate visual feedback
        btnStopTimer.isEnabled = false
        btnStopTimer.text = "Stopping..."
        btnStopActiveTimer.isEnabled = false
        btnStopActiveTimer.text = "Stopping..."
        
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_STOP_TIMER
        startService(intent)
        
        // Update UI immediately to show timer is stopping
        showTimerStoppingState()
        
        // Schedule a delayed UI update to ensure synchronization
        handler.postDelayed({
            updateTimerUI()
            updateBlockingStatus()
            // Re-enable buttons
            btnStartTimer.isEnabled = true
            btnStartTimer.text = "Start Timer"
            btnStopTimer.isEnabled = true
            btnStopTimer.text = "Stop Timer"
            btnStopActiveTimer.isEnabled = true
            btnStopActiveTimer.text = "Stop Timer"
        }, 500) // 500ms delay to allow service to update preferences
    }
    
    private fun extendFocusTimer(extendMinutes: Int) {
        // Provide immediate visual feedback
        btnExtendTimer.isEnabled = false
        btnExtendTimer.text = "Extending..."
        
        val intent = Intent(this, TimerService::class.java)
        intent.action = TimerService.ACTION_EXTEND_TIMER
        intent.putExtra(TimerService.EXTRA_EXTEND_MINUTES, extendMinutes)
        startService(intent)
        
        // Re-enable button after a short delay
        handler.postDelayed({
            btnExtendTimer.isEnabled = true
            btnExtendTimer.text = "Extend (+15min)"
        }, 1000)
    }
    
    private fun ensureAppBlockServiceRunning() {
        // Check if AppBlockService is running, if not start it
        val isAppBlockServiceRunning = isServiceRunning(AppBlockService::class.java)
        if (!isAppBlockServiceRunning) {
            startService(Intent(this, AppBlockService::class.java))
        }
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    private fun updateTimerUI() {
        try {
            val isTimerActive = preferenceDataHelper.isTimerActive()
            val remainingTime = preferenceDataHelper.getRemainingTimerTime()
            
            Log.d("MainActivity", "updateTimerUI: isTimerActive=$isTimerActive, remainingTime=$remainingTime")
            
            if (isTimerActive && remainingTime > 0) {
                // Timer is active and has remaining time
                layoutTimerControls.visibility = View.GONE
                layoutActiveTimer.visibility = View.VISIBLE
                updateTimerDisplay()
                
                // Ensure stop button is properly configured
                btnStopActiveTimer.isEnabled = true
                btnStopActiveTimer.text = "Stop Timer"
                
            } else {
                // Timer is not active or has no remaining time
                layoutTimerControls.visibility = View.VISIBLE
                layoutActiveTimer.visibility = View.GONE
                tvTimerDisplay.text = "00:00"
                
                // Ensure start button is properly configured
                btnStartTimer.isEnabled = true
                btnStartTimer.text = "Start Timer"
                btnStopTimer.isEnabled = true
                btnStopTimer.text = "Stop Timer"
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in updateTimerUI", e)
            // Fallback to default state
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
                    try {
                        if (preferenceDataHelper.isTimerActive()) {
                            updateTimerDisplay()
                            updateBlockingStatus()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error in timer update loop", e)
                    }
                }
            }
        }, 0, 1000) // Update every second
    }
    
    private fun stopTimerUpdateLoop() {
        timerUpdateTimer?.cancel()
        timerUpdateTimer = null
    }
    
    // Add a method to manually refresh timer state
    private fun refreshTimerState() {
        handler.post {
            forceRefreshUI()
        }
    }

    private fun checkForPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showTimerStartingState() {
        // Show loading state while timer is starting
        layoutTimerControls.visibility = View.GONE
        layoutActiveTimer.visibility = View.VISIBLE
        tvTimerDisplay.text = "Starting..."
        btnStopActiveTimer.isEnabled = false
        btnStopActiveTimer.text = "Starting..."
    }
    
    private fun showTimerStoppingState() {
        // Show stopping state while timer is stopping
        layoutTimerControls.visibility = View.GONE
        layoutActiveTimer.visibility = View.VISIBLE
        tvTimerDisplay.text = "Stopping..."
        btnStopActiveTimer.isEnabled = false
        btnStopActiveTimer.text = "Stopping..."
    }
}
