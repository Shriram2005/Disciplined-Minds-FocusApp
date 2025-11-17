package com.disciplinedminds

import android.app.Application
import com.disciplinedminds.pref.SharedPreferenceHelper

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferenceHelper.initialize(this)
    }
}
