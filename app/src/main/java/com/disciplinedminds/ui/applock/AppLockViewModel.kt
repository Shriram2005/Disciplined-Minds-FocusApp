package com.disciplinedminds.ui.applock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplinedminds.applist.AppInfo
import com.disciplinedminds.pref.PreferenceDataHelper
import com.disciplinedminds.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppLockViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceHelper = PreferenceDataHelper.getInstance(application)
    private val _uiState = MutableStateFlow(AppLockUiState(isLoading = true))
    val uiState: StateFlow<AppLockUiState> = _uiState

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val installedApps = AppUtils.getInstalledApplications(context)
            val preferenceMap = preferenceHelper.getAppList()?.toMutableMap() ?: mutableMapOf<String, Boolean>().also { map ->
                installedApps.forEach { app ->
                    val pkg = app.applicationPackage ?: return@forEach
                    val locked = !(app.isOpen ?: true)
                    map[pkg] = locked
                }
                preferenceHelper.setAppList(HashMap(map))
            }

            // Ensure new apps default to locked
            installedApps.forEach { app ->
                val packageName = app.applicationPackage ?: return@forEach
                if (preferenceMap[packageName] == null) {
                    preferenceMap[packageName] = true
                }
            }
            preferenceHelper.setAppList(HashMap(preferenceMap))

            val items = installedApps.mapNotNull { app ->
                val packageName = app.applicationPackage ?: return@mapNotNull null
                val appName = app.applicationName ?: return@mapNotNull null
                val locked = preferenceMap[packageName] ?: true
                AppLockItem(
                    appName = appName,
                    packageName = packageName,
                    icon = app.applicationIcon,
                    isLocked = locked
                )
            }.sortedBy { it.appName.lowercase() }

            _uiState.value = AppLockUiState(
                isLoading = false,
                locked = items.filter { it.isLocked },
                unlocked = items.filterNot { it.isLocked }
            )
        }
    }

    fun toggleLock(item: AppLockItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = preferenceHelper.getAppList() ?: hashMapOf()
            map[item.packageName] = !item.isLocked
            preferenceHelper.setAppList(map)
            loadApplications()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppLockViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AppLockViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class AppLockUiState(
    val isLoading: Boolean = false,
    val locked: List<AppLockItem> = emptyList(),
    val unlocked: List<AppLockItem> = emptyList()
)

data class AppLockItem(
    val appName: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable?,
    val isLocked: Boolean
)
