package com.ljj.PocketBench.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
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
import com.itos.coui.theme.LocalCouiTextStyle
import com.itos.coui.text.Text
import com.itos.coui.topappbar.TopAppBar
import com.ljj.PocketBench.notification.LiveUpdateController

@RequiresApi(34)
private class BottomBarRenderer {
    val renderer = CouiFloatingRenderer()
}

@Composable
fun PocketBenchApp(
    controller: LiveUpdateController,
    onStart: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var subPage by rememberSaveable { mutableStateOf("") }
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember {
        if (Build.VERSION.SDK_INT >= 34) runCatching { BottomBarRenderer() } else null
    }

    BackHandler(enabled = subPage.isNotEmpty()) {
        subPage = ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = pageColors.background,
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.text.BasicText(
                        text = when (subPage) {
                            "live" -> "实况通知"
                            "announcement" -> "公告"
                            else -> "PocketBench"
                        },
                        style = LocalCouiTextStyle.current,
                    )
                },
                navigationIcon = {
                    if (subPage.isNotEmpty()) {
                        androidx.compose.foundation.text.BasicText(
                            text = "‹",
                            style = LocalCouiTextStyle.current,
                            modifier = Modifier
                                .clickable { subPage = "" }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                },
                actions = {
                    if (subPage.isEmpty()) {
                        androidx.compose.foundation.text.BasicText(
                            text = "公告",
                            style = LocalCouiTextStyle.current,
                            modifier = Modifier
                                .clickable { subPage = "announcement" }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (subPage.isEmpty()) {
                val renderer = bottomRenderer?.getOrNull()?.renderer
                if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                    NavigationBar(
                        renderer = renderer,
                        items = listOf(
                            CouiNavigationItem("首页", Icons.Outlined.Home, Icons.Filled.Home),
                            CouiNavigationItem("设置", Icons.Outlined.Settings, Icons.Filled.Settings),
                        ),
                        selectedIndex = selectedTab,
                        onSelection = { selectedTab = it },
                    )
                }
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
                when (subPage) {
                    "live" -> LiveUpdatePage(controller, onStart)
                    "announcement" -> AnnouncementPage()
                    else -> if (selectedTab == 0) {
                        HomePage(onOpenLiveUpdate = { subPage = "live" })
                    } else {
                        SettingsPage(
                            controller = controller,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            onOpenPromotionSettings = onOpenPromotionSettings,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HomePage(onOpenLiveUpdate: () -> Unit) {
    Group(title = "首页", footer = "选择功能进入对应页面。") {
        Item(
            onClick = onOpenLiveUpdate,
            summary = {
                Text(
                    text = "创建和管理由 Android 系统展示的原生实况通知",
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

    Group(title = "关于 PocketBench") {
        Item(
            summary = {
                Text(text = "管理系统通知与应用常规选项", kind = CouiTextKind.ItemSummary)
            },
        ) {
            Text(text = "轻量、清晰，尽量使用系统原生能力", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun LiveUpdatePage(controller: LiveUpdateController, onStart: () -> Unit) {
    Group(title = "实况通知", footer = "通知由 Android 系统创建和渲染；本页面只负责配置与控制，不绘制仿系统通知。") {
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
            summary = { Text(text = "通知的主标题", kind = CouiTextKind.ItemSummary) },
            trailing = { TextEditor(controller.title, controller::updateTitle, "输入标题") },
        ) {
            Text(text = "标题", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = { Text(text = "用于系统实况通知的短文本字段", kind = CouiTextKind.ItemSummary) },
            trailing = { TextEditor(controller.shortText, controller::updateShortText, "短文本") },
        ) {
            Text(text = "短文本", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "进度", footer = "拖动滑块时会更新同一条系统通知。") {
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
        StatusItem("系统授权", if (controller.canPromote) "已允许" else "未允许或不可用")
        StatusItem("系统提升条件", if (controller.isPromotable) "符合" else "尚未确认")
        StatusItem("系统提升结果", if (controller.isPromoted) "已提升" else "尚未提升")
        Item(
            onClick = controller::refreshStatus,
            summary = { Text(text = "重新读取系统返回的通知状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "刷新状态", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun SettingsPage(
    controller: LiveUpdateController,
    onOpenNotificationSettings: () -> Unit,
    onOpenPromotionSettings: () -> Unit,
) {
    Group(title = "权限管理", footer = "权限由 Android 系统管理，点击后进入系统设置页面。") {
        StatusItem(
            "通知运行状态",
            if (controller.isRunning) "运行中" else "未运行",
        )
        StatusItem(
            "实况通知授权",
            if (controller.canPromote) "已允许" else "未允许或系统不支持",
        )
        Item(
            onClick = onOpenNotificationSettings,
            summary = { Text(text = "管理通知开关、通知类别等系统选项", kind = CouiTextKind.ItemSummary) },
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
            summary = { Text(text = "PocketBench 1.0.0", kind = CouiTextKind.ItemSummary) },
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
                    text = "建立首页与设置双 Tab 结构；实况通知调整为首页入口及独立子页面；继续使用 Android 原生通知。",
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "PocketBench 1.0.0", kind = CouiTextKind.ItemTitle)
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

@Composable
private fun TextEditor(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.Unspecified,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = Modifier.width(152.dp).padding(vertical = 8.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(text = placeholder, kind = CouiTextKind.ItemSummary)
            }
            innerTextField()
        },
    )
}
