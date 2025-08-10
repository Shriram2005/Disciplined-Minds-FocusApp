package com.disciplined.minds.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Square Infosoft.
 */

class SharedPreferenceHelper private constructor(appContext: Context) {
    private val mPrefs: SharedPreferences
    private fun getmPrefs(): SharedPreferences {
        return mPrefs
    }

    /**
     * Clears all data in SharedPreferences
     */
    fun clearPrefs() {
        val editor = getmPrefs().edit()
        editor.clear()
        editor.commit()
    }

    fun removeKey(key: String?) {
        getmPrefs().edit().remove(key).commit()
    }

    fun containsKey(key: String?): Boolean {
        return getmPrefs().contains(key)
    }

    fun getString(key: String?, defValue: String?): String? {
        return getmPrefs().getString(key, defValue)
    }

    fun getString(key: String?): String? {
        return getString(key, null)
    }

    fun setString(key: String?, value: String?) {
        val editor = getmPrefs().edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getInt(key: String?, defValue: Int): Int {
        return getmPrefs().getInt(key, defValue)
    }

    fun getInt(key: String?): Int {
        return getInt(key, 0)
    }

    fun setInt(key: String?, value: Int) {
        val editor = getmPrefs().edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getLong(key: String?, defValue: Long): Long {
        return getmPrefs().getLong(key, defValue)
    }

    fun getLong(key: String?): Long {
        return getLong(key, 0L)
    }

    fun setLong(key: String?, value: Long) {
        val editor = getmPrefs().edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return getmPrefs().getBoolean(key, defValue)
    }

    fun getBoolean(key: String?): Boolean {
        return getBoolean(key, false)
    }

    fun setBoolean(key: String?, value: Boolean) {
        val editor = getmPrefs().edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getFloat(key: String?): Float {
        return getmPrefs().getFloat(key, 0f)
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return if (containsKey(key)) getFloat(key) else defValue
    }

    fun setFloat(key: String?, value: Float?) {
        val editor = getmPrefs().edit()
        editor.putFloat(key, value!!)
        editor.apply()
    }

    companion object {
        const val PREF_NAME = BuildConfig.PACKAGE_NAME
        private var sInstance: SharedPreferenceHelper? = null

        /**
         * Returns unique sInstance of SharedPreferenceHelper class
         * Throws illegalStateException if @link #initialize(Context) not called first
         *
         * @return unique SharedPrefsHelper sInstance
         */
        fun getsInstance(): SharedPreferenceHelper? {
            checkNotNull(sInstance) {
                "SharedPrefsHelper is not initialized, call initialize(applicationContext) " +
                        "static method first"
            }
            return sInstance
        }

        /**
         * Creates a new sInstance of SharedPreferenceHelper if not created already
         * Should be called before accessing sInstance
         * Called in @Link #haygot.togyah.app.common.application.TopprApplication.onCreate()
         *
         * @param appContext
         */
        @Synchronized
        fun initialize(appContext: Context) {
            if (sInstance == null) {
                sInstance = SharedPreferenceHelper(appContext)
            }
        }
    }

    init {
        mPrefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}