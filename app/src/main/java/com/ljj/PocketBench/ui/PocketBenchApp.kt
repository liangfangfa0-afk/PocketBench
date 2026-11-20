package com.ljj.PocketBench.ui

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
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
) {
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember {
        if (Build.VERSION.SDK_INT >= 34) runCatching { BottomBarRenderer() } else null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = pageColors.background,
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.text.BasicText(
                        text = "PocketBench",
                        style = com.itos.coui.text.LocalCouiTextStyle.current,
                    )
                },
            )
        },
        bottomBar = {
            val renderer = bottomRenderer?.getOrNull()?.renderer
            if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                NavigationBar(
                    renderer = renderer,
                    items = listOf(
                        CouiNavigationItem("编辑", Icons.Outlined.Edit, Icons.Filled.Edit),
                        CouiNavigationItem("状态", Icons.Outlined.Info, Icons.Filled.Info),
                    ),
                    selectedIndex = selectedPage,
                    onSelection = { selectedPage = it },
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
                when (selectedPage) {
                    0 -> EditorPage(controller, onStart)
                    else -> StatusPage(controller, onOpenPromotionSettings)
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun EditorPage(controller: LiveUpdateController, onStart: () -> Unit) {
    Group(title = "实时动态", footer = "修改内容后会立即更新同一条常驻通知。") {
        Item(
            summary = {
                Text(
                    text = if (controller.isRunning) "通知已发布，正在同步编辑内容" else "启动后会发布一条常驻通知",
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
            Text(text = "启用实时动态", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "胶囊内容", footer = "短文本建议控制在 7 个字符以内，避免系统胶囊显示拥挤。") {
        Item(
            summary = { Text("显示在通知主标题位置", kind = CouiTextKind.ItemSummary) },
            trailing = {
                TextEditor(controller.title, controller::updateTitle, "输入标题")
            },
        ) {
            Text("标题", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = { Text("用于系统实时动态的短文本字段", kind = CouiTextKind.ItemSummary) },
            trailing = {
                TextEditor(controller.shortText, controller::updateShortText, "短文本")
            },
        ) {
            Text("胶囊短文本", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "进度", footer = "拖动滑块时会同步更新通知进度。") {
        Item(
            trailing = {
                Text(text = "${controller.progress}%", kind = CouiTextKind.ItemTitle)
            },
        ) {
            Text("当前进度", kind = CouiTextKind.ItemTitle)
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
            summary = { Text("发布或更新同一条实时动态通知", kind = CouiTextKind.ItemSummary) },
        ) {
            Text("启动 / 更新", kind = CouiTextKind.ItemTitle)
        }
        Item(
            onClick = controller::stop,
            summary = { Text("取消 PocketBench 当前通知", kind = CouiTextKind.ItemSummary) },
        ) {
            Text("停止实时动态", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun StatusPage(
    controller: LiveUpdateController,
    onOpenPromotionSettings: () -> Unit,
) {
    Group(
        title = "系统状态",
        footer = "系统是否将通知提升为实时动态，以系统返回的标记为准。",
    ) {
        StatusItem("通知状态", if (controller.isRunning) "正在运行" else "未启动")
        StatusItem("系统授权", if (controller.canPromote) "已允许" else "未允许或尚不可用")
        StatusItem("通知条件", if (controller.isPromotable) "符合提升条件" else "尚未确认符合")
        StatusItem(
            "系统提升结果",
            if (controller.isPromoted) "已获得系统提升标记" else "尚未获得提升标记",
        )
        Item(
            onClick = controller::refreshStatus,
            summary = { Text("重新读取通知标记与系统授权状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text("刷新状态", kind = CouiTextKind.ItemTitle)
        }
        if (!controller.canPromote) {
            Item(
                onClick = onOpenPromotionSettings,
                summary = { Text("前往系统通知设置中的实时动态授权页", kind = CouiTextKind.ItemSummary) },
            ) {
                Text("开启系统实时动态授权", kind = CouiTextKind.ItemTitle)
            }
        }
    }

    Group(title = "当前结果") {
        Item {
            Text(text = controller.statusMessage, kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Item(
        trailing = { Text(text = value, kind = CouiTextKind.ItemSummary) },
    ) {
        Text(label, kind = CouiTextKind.ItemTitle)
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
