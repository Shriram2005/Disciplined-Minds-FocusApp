package com.disciplined.minds.permission

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.disciplined.minds.MainActivity
import com.disciplined.minds.R

class PermissionActivity : AppCompatActivity() {

    private lateinit var usageAccessCard: CardView
    private lateinit var overlayCard: CardView
    private lateinit var usageAccessButton: Button
    private lateinit var overlayButton: Button
    private lateinit var usageAccessStatus: ImageView
    private lateinit var overlayStatus: ImageView
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        
        initViews()
        setupClickListeners()
        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun initViews() {
        usageAccessCard = findViewById(R.id.cardUsageAccess)
        overlayCard = findViewById(R.id.cardOverlay)
        usageAccessButton = findViewById(R.id.btnUsageAccess)
        overlayButton = findViewById(R.id.btnOverlay)
        usageAccessStatus = findViewById(R.id.ivUsageAccessStatus)
        overlayStatus = findViewById(R.id.ivOverlayStatus)
        continueButton = findViewById(R.id.btnContinue)
    }

    private fun setupClickListeners() {
        usageAccessButton.setOnClickListener {
            requestUsageAccessPermission()
        }

        overlayButton.setOnClickListener {
            requestOverlayPermission()
        }

        continueButton.setOnClickListener {
            if (hasAllPermissions()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun updatePermissionStatus() {
        val hasUsageAccess = checkUsageAccessPermission()
        val hasOverlay = Settings.canDrawOverlays(this)

        // Update usage access status
        if (hasUsageAccess) {
            usageAccessStatus.setImageResource(R.drawable.ic_check_circle)
            usageAccessStatus.setColorFilter(ContextCompat.getColor(this, R.color.darkgreen))
            usageAccessButton.text = "Granted"
            usageAccessButton.isEnabled = false
            usageAccessButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightgreen))
        } else {
            usageAccessStatus.setImageResource(R.drawable.ic_error)
            usageAccessStatus.setColorFilter(ContextCompat.getColor(this, R.color.red))
            usageAccessButton.text = "Grant Permission"
            usageAccessButton.isEnabled = true
            usageAccessButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }

        // Update overlay status
        if (hasOverlay) {
            overlayStatus.setImageResource(R.drawable.ic_check_circle)
            overlayStatus.setColorFilter(ContextCompat.getColor(this, R.color.darkgreen))
            overlayButton.text = "Granted"
            overlayButton.isEnabled = false
            overlayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightgreen))
        } else {
            overlayStatus.setImageResource(R.drawable.ic_error)
            overlayStatus.setColorFilter(ContextCompat.getColor(this, R.color.red))
            overlayButton.text = "Grant Permission"
            overlayButton.isEnabled = true
            overlayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }

        // Update continue button
        if (hasAllPermissions()) {
            continueButton.visibility = View.VISIBLE
            continueButton.isEnabled = true
        } else {
            continueButton.visibility = View.GONE
        }
    }

    private fun checkUsageAccessPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasAllPermissions(): Boolean {
        return checkUsageAccessPermission() && Settings.canDrawOverlays(this)
    }
}
