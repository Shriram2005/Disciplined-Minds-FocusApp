package com.disciplined.minds.applist.presenters
import com.disciplined.minds.applist.MVPBasePresenter
import com.disciplined.minds.applist.views.AppListView


/**
 * Created by Square Infosoft.
 */


class AppListPresenter : MVPBasePresenter<AppListView>() {

    fun onActivityCreated() {
        view.initView()
    }

}