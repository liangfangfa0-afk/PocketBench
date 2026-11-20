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

    CouiTheme {
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
            onOpenNotificationSettings = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                activity.startActivity(intent)
            },
        )
    }
}
