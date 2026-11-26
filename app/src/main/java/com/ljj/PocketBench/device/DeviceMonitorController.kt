package com.ljj.PocketBench.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DeviceMonitorController(context: Context) {
    private val appContext = context.applicationContext
    private val rootPreferences = appContext.getSharedPreferences(ROOT_PREFERENCES, Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())

    var batteryPercent by mutableStateOf(0); private set
    var batteryStatus by mutableStateOf("读取中"); private set
    var batteryTemperature by mutableStateOf("暂不可用"); private set
    var batteryVoltage by mutableStateOf("暂不可用"); private set
    var cpuCoreCount by mutableStateOf(Runtime.getRuntime().availableProcessors().toString()); private set
    var thermalStatus by mutableStateOf("系统未提供热状态"); private set
    var rootGranted by mutableStateOf(false); private set
    var rootKsuVersion by mutableStateOf("暂不可用"); private set
    var isCheckingRoot by mutableStateOf(false); private set
    var rootStatus by mutableStateOf("尚未请求 ROOT 授权"); private set
    var rootSystemProperties by mutableStateOf<Map<String, String>>(emptyMap()); private set
    var deviceInfoSections by mutableStateOf<List<Pair<String, List<Pair<String, String>>>>>(emptyList()); private set

    init { refreshDeviceInfo(); checkRootAuthorization() }

    fun refreshDeviceInfo() {
        val battery = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (battery != null) {
            val level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val manager = appContext.getSystemService(BatteryManager::class.java)
            batteryPercent = if (level >= 0 && scale > 0) level * 100 / scale else manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceAtLeast(0)
            batteryStatus = when (battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
                BatteryManager.BATTERY_STATUS_FULL -> "已充满"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
                else -> "状态未知"
            }
            val temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            batteryTemperature = if (temp > 0 && temp != Int.MIN_VALUE) String.format(Locale.US, "%.1f °C", temp / 10f) else "系统未提供"
            val voltage = battery.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            batteryVoltage = if (voltage > 0) "$voltage mV" else "系统未提供"
        } else {
            batteryPercent = appContext.getSystemService(BatteryManager::class.java).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceAtLeast(0)
            batteryStatus = "系统暂未返回"; batteryTemperature = "系统未提供"; batteryVoltage = "系统未提供"
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

        val metrics = DisplayMetrics()
        val refreshRate = runCatching {
            @Suppress("DEPRECATION")
            (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.also {
                it.getRealMetrics(metrics)
            }.refreshRate
        }.getOrNull()
        val memory = ActivityManager.MemoryInfo()
        appContext.getSystemService(ActivityManager::class.java).getMemoryInfo(memory)
        val stats = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stats.totalBytes
        val freeStorage = stats.availableBytes
        val sensors = runCatching { appContext.getSystemService(SensorManager::class.java).getSensorList(Sensor.TYPE_ALL) }.getOrNull()
        val sensorSummary = sensors?.let { it.size.toString() + " 个传感器" } ?: "系统未提供"
        val batteryHealth = battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)?.let {
            when (it) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
                BatteryManager.BATTERY_HEALTH_DEAD -> "故障"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "电压异常"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "未指定故障"
                BatteryManager.BATTERY_HEALTH_COLD -> "温度过低"
                else -> "系统未提供"
            }
        } ?: "系统未提供"
        val runtime = Runtime.getRuntime()
        val features = listOf(
            "NFC" to appContext.packageManager.hasSystemFeature("android.hardware.nfc"),
            "指纹识别" to appContext.packageManager.hasSystemFeature("android.hardware.fingerprint"),
            "摄像头" to appContext.packageManager.hasSystemFeature("android.hardware.camera.any"),
            "陀螺仪" to appContext.packageManager.hasSystemFeature("android.hardware.sensor.gyroscope"),
            "加速度计" to appContext.packageManager.hasSystemFeature("android.hardware.sensor.accelerometer"),
        ).joinToString(" · ") { it.first + "：" + if (it.second) "支持" else "未检测到" }

        deviceInfoSections = listOf(
            "设备标识" to listOf(
                "制造商" to Build.MANUFACTURER.ifBlank { "系统未提供" },
                "品牌" to Build.BRAND.ifBlank { "系统未提供" },
                "产品名称" to Build.PRODUCT.ifBlank { "系统未提供" },
                "设备代号" to Build.DEVICE.ifBlank { "系统未提供" },
                "机型" to Build.MODEL.ifBlank { "系统未提供" },
                "主板" to Build.BOARD.ifBlank { "系统未提供" },
                "硬件平台" to Build.HARDWARE.ifBlank { "系统未提供" },
                "Bootloader" to Build.BOOTLOADER.ifBlank { "系统未提供" },
            ),
            "系统信息" to listOf(
                "Android 版本" to Build.VERSION.RELEASE,
                "API Level" to Build.VERSION.SDK_INT.toString(),
                "系统构建版本" to Build.DISPLAY.ifBlank { "系统未提供" },
                "Build ID" to Build.ID.ifBlank { "系统未提供" },
                "增量版本" to Build.VERSION.INCREMENTAL.ifBlank { "系统未提供" },
                "安全补丁级别" to Build.VERSION.SECURITY_PATCH.ifBlank { "系统未提供" },
                "系统类型" to Build.TYPE.ifBlank { "系统未提供" },
                "系统标签" to Build.TAGS.ifBlank { "系统未提供" },
                "系统指纹" to Build.FINGERPRINT.ifBlank { "系统未提供" },
                "基底系统" to Build.VERSION.BASE_OS.ifBlank { "系统未提供" },
            ),
            "处理器与运行环境" to listOf(
                "可用 CPU 核心数" to cpuCoreCount,
                "支持的 ABI" to Build.SUPPORTED_ABIS.joinToString().ifBlank { "系统未提供" },
                "最大应用堆内存" to formatBytes(runtime.maxMemory()),
                "当前应用堆内存" to formatBytes(runtime.totalMemory() - runtime.freeMemory()),
                "GPU 型号" to "系统未提供统一公开接口",
            ),
            "屏幕信息" to listOf(
                "物理分辨率" to if (metrics.widthPixels > 0 && metrics.heightPixels > 0) metrics.widthPixels.toString() + " × " + metrics.heightPixels else "系统未提供",
                "屏幕密度" to if (metrics.density > 0f) String.format(Locale.US, "%.2f", metrics.density) else "系统未提供",
                "密度 DPI" to if (metrics.densityDpi > 0) metrics.densityDpi.toString() else "系统未提供",
                "当前刷新率" to (refreshRate?.let { String.format(Locale.US, "%.1f Hz", it) } ?: "系统未提供"),
            ),
            "内存信息" to listOf(
                "设备总内存" to formatBytes(memory.totalMem),
                "当前可用内存" to formatBytes(memory.availMem),
                "低内存状态" to if (memory.lowMemory) "是" else "否",
            ),
            "存储信息" to listOf(
                "内部存储总量" to formatBytes(totalStorage),
                "内部存储可用" to formatBytes(freeStorage),
                "内部存储已用" to formatBytes((totalStorage - freeStorage).coerceAtLeast(0)),
                "外部存储状态" to Environment.getExternalStorageState(),
            ),
            "电池与电源" to listOf(
                "当前电量" to batteryPercent.toString() + "%",
                "充电状态" to batteryStatus,
                "电池温度" to batteryTemperature,
                "电池电压" to batteryVoltage,
                "电池健康状态" to batteryHealth,
                "电池技术类型" to (battery?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: "系统未提供"),
                "系统热状态" to thermalStatus,
            ),
            "传感器与硬件能力" to listOf(
                "传感器概况" to sensorSummary,
                "硬件功能" to features,
            ),
        )
    }

    fun checkRootAuthorization() {
        if (!rootPreferences.getBoolean(KEY_ROOT_WAS_GRANTED, false) || isCheckingRoot) return
        runRootCheck(isAutomatic = true)
    }
    fun requestRootAuthorization() { if (!isCheckingRoot) runRootCheck(isAutomatic = false) }

    private fun runRootCheck(isAutomatic: Boolean) {
        isCheckingRoot = true
        rootStatus = if (isAutomatic) "正在自动检测 ROOT 授权…" else "正在等待 ROOT 管理器授权…"
        thread(name = "PocketBench-RootCheck", isDaemon = true) {
            val result = RootAccessChecker.check(includeKsuVersion = true)
            val props = if (result.granted) readRootProperties() else emptyMap()
            mainHandler.post {
                rootGranted = result.granted
                rootKsuVersion = if (result.granted) result.version else "暂不可用"
                rootSystemProperties = props
                rootStatus = result.status
                isCheckingRoot = false
                if (result.granted) rootPreferences.edit().putBoolean(KEY_ROOT_WAS_GRANTED, true).apply()
                else if (isAutomatic) rootPreferences.edit().putBoolean(KEY_ROOT_WAS_GRANTED, false).apply()
            }
        }
    }

    private fun readRootProperties(): Map<String, String> {
        var process: Process? = null
        return try {
            val p = ProcessBuilder("su", "-c", "getprop").redirectErrorStream(true).start()
            process = p
            if (!p.waitFor(8, TimeUnit.SECONDS)) { p.destroyForcibly(); return emptyMap() }
            p.inputStream.bufferedReader().useLines { lines ->
                lines.mapNotNull { line ->
                    val m = Regex("""^\[([^]]+)]\s*:\s*\[(.*)]$""").find(line) ?: return@mapNotNull null
                    val key = m.groupValues[1]
                    val value = m.groupValues[2]
                    val allowed = key.startsWith("ro.build.") || key.startsWith("ro.product.") ||
                        key.startsWith("ro.vendor.build.") || key.startsWith("ro.hardware") || key.startsWith("ro.boot.")
                    val sensitive = listOf("serial", "imei", "meid", "mac", "wifi", "bluetooth").any { key.contains(it, true) }
                    if (allowed && !sensitive && value.isNotBlank()) key to value else null
                }.take(120).toList().toMap()
            }
        } catch (_: Exception) { emptyMap() }
        finally { process?.destroy() }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "系统未提供"
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) String.format(Locale.US, "%.2f GB", gb)
        else String.format(Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
    }

    companion object {
        const val ROOT_PREFERENCES = "pocketbench_root"
        const val KEY_ROOT_WAS_GRANTED = "root_was_granted"
    }
}
