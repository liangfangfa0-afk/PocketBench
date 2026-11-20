package com.ljj.PocketBench.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopLiveUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(LiveUpdateController.NOTIFICATION_ID)
    }
}
