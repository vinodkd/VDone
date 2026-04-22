package com.vdone

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.vdone.ui.theme.VDoneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var navController: NavController? = null

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions granted or denied — app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        requestExactAlarmPermission()
        requestOverlayPermission()
        requestFullScreenIntentPermission()
        val app = application as VDoneApp
        setContent {
            VDoneTheme {
                VDoneNavHost(
                    repository = app.taskRepository,
                    conditionRepository = app.conditionRepository,
                    initialRoute = widgetRoute(intent),
                    onNavControllerReady = { navController = it },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val repo = (application as VDoneApp).taskRepository
        lifecycleScope.launch { repo.autoDoneOvernight() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val route = widgetRoute(intent) ?: return
        navController?.navigate(route)
    }

    /** Translates a vdone://open/detail/{id} URI to a nav route, or null if not a widget intent. */
    private fun widgetRoute(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (uri.scheme != "vdone") return null
        val segments = uri.pathSegments
        if (segments.size >= 2 && segments[0] == "detail") return "detail/${segments[1]}"
        return null
    }

    private fun requestFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            @Suppress("NewApi")
            if (!nm.canUseFullScreenIntent()) {
                try {
                    startActivity(
                        Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENTS").apply {
                            data = android.net.Uri.parse("package:$packageName")
                        }
                    )
                } catch (_: android.content.ActivityNotFoundException) {
                    // Device doesn't have this settings page (some OEM builds);
                    // user can grant it via Settings → Permissions in the app.
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            tryStartActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                tryStartActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    /** Starts an activity only if something on the device can handle the intent. */
    private fun tryStartActivity(intent: Intent) {
        if (packageManager.resolveActivity(intent, 0) != null) {
            startActivity(intent)
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
