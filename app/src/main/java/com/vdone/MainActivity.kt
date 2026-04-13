package com.vdone

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.vdone.ui.theme.VDoneTheme

class MainActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions granted or denied — app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        requestExactAlarmPermission()
        val repository = (application as VDoneApp).taskRepository
        setContent {
            VDoneTheme {
                VDoneNavHost(repository = repository)
            }
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    private fun requestNeededPermissions() {
        val needed = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (needed.isNotEmpty()) requestPermissions.launch(needed.toTypedArray())
    }
}
