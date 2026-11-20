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
import com.itos.coui.dialog.InputDialog
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
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember { CouiFloatingRenderer() }
    val isSubPage = currentPage == "live" || currentPage == "announcement" || currentPage == "device"

    fun navigate(page: String) {
        navigationForward = page != "home"
        currentPage = page
        selectedTab = if (page == "settings") 1 else 0
    }

    fun goBack() {
        navigationForward = false
        currentPage = "home"
        selectedTab = 0
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
                            "announcement" -> "公告"
                            "device" -> "电池与温度"
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
                        CouiAnnouncementTopBarButton(onClick = { navigate("announcement") })
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
                    "announcement" -> AnnouncementPage()
                    "device" -> DeviceMonitorPage(deviceMonitor)
                    "settings" -> SettingsPage(
                        controller = controller,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onOpenPromotionSettings = onOpenPromotionSettings,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                    )
                    else -> HomePage(
                        controller = controller,
                        onStart = onStart,
                        onOpenLiveUpdate = { navigate("live") },
                        onOpenDeviceMonitor = { deviceMonitor.refreshDeviceInfo(); navigate("device") },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
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

    Group(title = "使用说明") {
        Item(
            summary = {
                Text(
                    text = "通知由 Android 系统创建和渲染；应用页面负责配置与控制。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "原生通知接入", kind = CouiTextKind.ItemTitle)
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
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
) {
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
            summary = { Text(text = "PocketBench 1.0.0.9", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "应用版本", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun AnnouncementPage() {
    Group(title = "最新公告", footer = "每次更新都会在这里保留对应版本的更新内容。") {
        Item(
            summary = {
                Text(
                    text = "新增电池与温度设备监测页；Root 授权仅执行只读 UID 身份确认，不运行写入或系统修改命令。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 1.0.0.9", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = {
                Text(
                    text = "恢复二级页面方向性切换动画；增加跟随系统、浅色模式、深色模式三种外观选择。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 1.0.0.7", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = {
                Text(
                    text = "保留通知标题、短文本和进度的自定义编辑；加入 COUI 主题选择。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 1.0.0.6", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = {
                Text(
                    text = "使用 COUI 页面导航和二级页面转场；通知由 Android 原生 API 创建和管理。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 1.0.0.5", kind = CouiTextKind.ItemTitle)
        }
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
