package com.ljj.PocketBench.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import com.ljj.PocketBench.device.DeviceMonitorController

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.Scaffold
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.slider.Slider
import com.itos.coui.switch.Switch
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.TopAppBarButtonGroup
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.Dialog
import com.itos.coui.dialog.InputDialog
import androidx.compose.ui.platform.LocalUriHandler
import com.ljj.PocketBench.notification.LiveUpdateController

@Composable
fun PocketBenchApp(
    controller: LiveUpdateController,
    deviceMonitor: DeviceMonitorController,
    onStart: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var currentPage by rememberSaveable { mutableStateOf("home") }
    var navigationForward by remember { mutableStateOf(true) }
    var returnPage by rememberSaveable { mutableStateOf("home") }
    var announcementVisible by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember { CouiFloatingRenderer() }
    val isSubPage = currentPage in setOf("live", "device", "root", "systemWebsites", "opensource", "coloros", "harmony", "originos", "adaptationHub", "adaptation_coloros", "adaptation_harmony", "adaptation_originos", "adaptation_xiaomi", "adaptation_honor", "adaptation_apple")

    fun navigate(page: String) {
        if (page != "home" && page != "settings") returnPage = currentPage
        navigationForward = page != "home"
        currentPage = page
        selectedTab = if (page == "settings") 1 else 0
    }

    fun goBack() {
        navigationForward = false
        currentPage = returnPage
        selectedTab = if (currentPage == "settings") 1 else 0
        returnPage = if (currentPage == "adaptationHub") "home" else "home"
    }

    BackHandler(enabled = isSubPage) { goBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = pageColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentPage) {
                            "live" -> "实况通知"
                            "device" -> "电池与温度"
                            "root" -> "ROOT"
                            "systemWebsites" -> "各系统官网"
                            "opensource" -> "开源与组件"
                            "coloros" -> "ColorOS 17 适配"
                            "harmony" -> "HarmonyOS 7 适配"
                            "originos" -> "OriginOS 7 适配"
                            "adaptationHub" -> "各厂商新系统适配名单"
                            "adaptation_coloros" -> "ColorOS 17"
                            "adaptation_harmony" -> "HarmonyOS 7"
                            "adaptation_originos" -> "OriginOS 7"
                            "adaptation_xiaomi" -> "Xiaomi HyperOS 4"
                            "adaptation_honor" -> "荣耀 MagicOS 11"
                            "adaptation_apple" -> "Apple iOS 27"
                            "settings" -> "设置"
                            else -> "PocketBench"
                        },
                        kind = CouiTextKind.ItemTitle,
                    )
                },
                navigationIcon = {
                    if (isSubPage) {
                        CouiBackTopBarButton(onClick = ::goBack)
                    }
                },
                actions = {
                    if (currentPage == "home" || currentPage == "settings") {
                        CouiAnnouncementTopBarButton(onClick = { announcementVisible = true })
                    }
                },
            )
        },
        bottomBar = {
            if (!isSubPage) {
                NavigationBar(
                        renderer = bottomRenderer,
                        items = listOf(
                            CouiNavigationItem("首页", CouiIcons.Filled.Grid, CouiIcons.Filled.Grid),
                            CouiNavigationItem("设置", CouiIcons.Filled.Setting, CouiIcons.Filled.Setting),
                        ),
                        selectedIndex = selectedTab,
                        onSelection = {
                            val previousTab = selectedTab
                            navigationForward = it > previousTab
                            selectedTab = it
                            currentPage = if (it == 0) "home" else "settings"
                        },
                    )
            }
        },
    ) { contentPadding ->
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
                targetState = currentPage,
                transitionSpec = {
                    val enter = if (navigationForward) {
                        slideInHorizontally(initialOffsetX = { it / 5 }, animationSpec = tween(280)) +
                            fadeIn(animationSpec = tween(220))
                    } else {
                        slideInHorizontally(initialOffsetX = { -it / 5 }, animationSpec = tween(280)) +
                            fadeIn(animationSpec = tween(220))
                    }
                    val exit = if (navigationForward) {
                        slideOutHorizontally(targetOffsetX = { -it / 8 }, animationSpec = tween(220)) +
                            fadeOut(animationSpec = tween(180))
                    } else {
                        slideOutHorizontally(targetOffsetX = { it / 8 }, animationSpec = tween(220)) +
                            fadeOut(animationSpec = tween(180))
                    }
                    enter togetherWith exit
                },
                label = "PocketBenchPageTransition",
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
            ) {
                when (page) {
                    "live" -> LiveUpdatePage(controller, onStart)
                    "device" -> DeviceMonitorPage(deviceMonitor)
                    "root" -> RootPage(deviceMonitor)
                    "systemWebsites" -> SystemWebsitesPage(onOpenOfficial = { uriHandler.openUri(it) })
                    "opensource" -> OpenSourcePage(
                        onOpenPocketBench = { uriHandler.openUri("https://github.com/liangfangfa0-afk/PocketBench") },
                        onOpenCoui = { uriHandler.openUri("https://github.com/ItosEO/COUI-Kit") },
                        onOpenCouiDocs = { uriHandler.openUri("https://github.com/ItosEO/COUI-Kit/tree/main/docs/components") },
                    )
                    "coloros" -> ColorOsAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://www.coloros.com/upgrade/") })
                    "harmony" -> HarmonyAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://consumer.huawei.com/cn/support/harmonyos/models-7/") })
                    "originos" -> OriginOsAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://www.vivo.com.cn/originos/") })
                    "adaptationHub" -> AdaptationHubPage(onOpenSystem = { navigate(it) })
                    "adaptation_coloros" -> ColorOsAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://www.coloros.com/upgrade/") })
                    "adaptation_harmony" -> HarmonyAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://consumer.huawei.com/cn/support/harmonyos/models-7/") })
                    "adaptation_originos" -> OriginOsAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://www.vivo.com.cn/originos/") })
                    "adaptation_xiaomi" -> RemoteAdaptationPage(section = "xiaomi", title = "Xiaomi HyperOS 4", onOpenOfficial = { uriHandler.openUri("https://os.mi.com/") })
                    "adaptation_honor" -> RemoteAdaptationPage(section = "honor", title = "荣耀 MagicOS 11", onOpenOfficial = { uriHandler.openUri("https://www.honor.com/cn/magic-os/") })
                    "adaptation_apple" -> RemoteAdaptationPage(section = "apple", title = "Apple iOS 27", onOpenOfficial = { uriHandler.openUri("https://support.apple.com/zh-cn/guide/iphone/iphe3fa5df43/27/ios/27") })
                    "settings" -> SettingsPage(
                        controller = controller,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onOpenPromotionSettings = onOpenPromotionSettings,
                        onOpenSourceInfo = { navigate("opensource") },
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                    )
                    else -> HomePage(
                        controller = controller,
                        onStart = onStart,
                        onOpenLiveUpdate = { navigate("live") },
                        onOpenDeviceMonitor = { deviceMonitor.refreshDeviceInfo(); navigate("device") },
                        onOpenRoot = { navigate("root") },
                        onOpenSystemWebsites = { navigate("systemWebsites") },
                        onOpenAdaptationHub = { navigate("adaptationHub") },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    Dialog(
        visible = announcementVisible,
        onDismissRequest = { announcementVisible = false },
        title = "PocketBench 更新公告",
        message = "1.0.0.15\n\n新增各系统官网快捷入口；ROOT 信息页独立展示，可在用户授权后补充读取只读系统属性；隐藏云端配置接口地址，不再提供复制入口。电池与温度页面仅保留设备监测信息。",
        confirmAction = CouiDialogAction(text = "知道了", onClick = { announcementVisible = false }),
    )
}

@RequiresApi(34)
@Composable
private fun CouiBackTopBarButton(onClick: () -> Unit) {
    TopAppBarButtonGroup {
        TopAppBarButton(onClick = onClick) {
            Image(
                imageVector = CouiIcons.Filled.Back,
                contentDescription = "返回",
            )
        }
    }
}

@RequiresApi(34)
@Composable
private fun CouiAnnouncementTopBarButton(onClick: () -> Unit) {
    TopAppBarButtonGroup {
        TopAppBarButton(onClick = onClick, text = true) {
            Text(text = "公告", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun HomePage(
    controller: LiveUpdateController,
    onStart: () -> Unit,
    onOpenLiveUpdate: () -> Unit,
    onOpenDeviceMonitor: () -> Unit,
    onOpenRoot: () -> Unit,
    onOpenSystemWebsites: () -> Unit,
    onOpenAdaptationHub: () -> Unit,
) {
    Group(title = "PocketBench", footer = "轻量、清晰，尽量使用 Android 系统能力。") {
        Item(
            onClick = onOpenLiveUpdate,
            summary = {
                Text(
                    text = "配置内容、进度，并控制 Android 系统原生实况通知",
                    kind = CouiTextKind.ItemSummary,
                )
            },
            trailing = {
                Switch(
                    checked = controller.isRunning,
                    onCheckedChange = { enabled ->
                        if (enabled) onStart() else controller.stop()
                    },
                )
            },
        ) {
            Text(text = "实况通知", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "设备监测") {
        Item(onClick = onOpenDeviceMonitor, summary = { Text(text = "查看电量、电池温度、CPU 核心数与设备热状态", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "电池与温度", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "系统适配") {
        Item(onClick = onOpenAdaptationHub, summary = { Text(text = "集中查看各厂商最新系统版本、官方适配名单与升级批次", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "各厂商新系统适配名单", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "系统工具") {
        Item(onClick = onOpenRoot, summary = { Text(text = "查看设备型号、Android 版本与系统属性；需要时可申请 ROOT 只读补充读取", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "ROOT", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "官方资源") {
        Item(onClick = onOpenSystemWebsites, summary = { Text(text = "直达各手机厂商操作系统官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "各系统官网", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun AdaptationHubPage(onOpenSystem: (String) -> Unit) {
    Group(title = "OPPO") {
        Item(onClick = { onOpenSystem("adaptation_coloros") }, summary = { Text(text = "ColorOS 17 · 官方升级计划", kind = CouiTextKind.ItemSummary) }) { Text(text = "ColorOS 17", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "华为") {
        Item(onClick = { onOpenSystem("adaptation_harmony") }, summary = { Text(text = "HarmonyOS 7 · 正式版与公测版支持机型", kind = CouiTextKind.ItemSummary) }) { Text(text = "HarmonyOS 7", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "vivo") {
        Item(onClick = { onOpenSystem("adaptation_originos") }, summary = { Text(text = "OriginOS 7 · vivo 与 iQOO 官方升级计划", kind = CouiTextKind.ItemSummary) }) { Text(text = "OriginOS 7", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "小米") {
        Item(onClick = { onOpenSystem("adaptation_xiaomi") }, summary = { Text(text = "Xiaomi HyperOS 4 · 官方 Beta 推送计划", kind = CouiTextKind.ItemSummary) }) { Text(text = "Xiaomi HyperOS 4", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "荣耀") {
        Item(onClick = { onOpenSystem("adaptation_honor") }, summary = { Text(text = "荣耀 MagicOS 11 · 官方升级计划", kind = CouiTextKind.ItemSummary) }) { Text(text = "荣耀 MagicOS 11", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "Apple") {
        Item(onClick = { onOpenSystem("adaptation_apple") }, summary = { Text(text = "iOS 27 · Apple 官方兼容机型名单", kind = CouiTextKind.ItemSummary) }) { Text(text = "iOS 27", kind = CouiTextKind.ItemTitle) }
    }
}

@Composable
private fun LiveUpdatePage(controller: LiveUpdateController, onStart: () -> Unit) {
    var editingField by rememberSaveable { mutableStateOf("") }
    var editingValue by remember { mutableStateOf(TextFieldValue("")) }

    fun edit(field: String, value: String) {
        editingField = field
        editingValue = TextFieldValue(value)
    }

    Group(
        title = "实况通知",
        footer = "本页面只负责配置和控制；通知内容、状态栏展示及实况通知呈现均由 Android 系统处理。",
    ) {
        Item(
            summary = {
                Text(
                    text = if (controller.isRunning) "原生通知正在运行" else "尚未发布原生通知",
                    kind = CouiTextKind.ItemSummary,
                )
            },
            trailing = {
                Switch(
                    checked = controller.isRunning,
                    onCheckedChange = { enabled ->
                        if (enabled) onStart() else controller.stop()
                    },
                )
            },
        ) {
            Text(text = "启用实况通知", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "通知内容", footer = "短文本建议控制在 7 个字符以内，系统会根据设备界面决定展示方式。") {
        Item(
            onClick = { edit("title", controller.title) },
            summary = { Text(text = controller.title, kind = CouiTextKind.ItemSummary) },
            trailing = { Text(text = "编辑", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "标题", kind = CouiTextKind.ItemTitle)
        }
        Item(
            onClick = { edit("shortText", controller.shortText) },
            summary = { Text(text = controller.shortText, kind = CouiTextKind.ItemSummary) },
            trailing = { Text(text = "编辑", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "短文本", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "进度", footer = "拖动 COUI 滑块时，会更新同一条 Android 原生通知。") {
        Item(
            trailing = { Text(text = "${controller.progress}%", kind = CouiTextKind.ItemTitle) },
        ) {
            Text(text = "当前进度", kind = CouiTextKind.ItemTitle)
        }
        Slider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            value = controller.progress.toFloat(),
            onValueChange = { controller.updateProgress(it.toInt()) },
            valueRange = 0f..100f,
        )
        Spacer(Modifier.height(8.dp))
    }

    Group(title = "操作") {
        Item(
            onClick = onStart,
            summary = { Text(text = "发布或更新 Android 原生实况通知", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "启动 / 更新", kind = CouiTextKind.ItemTitle)
        }
        Item(
            onClick = controller::stop,
            summary = { Text(text = "取消当前 PocketBench 系统通知", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "停止实况通知", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "系统状态") {
        StatusItem("通知状态", if (controller.isRunning) "正在运行" else "未启动")
        StatusItem(
            "实况通知授权",
            if (Build.VERSION.SDK_INT < 36) "当前系统版本不支持" else if (controller.canPromote) "已允许" else "未允许",
        )
        StatusItem("系统提升条件", if (controller.isPromotable) "符合" else "尚未确认")
        StatusItem("系统提升结果", if (controller.isPromoted) "已提升" else "尚未提升")
        Item(
            onClick = controller::refreshStatus,
            summary = { Text(text = "重新读取系统返回的通知状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "刷新状态", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(
        title = "使用说明",
        footer = "实况通知由 Android 系统负责创建和渲染；PocketBench 只提交标题、短文本和进度，并读取系统返回的授权与提升状态。",
    ) {
        Item { Text(text = "1. 先在上方开启实况通知；首次启动时按系统提示授予通知权限。", kind = CouiTextKind.ItemSummary) }
        Item { Text(text = "2. 在通知内容中编辑标题和短文本，在进度中调整进度值。", kind = CouiTextKind.ItemSummary) }
        Item { Text(text = "3. 点击启动 / 更新发布或更新系统通知；点击停止实况通知取消通知。", kind = CouiTextKind.ItemSummary) }
        Item { Text(text = "4. 若系统未提升为实况通知，请到设置页检查系统授权；是否显示由 Android 版本和设备策略决定。", kind = CouiTextKind.ItemSummary) }
    }

    if (editingField.isNotEmpty()) {
        val field = editingField
        InputDialog(
            value = editingValue,
            onValueChange = { editingValue = it },
            onDismissRequest = { editingField = "" },
            confirmAction = CouiDialogAction(
                text = "保存",
                onClick = {
                    when (field) {
                        "title" -> controller.updateTitle(editingValue.text)
                        "shortText" -> controller.updateShortText(editingValue.text)
                    }
                    editingField = ""
                },
            ),
            dismissAction = CouiDialogAction(
                text = "取消",
                onClick = { editingField = "" },
            ),
            clearContentDescription = "清除输入",
            title = if (field == "title") "编辑通知标题" else "编辑短文本",
            placeholder = if (field == "title") "输入通知标题" else "输入短文本",
        )
    }
}

@Composable
private fun DeviceMonitorPage(controller: DeviceMonitorController) {
    val champagne = Color(0xFFC9A66B)
    Group(title = "设备概览", footer = "数据由 Android 系统公开接口读取；电池温度为电池传感器读数，设备热状态为系统热状态等级。") {
        Item(summary = { Text(text = "${controller.batteryPercent}% · ${controller.batteryStatus}", kind = CouiTextKind.ItemSummary, color = champagne) }, trailing = { Text(text = "${controller.batteryPercent}%", kind = CouiTextKind.ItemTitle, color = champagne) }) { Text(text = "当前电量", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.batteryTemperature, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "电池温度", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.batteryVoltage, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "电池电压", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.cpuCoreCount, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "CPU 核心数", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.thermalStatus, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "系统热状态", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "操作") {
        Item(onClick = controller::refreshDeviceInfo, summary = { Text(text = "重新读取 Android 系统提供的电池与温度数据", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "刷新设备信息", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun RootPage(controller: DeviceMonitorController) {
    Group(
        title = "ROOT",
        footer = if (controller.rootGranted) "SU 已返回 UID 0，PocketBench ROOT 权限可用。" else "授权后通过固定的 SU 身份查询确认 UID 0。",
    ) {
        if (controller.rootGranted) {
            Item(
                summary = { Text(text = "✓  工作中", kind = CouiTextKind.ItemTitle) },
                trailing = { Text(text = "UID 0", kind = CouiTextKind.ItemSummary) },
            ) {
                Text(text = "ROOT", kind = CouiTextKind.ItemTitle)
            }
        } else {
            Item(
                onClick = controller::requestRootAuthorization,
                enabled = !controller.isCheckingRoot,
                summary = {
                    Text(
                        text = if (controller.isCheckingRoot) "正在等待管理器授权…" else "点击后由本机 ROOT 管理器弹出授权提示",
                        kind = CouiTextKind.ItemSummary,
                    )
                },
                trailing = {
                    Text(
                        text = if (controller.isCheckingRoot) "请求中…" else "授权",
                        kind = CouiTextKind.ItemTitle,
                    )
                },
            ) {
                Text(text = "ROOT 授权", kind = CouiTextKind.ItemTitle)
            }
        }
    }

    Group(title = "设备信息") {
        StatusItem("机型", Build.MODEL.orEmpty().ifBlank { "系统未提供" })
        StatusItem("系统版本", Build.DISPLAY.orEmpty().ifBlank { Build.VERSION.RELEASE.orEmpty().ifBlank { "系统未提供" } })
        StatusItem("KSU 版本", controller.rootKsuVersion)
    }
}

@Composable
private fun SystemWebsitesPage(onOpenOfficial: (String) -> Unit) {
    Group(title = "OPPO") {
        Item(onClick = { onOpenOfficial("https://www.coloros.com/") }, summary = { Text(text = "ColorOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "ColorOS", kind = CouiTextKind.ItemTitle)
        }
    }
    Group(title = "华为") {
        Item(onClick = { onOpenOfficial("https://www.harmonyos.com/") }, summary = { Text(text = "HarmonyOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "HarmonyOS", kind = CouiTextKind.ItemTitle)
        }
    }
    Group(title = "vivo") {
        Item(onClick = { onOpenOfficial("https://www.vivo.com.cn/originos/") }, summary = { Text(text = "OriginOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "OriginOS", kind = CouiTextKind.ItemTitle)
        }
    }
    Group(title = "小米") {
        Item(onClick = { onOpenOfficial("https://os1.hyperos.mi.com/") }, summary = { Text(text = "Xiaomi HyperOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "Xiaomi HyperOS", kind = CouiTextKind.ItemTitle)
        }
    }
    Group(title = "荣耀") {
        Item(onClick = { onOpenOfficial("https://www.honor.com/cn/magic-os/") }, summary = { Text(text = "荣耀 MagicOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "荣耀 MagicOS", kind = CouiTextKind.ItemTitle)
        }
    }
    Group(title = "Apple") {
        Item(onClick = { onOpenOfficial("https://www.apple.com.cn/ios/") }, summary = { Text(text = "Apple iOS 官方网站", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "iOS", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun SettingsPage(
    controller: LiveUpdateController,
    onOpenNotificationSettings: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
    onOpenSourceInfo: () -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
) {
    Group(title = "开源与组件") {
        Item(onClick = onOpenSourceInfo, summary = { Text(text = "项目源码、开源许可、COUI 依赖与官方组件示例", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "开源与组件", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "权限管理", footer = "权限由 Android 系统管理，点击后进入对应系统设置页面。") {
        StatusItem(
            "通知运行状态",
            if (controller.isRunning) "运行中" else "未运行",
        )
        StatusItem(
            "实况通知授权",
            if (Build.VERSION.SDK_INT < 36) "当前系统版本不支持" else if (controller.canPromote) "已允许" else "未允许",
        )
        Item(
            onClick = onOpenNotificationSettings,
            summary = { Text(text = "管理通知开关和通知类别", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "通知权限设置", kind = CouiTextKind.ItemTitle)
        }
        Item(
            onClick = onOpenPromotionSettings,
            summary = { Text(text = "在系统支持时管理实况通知授权", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "实况通知授权", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "外观", footer = "跟随系统会自动匹配设备当前的浅色或深色外观。") {
        listOf("system" to "跟随系统", "light" to "浅色模式", "dark" to "深色模式").forEach { (mode, label) ->
            Item(
                onClick = { onThemeModeChange(mode) },
                trailing = {
                    Text(
                        text = if (themeMode == mode) "已选择" else "",
                        kind = CouiTextKind.ItemSummary,
                    )
                },
            ) {
                Text(text = label, kind = CouiTextKind.ItemTitle)
            }
        }
    }

    Group(title = "常规") {
        Item(
            onClick = controller::refreshStatus,
            summary = { Text(text = "重新读取当前通知与系统授权状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "刷新应用状态", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = { Text(text = "PocketBench 1.0.0.13", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "应用版本", kind = CouiTextKind.ItemTitle)
        }
    }
}


@Composable
private fun OpenSourcePage(onOpenPocketBench: () -> Unit, onOpenCoui: () -> Unit, onOpenCouiDocs: () -> Unit) {
    Group(title = "项目开源", footer = "PocketBench 源码公开托管在 GitHub。当前仓库尚未声明独立开源许可证，请以仓库后续正式添加的 LICENSE 文件为准。") {
        Item(onClick = onOpenPocketBench, summary = { Text(text = "liangfangfa0-afk/PocketBench · GitHub", kind = CouiTextKind.ItemSummary) }) { Text(text = "PocketBench 源码仓库", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "引用组件 · COUI-Kit", footer = "COUI-Kit 官方仓库声明其项目自有源码采用 Mozilla Public License 2.0（MPL-2.0）；第三方内容遵循各自许可。PocketBench 与 OPPO 无隶属、授权或背书关系。") {
        Item(onClick = onOpenCoui, summary = { Text(text = "ItosEO/COUI-Kit · 官方源码、许可与更新记录", kind = CouiTextKind.ItemSummary) }) { Text(text = "COUI-Kit 官方开源仓库", kind = CouiTextKind.ItemTitle) }
        Item(onClick = onOpenCouiDocs, summary = { Text(text = "Group、Item、Dialog、NavigationBar、Slider 等组件 API", kind = CouiTextKind.ItemSummary) }) { Text(text = "COUI 官方组件文档", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "官方依赖引用", footer = "以下版本与 PocketBench 当前 Gradle 配置保持一致。COUI-Kit 官方 README 的核心依赖写法为 io.github.itoseo:coui。") {
        Item { Text(text = "dependencies {\n    implementation(\"io.github.itoseo:coui:0.0.1-alpha12\")\n    implementation(\"io.github.itoseo:coui-icons:0.0.1-alpha12\")\n}", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "官方示例 · Group + Item", footer = "按 COUI-Kit 官方 docs/components/group.md 与 item.md 示例结构：先读取 CouiPageDefaults.themeColors()，再组合 Group、Item 与 COUI Text。") {
        Item { Text(text = "@Composable\nfun SettingsGroupExample(\n    modifier: Modifier = Modifier,\n    groupTitle: String,\n    rowTitle: String,\n    footerText: String,\n    onRowClick: () -> Unit,\n) {\n    val colors = CouiPageDefaults.themeColors()\n    Group(\n        modifier = modifier,\n        title = groupTitle,\n        footer = footerText,\n        colors = colors,\n    ) {\n        Item(onClick = onRowClick) {\n            Text(\n                text = rowTitle,\n                kind = CouiTextKind.ItemTitle,\n                color = colors.title,\n            )\n        }\n    }\n}", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "官方示例 · COUI Dialog", footer = "公告弹窗采用 COUI-Kit Dialog + CouiDialogAction API；显隐状态由宿主持有，操作回调中关闭弹窗。") {
        Item { Text(text = "Dialog(\n    visible = visible,\n    onDismissRequest = onDismissRequest,\n    title = title,\n    message = message,\n    confirmAction = CouiDialogAction(\n        text = \"知道了\",\n        onClick = { visible = false },\n    ),\n)", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "官方资料") {
        Item(onClick = onOpenCouiDocs, summary = { Text(text = "组件索引、参数说明、接入边界与源码链接", kind = CouiTextKind.ItemSummary) }) { Text(text = "COUI 组件目录", kind = CouiTextKind.ItemTitle) }
    }
}



@Composable
private fun OriginOsAdaptationPage(onOpenOfficial: () -> Unit) {
    RemoteAdaptationPage(section = "originos", title = "OriginOS 7 官方适配计划", onOpenOfficial = onOpenOfficial)
}

private const val REMOTE_ADAPTATION_URL =
    "https://raw.githubusercontent.com/liangfangfa0-afk/PocketBench/main/data/device_adaptation.json"

private fun fetchAdaptationJson(): String {
    val connection = (URL(REMOTE_ADAPTATION_URL).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8000
        readTimeout = 10000
        setRequestProperty("Accept", "application/json")
        setRequestProperty("Cache-Control", "no-cache")
    }
    return try {
        if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
        connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

@Composable
private fun RemoteAdaptationPage(
    section: String,
    title: String,
    onOpenOfficial: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("pocketbench_remote_data", android.content.Context.MODE_PRIVATE)
    }
    var refreshKey by rememberSaveable(section) { mutableIntStateOf(0) }
    var rawJson by remember(section) { mutableStateOf(preferences.getString("adaptation_json", null)) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf("") }

    LaunchedEffect(refreshKey, section) {
        loading = true
        loadError = ""
        try {
            val latest = withContext(Dispatchers.IO) { fetchAdaptationJson() }
            val parsed = JSONObject(latest)
            require(parsed.optInt("schema_version") == 1) { "不支持的数据格式版本" }
            require(listOf("coloros", "harmony", "originos", "xiaomi", "honor", "apple").all(parsed::has)) { "配置缺少必要字段" }
            rawJson = latest
            preferences.edit().putString("adaptation_json", latest).apply()
        } catch (error: Exception) {
            loadError = error.message ?: "网络请求失败"
        } finally {
            loading = false
        }
    }

    val config = remember(rawJson) {
        runCatching { rawJson?.let(::JSONObject) }.getOrNull()
    }
    val updatedAt = config?.optString("updated_at").orEmpty()
    val configVersion = config?.optString("config_version").orEmpty()

    Group(
        title = title,
        footer = "数据由应用在后台读取并缓存；接口地址仅作为内部数据源使用，不在页面中展示或提供复制。刷新只读取 JSON，不执行远程代码或系统命令。"
    ) {
        Item(
            onClick = { refreshKey++ },
            trailing = { Text(text = if (loading) "更新中…" else "刷新", kind = CouiTextKind.ItemTitle) },
            summary = {
                Text(
                    text = when {
                        loading -> "正在检查 GitHub 远程配置"
                        loadError.isNotEmpty() && rawJson != null -> "联网失败，当前显示本地缓存 · $loadError"
                        loadError.isNotEmpty() -> "获取失败：$loadError"
                        rawJson == null -> "尚无缓存数据"
                        else -> "配置版本 $configVersion · 更新于 $updatedAt"
                    },
                    kind = CouiTextKind.ItemSummary
                )
            }
        ) { Text(text = "云端数据", kind = CouiTextKind.ItemTitle) }
    }

    if (config == null) {
        Group(title = "暂无可用数据") {
            Item { Text(text = "请检查网络连接；首次加载失败时，页面不会显示未经验证的远程内容。", kind = CouiTextKind.ItemSummary) }
        }
        return
    }

    if (section == "coloros") {
        val versions = config.optJSONArray("coloros") ?: JSONArray()
        for (versionIndex in 0 until versions.length()) {
            val version = versions.optJSONObject(versionIndex) ?: continue
            val versionName = version.optString("version")
            val region = version.optString("region")
            Group(title = versionName, footer = region) {
                val schedule = version.optJSONArray("schedule") ?: JSONArray()
                for (batchIndex in 0 until schedule.length()) {
                    val batch = schedule.optJSONObject(batchIndex) ?: continue
                    val models = batch.optJSONArray("models") ?: JSONArray()
                    val names = (0 until models.length()).mapNotNull { models.optString(it).takeIf(String::isNotBlank) }
                    Group(title = batch.optString("period")) {
                        names.forEach { model ->
                            Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                        }
                    }
                }
                val notes = version.optJSONArray("notes") ?: JSONArray()
                for (noteIndex in 0 until notes.length()) {
                    Item { Text(text = notes.optString(noteIndex), kind = CouiTextKind.ItemSummary) }
                }
            }
        }
    } else if (section == "originos") {
        val originos = config.optJSONObject("originos") ?: JSONObject()
        val schedule = originos.optJSONArray("schedule") ?: JSONArray()
        Group(title = originos.optString("version"), footer = originos.optString("region")) {
            for (periodIndex in 0 until schedule.length()) {
                val period = schedule.optJSONObject(periodIndex) ?: continue
                Group(title = period.optString("period")) {
                    val brands = period.optJSONArray("brands") ?: JSONArray()
                    for (brandIndex in 0 until brands.length()) {
                        val brand = brands.optJSONObject(brandIndex) ?: continue
                        Group(title = brand.optString("name")) {
                            val models = brand.optJSONArray("models") ?: JSONArray()
                            for (modelIndex in 0 until models.length()) {
                                val model = models.optString(modelIndex)
                                if (model.isNotBlank()) Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                            }
                        }
                    }
                }
            }
        }
        val notes = originos.optJSONArray("notes") ?: JSONArray()
        for (noteIndex in 0 until notes.length()) {
            Item { Text(text = notes.optString(noteIndex), kind = CouiTextKind.ItemSummary) }
        }
    } else if (section == "xiaomi" || section == "honor") {
        val vendor = config.optJSONObject(section) ?: JSONObject()
        Group(title = vendor.optString("version"), footer = vendor.optString("region")) {
            val schedule = vendor.optJSONArray("schedule") ?: JSONArray()
            for (index in 0 until schedule.length()) {
                val entry = schedule.optJSONObject(index) ?: continue
                Group(title = entry.optString("period")) {
                    val category = entry.optString("category")
                    if (category.isNotBlank()) Item { Text(text = category, kind = CouiTextKind.ItemTitle) }
                    val models = entry.optJSONArray("models") ?: JSONArray()
                    for (modelIndex in 0 until models.length()) {
                        val model = models.optString(modelIndex)
                        if (model.isNotBlank()) Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                    }
                }
            }
        }
        val notes = vendor.optJSONArray("notes") ?: JSONArray()
        for (index in 0 until notes.length()) Item { Text(text = notes.optString(index), kind = CouiTextKind.ItemSummary) }
    } else if (section == "apple") {
        val apple = config.optJSONObject("apple") ?: JSONObject()
        Group(title = apple.optString("version"), footer = apple.optString("region")) {
            val categories = apple.optJSONArray("categories") ?: JSONArray()
            for (index in 0 until categories.length()) {
                val category = categories.optJSONObject(index) ?: continue
                Group(title = category.optString("name")) {
                    val models = category.optJSONArray("models") ?: JSONArray()
                    for (modelIndex in 0 until models.length()) {
                        val model = models.optString(modelIndex)
                        if (model.isNotBlank()) Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                    }
                }
            }
        }
        val notes = apple.optJSONArray("notes") ?: JSONArray()
        for (index in 0 until notes.length()) Item { Text(text = notes.optString(index), kind = CouiTextKind.ItemSummary) }
    } else {
        val harmony = config.optJSONObject("harmony") ?: JSONObject()
        Group(title = "HarmonyOS 官方时间信息", footer = harmony.optString("region")) {
            val timeline = harmony.optJSONArray("timeline") ?: JSONArray()
            for (index in 0 until timeline.length()) {
                val entry = timeline.optJSONObject(index) ?: continue
                Item(summary = { Text(text = entry.optString("status"), kind = CouiTextKind.ItemSummary) }) {
                    Text(text = "${entry.optString("period")} · ${entry.optString("version")}", kind = CouiTextKind.ItemTitle)
                }
            }
        }
        val versions = harmony.optJSONArray("versions") ?: JSONArray()
        for (versionIndex in 0 until versions.length()) {
            val version = versions.optJSONObject(versionIndex) ?: continue
            Group(title = version.optString("version"), footer = version.optString("source")) {
                val categories = version.optJSONArray("categories") ?: JSONArray()
                for (categoryIndex in 0 until categories.length()) {
                    val category = categories.optJSONObject(categoryIndex) ?: continue
                    val models = category.optJSONArray("models") ?: JSONArray()
                    val names = (0 until models.length()).mapNotNull { models.optString(it).takeIf(String::isNotBlank) }
                    Group(title = category.optString("name")) {
                        names.forEach { model ->
                            Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                        }
                    }
                }
                val notes = version.optJSONArray("notes") ?: JSONArray()
                for (noteIndex in 0 until notes.length()) {
                    Item { Text(text = notes.optString(noteIndex), kind = CouiTextKind.ItemSummary) }
                }
            }
        }
    }

    Group(title = "官方原始资料") {
        Item(onClick = onOpenOfficial, summary = { Text(text = "打开厂商官方页面核对名单和最新推送安排", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "查看官方升级信息", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun ColorOsAdaptationPage(onOpenOfficial: () -> Unit) {
    RemoteAdaptationPage(section = "coloros", title = "ColorOS 官方适配计划", onOpenOfficial = onOpenOfficial)
}

@Composable
private fun HarmonyAdaptationPage(onOpenOfficial: () -> Unit) {
    RemoteAdaptationPage(section = "harmony", title = "HarmonyOS 官方适配计划", onOpenOfficial = onOpenOfficial)
}

@Composable
private fun StatusItem(label: String, value: String) {
    Item(
        trailing = { Text(text = value, kind = CouiTextKind.ItemSummary) },
    ) {
        Text(text = label, kind = CouiTextKind.ItemTitle)
    }
}
