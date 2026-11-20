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
    val isSubPage = currentPage in setOf("live", "device", "opensource", "coloros", "harmony")

    fun navigate(page: String) {
        if (page != "home" && page != "settings") returnPage = currentPage
        navigationForward = page != "home"
        currentPage = page
        selectedTab = if (page == "settings") 1 else 0
    }

    fun goBack() {
        navigationForward = false
        currentPage = if (returnPage == "settings") "settings" else "home"
        selectedTab = if (currentPage == "settings") 1 else 0
        returnPage = "home"
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
                            "opensource" -> "开源与组件"
                            "coloros" -> "ColorOS 17 适配"
                            "harmony" -> "HarmonyOS 7 适配"
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
                    "opensource" -> OpenSourcePage(
                        onOpenPocketBench = { uriHandler.openUri("https://github.com/liangfangfa0-afk/PocketBench") },
                        onOpenCoui = { uriHandler.openUri("https://github.com/ItosEO/COUI-Kit") },
                        onOpenCouiDocs = { uriHandler.openUri("https://github.com/ItosEO/COUI-Kit/tree/main/docs/components") },
                    )
                    "coloros" -> ColorOsAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://www.coloros.com/upgrade/") })
                    "harmony" -> HarmonyAdaptationPage(onOpenOfficial = { uriHandler.openUri("https://consumer.huawei.com/cn/support/harmonyos/models-7/") })
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
                        onOpenColorOs = { navigate("coloros") },
                        onOpenHarmony = { navigate("harmony") },
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
        message = "1.0.0.10\n\n精简首页说明，新增 ColorOS 17 与 HarmonyOS 7 机型适配进度页面；设置页新增项目开源与 COUI 官方组件引用说明。\n\n适配批次以厂商公开计划为准，计划启动日期不代表全量推送完成。",
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
    onOpenColorOs: () -> Unit,
    onOpenHarmony: () -> Unit,
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
        Item(onClick = onOpenColorOs, summary = { Text(text = "查看 ColorOS 17 官方升级批次、机型与计划节点", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "ColorOS 17 机型适配", kind = CouiTextKind.ItemTitle)
        }
        Item(onClick = onOpenHarmony, summary = { Text(text = "查看 HarmonyOS 7 正式版与公测版支持机型", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "鸿蒙机型适配", kind = CouiTextKind.ItemTitle)
        }
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
    Group(title = "设备概览", footer = "基础数据由 Android 系统接口读取；电池温度为电池传感器读数，设备热状态为系统热状态等级。") {
        Item(summary = { Text(text = "电量 ${controller.batteryPercent}% · ${controller.batteryStatus}", kind = CouiTextKind.ItemSummary, color = champagne) }, trailing = { Text(text = "${controller.batteryPercent}%", kind = CouiTextKind.ItemTitle, color = champagne) }) { Text(text = "当前电量", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.batteryTemperature, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "电池温度", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.batteryVoltage, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "电池电压", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.cpuCoreCount, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "CPU 核心数", kind = CouiTextKind.ItemTitle) }
        Item(summary = { Text(text = controller.thermalStatus, kind = CouiTextKind.ItemSummary, color = champagne) }) { Text(text = "系统热状态", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "Root 授权", footer = "仅请求一次只读身份确认（su -c id），用于确认 SU 是否授予 UID 0。不会执行写入、改权限、挂载、刷写、删除或调频命令；基础设备数据仍通过 Android 系统 API 读取。") {
        Item(summary = { Text(text = controller.rootStatus, kind = CouiTextKind.ItemSummary) }, trailing = { Text(text = if (controller.isCheckingRoot) "检查中…" else if (controller.rootGranted) "已授权" else "未授权", kind = CouiTextKind.ItemTitle, color = champagne) }) { Text(text = "SU 状态", kind = CouiTextKind.ItemTitle) }
        Item(onClick = controller::requestRootAuthorization, enabled = !controller.isCheckingRoot, summary = { Text(text = "点击后由本机 Root 管理器显示授权请求；拒绝不会影响普通设备信息读取。", kind = CouiTextKind.ItemSummary) }) { Text(text = if (controller.rootGranted) "重新检查 Root 授权" else "请求 Root 授权", kind = CouiTextKind.ItemTitle) }
        Item(onClick = controller::refreshDeviceInfo, summary = { Text(text = "重新读取 Android 系统提供的电池与温度数据", kind = CouiTextKind.ItemSummary) }) { Text(text = "刷新设备信息", kind = CouiTextKind.ItemTitle) }
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
            summary = { Text(text = "PocketBench 1.0.0.10", kind = CouiTextKind.ItemSummary) },
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
private fun ColorOsAdaptationPage(onOpenOfficial: () -> Unit) {
    val batches = listOf(
        "11 月 5 日 · 首批计划" to listOf("OPPO Pad 6", "OPPO Pad 6 柔光版", "OPPO Pad 5", "OPPO Pad 5 柔光版", "一加 Ace 5 Pro", "一加 Ace 5", "一加 Ace 5 至尊版", "一加 Ace 5 竞速版", "一加平板 2", "真我 GT7", "真我 GT7 阿斯顿马丁 F1 限量版", "真我 Neo7", "真我 Neo7 不良人限定版", "真我 Neo7 Turbo"),
        "11 月 17 日" to listOf("OPPO Find X7 Ultra", "OPPO Find X7 Ultra 卫星通信版", "OPPO Find X7", "一加 12"),
        "11 月 18 日" to listOf("OPPO Reno15 Pro", "OPPO Reno15", "OPPO Reno15c", "OPPO Pad 4 Pro", "一加平板 2 Pro", "真我 Neo7 SE"),
        "11 月 19 日" to listOf("真我 15 Pro", "真我 15 Pro《权力的游戏》限定版", "真我 15", "真我 15T"),
        "11 月 24 日" to listOf("OPPO Find N3", "OPPO Find N3 典藏版", "OPPO Find N3 Flip", "OPPO Reno14 Pro", "OPPO Reno14", "真我 GT5 Pro"),
        "11 月 26 日" to listOf("OPPO K15 Pro+", "OPPO K15 Pro", "OPPO K15", "一加 Turbo 6", "一加 Turbo 6V"),
        "11 月 30 日" to listOf("OPPO Watch S"),
        "12 月" to listOf("OPPO Find X6 Pro", "OPPO Find X6", "OPPO Reno13 Pro", "OPPO Reno13", "OPPO K13 Turbo Pro", "OPPO K13 Turbo", "OPPO K12s", "OPPO Pad Air5", "OPPO Pad Air5 柔光版", "OPPO Pad 3 Pro", "OPPO Pad 3", "一加 11", "一加 11 木星岩定制版", "一加 Ace 3 Pro", "一加 Ace 3", "一加 Ace 3 原神刻晴定制机", "一加 Ace 3V", "一加 Turbo 6X Pro", "一加 Turbo 6X", "一加平板 Pro", "一加平板", "真我 GT6", "真我 Neo6", "真我 Neo6 SE", "真我 Neo7x", "真我 14 Pro+", "真我 14 Pro", "真我 14", "真我 V70", "真我 V70s"),
        "2027 年 Q1" to listOf("OPPO Find N2", "OPPO Find N2 Flip"),
    )
    Group(title = "ColorOS 17 官方适配进度", footer = "截至 2026 年 11 月 20 日。进度条统计已到达的公开计划启动节点（4/9），不是实际升级覆盖率；官方说明计划日期代表开始推送，不代表全量推送完成。") {
        Item(trailing = { Text(text = "44%", kind = CouiTextKind.ItemTitle) }) { Text(text = "计划节点进度", kind = CouiTextKind.ItemTitle) }
        Slider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), value = 4f / 9f, onValueChange = {}, valueRange = 0f..1f, enabled = false)
        Item { Text(text = "已到达 4 个计划节点 · 共公布 9 个时间节点", kind = CouiTextKind.ItemSummary) }
    }
    batches.forEachIndexed { index, pair ->
        val (date, models) = pair
        Group(title = date, footer = if (index < 4) "官方计划启动日期已到；各机型仍可能分批推送。" else "官方公布的后续计划批次，实际安排以官方更新为准。") {
            Item { Text(text = models.joinToString("、"), kind = CouiTextKind.ItemSummary) }
        }
    }
    Group(title = "已发布机型（截至 11 月 1 日）", footer = "以下为官方页面单独列出的已发布机型，并非上述后续计划批次。") {
        Item { Text(text = "OPPO Find N6 / N6 卫星通信版、Find N5 / N5 卫星通信版、Find X9 Ultra / Ultra 卫星通信版、Find X9s Pro、Find X9 Pro / 卫星通信版、Find X9、Find X8 Ultra / 卫星通信版、Find X8s+、Find X8s、Find X8 Pro、Find X8 Pro 卫星通信版、Find X8、Reno16 Pro、Reno16、Reno16t、Pad 5 Pro、Pad Mini、Pad Mini 柔光版、Watch X3、Watch X3 Mini、Watch X2、Watch X2 Mini、一加 15、15T、13、13T、Ace 6、Ace 6 至尊版、Ace 6T、Ace 6T 原神神里绫华定制机、一加平板 3 Pro、真我 GT8 Pro、GT8 Pro 阿斯顿马丁 F1 限量版、GT8、GT7 Pro、GT7 Pro 竞速版、Neo8。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "官方说明") {
        Item(onClick = onOpenOfficial, summary = { Text(text = "打开 ColorOS 官方升级计划，查看实时更新与完整机型名单", kind = CouiTextKind.ItemSummary) }) { Text(text = "ColorOS 官方升级计划", kind = CouiTextKind.ItemTitle) }
        Item { Text(text = "官方备注：机型排列不代表实际推送先后；计划可能随研发进展调整，部分功能受硬件支持情况影响。", kind = CouiTextKind.ItemSummary) }
    }
}


@Composable
private fun HarmonyAdaptationPage(onOpenOfficial: () -> Unit) {
    Group(title = "HarmonyOS 7 官方适配", footer = "名单来自华为官方支持机型页面。官方说明目前已公布可支持升级尝鲜的具体机型，更多机型陆续上线中；适用地区为中国大陆。") {
        Item { Text(text = "华为官方已公布正式版与公测版名单；官方未公布整体适配百分比，因此这里不虚构完成率。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "正式版 · 手机") {
        Item { Text(text = "HUAWEI Mate 80、Mate 80 Pro、Mate 80 Pro Max、Mate 80 Pro Max 风驰版、Mate 80 RS 非凡大师；Mate 70、Mate 70 Pro、Mate 70 Pro 优享版、Mate 70 Pro+、Mate 70 RS 非凡大师、Mate 70 Air；Mate 60、Mate 60 Pro、Mate 60 Pro+、Mate 60 RS 非凡大师；Mate X7、Mate X7 典藏版、Mate X6、Mate X6 典藏版、Mate X5、Mate X5 典藏版、Mate XTs 非凡大师、Mate XT 非凡大师；Pura 90、Pura 90 Pro、Pura 90 Pro Max、Pura 80、Pura 80 Pro、Pura 80 Pro+、Pura 80 Ultra、Pura 70、Pura 70 北斗卫星消息版、Pura 70 Pro、Pura 70 Pro+、Pura 70 Ultra；Pura X Max、Pura X Max 典藏版、Pura X、Pura X 典藏版；Pocket 2、Pocket 2 艺术定制版、Pocket 2 优享版；nova 16、nova 16 Pro、nova 16 Ultra、nova 16 SE、nova 16z；nova 15、nova 15 Pro、nova 15 Ultra；nova 14、nova 14 Pro、nova 14 Ultra、nova 14 活力版；nova 13、nova 13 Pro；nova 12、nova 12 Pro、nova 12 Ultra、nova 12 Ultra 星耀版；nova Flip S、nova Flip；畅享 90、畅享 90 Plus、畅享 90m Plus、畅享 90 Pro Max、畅享 70X、畅享 70X 尊享版。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "正式版 · 平板") {
        Item { Text(text = "MatePad Edge、MatePad Pro Max、MatePad Pro 13.2 英寸 2025、MatePad Pro 12.2 英寸 2025、MatePad Pro 12 英寸、MatePad Mini、MatePad Air 12 英寸 2026、MatePad Air 12 英寸 2025、MatePad 11.5 2026、MatePad 11.5 S 2025、MatePad 11.5 S 灵动款 2025、MatePad 11.5 S 活力版 2025。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "正式版 · PC") {
        Item { Text(text = "MateBook Fold 非凡大师 麒麟X90 Plus、MateBook Fold 非凡大师、MateBook Pro 麒麟X90 Plus、MateBook Pro、MateBook Pro S、MateBook 14 鸿蒙版。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "正式版 · 穿戴") {
        Item { Text(text = "WATCH Ultimate 2 非凡探索、WATCH ULTIMATE DESIGN 非凡大师紫金款、WATCH ULTIMATE DESIGN 非凡大师尊界定制款、WATCH GT 7、WATCH GT 7 Pro、WATCH GT 6、WATCH GT 6 Pro、WATCH GT Runner 2、WATCH FIT 5、WATCH FIT 5 Pro。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "公测版 · 平板") {
        Item { Text(text = "MatePad Pro 13.2 英寸 2023、MatePad Pro 13.2 英寸 2023 典藏版、MatePad Pro 12.2 英寸 2024、MatePad Pro 11 英寸 2024、MatePad Air 12 英寸 2024、MatePad 11.5 S 2024、MatePad 11.5 S 灵动款 2024。", kind = CouiTextKind.ItemSummary) }
    }
    Group(title = "官方说明") {
        Item(onClick = onOpenOfficial, summary = { Text(text = "打开华为官方 HarmonyOS 7 支持机型页面，查看更新后的名单", kind = CouiTextKind.ItemSummary) }) { Text(text = "HarmonyOS 7 官方支持机型", kind = CouiTextKind.ItemTitle) }
        Item { Text(text = "各机型对 HarmonyOS 7 特性的支持情况因产品而异，请以华为实际公告为准。", kind = CouiTextKind.ItemSummary) }
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Item(
        trailing = { Text(text = value, kind = CouiTextKind.ItemSummary) },
    ) {
        Text(text = label, kind = CouiTextKind.ItemTitle)
    }
}
