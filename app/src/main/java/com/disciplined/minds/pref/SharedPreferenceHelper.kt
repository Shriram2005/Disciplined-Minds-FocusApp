package com.disciplined.minds.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple SharedPreferences helper used by the legacy background services.
 */
class SharedPreferenceHelper private constructor(appContext: Context) {
    private val prefs: SharedPreferences = appContext.getSharedPreferences(appContext.packageName, Context.MODE_PRIVATE)

    fun clearPrefs() {
        prefs.edit().clear().apply()
    }

    fun removeKey(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun containsKey(key: String): Boolean = prefs.contains(key)

    fun getString(key: String, defValue: String? = null): String? = prefs.getString(key, defValue)

    fun setString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    fun getInt(key: String, defValue: Int = 0): Int = prefs.getInt(key, defValue)

    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getLong(key: String, defValue: Long = 0L): Long = prefs.getLong(key, defValue)

    fun setLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String, defValue: Boolean = false): Boolean = prefs.getBoolean(key, defValue)

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getFloat(key: String, defValue: Float = 0f): Float = prefs.getFloat(key, defValue)

    fun setFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    companion object {
        @Volatile
        private var instance: SharedPreferenceHelper? = null

        fun getInstance(context: Context): SharedPreferenceHelper = instance ?: synchronized(this) {
            instance ?: SharedPreferenceHelper(context.applicationContext).also { instance = it }
        }

        fun initialize(context: Context) {
            getInstance(context)
        }
    }
}
