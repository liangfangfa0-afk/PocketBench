package com.ljj.PocketBench.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import kotlin.concurrent.thread

class RootBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val preferences = appContext.getSharedPreferences(
            DeviceMonitorController.ROOT_PREFERENCES,
            Context.MODE_PRIVATE,
        )
        if (!preferences.getBoolean(DeviceMonitorController.KEY_ROOT_WAS_GRANTED, false)) return

        val pendingResult = goAsync()
        thread(name = "PocketBench-BootRootCheck", isDaemon = true) {
            try {
                val result = RootAccessChecker.check(includeKsuVersion = false, timeoutSeconds = 6)
                preferences.edit()
                    .putBoolean("root_last_boot_check_granted", result.granted)
                    .putString("root_last_boot_check_status", result.status)
                    .putLong("root_last_boot_check_time", System.currentTimeMillis())
                    .apply()
                if (!result.granted) {
                    preferences.edit()
                        .putBoolean(DeviceMonitorController.KEY_ROOT_WAS_GRANTED, false)
                        .apply()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
