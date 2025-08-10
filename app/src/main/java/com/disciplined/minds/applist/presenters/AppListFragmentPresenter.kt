package com.disciplined.minds.applist.presenters
import com.disciplined.minds.applist.MVPBasePresenter
import com.disciplined.minds.applist.views.AppListFragmentView


/**
 * Created by Square Infosoft.
 */

class AppListFragmentPresenter : MVPBasePresenter<AppListFragmentView>() {


    fun onActivityCreated() {
        view.initViews()
    }
}