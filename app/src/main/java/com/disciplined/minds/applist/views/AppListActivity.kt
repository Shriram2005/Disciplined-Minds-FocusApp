package com.disciplined.minds.applist.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.ViewPager
import com.disciplined.minds.R
import com.disciplined.minds.applist.AppInfo
import com.disciplined.minds.applist.BaseActivity
import com.disciplined.minds.applist.adapters.ViewPagerAdapter
import com.disciplined.minds.applist.listeners.UpdateAppList
import com.disciplined.minds.applist.presenters.AppListPresenter
import com.google.android.material.tabs.TabLayout

/**
 * Created by Square Infosoft.
 */

class AppListActivity : BaseActivity<AppListPresenter>(), AppListView, UpdateAppList {

    private lateinit var openAppFragment: AppListFragment
    private lateinit var lockAppFragment: AppListFragment
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var tabLayout : TabLayout
    private lateinit var ivBack : AppCompatImageView
    private lateinit var tvTittle : AppCompatTextView
    private lateinit var pbApp : ProgressBar
    private lateinit var viewPager : ViewPager
    companion object {
        fun start(context: AppCompatActivity) {
            val intent = Intent(context, AppListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        presenter.onActivityCreated()

    }

    override fun initView() {
        tabLayout = findViewById<TabLayout>(R.id.tabLayout) as TabLayout
        ivBack = findViewById<AppCompatImageView>(R.id.ivBack) as AppCompatImageView
        tvTittle = findViewById<AppCompatTextView>(R.id.tvTittle) as AppCompatTextView
        pbApp = findViewById<ProgressBar>(R.id.pbApp) as ProgressBar
        viewPager = findViewById<ViewPager>(R.id.viewPager) as ViewPager

        ivBack.visibility = View.VISIBLE
        tvTittle.text = getString(R.string.configure_app)
        ivBack.setOnClickListener { onBackPressed() }
        setupTabLayout()
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.open_app)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.locked_apps)))
        adapter = ViewPagerAdapter(supportFragmentManager)
        pbApp.visibility = View.VISIBLE
        LoadInstallApp().execute()
    }

    private fun addLockAppFragment(installApp: ArrayList<AppInfo>) {
        lockAppFragment = AppListFragment.newInstance(2, installApp)
        lockAppFragment.setClickInterface(this)
        adapter.addFragment(lockAppFragment, getString(R.string.locked_apps))
    }

    private fun addOpenAppFragment(installApp: ArrayList<AppInfo>) {
        openAppFragment = AppListFragment.newInstance(1, installApp)
        openAppFragment.setClickInterface(this)
        adapter.addFragment(openAppFragment, getString(R.string.open_app))
    }

    override fun getNewPresenter(): AppListPresenter {
        return AppListPresenter()
    }

    override fun updateList(type: Int, item: AppInfo) {
        if (type == 1) {
            openAppFragment.updateAppList(item)
        } else {
            lockAppFragment.updateAppList(item)
        }
        updateTabAppCount()
    }

    private fun updateTabAppCount() {
        var openAppCount = 0
        var lockAppCount = 0
        val appList = PreferenceDataHelper.getInstance(this)!!.getAppList()
        if (appList != null) {
            for (element in appList.values) {
                if (element) {
                    lockAppCount += 1
                } else {
                    openAppCount += 1
                }
            }
            val tab = tabLayout.getTabAt(0)
            tab!!.text = getString(R.string.open_app) + " [" + openAppCount + "]"
            val tab1 = tabLayout.getTabAt(1)
            tab1!!.text = getString(R.string.locked_apps) + " [" + lockAppCount + "]"
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadInstallApp : AsyncTask<Void, Void, Void>() {
        private var installApp = ArrayList<AppInfo>()

        override fun doInBackground(vararg params: Void?): Void? {
            installApp =
                    AppUtils.getInstalledApplications(this@AppListActivity) as ArrayList<AppInfo>
            installApp.sortWith(Comparator { o1: AppInfo, o2: AppInfo ->
                o1.applicationName!!.compareTo(o2.applicationName!!)
            })
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            setupViewPager(installApp)
        }
    }

    private fun setupViewPager(installApp: ArrayList<AppInfo>) {
        pbApp.visibility = View.GONE
        addOpenAppFragment(installApp)
        addLockAppFragment(installApp)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        updateTabAppCount()
    }
}