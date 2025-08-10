package com.disciplined.minds.applist.views

import com.disciplined.minds.applist.MVPBaseView
import com.disciplined.minds.applist.presenters.AppListFragmentPresenter


interface AppListFragmentView : MVPBaseView<AppListFragmentPresenter> {
    fun initViews()
}