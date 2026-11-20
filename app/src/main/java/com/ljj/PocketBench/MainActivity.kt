package com.ljj.PocketBench

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.itos.coui.theme.CouiTheme
import com.ljj.PocketBench.notification.LiveUpdateController
import com.ljj.PocketBench.ui.PocketBenchApp

class MainActivity : ComponentActivity() {
    private lateinit var liveUpdateController: LiveUpdateController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveUpdateController = LiveUpdateController(applicationContext)
        setContent { CouiThemeHost(liveUpdateController) }
    }

    override fun onResume() {
        super.onResume()
        if (::liveUpdateController.isInitialized) liveUpdateController.refreshStatus()
    }
}

@Composable
private fun CouiThemeHost(controller: LiveUpdateController) {
    val activity = androidx.compose.ui.platform.LocalContext.current as Activity
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) controller.startOrUpdate()
        else controller.statusMessage = "需要通知权限才能发布这条实况通知"
    }

    val preferences = remember(activity) {
        activity.getSharedPreferences("pocketbench_preferences", Activity.MODE_PRIVATE)
    }
    var darkTheme by remember {
        mutableStateOf(preferences.getBoolean("dark_theme", false))
    }

    CouiTheme(darkTheme = darkTheme) {
        PocketBenchApp(
            controller = controller,
            onStart = {
                val granted = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) controller.startOrUpdate()
                else permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onOpenPromotionSettings = { controller.openPromotionSettings(activity) },
            darkTheme = darkTheme,
            onDarkThemeChange = { enabled ->
                darkTheme = enabled
                preferences.edit().putBoolean("dark_theme", enabled).apply()
            },
            onOpenNotificationSettings = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                activity.startActivity(intent)
            },
        )
    }
}
