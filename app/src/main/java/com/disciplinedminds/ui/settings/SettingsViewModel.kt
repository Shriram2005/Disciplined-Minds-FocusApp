package com.disciplinedminds.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplinedminds.pref.PreferenceDataHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceDataHelper.getInstance(application)

    private val _darkTheme = MutableStateFlow(prefs.isDarkThemeEnabled())
    val darkTheme: StateFlow<Boolean> = _darkTheme

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDarkThemeEnabled(enabled)
            _darkTheme.value = enabled
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
