package com.arin.pocketcompanion.android

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arin.pocketcompanion.android.floating.FloatingMiloService
import com.arin.pocketcompanion.android.onboarding.OnboardingPrefs
import com.arin.pocketcompanion.android.onboarding.OnboardingScreen
import com.arin.pocketcompanion.android.onboarding.Permissions
import com.arin.pocketcompanion.ui.App

class MainActivity : ComponentActivity() {

    private lateinit var prefs: OnboardingPrefs

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* Milo still works without it */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = OnboardingPrefs(this)

        setContent {
            var showOnboarding by mutableStateOf(!prefs.completed)

            if (showOnboarding) {
                OnboardingScreen(
                    onRequestNotifications = {
                        if (Build.VERSION.SDK_INT >= 33 && !Permissions.notificationsGranted(this)) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenAlarmSettings = {
                        if (!Permissions.exactAlarmsGranted(this)) Permissions.openExactAlarmSettings(this)
                    },
                    onFinish = {
                        prefs.completed = true
                        showOnboarding = false
                    },
                )
            } else {
                App(
                    userName = "Arin",
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
        
        checkOverlayPermission()
    }
    
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                startFloatingService()
            }
        } else {
            startFloatingService()
        }
    }
    
    private fun startFloatingService() {
        val intent = Intent(this, FloatingMiloService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}