package com.disciplinedminds.ui.schedule

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplinedminds.pref.PreferenceDataHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceHelper = PreferenceDataHelper.getInstance(application)
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    init {
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            val schedulesList = getSchedulesFromPrefs()
            _schedules.value = schedulesList
        }
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val currentSchedules = _schedules.value.toMutableList()
            currentSchedules.add(schedule)
            saveSchedules(currentSchedules)
            _schedules.value = currentSchedules
            
            if (schedule.isEnabled) {
                scheduleAlarms(schedule)
            }
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val currentSchedules = _schedules.value.toMutableList()
            val index = currentSchedules.indexOfFirst { it.id == schedule.id }
            if (index != -1) {
                currentSchedules[index] = schedule
                saveSchedules(currentSchedules)
                _schedules.value = currentSchedules
                
                // Cancel old alarms and schedule new ones
                cancelScheduleAlarms(schedule.id)
                if (schedule.isEnabled) {
                    scheduleAlarms(schedule)
                }
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            val currentSchedules = _schedules.value.toMutableList()
            currentSchedules.removeAll { it.id == scheduleId }
            saveSchedules(currentSchedules)
            _schedules.value = currentSchedules
            
            cancelScheduleAlarms(scheduleId)
        }
    }

    fun toggleSchedule(scheduleId: String) {
        viewModelScope.launch {
            val currentSchedules = _schedules.value.toMutableList()
            val index = currentSchedules.indexOfFirst { it.id == scheduleId }
            if (index != -1) {
                val schedule = currentSchedules[index]
                val updatedSchedule = schedule.copy(isEnabled = !schedule.isEnabled)
                currentSchedules[index] = updatedSchedule
                saveSchedules(currentSchedules)
                _schedules.value = currentSchedules
                
                if (updatedSchedule.isEnabled) {
                    scheduleAlarms(updatedSchedule)
                } else {
                    cancelScheduleAlarms(scheduleId)
                }
            }
        }
    }

    private fun scheduleAlarms(schedule: Schedule) {
        val context = getApplication<Application>()
        
        schedule.daysOfWeek.forEach { dayOfWeek ->
            // Schedule start alarm
            val startIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ScheduleReceiver.ACTION_START_TIMER
                putExtra("schedule_id", schedule.id)
                putExtra("schedule_name", schedule.name)
                putExtra("start_time", schedule.startTime)
                putExtra("end_time", schedule.endTime)
            }
            
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                "${schedule.id}_${dayOfWeek}_start".hashCode(),
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val startCalendar = getNextOccurrence(schedule.startTime, dayOfWeek)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        startCalendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        startPendingIntent
                    )
                }
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startCalendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    startPendingIntent
                )
            }
            
            // Schedule end alarm
            val endIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ScheduleReceiver.ACTION_STOP_TIMER
                putExtra("schedule_id", schedule.id)
            }
            
            val endPendingIntent = PendingIntent.getBroadcast(
                context,
                "${schedule.id}_${dayOfWeek}_end".hashCode(),
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val endCalendar = getNextOccurrence(schedule.endTime, dayOfWeek)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        endCalendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        endPendingIntent
                    )
                }
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    endCalendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    endPendingIntent
                )
            }
        }
    }

    private fun cancelScheduleAlarms(scheduleId: String) {
        val context = getApplication<Application>()
        
        // Cancel all possible day combinations (1-7)
        for (dayOfWeek in 1..7) {
            val startIntent = Intent(context, ScheduleReceiver::class.java)
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                "${scheduleId}_${dayOfWeek}_start".hashCode(),
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(startPendingIntent)
            
            val endIntent = Intent(context, ScheduleReceiver::class.java)
            val endPendingIntent = PendingIntent.getBroadcast(
                context,
                "${scheduleId}_${dayOfWeek}_end".hashCode(),
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(endPendingIntent)
        }
    }

    private fun getNextOccurrence(time: String, dayOfWeek: Int): Calendar {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Adjust for day of week (Calendar uses 1=Sunday, we use 1=Monday)
        val targetDayOfWeek = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        var daysToAdd = targetDayOfWeek - currentDayOfWeek
        if (daysToAdd < 0) {
            daysToAdd += 7
        } else if (daysToAdd == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
            daysToAdd = 7
        }
        
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        
        return calendar
    }

    private fun saveSchedules(schedules: List<Schedule>) {
        val gson = Gson()
        val json = gson.toJson(schedules)
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("schedules", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("schedule_list", json).apply()
    }

    private fun getSchedulesFromPrefs(): List<Schedule> {
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("schedules", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("schedule_list", null) ?: return emptyList()
        val type = object : TypeToken<List<Schedule>>() {}.type
        return Gson().fromJson(json, type)
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ScheduleViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
