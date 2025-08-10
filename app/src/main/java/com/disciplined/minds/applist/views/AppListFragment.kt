package com.disciplined.minds.applist.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.disciplined.minds.R
import com.disciplined.minds.applist.AppInfo
import com.disciplined.minds.applist.BaseFragment
import com.disciplined.minds.applist.adapters.AppListAdapter
import com.disciplined.minds.applist.listeners.UpdateAppList
import com.disciplined.minds.applist.presenters.AppListFragmentPresenter
import com.disciplined.minds.pref.PreferenceDataHelper

class AppListFragment : BaseFragment<AppListFragmentPresenter>(), AppListFragmentView {

    var appType: Int? = null
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var updateAppListListener: UpdateAppList
    private lateinit var rvApp: RecyclerView
    var finalAppList = ArrayList<AppInfo>()
    var appList: HashMap<String, Boolean>? = null

    companion object {
        fun newInstance(appType: Int, installApp: ArrayList<AppInfo>): AppListFragment {
            val args = Bundle()
            val fragment = AppListFragment()
            args.putInt(BundleParameter.APP_TYPE, appType)
            args.putSerializable(BundleParameter.INSTALLED_APP, installApp)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onActivityCreated()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view : View =  inflater.inflate(R.layout.fragment_app_list, container, false)
        rvApp = view.findViewById<RecyclerView>(R.id.rvApp) as RecyclerView
        return view
    }

    override fun initViews() {
        appType = requireArguments().getInt(BundleParameter.APP_TYPE, 1)
        val installApp =
                requireArguments().getSerializable(BundleParameter.INSTALLED_APP) as ArrayList<AppInfo>
        setUpRecyclerView(installApp)
    }

    private fun setUpRecyclerView(installApp: ArrayList<AppInfo>) {
        val openAppList = ArrayList<AppInfo>()
        val lockAppList = ArrayList<AppInfo>()
        appList = PreferenceDataHelper.getInstance(requireContext())!!.getAppList()

        if (appList == null) {
            appList = HashMap<String, Boolean>()
            for (i in 0 until installApp.size) {
                if (installApp[i].isOpen!!) {
                    openAppList.add(installApp[i])
                    appList!![installApp[i].applicationPackage!!] = false
                } else {
                    lockAppList.add(installApp[i])
                    appList!![installApp[i].applicationPackage!!] = true
                }
            }
        } else {
            for (i in 0 until installApp.size) {
                if (appList!![installApp[i].applicationPackage!!] != null) {
                    if (appList!![installApp[i].applicationPackage!!]!!) {
                        installApp[i].isOpen = false
                        lockAppList.add(installApp[i])
                    } else {
                        installApp[i].isOpen = true
                        openAppList.add(installApp[i])
                    }
                } else {
                    appList!![installApp[i].applicationPackage!!] = true
                }
            }
        }

        updatePreference(appList!!)

        if (appType == 1) {
            finalAppList = openAppList
        } else {
            finalAppList = lockAppList
        }

        rvApp.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        appListAdapter = AppListAdapter(finalAppList, fun(pos: Int) {
            openAlertDialog(pos)
        })
        rvApp.adapter = appListAdapter
    }

    @SuppressLint("StringFormatMatches")
    private fun openAlertDialog(pos: Int) {
        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialog)
        val appName = finalAppList[pos].applicationName
        val type: String
        if (appType == 1) {
            type = getString(R.string.lock)
        } else {
            type = getString(R.string.open)
        }
        val message = "Do you wish to move $appName APP to $type APP list ?"
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
                    updateApp(pos)
                    dialogInterface.cancel()
                }
                .setNegativeButton(getString(R.string.no),
                        fun(dialogInterface: DialogInterface, i: Int) {
                            dialogInterface.cancel()
                        })
        val alert = builder.create()
        alert.show()
    }

    private fun  updateApp(pos: Int) {
        appList!![finalAppList[pos].applicationPackage!!] = finalAppList[pos].isOpen!!
        updatePreference(appList!!)
        val fragmentType: Int
        val type: String
        if (appType == 1) {
            fragmentType = 2
            type = getString(R.string.locked)
        } else {
            fragmentType = 1
            type = getString(R.string.unlocked)
        }
        val message = finalAppList[pos].applicationName + " is " + type + "."
        updateAppListListener.updateList(fragmentType, finalAppList[pos])
        finalAppList.removeAt(pos)
        appListAdapter.notifyItemRemoved(pos)
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    private fun updatePreference(appList: HashMap<String, Boolean>) {
        PreferenceDataHelper.getInstance(requireContext())!!.setAppList(appList)
    }

    override fun getNewPresenter(): AppListFragmentPresenter {
        return AppListFragmentPresenter()
    }

    override fun getScreenName(): String {
        return AppListFragment::getScreenName.name
    }

    fun setClickInterface(updateAppList: UpdateAppList) {
        this.updateAppListListener = updateAppList
    }

    fun updateAppList(item: AppInfo) {
        finalAppList.add(item)
        finalAppList.sortWith(Comparator { o1: AppInfo, o2: AppInfo ->
            o1.applicationName!!.compareTo(o2.applicationName!!)
        })
        appListAdapter.notifyDataSetChanged()
    }

}