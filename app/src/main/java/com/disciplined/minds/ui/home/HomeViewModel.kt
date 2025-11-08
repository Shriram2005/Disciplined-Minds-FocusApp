package com.disciplined.minds.ui.home

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplined.minds.applist.service.AppBlockService
import com.disciplined.minds.pref.PreferenceDataHelper
import com.disciplined.minds.timer.service.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceDataHelper = PreferenceDataHelper.getInstance(application)
    private val _selectedDuration = MutableStateFlow(30)
    val selectedDuration: StateFlow<Int> = _selectedDuration

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var tickerJob: Job? = null

    init {
        refreshState()
    }

    fun refreshState() {
        viewModelScope.launch {
            updateUiState()
        }
    }

    fun updateSelectedDuration(minutes: Int) {
        _selectedDuration.value = minutes
    }

    fun startFocusTimer() {
        val context = getApplication<Application>()
        val durationMinutes = selectedDuration.value
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_TIMER_DURATION, durationMinutes)
        }
        ContextCompat.startForegroundService(context, intent)
        ensureAppBlockServiceRunning()
        refreshState()
    }

    fun stopFocusTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_STOP_TIMER }
        context.startService(intent)
        refreshState()
    }

    fun extendFocusTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_EXTEND_TIMER
            putExtra(TimerService.EXTRA_EXTEND_MINUTES, 15)
        }
        context.startService(intent)
        refreshState()
    }

    fun toggleStudyMode(enabled: Boolean) {
        preferenceDataHelper.setStudyMode(enabled)
        if (enabled) {
            ensureAppBlockServiceRunning()
        }
        refreshState()
    }

    private fun ensureAppBlockServiceRunning() {
        val context = getApplication<Application>()
        if (!isServiceRunning(AppBlockService::class.java)) {
            val intent = Intent(context, AppBlockService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private suspend fun updateUiState() {
        val remainingTime = preferenceDataHelper.getRemainingTimerTime()
        val isTimerActive = preferenceDataHelper.isTimerActive() && remainingTime > 0
        val isStudyMode = preferenceDataHelper.isStudyMode()
        val appList = preferenceDataHelper.getAppList()
        val lockedCount = appList?.values?.count { it } ?: 0
        val unlockedCount = appList?.size?.minus(lockedCount) ?: 0
        val isBlocking = isStudyMode || isTimerActive

        _uiState.value = HomeUiState(
            isTimerActive = isTimerActive,
            remainingTimeMillis = remainingTime,
            isBlocking = isBlocking,
            isStudyMode = isStudyMode,
            lockedApps = lockedCount,
            unlockedApps = unlockedCount
        )

        if (isTimerActive) {
            startTicker()
        } else {
            stopTicker()
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val remaining = preferenceDataHelper.getRemainingTimerTime()
                val isTimerActive = preferenceDataHelper.isTimerActive() && remaining > 0
                val isStudyMode = preferenceDataHelper.isStudyMode()
                _uiState.value = _uiState.value.copy(
                    isTimerActive = isTimerActive,
                    remainingTimeMillis = remaining,
                    isBlocking = isStudyMode || isTimerActive
                )
                if (!isTimerActive) {
                    stopTicker()
                    break
                }
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun handleTimerBroadcast() {
        refreshState()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class HomeUiState(
    val isTimerActive: Boolean = false,
    val remainingTimeMillis: Long = 0L,
    val isBlocking: Boolean = false,
    val isStudyMode: Boolean = false,
    val lockedApps: Int = 0,
    val unlockedApps: Int = 0
) {
    val timerDisplay: String
        get() {
            val totalSeconds = (remainingTimeMillis / 1000).coerceAtLeast(0)
            val minutes = (totalSeconds / 60)
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val blockingStatusLabel: String
        get() = when {
            isTimerActive -> "Timer Active"
            isStudyMode -> "Study Mode Active"
            else -> "Inactive"
        }
}
