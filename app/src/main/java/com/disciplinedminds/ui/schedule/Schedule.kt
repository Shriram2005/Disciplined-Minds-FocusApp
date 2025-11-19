package com.disciplinedminds.ui.schedule

data class Schedule(
    val id: String,
    val name: String,
    val startTime: String, // Format: HH:mm
    val endTime: String,   // Format: HH:mm
    val daysOfWeek: List<Int>, // 1=Monday, 7=Sunday
    val isEnabled: Boolean = true
)
