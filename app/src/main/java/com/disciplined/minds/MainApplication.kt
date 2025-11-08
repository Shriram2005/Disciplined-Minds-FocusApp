package com.disciplined.minds

import android.app.Application
import com.disciplined.minds.pref.SharedPreferenceHelper

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferenceHelper.initialize(this)
    }
}
