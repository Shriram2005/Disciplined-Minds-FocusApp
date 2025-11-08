package com.disciplined.minds.pref

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Wrapper over SharedPreferences used by services and view models.
 */
class PreferenceDataHelper private constructor(context: Context) {

    private val sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context)

    companion object {
        private const val APP_LIST = "app_list"
        private const val IS_STUDY_MODE = "is_study_mode"
        private const val IS_FOCUSED_STUDY_SESSION = "is_focused_study_session"
        private const val IS_START_IN_BACKGROUND = "is_start_in_background"
        private const val STUDY_TIME = "study_time"
        private const val MEDITATION_TYPE = "meditation_type"
        private const val OBJECT_GAZING_TYPE = "object_gazing_type"
        private const val TIMER_DURATION = "timer_duration"
        private const val TIMER_START_TIME = "timer_start_time"
        private const val IS_TIMER_ACTIVE = "is_timer_active"
        private const val IS_TIMER_BLOCKING_ENABLED = "is_timer_blocking_enabled"

        @Volatile
        private var instance: PreferenceDataHelper? = null

        fun getInstance(context: Context): PreferenceDataHelper = instance ?: synchronized(this) {
            instance ?: PreferenceDataHelper(context.applicationContext).also { instance = it }
        }
    }

    fun setAppList(dict: HashMap<String, Boolean>) {
        val dictString = Gson().toJson(dict)
        sharedPreferenceHelper.setString(APP_LIST, dictString)
    }

    fun getAppList(): HashMap<String, Boolean>? {
        val json = sharedPreferenceHelper.getString(APP_LIST)
        if (json.isNullOrBlank()) return null
        val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun isStudyMode(): Boolean = sharedPreferenceHelper.getBoolean(IS_STUDY_MODE)

    fun setStudyMode(isStudyMode: Boolean) {
        sharedPreferenceHelper.setBoolean(IS_STUDY_MODE, isStudyMode)
    }

    fun isFocusedStudySession(): Boolean = sharedPreferenceHelper.getBoolean(IS_FOCUSED_STUDY_SESSION)

    fun setFocusedStudySession(value: Boolean) {
        sharedPreferenceHelper.setBoolean(IS_FOCUSED_STUDY_SESSION, value)
    }

    fun isStartInBackground(): Boolean = sharedPreferenceHelper.getBoolean(IS_START_IN_BACKGROUND)

    fun setStartInBackground(value: Boolean) {
        sharedPreferenceHelper.setBoolean(IS_START_IN_BACKGROUND, value)
    }

    fun setStudyTime(studyTime: Int) {
        sharedPreferenceHelper.setInt(STUDY_TIME, studyTime)
    }

    fun getStudyTime(): Int = sharedPreferenceHelper.getInt(STUDY_TIME)

    fun setMeditationType(type: String) {
        sharedPreferenceHelper.setString(MEDITATION_TYPE, type)
    }

    fun getMeditationType(): String? = sharedPreferenceHelper.getString(MEDITATION_TYPE)

    fun setObjectGazingType(type: String) {
        sharedPreferenceHelper.setString(OBJECT_GAZING_TYPE, type)
    }

    fun getObjectGazingType(): String? = sharedPreferenceHelper.getString(OBJECT_GAZING_TYPE)

    fun firstTimeAskingPermission(permission: String, isFirstTime: Boolean) {
        sharedPreferenceHelper.setBoolean(permission, isFirstTime)
    }

    fun isFirstTimeAskingPermission(permission: String): Boolean = sharedPreferenceHelper.getBoolean(permission, true)

    fun setTimerDuration(durationMinutes: Int) {
        sharedPreferenceHelper.setInt(TIMER_DURATION, durationMinutes)
    }

    fun getTimerDuration(): Int = sharedPreferenceHelper.getInt(TIMER_DURATION, 30)

    fun setTimerStartTime(startTime: Long) {
        sharedPreferenceHelper.setLong(TIMER_START_TIME, startTime)
    }

    fun getTimerStartTime(): Long = sharedPreferenceHelper.getLong(TIMER_START_TIME, 0L)

    fun setTimerActive(isActive: Boolean) {
        sharedPreferenceHelper.setBoolean(IS_TIMER_ACTIVE, isActive)
    }

    fun isTimerActive(): Boolean = sharedPreferenceHelper.getBoolean(IS_TIMER_ACTIVE)

    fun setTimerBlockingEnabled(enabled: Boolean) {
        sharedPreferenceHelper.setBoolean(IS_TIMER_BLOCKING_ENABLED, enabled)
    }

    fun isTimerBlockingEnabled(): Boolean = sharedPreferenceHelper.getBoolean(IS_TIMER_BLOCKING_ENABLED)

    fun getRemainingTimerTime(): Long {
        if (!isTimerActive()) return 0L
        val startTime = getTimerStartTime()
        val duration = getTimerDuration() * 60 * 1000L
        val elapsed = System.currentTimeMillis() - startTime
        return if (elapsed >= duration) 0L else duration - elapsed
    }
}
