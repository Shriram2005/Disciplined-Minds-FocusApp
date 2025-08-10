package com.disciplined.minds.applist.listeners

import com.disciplined.minds.applist.AppInfo

interface UpdateAppList {
    fun updateList(type: Int, item: AppInfo)

}