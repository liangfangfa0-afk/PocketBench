package com.ljj.PocketBench.ui

import android.os.Build
import androidx.annotation.RequiresApi

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    onStart: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var currentPage by rememberSaveable { mutableStateOf("home") }
    var navigationForward by remember { mutableStateOf(true) }
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember { CouiFloatingRenderer() }
    val isSubPage = currentPage == "live" || currentPage == "announcement"

    fun navigate(page: String) {
        navigationForward = true
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
                            navigationForward = it > selectedTab
                            selectedTab = it
                            currentPage = if (it == 0) "home" else "settings"
                        },
                    )
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
                .consumeWindowInsets(contentPadding),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        val enter = if (navigationForward) {
                            slideInHorizontally(
                                initialOffsetX = { it / 5 },
                                animationSpec = tween(280),
                            ) + fadeIn(animationSpec = tween(220))
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it / 5 },
                                animationSpec = tween(280),
                            ) + fadeIn(animationSpec = tween(220))
                        }
                        val exit = if (navigationForward) {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 8 },
                                animationSpec = tween(220),
                            ) + fadeOut(animationSpec = tween(180))
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it / 8 },
                                animationSpec = tween(220),
                            ) + fadeOut(animationSpec = tween(180))
                        }
                        enter togetherWith exit
                    },
                    label = "PocketBenchSecondaryPageTransition",
                ) { page ->
                    when (page) {
                        "live" -> LiveUpdatePage(controller, onStart)
                        "announcement" -> AnnouncementPage()
                        "settings" -> SettingsPage(
                            controller = controller,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            onOpenPromotionSettings = onOpenPromotionSettings,
                        )
                        else -> HomePage(onOpenLiveUpdate = { navigate("live") })
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
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
private fun HomePage(onOpenLiveUpdate: () -> Unit) {
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
                Text(text = "›", kind = CouiTextKind.ItemTitle)
            },
        ) {
            Text(text = "实况通知", kind = CouiTextKind.ItemTitle)
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
private fun SettingsPage(
    controller: LiveUpdateController,
    onOpenNotificationSettings: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
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

    Group(title = "常规") {
        Item(
            onClick = controller::refreshStatus,
            summary = { Text(text = "重新读取当前通知与系统授权状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "刷新应用状态", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = { Text(text = "PocketBench 0.5", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "应用版本", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun AnnouncementPage() {
    Group(title = "最新公告", footer = "后续版本更新时，会在这里补充对应版本的更新内容。") {
        Item(
            summary = {
                Text(
                    text = "重做 COUI 页面导航与交互；加入二级页面转场；通知继续由 Android 原生 API 创建和管理。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 0.5", kind = CouiTextKind.ItemTitle)
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
