package com.ljj.PocketBench.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DeviceMonitorController(context: Context) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    var batteryPercent by mutableStateOf(0)
        private set
    var batteryStatus by mutableStateOf("读取中")
        private set
    var batteryTemperature by mutableStateOf("暂不可用")
        private set
    var batteryVoltage by mutableStateOf("暂不可用")
        private set
    var cpuCoreCount by mutableStateOf(Runtime.getRuntime().availableProcessors().toString())
        private set
    var thermalStatus by mutableStateOf("系统未提供热状态")
        private set
    var rootGranted by mutableStateOf(false)
        private set
    var isCheckingRoot by mutableStateOf(false)
        private set
    var rootStatus by mutableStateOf("尚未请求 ROOT 授权")
    var rootSystemProperties by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    init { refreshDeviceInfo() }

    fun refreshDeviceInfo() {
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryPercent = if (level >= 0 && scale > 0) level * 100 / scale else appContext.getSystemService(BatteryManager::class.java).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceAtLeast(0)
            batteryStatus = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
                BatteryManager.BATTERY_STATUS_FULL -> "已充满"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
                else -> "状态未知"
            }
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            batteryTemperature = if (temp > 0 && temp != Int.MIN_VALUE) "%.1f °C".format(temp / 10f) else "系统未提供"
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            batteryVoltage = if (voltage > 0) "$voltage mV" else "系统未提供"
        } else {
            batteryStatus = "系统暂未返回电池信息"
            batteryTemperature = "系统未提供"
            batteryVoltage = "系统未提供"
        }
        cpuCoreCount = Runtime.getRuntime().availableProcessors().toString()
        thermalStatus = if (Build.VERSION.SDK_INT >= 29) {
            when (appContext.getSystemService(PowerManager::class.java).currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "正常"
                PowerManager.THERMAL_STATUS_LIGHT -> "轻微"
                PowerManager.THERMAL_STATUS_MODERATE -> "中等"
                PowerManager.THERMAL_STATUS_SEVERE -> "较高"
                PowerManager.THERMAL_STATUS_CRITICAL -> "严重"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "紧急"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "系统即将关机"
                else -> "未知"
            }
        } else "当前 Android 版本不支持"
    }

    fun requestRootAuthorization() {
        if (isCheckingRoot) return
        isCheckingRoot = true
        rootStatus = "正在等待 ROOT 管理器授权…"
        thread(name = "PocketBench-RootCheck", isDaemon = true) {
            var process: Process? = null
            var granted = false
            var ksuVersion = "暂不可用"
            val status = try {
                process = ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start()
                val authProcess = process!!
                if (!authProcess.waitFor(8, TimeUnit.SECONDS)) {
                    authProcess.destroyForcibly()
                    "授权请求超时"
                } else {
                    val identity = authProcess.inputStream.bufferedReader().use { it.readText() }.trim()
                    granted = authProcess.exitValue() == 0 &&
                        Regex("""(^|\\s)uid=0(?:\\(|\\s|$)""").containsMatchIn(identity)
                    if (!granted) {
                        "未获得 UID 0 授权"
                    } else {
                        // 只读取 KernelSU userspace 提供的内核版本信息；命令固定且只读。
                        process = ProcessBuilder("su", "-c", "ksud debug version").redirectErrorStream(true).start()
                        val versionProcess = process!!
                        if (versionProcess.waitFor(5, TimeUnit.SECONDS)) {
                            val output = versionProcess.inputStream.bufferedReader().use { it.readText() }.trim()
                            if (versionProcess.exitValue() == 0 && output.isNotBlank()) {
                                ksuVersion = output.lineSequence().first().take(48)
                            }
                        } else {
                            versionProcess.destroyForcibly()
                        }
                        "已获得 UID 0"
                    }
                }
            } catch (_: Exception) {
                if (granted) "已获得 UID 0" else "ROOT 授权检查失败"
            } finally {
                process?.destroy()
            }
            mainHandler.post {
                rootGranted = granted
                rootKsuVersion = if (granted) ksuVersion else "暂不可用"
                rootStatus = status
                isCheckingRoot = false
            }
        }
    }
}
