package com.disciplined.minds.applist

import android.graphics.drawable.Drawable
import java.io.Serializable

/**
 * Represents a single installed application along with its lock state.
 */
class AppInfo : Serializable {
    var applicationName: String? = null
    @Transient
    var applicationIcon: Drawable? = null
    var applicationPackage: String? = null
    var isOpen: Boolean? = null
}
