package com.disciplined.minds.applist.views

import com.disciplined.minds.applist.MVPBaseView
import com.disciplined.minds.applist.presenters.AppListPresenter


interface AppListView : MVPBaseView<AppListPresenter> {
    fun initView()
}