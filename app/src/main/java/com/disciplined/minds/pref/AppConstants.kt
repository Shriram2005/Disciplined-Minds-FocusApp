package com.disciplined.minds.pref

/**
 * Constant keys used throughout the app.
 */
object AppConstants {
    const val DRAW_OVERLAY_SETTING_REQ_CODE = 101
    const val NOTIFICATION_ACCESS_REQ_CODE = 102
    const val USER_APP_ACCESS_REQ_CODE = 103
    const val MODIFY_TIME_REQ_CODE = 104
    const val BACKGROUND_SETTING_REQ_CODE = 105

    const val STUDY_MODE = "study_mode"
    const val FOCUS_STUDY_SESSION = "focus_study_session"
    const val GUIDED_MEDITATION = "guided_meditation"
    const val OBJECT_GAZING = "object_gazing"

    const val XIAOMI = "Xiaomi"

    const val ENABLE_NOTIFICATION_LISTENER = "enabled_notification_listeners"

    val DefaultApp = arrayOf(
        "Camera",
        "Clock",
        "Contacts",
        "Gallery",
        "Gmail",
        "Google Play Store",
        "Messages",
        "Phone",
        "Settings"
    )
}
