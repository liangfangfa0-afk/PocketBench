package com.ljj.PocketBench.device

import java.util.concurrent.TimeUnit

data class RootAccessResult(
    val granted: Boolean,
    val status: String,
    val version: String = "暂不可用",
)

object RootAccessChecker {
    fun check(includeKsuVersion: Boolean, timeoutSeconds: Long = 8): RootAccessResult {
        var process: Process? = null
        try {
            val identityProcess = ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start()
            process = identityProcess
            if (!identityProcess.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                identityProcess.destroyForcibly()
                return RootAccessResult(false, "ROOT 检测超时")
            }
            val identity = identityProcess.inputStream.bufferedReader().use { it.readText() }.trim()
            val granted = identityProcess.exitValue() == 0 &&
                Regex("""(^|\s)uid=0(?:\(|\s|$)""").containsMatchIn(identity)
            if (!granted) return RootAccessResult(false, "未获得 UID 0 授权")
            if (!includeKsuVersion) return RootAccessResult(true, "已获得 UID 0")

            val versionProcess = ProcessBuilder("su", "-c", "ksud debug version").redirectErrorStream(true).start()
            process = versionProcess
            if (!versionProcess.waitFor(5, TimeUnit.SECONDS)) {
                versionProcess.destroyForcibly()
                return RootAccessResult(true, "已获得 UID 0")
            }
            val output = versionProcess.inputStream.bufferedReader().use { it.readText() }.trim()
            val version = if (versionProcess.exitValue() == 0 && output.isNotBlank()) {
                output.lineSequence().first().take(48)
            } else {
                "暂不可用"
            }
            return RootAccessResult(true, "已获得 UID 0", version)
        } catch (_: Exception) {
            return RootAccessResult(false, "ROOT 授权检查失败")
        } finally {
            process?.destroy()
        }
    }
}
