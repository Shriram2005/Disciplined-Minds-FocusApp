package com.disciplined.minds

import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.disciplined.minds.applist.service.AppBlockService
import com.disciplined.minds.applist.views.AppListActivity
import com.disciplined.minds.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        //checking usage permission
        if(!checkForPermission(this@MainActivity)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:$packageName"
                )
            )
            startActivityForResult(intent, 0)
        }

        startService(Intent(this@MainActivity, AppBlockService::class.java))
        val btnAppLock = findViewById<Button>(R.id.btnAppLock) as Button

        btnAppLock.setOnClickListener{view ->

            val intent = Intent(this, AppListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkForPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }


}
