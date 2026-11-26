package com.ljj.PocketBench.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ext.SdkExtensions
import android.provider.Settings
import com.ljj.PocketBench.MainActivity
import com.ljj.PocketBench.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LiveUpdateController(context: Context) {
    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    var title by mutableStateOf("PocketBench 正在运行")
        private set
    var shortText by mutableStateOf("运行中")
        private set
    var isRunning by mutableStateOf(false)
        private set
    var canPromote by mutableStateOf(false)
        private set
    var isPromotable by mutableStateOf(false)
        private set
    var isPromoted by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("尚未启动实时动态")

    fun updateTitle(value: String) {
        title = value
        updatePostedNotification()
    }

    fun updateShortText(value: String) {
        shortText = value
        updatePostedNotification()
    }

    fun startOrUpdate() {
        ensureChannel()
        try {
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
            isRunning = true
            refreshStatus()
        } catch (_: SecurityException) {
            statusMessage = "通知权限未开启，无法发布实时动态"
        }
    }

    fun stop() {
        notificationManager.cancel(NOTIFICATION_ID)
        isRunning = false
        isPromotable = false
        isPromoted = false
        statusMessage = "实时动态已停止"
    }

    fun refreshStatus() {
        val active = notificationManager.activeNotifications
            .firstOrNull { it.id == NOTIFICATION_ID }
        isRunning = active != null

        if (Build.VERSION.SDK_INT < 36) {
            canPromote = false
            isPromotable = false
            isPromoted = false
            statusMessage = if (isRunning) "通知正在运行" else "尚未启动实时动态"
            return
        }

        canPromote = notificationManager.canPostPromotedNotifications()
        if (active == null) {
            isPromotable = false
            isPromoted = false
            statusMessage = "尚未启动实时动态"
            return
        }

        isPromotable = active.notification.hasPromotableCharacteristics()
        isPromoted =
            active.notification.flags and Notification.FLAG_PROMOTED_ONGOING != 0

        statusMessage = when {
            !canPromote -> "系统未允许 PocketBench 发布实时动态，请开启系统授权"
            !isPromotable -> "当前通知尚不符合系统提升条件"
            isPromoted -> "系统已将这条通知提升为实时动态"
            else -> "已请求系统提升，当前尚未获得实时动态标记"
        }
    }

    fun openPromotionSettings(context: Context) {
        val promotionIntent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
        try {
            context.startActivity(promotionIntent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
            context.startActivity(fallbackIntent)
        }
    }

    private fun updatePostedNotification() {
        if (isRunning) startOrUpdate()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "PocketBench 实时动态",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "显示可自定义的实况通知"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            appContext,
            REQUEST_OPEN,
            Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_STOP,
            Intent(appContext, StopLiveUpdateReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = Notification.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_pocketbench)
            .setContentTitle(title.ifBlank { "PocketBench" })
            .setContentText("PocketBench · 实况通知")
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_stat_stop, "停止", stopIntent)

        if (Build.VERSION.SDK_INT >= 36) {
            builder.setShortCriticalText(shortText.take(MAX_SHORT_TEXT_LENGTH))
            if (SdkExtensions.getExtensionVersion(36) >= 1) {
                builder.setRequestPromotedOngoing(true)
            } else {
                builder.extras.putBoolean("android.requestPromotedOngoing", true)
            }
        }

        return builder.build()
    }

    companion object {
        const val NOTIFICATION_ID = 4101
        private const val CHANNEL_ID = "pocketbench_live_updates"
        private const val REQUEST_OPEN = 4102
        private const val REQUEST_STOP = 4103
        private const val MAX_SHORT_TEXT_LENGTH = 7
    }
}
