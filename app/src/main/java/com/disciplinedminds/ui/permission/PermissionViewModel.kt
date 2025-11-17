package com.disciplinedminds.ui.permission

import android.app.Application
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.disciplinedminds.utils.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Handles permission state for usage access, overlay, and notification listener permissions.
 */
class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val hasUsageAccess = StringUtils.isAccessGranted(context)
            val hasOverlay =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
            val hasNotificationAccess = StringUtils.isNotificationServiceEnabled(context)
            _permissionState.value = PermissionState(
                hasUsageAccess = hasUsageAccess,
                hasOverlay = hasOverlay,
                hasNotificationAccess = hasNotificationAccess,
                shouldShowUsageRationale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasUsageAccess
            )
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PermissionViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

/**
 * Aggregated permission status for gatekeeping app access.
 */
data class PermissionState(
    val hasUsageAccess: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val shouldShowUsageRationale: Boolean = false
) {
    val allGranted: Boolean
        get() = hasUsageAccess && hasOverlay
}
