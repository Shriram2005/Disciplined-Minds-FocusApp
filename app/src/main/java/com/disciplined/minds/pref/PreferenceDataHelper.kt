
package com.disciplined.minds.pref

import android.content.Context
import com.disciplined.minds.pref.SharedPreferenceHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Square Infosoft.
 */

class PreferenceDataHelper private constructor(context: Context) {

    private val mSharedPreferenceHelper: SharedPreferenceHelper




    companion object {

        private const val APP_LIST = "app_list"
        private const val IS_STUDY_MODE = "is_study_mode"
        private const val IS_FOCUSED_STUDY_SESSION = "is_focused_study_session"
        private const val IS_START_IN_BACKGROUND = "is_start_in_background"
        private const val STUDY_TIME = "study_time"
        private const val MEDITATION_TYPE = "meditation_type"
        private const val OBJECT_GAZING_TYPE = "object_gazing_type"
        
        // Timer-related constants
        private const val TIMER_DURATION = "timer_duration"
        private const val TIMER_START_TIME = "timer_start_time"
        private const val IS_TIMER_ACTIVE = "is_timer_active"
        private const val IS_TIMER_BLOCKING_ENABLED = "is_timer_blocking_enabled"
        
        private var sInstance: PreferenceDataHelper? = null

        @Synchronized
        fun getInstance(context: Context): PreferenceDataHelper? {
            if (sInstance == null) {
                sInstance = PreferenceDataHelper(context)
            }
            return sInstance
        }
    }

    init {
        SharedPreferenceHelper.initialize(context)
        mSharedPreferenceHelper = SharedPreferenceHelper.getsInstance()!!
    }

    fun setAppList(dict: HashMap<String, Boolean>) {
        val dictString: String = Gson().toJson(dict)
        mSharedPreferenceHelper.setString(APP_LIST, dictString)
    }

    fun getAppList(): HashMap<String, Boolean>? {
        val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
        return Gson().fromJson(mSharedPreferenceHelper.getString(APP_LIST), type)
    }

    fun isStudyMode(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_STUDY_MODE, false)
    }

    fun setStudyMode(isStudyMode: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_STUDY_MODE, isStudyMode)
    }

    fun isFocusedStudySession(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_FOCUSED_STUDY_SESSION, false)
    }

    fun setFocusedStudySession(isFocusedStudySession: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_FOCUSED_STUDY_SESSION, isFocusedStudySession)
    }

    fun isStartInBackground(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_START_IN_BACKGROUND, false)
    }

    fun setStartInBackground(isFocusedStudySession: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_START_IN_BACKGROUND, isFocusedStudySession)
    }

    fun setStudyTime(studyTime: Int) {
        return mSharedPreferenceHelper.setInt(STUDY_TIME, studyTime)
    }

    fun getStudyTime(): Int {
        return mSharedPreferenceHelper.getInt(STUDY_TIME)
    }

    fun setMeditationType(meditationType: String) {
        return mSharedPreferenceHelper.setString(MEDITATION_TYPE, meditationType)
    }

    fun getMeditationType(): String? {
        return mSharedPreferenceHelper.getString(MEDITATION_TYPE)
    }

    fun setObjectGazingType(objectGazingType: String) {
        return mSharedPreferenceHelper.setString(OBJECT_GAZING_TYPE, objectGazingType)
    }

    fun getObjectGazingType(): String? {
        return mSharedPreferenceHelper.getString(OBJECT_GAZING_TYPE)
    }

    fun firstTimeAskingPermission(permission: String, isFirstTime: Boolean) {
        mSharedPreferenceHelper.setBoolean(permission, isFirstTime)
    }

    fun isFirstTimeAskingPermission(permission: String): Boolean {
        return mSharedPreferenceHelper.getBoolean(permission, true)
    }
    
    // Timer-related methods
    fun setTimerDuration(durationMinutes: Int) {
        mSharedPreferenceHelper.setInt(TIMER_DURATION, durationMinutes)
    }
    
    fun getTimerDuration(): Int {
        return mSharedPreferenceHelper.getInt(TIMER_DURATION, 30) // Default 30 minutes
    }
    
    fun setTimerStartTime(startTime: Long) {
        mSharedPreferenceHelper.setLong(TIMER_START_TIME, startTime)
    }
    
    fun getTimerStartTime(): Long {
        return mSharedPreferenceHelper.getLong(TIMER_START_TIME, 0L)
    }
    
    fun setTimerActive(isActive: Boolean) {
        mSharedPreferenceHelper.setBoolean(IS_TIMER_ACTIVE, isActive)
    }
    
    fun isTimerActive(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_TIMER_ACTIVE, false)
    }
    
    fun setTimerBlockingEnabled(enabled: Boolean) {
        mSharedPreferenceHelper.setBoolean(IS_TIMER_BLOCKING_ENABLED, enabled)
    }
    
    fun isTimerBlockingEnabled(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_TIMER_BLOCKING_ENABLED, false)
    }
    
    fun getRemainingTimerTime(): Long {
        if (!isTimerActive()) return 0L
        
        val startTime = getTimerStartTime()
        val duration = getTimerDuration() * 60 * 1000L // Convert minutes to milliseconds
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - startTime
        
        return if (elapsed >= duration) {
            0L
        } else {
            duration - elapsed
        }
    }
}