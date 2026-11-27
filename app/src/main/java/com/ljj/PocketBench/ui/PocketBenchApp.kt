package com.ljj.PocketBench.ui

import android.os.Build
import androidx.annotation.RequiresApi
import android.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.ljj.PocketBench.device.DeviceMonitorController
import com.ljj.PocketBench.catalog.AdaptationCatalogSpec
import com.ljj.PocketBench.catalog.CloudCatalogStore
import com.ljj.PocketBench.catalog.CloudCatalogUi
import com.ljj.PocketBench.catalog.DeviceCatalogSpec
import com.ljj.PocketBench.catalog.RootCatalogSpec
import com.ljj.PocketBench.catalog.rememberCloudCatalog

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.Scaffold
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.switch.Switch
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.navigation.CouiNavigationBarDefaults
import com.itos.coui.shape.CouiShape
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.TopAppBarButtonGroup
import com.itos.coui.topappbar.TopAppBarSearch
import com.itos.coui.topappbar.TopAppBarSearchButton
import com.itos.coui.topappbar.rememberCouiTopAppBarSearchState
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiSingleChoiceItem
import com.itos.coui.dialog.SingleChoiceDialog
import com.itos.coui.menu.ProvideCouiMenuBackdrop
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.dialog.Dialog
import com.itos.coui.dialog.InputDialog
import androidx.compose.ui.platform.LocalUriHandler
import com.ljj.PocketBench.notification.LiveUpdateController
import top.yukonga.miuix.kmp.basic.Card as KsuStatusCard
import top.yukonga.miuix.kmp.basic.CardDefaults as KsuCardDefaults
import top.yukonga.miuix.kmp.basic.Text as KsuCardText
import top.yukonga.miuix.kmp.basic.Icon as KsuIcon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

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
    val context = LocalContext.current
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) { CloudCatalogStore.cleanupLegacy(context) }
    }
    var announcementVisible by rememberSaveable { mutableStateOf(false) }
    var selectedCatalogVendor by rememberSaveable { mutableStateOf("oppo") }
    var selectedCatalogProduct by rememberSaveable { mutableStateOf("oppo-find-x10") }
    var selectedCatalogSeries by rememberSaveable { mutableStateOf("Find X 系列") }
    var selectedRootCategory by rememberSaveable { mutableStateOf("1") }
    var selectedRootResource by rememberSaveable { mutableStateOf("") }
    var catalogBackStack by rememberSaveable { mutableStateOf(listOf<String>()) }
    val uriHandler = LocalUriHandler.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pageColors = CouiPageDefaults.themeColors()
    val bottomRenderer = remember { runCatching { CouiFloatingRenderer() }.getOrNull() }
    val catalogPages = setOf("deviceCatalog", "deviceCatalogVendor", "deviceCatalogSeries", "deviceCatalogProduct")
    val isCatalogPage = currentPage in catalogPages
    var catalogSearchQuery by rememberSaveable { mutableStateOf("") }
    var catalogSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var catalogSearchFocusPending by remember { mutableStateOf(false) }
    var catalogSearchFallbackVisible by rememberSaveable { mutableStateOf(false) }
    var catalogSearchFallbackValue by remember { mutableStateOf(TextFieldValue("")) }
    val catalogSearchRenderer = remember(isCatalogPage) {
        if (isCatalogPage) runCatching { CouiFloatingRenderer.standalone() }.getOrNull() else null
    }
    val catalogSearchState = if (catalogSearchRenderer != null) {
        rememberCouiTopAppBarSearchState(catalogSearchExpanded, catalogSearchRenderer)
    } else {
        null
    }
    val isSubPage = currentPage in setOf("live", "device", "root", "systemWebsites", "opensource", "coloros", "harmony", "originos", "adaptationHub", "deviceCatalog", "deviceCatalogVendor", "deviceCatalogSeries", "deviceCatalogProduct", "adaptation_coloros", "adaptation_harmony", "adaptation_originos", "adaptation_xiaomi", "adaptation_honor", "adaptation_apple", "rootResourcesCategory", "rootResourcesDetail", "rootResourcesNotice")

    fun tabIndexFor(page: String): Int = when (page) {
        "rootResources" -> 1
        "settings" -> 2
        else -> 0
    }

    fun navigate(page: String) {
        if (page in catalogPages) {
            if (currentPage in catalogPages) {
                catalogBackStack = catalogBackStack + currentPage
            } else {
                catalogBackStack = emptyList()
                returnPage = currentPage
            }
        } else {
            catalogBackStack = emptyList()
            catalogSearchExpanded = false
            catalogSearchFocusPending = false
            keyboardController?.hide()
            if (page != "home" && page != "rootResources" && page != "settings") returnPage = currentPage
        }
        navigationForward = page != "home"
        currentPage = page
        selectedTab = tabIndexFor(page)
    }

    fun goBack() {
        navigationForward = false
        if (currentPage in catalogPages && catalogBackStack.isNotEmpty()) {
            currentPage = catalogBackStack.last()
            catalogBackStack = catalogBackStack.dropLast(1)
        } else if (currentPage == "rootResourcesDetail") {
            currentPage = "rootResourcesCategory"
            catalogBackStack = emptyList()
        } else if (currentPage == "rootResourcesCategory" || currentPage == "rootResourcesNotice") {
            currentPage = "rootResources"
            catalogBackStack = emptyList()
        } else {
            currentPage = returnPage
            returnPage = "home"
            catalogBackStack = emptyList()
        }
        if (currentPage !in catalogPages) {
            catalogSearchExpanded = false
            catalogSearchFocusPending = false
            keyboardController?.hide()
        }
        selectedTab = tabIndexFor(currentPage)
    }

    BackHandler(enabled = isSubPage) { goBack() }
    BackHandler(enabled = isCatalogPage && catalogSearchExpanded) {
        catalogSearchExpanded = false
        catalogSearchFocusPending = false
        keyboardController?.hide()
    }

    ProvideCouiMenuBackdrop { windowSource ->
        Scaffold(
        modifier = Modifier.fillMaxSize().then(windowSource),
        containerColor = pageColors.background,
        topBar = {
            TopAppBar(
                title = {
                    val pageTitle = when (currentPage) {
                        "live" -> "实况通知"
                        "device" -> "设备信息"
                        "root" -> "ROOT"
                        "systemWebsites" -> "各系统官网"
                        "opensource" -> "开源与组件"
                        "coloros" -> "ColorOS 17 适配"
                        "harmony" -> "HarmonyOS 7 适配"
                        "originos" -> "OriginOS 7 适配"
                        "adaptationHub" -> "各厂商新系统适配名单"
                        "deviceCatalog" -> "厂商各机型信息"
                        "deviceCatalogVendor" -> "厂商机型目录"
                        "deviceCatalogSeries" -> selectedCatalogSeries
                        "deviceCatalogProduct" -> "机型详情"
                        "rootResources" -> "资源专区"
                        "rootResourcesCategory" -> "资源列表"
                        "rootResourcesDetail" -> "资源详情"
                        "rootResourcesNotice" -> "使用须知"
                        "adaptation_coloros" -> "ColorOS 17"
                        "adaptation_harmony" -> "HarmonyOS 7"
                        "adaptation_originos" -> "OriginOS 7"
                        "adaptation_xiaomi" -> "Xiaomi HyperOS 4"
                        "adaptation_honor" -> "荣耀 MagicOS 11"
                        "adaptation_apple" -> "Apple iOS 27"
                        "settings" -> "设置"
                        else -> "PocketBench"
                    }
                    if (isCatalogPage && catalogSearchState != null) {
                        TopAppBarSearch(
                            modifier = Modifier.fillMaxWidth(),
                            state = catalogSearchState,
                            query = catalogSearchQuery,
                            onQueryChange = { catalogSearchQuery = it },
                            onSearch = { keyboardController?.hide() },
                            placeholder = "搜索厂商、系列、机型或入网型号",
                            focusPending = catalogSearchFocusPending,
                            onFocused = { catalogSearchFocusPending = false },
                            themeTint = Color.Transparent,
                            action = { Text(text = "取消", kind = CouiTextKind.ItemTitle) },
                            onActionClick = {
                                catalogSearchExpanded = false
                                catalogSearchFocusPending = false
                                keyboardController?.hide()
                            },
                        ) {
                            Text(text = pageTitle, kind = CouiTextKind.ItemTitle)
                        }
                    } else {
                        Text(text = pageTitle, kind = CouiTextKind.ItemTitle)
                    }
                },
                navigationIcon = {
                    if (isSubPage) {
                        CouiBackTopBarButton(onClick = ::goBack)
                    }
                },
                actions = {
                    if (isCatalogPage && catalogSearchState != null) {
                        TopAppBarButtonGroup {
                            TopAppBarSearchButton(
                                state = catalogSearchState,
                                onClick = {
                                    catalogSearchExpanded = true
                                    catalogSearchFocusPending = true
                                },
                                contentDescription = "搜索机型",
                                themeTint = Color.Transparent,
                            )
                        }
                    } else if (isCatalogPage) {
                        TopAppBarButtonGroup {
                            TopAppBarButton(
                                onClick = {
                                    catalogSearchFallbackValue = TextFieldValue(catalogSearchQuery)
                                    catalogSearchFallbackVisible = true
                                },
                                text = true,
                                themeTint = Color.Transparent,
                            ) {
                                Text(text = "搜索", kind = CouiTextKind.ItemTitle)
                            }
                        }
                    } else if (currentPage == "home" || currentPage == "settings") {
                        CouiAnnouncementTopBarButton(onClick = { announcementVisible = true })
                    }
                },
            )
        },
        bottomBar = {
            if (!isSubPage) {
                if (bottomRenderer != null) {
                    val dark = CouiTheme.isDark
                    val barMaterial = CouiNavigationBarDefaults.material(dark = dark).copy(
                        firstColor = if (dark) Color(0x80666666) else Color(0xA3666666),
                        firstBlendMode = if (dark) BlendMode.OVERLAY else BlendMode.COLOR_DODGE,
                        secondColor = if (dark) Color(0xB3262626) else Color(0x80F0F0F0),
                        lightColor = Color.White,
                        themeTint = Color.Transparent,
                    )
                    NavigationBar(
                        renderer = bottomRenderer,
                        material = barMaterial,
                        items = listOf(
                            CouiNavigationItem("首页", CouiIcons.Filled.Grid, CouiIcons.Filled.Grid),
                            CouiNavigationItem("资源专区", CouiIcons.Filled.CloudData, CouiIcons.Filled.CloudData),
                            CouiNavigationItem("设置", CouiIcons.Filled.Setting, CouiIcons.Filled.Setting),
                        ),
                        selectedIndex = selectedTab,
                        onSelection = {
                            val previousTab = selectedTab
                            navigationForward = it > previousTab
                            selectedTab = it
                            currentPage = when (it) {
                                0 -> "home"
                                1 -> "rootResources"
                                else -> "settings"
                            }
                        },
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Item(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navigationForward = selectedTab != 0
                                selectedTab = 0
                                currentPage = "home"
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            icon = { Image(CouiIcons.Filled.Grid, contentDescription = "首页") },
                        ) {
                            Text(text = "首页", kind = CouiTextKind.ItemTitle)
                        }
                        Item(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navigationForward = selectedTab > 1
                                selectedTab = 1
                                currentPage = "rootResources"
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            icon = { Image(CouiIcons.Filled.CloudData, contentDescription = "资源专区") },
                        ) {
                            Text(text = "资源专区", kind = CouiTextKind.ItemTitle)
                        }
                        Item(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navigationForward = selectedTab < 2
                                selectedTab = 2
                                currentPage = "settings"
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            icon = { Image(CouiIcons.Filled.Setting, contentDescription = "设置") },
                        ) {
                            Text(text = "设置", kind = CouiTextKind.ItemTitle)
                        }
                    }
                }
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
                    .padding(contentPadding)                    .consumeWindowInsets(contentPadding),
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
                    "deviceCatalog" -> DeviceCatalogPage(query = catalogSearchQuery, onOpenVendor = { selectedCatalogVendor = it; navigate("deviceCatalogVendor") }, onOpenProduct = { selectedCatalogProduct = it; navigate("deviceCatalogProduct") })
                    "deviceCatalogVendor" -> DeviceCatalogVendorPage(vendorId = selectedCatalogVendor, query = catalogSearchQuery, onOpenSeries = { selectedCatalogSeries = it; navigate("deviceCatalogSeries") })
                    "deviceCatalogSeries" -> DeviceCatalogSeriesPage(vendorId = selectedCatalogVendor, seriesName = selectedCatalogSeries, query = catalogSearchQuery, onOpenProduct = { selectedCatalogProduct = it; navigate("deviceCatalogProduct") })
                    "deviceCatalogProduct" -> DeviceCatalogProductPage(productId = selectedCatalogProduct)
                    "rootResources" -> RootResourcesHomePage(onOpenCategory = { selectedRootCategory = it; navigate("rootResourcesCategory") }, onOpenNotice = { navigate("rootResourcesNotice") })
                    "rootResourcesCategory" -> RootResourcesCategoryPage(categoryId = selectedRootCategory, onOpenResource = { selectedRootResource = it; navigate("rootResourcesDetail") })
                    "rootResourcesDetail" -> RootResourcesDetailPage(resourceId = selectedRootResource)
                    "rootResourcesNotice" -> RootResourcesNoticePage()
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
                        onOpenDeviceCatalog = { navigate("deviceCatalog") },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

        val backdrop = rememberCouiWindowBackdrop()
        val dark = CouiTheme.isDark
        val dialogMaterial = if (CouiDialogDefaults.isMaterialSupported() && backdrop != null) {
            CouiDialogDefaults.material(dark)
        } else {
            null
        }
        InputDialog(
            value = catalogSearchFallbackValue,
            onValueChange = { catalogSearchFallbackValue = it },
            visible = catalogSearchFallbackVisible,
            onDismissRequest = { catalogSearchFallbackVisible = false },
            title = "搜索机型",
            placeholder = "厂商、系列、机型或入网型号",
            clearContentDescription = "清除搜索内容",
            confirmAction = CouiDialogAction(
                text = "搜索",
                onClick = {
                    catalogSearchQuery = catalogSearchFallbackValue.text.trim()
                    catalogSearchFallbackVisible = false
                },
                enabled = catalogSearchFallbackValue.text.isNotBlank(),
            ),
            dismissAction = CouiDialogAction(
                text = "取消",
                onClick = { catalogSearchFallbackVisible = false },
            ),
            material = dialogMaterial,
            backdrop = backdrop,
            themeTint = Color.Transparent,
        )
        Dialog(
            visible = announcementVisible,
            onDismissRequest = { announcementVisible = false },
            dark = dark,
            title = "PocketBench 更新公告",
            message = """
1.0.0.19

本次更新
• 「资源专区」本次补齐 15 个 KSU 模块，可以直接查看模块说明、适用系统、作者和来源，点一下就能打开官方页面。
• 新增：Play Integrity Fix、Hybrid Mount、Magic Mount-rs、KernelSU Tuner、HyperCool、QSC 充电控制、ColorOS 温控与云控、ColorOS16 优化版、ColorOS WIFI 感知、Bye Blur、别动我的剪切板、证书桥、Zygisk FuseHide、MoveCertificate、KernelSU Grant Toast。
• 上一轮加入的 HMA-OSS 继续保留；资源目录现在共 28 条，KSU 分类 21 条。
• 应用会自动从几个公开地址获取最新资源目录，哪个版本新就用哪个；断网时也能打开内置目录，已经取到的目录会留在手机里，下次不用重新下载。
• 资源详情只展示说明和官方入口，不在应用内下载、安装或执行模块。
• 继续使用 COUI 原生组件：底栏三项、资源宫格圆角、公告弹窗、搜索和设置都保持原生样式。
• 版本号 1.0.0.19。
""".trimIndent(),

            confirmAction = CouiDialogAction(text = "知道了", onClick = { announcementVisible = false }),
            material = dialogMaterial,
            backdrop = backdrop,
            themeTint = Color.Transparent,
        )
    }
}

@RequiresApi(34)
@Composable
private fun CouiBackTopBarButton(onClick: () -> Unit) {
    TopAppBarButtonGroup {
        TopAppBarButton(onClick = onClick, themeTint = Color.Transparent) {
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
        TopAppBarButton(onClick = onClick, text = true, themeTint = Color.Transparent) {
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
    onOpenDeviceCatalog: () -> Unit,
) {
    Group(title = "PocketBench") {
        Item(
            onClick = onOpenLiveUpdate,
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

    Group(title = "设备") {
        Item(onClick = onOpenDeviceMonitor) {
            Text(text = "设备信息", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "ROOT") {
        Item(onClick = onOpenRoot) {
            Text(text = "ROOT", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "手机资料库") {
        Item(onClick = onOpenDeviceCatalog) { Text(text = "厂商各机型信息", kind = CouiTextKind.ItemTitle) }
    }

    Group(title = "系统适配") {
        Item(onClick = onOpenAdaptationHub) {
            Text(text = "各厂商新系统适配名单", kind = CouiTextKind.ItemTitle)
        }
    }

    Group(title = "官方资源") {
        Item(onClick = onOpenSystemWebsites) {
            Text(text = "各系统官网", kind = CouiTextKind.ItemTitle)
        }
    }
}

private fun catalogMatches(query: String, vararg candidates: String?): Boolean {
    val rawQuery = query.trim()
    if (rawQuery.isBlank()) return true

    fun normalize(value: String): String = value
        .lowercase()
        .filter { it.isLetterOrDigit() }

    val normalizedQuery = normalize(rawQuery)
    val terms = rawQuery
        .split(Regex("[\\s，。；;、,|/]+"))
        .map(::normalize)
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(normalizedQuery) }

    return candidates
        .filterNotNull()
        .map(::normalize)
        .filter { it.isNotBlank() }
        .any { candidate ->
            candidate.contains(normalizedQuery) ||
                terms.any { term -> candidate.contains(term) }
        }
}

@Composable
private fun DeviceCatalogPage(
    query: String,
    onOpenVendor: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
) {
    DeviceCatalogCloudData { data ->
        val vendors = data.optJSONArray("vendors") ?: JSONArray()
        val products = data.optJSONArray("products") ?: JSONArray()
        val vendorNames = (0 until vendors.length()).mapNotNull { index ->
            vendors.optJSONObject(index)?.let { it.optString("id") to it.optString("name") }
        }.toMap()

        if (query.isNotBlank()) {
            val results = (0 until products.length()).mapNotNull { products.optJSONObject(it) }.filter { product ->
                catalogMatches(
                    query,
                    product.optString("name"),
                    product.optString("model"),
                    product.optString("series"),
                    product.optString("vendor"),
                    vendorNames[product.optString("vendor")],
                    product.optString("launch"),
                    product.optString("market"),
                )
            }
            Group(title = "搜索结果") {
                if (results.isEmpty()) {
                    Item { Text(text = "没有找到匹配的已收录机型", kind = CouiTextKind.ItemSummary) }
                } else {
                    results.forEach { product ->
                        Item(
                            onClick = { onOpenProduct(product.optString("id")) },
                            summary = {
                                Text(
                                    text = product.optString("model"),
                                    kind = CouiTextKind.ItemSummary,
                                )
                            },
                        ) {
                            Text(text = product.optString("name"), kind = CouiTextKind.ItemTitle)
                        }
                    }
                }
            }
        }

        for (index in 0 until vendors.length()) {
            val vendor = vendors.optJSONObject(index) ?: continue
            val vendorId = vendor.optString("id")
            if (vendorId.isBlank()) continue
            val vendorName = vendor.optString("name").ifBlank { "未命名厂商" }
            Group {
                Item(onClick = { onOpenVendor(vendorId) }) {
                    Text(text = vendorName, kind = CouiTextKind.ItemTitle)
                }
            }
        }
    }
}

@Composable
private fun DeviceCatalogVendorPage(
    vendorId: String,
    query: String,
    onOpenSeries: (String) -> Unit,
) {
    DeviceCatalogCloudData { data ->
        val vendors = data.optJSONArray("vendors") ?: JSONArray()
        val products = data.optJSONArray("products") ?: JSONArray()
        var vendorName = "厂商"
        var vendorSeries = JSONArray()
        for (index in 0 until vendors.length()) {
            val item = vendors.optJSONObject(index) ?: continue
            if (item.optString("id") == vendorId) {
                vendorName = item.optString("name").ifBlank { "厂商" }
                vendorSeries = item.optJSONArray("series") ?: JSONArray()
                break
            }
        }
        val vendorProducts = (0 until products.length())
            .mapNotNull { products.optJSONObject(it) }
            .filter { it.optString("vendor") == vendorId }
        Group(title = vendorName, footer = "选择系列，进入独立机型目录") {
            val seriesNames = (0 until vendorSeries.length())
                .map { vendorSeries.optString(it).trim() }
                .filter { it.isNotBlank() }
            if (seriesNames.isEmpty()) {
                Item { Text(text = "该厂商系列资料待补充。", kind = CouiTextKind.ItemSummary) }
            } else {
                for (seriesName in seriesNames) {
                    val seriesKey = seriesName.removeSuffix(" 系列").trim()
                    val count = vendorProducts.count { product ->
                        val productSeries = product.optString("series").trim()
                        productSeries.equals(seriesKey, true) ||
                            (productSeries.startsWith(seriesKey, true) &&
                                productSeries.getOrNull(seriesKey.length)?.let {
                                    it.isDigit() || it.isWhitespace() || it == '-'
                                } == true)
                    }
                    if (!catalogMatches(
                            query,
                            seriesName,
                            vendorName,
                            vendorId,
                            *vendorProducts.flatMap { product ->
                                listOf(
                                    product.optString("series"),
                                    product.optString("name"),
                                    product.optString("model"),
                                )
                            }.toTypedArray(),
                        )
                    ) continue
                    Item(
                        onClick = { onOpenSeries(seriesName) },
                        summary = {
                            Text(
                                text = if (count > 0) "$count 款机型" else "机型资料持续补充中",
                                kind = CouiTextKind.ItemSummary,
                            )
                        },
                    ) {
                        Text(text = seriesName, kind = CouiTextKind.ItemTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCatalogSeriesPage(
    vendorId: String,
    seriesName: String,
    query: String,
    onOpenProduct: (String) -> Unit,
) {
    DeviceCatalogCloudData { data ->
        val vendors = data.optJSONArray("vendors") ?: JSONArray()
        val products = data.optJSONArray("products") ?: JSONArray()
        val vendorName = (0 until vendors.length())
            .mapNotNull { vendors.optJSONObject(it) }
            .firstOrNull { it.optString("id") == vendorId }
            ?.optString("name")
            .orEmpty()
        val seriesKey = seriesName.removeSuffix(" 系列").trim()
        val seriesProducts = (0 until products.length())
            .mapNotNull { products.optJSONObject(it) }
            .filter { product ->
                val productSeries = product.optString("series").trim()
                product.optString("vendor") == vendorId &&
                    (productSeries.equals(seriesKey, true) ||
                        (productSeries.startsWith(seriesKey, true) &&
                            productSeries.getOrNull(seriesKey.length)?.let {
                                it.isDigit() || it.isWhitespace() || it == '-'
                            } == true))
            }
            .filter { product ->
                catalogMatches(
                    query,
                    product.optString("name"),
                    product.optString("model"),
                    product.optString("series"),
                    vendorName,
                    vendorId,
                    product.optString("launch"),
                    product.optString("market"),
                )
            }
        Group(title = "$vendorName · $seriesName", footer = "共 ${seriesProducts.size} 款已收录机型") {
            if (seriesProducts.isEmpty()) {
                Item {
                    Text(
                        text = if (query.isBlank()) "该系列机型资料正在整理，后续通过云端更新。" else "当前系列没有匹配的机型。",
                        kind = CouiTextKind.ItemSummary,
                    )
                }
            } else {
                for (product in seriesProducts) {
                    val id = product.optString("id")
                    val name = product.optString("name").ifBlank { "未命名机型" }
                    val model = product.optString("model")
                    val detailCount = (product.optJSONArray("specs") ?: JSONArray()).length()
                    Item(
                        onClick = { onOpenProduct(id) },
                        summary = {
                            Text(
                                text = listOf(model, if (detailCount > 0) "$detailCount 项参数" else "参数待补充")
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                                kind = CouiTextKind.ItemSummary,
                            )
                        },
                    ) {
                        Text(text = name, kind = CouiTextKind.ItemTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCatalogProductPage(productId: String) {
    val uri = LocalUriHandler.current
    DeviceCatalogCloudData { data ->
        val products = data.optJSONArray("products") ?: JSONArray()
        val product = (0 until products.length())
            .mapNotNull { products.optJSONObject(it) }
            .firstOrNull { it.optString("id") == productId }

        if (product == null) {
            Group(title = "机型详情") {
                Item { Text(text = "云端暂未收录该机型。", kind = CouiTextKind.ItemSummary) }
            }
        } else {
            val name = product.optString("name")
            val model = product.optString("model")
            val price = product.optString("price")
            val colors = product.optJSONArray("colors") ?: JSONArray()
            val storage = product.optJSONArray("storage") ?: JSONArray()
            val specs = product.optJSONArray("specs") ?: JSONArray()
            Group(
                title = name,
                footer = listOf(product.optString("market"), product.optString("sourceLabel"))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
            ) {
                Item { Text(text = "入网型号：${model.ifBlank { "待补充" }}", kind = CouiTextKind.ItemSummary) }
                if (product.optString("launch").isNotBlank() && product.optString("launch") != "待补充") {
                    Item { Text(text = "上市时间：${product.optString("launch")}", kind = CouiTextKind.ItemSummary) }
                }
                Item {
                    Text(
                        text = "官方售价：${price.ifBlank { "待补充" }}",
                        kind = CouiTextKind.ItemTitle,
                    )
                }
                if (colors.length() > 0) {
                    Item {
                        Text(
                            text = "配色：${(0 until colors.length()).joinToString("、") { colors.optString(it) }}",
                            kind = CouiTextKind.ItemSummary,
                        )
                    }
                }
                if (storage.length() > 0) {
                    Item {
                        Text(
                            text = "存储版本：${(0 until storage.length()).joinToString("、") { storage.optString(it) }}",
                            kind = CouiTextKind.ItemSummary,
                        )
                    }
                }
                if (specs.length() == 0) {
                    Item {
                        Text(
                            text = "该机型的详细参数正在整理，后续将通过云端资料更新。",
                            kind = CouiTextKind.ItemSummary,
                        )
                    }
                }
            }

            for (index in 0 until specs.length()) {
                val row = specs.optJSONArray(index) ?: continue
                val label = row.optString(0).ifBlank { "其他参数" }
                val value = row.optString(1).ifBlank { "官方暂未公布" }
                Group(title = label) {
                    Item { Text(text = value, kind = CouiTextKind.ItemSummary) }
                }
            }

            val official = product.optString("official")
            val store = product.optString("store")
            if (official.isNotBlank() || store.isNotBlank()) {
                Group(title = "官方来源", footer = "参数以中国大陆官方页面为准；如官网后续更新，以官网最新信息为准。") {
                    if (official.isNotBlank()) {
                        Item(onClick = { uri.openUri(official) }) {
                            Text(text = "品牌中国官网完整参数页", kind = CouiTextKind.ItemTitle)
                        }
                    }
                    if (store.isNotBlank()) {
                        Item(onClick = { uri.openUri(store) }) {
                            Text(text = "中国大陆官方商城", kind = CouiTextKind.ItemTitle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogStatusGroup(
    title: String,
    action: String,
    state: CloudCatalogUi,
) {
    val payload = state.payload
    Group(
        title = title,
        footer = when {
            state.loading && payload != null -> "正在同步云端目录；当前显示 ${payload.source} 版本 ${payload.version}"
            state.loading -> "正在同步云端目录…"
            state.error.isNotBlank() && payload != null -> "${state.error}；当前显示 ${payload.source} 版本 ${payload.version}"
            state.error.isNotBlank() -> state.error
            payload != null -> "${payload.source} · 版本 ${payload.version} · 更新于 ${payload.updatedAt.ifBlank { "未知" }}"
            else -> "目录尚未就绪"
        },
    ) {
        Item(onClick = state.refresh) {
            Text(text = if (state.loading) "正在更新…" else action, kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun DeviceCatalogCloudData(content: @Composable (JSONObject) -> Unit) {
    val state = rememberCloudCatalog(DeviceCatalogSpec)
    CatalogStatusGroup(title = "云端资料库", action = "刷新云端资料", state = state)
    val parsed = state.payload?.parsed
    if (parsed != null) {
        content(parsed)
    } else {
        Group(title = "暂无资料") {
            Item { Text(text = state.error.ifBlank { "正在读取内置机型资料…" }, kind = CouiTextKind.ItemSummary) }
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
        footer = "实况通知由 Android 系统负责创建和渲染；PocketBench 只提交标题和短文本，并读取系统返回的授权与提升状态。",
    ) {
        Item { Text(text = "1. 先在上方开启实况通知；首次启动时按系统提示授予通知权限。", kind = CouiTextKind.ItemSummary) }
        Item { Text(text = "2. 在通知内容中编辑标题和短文本。", kind = CouiTextKind.ItemSummary) }
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
            themeTint = Color.Transparent,
        )
    }
}

@Composable
private fun DeviceMonitorPage(controller: DeviceMonitorController) {    controller.deviceInfoSections.forEach { section ->
        Group(title = section.first) {
            section.second.forEach { row -> DeviceInfoStatusItem(row.first, row.second) }
        }
    }
    Group(title = "受系统权限限制的信息", footer = "Android 会限制应用读取部分唯一标识和受保护数据；这些项目不会伪造或猜测。") {
        DeviceInfoStatusItem("IMEI / MEID", "普通应用不可读取：受 Android 电话权限与系统签名限制")
        DeviceInfoStatusItem("设备序列号", "普通应用不可读取：受 Android 隐私权限限制")
        DeviceInfoStatusItem("Wi-Fi / 蓝牙 MAC", "系统通常返回随机化地址或限制访问")
        DeviceInfoStatusItem("完整进程与内核细节", "需要 ROOT 或系统级权限")
        DeviceInfoStatusItem("GPU 具体型号", "系统未提供统一公开查询接口，当前设备可能无法读取")
    }
    Group(title = "ROOT 信息") {
        DeviceInfoStatusItem("ROOT 信息", if (controller.rootGranted) "已读取" else "未读取")
    }
    Group(title = "操作") {
        Item(onClick = controller::refreshDeviceInfo,
            summary = { Text(text = "重新读取 Android 系统公开接口提供的设备数据", kind = CouiTextKind.ItemSummary) }) {
            Text(text = "刷新设备信息", kind = CouiTextKind.ItemTitle)
        }
    }
}

@Composable
private fun DeviceInfoStatusItem(label: String, value: String) {
    Item(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, kind = CouiTextKind.ItemTitle)
            Text(text = value, kind = CouiTextKind.ItemSummary)
        }
    }
}

@Composable
private fun RootPage(controller: DeviceMonitorController) {
    Group(
        title = "ROOT",
        footer = when {
            controller.rootGranted -> "SU 已返回 UID 0，PocketBench ROOT 权限可用。"
            controller.isCheckingRoot -> "正在自动检测此前授权的 ROOT 状态…"
            else -> "首次使用请点击授权，由本机 ROOT 管理器确认。"
        },
    ) {
        if (controller.rootGranted) {
            MiuixTheme {
                KsuStatusCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                    colors = KsuCardDefaults.defaultColors(color = Color(0xFFDFFAE4)),
                    onClick = {},
                    showIndication = true,
                    pressFeedbackType = PressFeedbackType.Tilt,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                    ) {
                        KsuCardText(
                            text = "工作中",
                            fontSize = 22.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = Color(0xFF176B35),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(18.dp, 22.dp),
                            contentAlignment = androidx.compose.ui.Alignment.BottomEnd,
                        ) {
                            KsuIcon(
                                modifier = Modifier.size(88.dp),
                                imageVector = CouiIcons.Filled.ShieldCheck,
                                tint = Color(0xFF36D167),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        } else {
            Item(
                onClick = controller::requestRootAuthorization,
                enabled = !controller.isCheckingRoot,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            ) {
                Text(
                    text = if (controller.isCheckingRoot) "检测中…" else "授权",
                    kind = CouiTextKind.ItemTitle,
                    color = Color(0xFF2878E8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    var themeDialogVisible by rememberSaveable { mutableStateOf(false) }
    val themeOptions = remember {
        listOf(
            CouiSingleChoiceItem(value = "system", text = "跟随系统"),
            CouiSingleChoiceItem(value = "light", text = "浅色模式"),
            CouiSingleChoiceItem(value = "dark", text = "深色模式"),
        )
    }
    val context = LocalContext.current
    val appVersion = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
    }

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
        Item(
            onClick = { themeDialogVisible = true },
            trailing = {
                Text(
                    text = when (themeMode) {
                        "light" -> "浅色模式"
                        "dark" -> "深色模式"
                        else -> "跟随系统"
                    },
                    kind = CouiTextKind.ItemSummary,
                )
            },
        ) {
            Text(text = "主题模式", kind = CouiTextKind.ItemTitle)
        }
    }
    SingleChoiceDialog(
        visible = themeDialogVisible,
        title = "选择外观",
        items = themeOptions,
        selectedValue = themeMode,
        onItemSelected = { mode ->
            onThemeModeChange(mode)
            themeDialogVisible = false
        },
        onDismissRequest = { themeDialogVisible = false },
        themeTint = Color.Transparent,
    )

    Group(title = "常规") {
        Item(
            onClick = controller::refreshStatus,
            summary = { Text(text = "重新读取当前通知与系统授权状态", kind = CouiTextKind.ItemSummary) },
        ) {
            Text(text = "刷新应用状态", kind = CouiTextKind.ItemTitle)
        }
        Item(
            summary = { Text(text = "PocketBench $appVersion", kind = CouiTextKind.ItemSummary) },
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
        Item(onClick = onOpenCouiDocs, summary = { Text(text = "Group、Item、Dialog、SingleChoiceDialog、NavigationBar、Switch、InputDialog 等组件 API", kind = CouiTextKind.ItemSummary) }) { Text(text = "COUI 官方组件文档", kind = CouiTextKind.ItemTitle) }
    }
    Group(title = "官方依赖引用", footer = "以下版本与 PocketBench 当前 Gradle 配置保持一致。COUI-Kit 0.0.1-alpha21 通过 JitPack 分发，仓库坐标为 com.github.itoseo.coui-kit。") {
        Item { Text(text = "dependencies {\n    implementation(\"com.github.itoseo.coui-kit:coui:0.0.1-alpha21\")\n    implementation(\"com.github.itoseo.coui-kit:coui-icons:0.0.1-alpha21\")\n}", kind = CouiTextKind.ItemSummary) }
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

@Composable
private fun RemoteAdaptationPage(
    section: String,
    title: String,
    onOpenOfficial: () -> Unit,
) {
    val catalog = rememberCloudCatalog(AdaptationCatalogSpec)
    val config = catalog.payload?.parsed
    val updatedAt = config?.optString("updated_at").orEmpty()
    val configVersion = config?.optString("config_version").orEmpty()
    Group(
        title = title,
        footer = "数据由应用在后台读取并缓存；接口地址仅作为内部数据源使用，不在页面中展示或提供复制。刷新只读取 JSON，不执行远程代码或系统命令。"
    ) {
        Item(
            onClick = catalog.refresh,
            trailing = { Text(text = if (catalog.loading) "更新中…" else "刷新", kind = CouiTextKind.ItemTitle) },
            summary = {
                Text(
                    text = when {
                        catalog.loading -> "正在同步云端适配目录"
                        catalog.error.isNotEmpty() && config != null -> "${catalog.error} · 当前显示 ${catalog.payload.source} 版本 $configVersion"
                        catalog.error.isNotEmpty() -> "获取失败：${catalog.error}"
                        config == null -> "尚无可用数据"
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
                val actual = version.optJSONArray("actual") ?: JSONArray()
                if (actual.length() > 0) {
                    Group(title = "实际已开启推送") {
                        for (actualIndex in 0 until actual.length()) {
                            val batch = actual.optJSONObject(actualIndex) ?: continue
                            val models = batch.optJSONArray("models") ?: JSONArray()
                            Group(title = batch.optString("period")) {
                                for (modelIndex in 0 until models.length()) {
                                    val model = models.optString(modelIndex)
                                    if (model.isNotBlank()) Item { Text(text = model, kind = CouiTextKind.ItemSummary) }
                                }
                            }
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

@Composable
private fun RootResourcesHomePage(
    onOpenCategory: (String) -> Unit,
    onOpenNotice: () -> Unit,
) {
    RootCatalogData { data ->
        val categories = data.optJSONArray("categories") ?: JSONArray()
        val entries = (0 until categories.length()).mapNotNull { index ->
            categories.optJSONObject(index)?.let { category ->
                category.optString("id").takeIf(String::isNotBlank)?.let { id ->
                    id to category.optString("name").ifBlank { "资源入口 $id" }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            entries.chunked(2).forEach { rowEntries ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowEntries.forEach { (id, title) ->
                        Group(modifier = Modifier.weight(1f).aspectRatio(1f).clip(CouiShape(20.dp))) {
                            Item(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (id == "4") onOpenNotice() else onOpenCategory(id)
                                },
                                contentPadding = PaddingValues(8.dp),
                            ) {
                                Text(
                                    text = title,
                                    kind = CouiTextKind.ItemTitle,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                    if (rowEntries.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
            Text(
                text = "仅提供资源信息与来源索引；所有条目以作者发布页为准。",
                kind = CouiTextKind.GroupFooter,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RootCatalogData(content: @Composable (JSONObject) -> Unit) {
    val state = rememberCloudCatalog(RootCatalogSpec)
    CatalogStatusGroup(title = "云端索引", action = "刷新资源索引", state = state)
    content(state.payload?.parsed ?: JSONObject())
}

@Composable
private fun RootResourcesCategoryPage(
    categoryId: String,
    onOpenResource: (String) -> Unit,
) {
    RootCatalogData { data ->
        val categories = data.optJSONArray("categories") ?: JSONArray()
        val category = (0 until categories.length()).mapNotNull { categories.optJSONObject(it) }
            .firstOrNull { it.optString("id") == categoryId }
        val resources = data.optJSONArray("resources") ?: JSONArray()
        val entries = (0 until resources.length()).mapNotNull { resources.optJSONObject(it) }
            .filter { it.optString("category_id") == categoryId }
        if (entries.isEmpty()) {
            Group(
                title = category?.optString("name").orEmpty().ifBlank { "分类 $categoryId" },
                footer = "共 0 项索引记录",
            ) {
                Item { Text(text = "该分类暂时没有资源条目。", kind = CouiTextKind.ItemSummary) }
            }
        } else {
            entries.forEach { item ->
                Group {
                    Item(onClick = { onOpenResource(item.optString("id")) }) {
                        Text(
                            text = "${item.optString("name").ifBlank { "未命名资源" }} 下载与入口",
                            kind = CouiTextKind.ItemTitle,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RootResourcesDetailPage(resourceId: String) {
    val uri = LocalUriHandler.current
    RootCatalogData { data ->
        val resources = data.optJSONArray("resources") ?: JSONArray()
        val item = (0 until resources.length()).mapNotNull { resources.optJSONObject(it) }
            .firstOrNull { it.optString("id") == resourceId }
        if (item == null) {
            Group(title = "资源详情") { Item { Text(text = "该条目不存在或已从云端目录移除。", kind = CouiTextKind.ItemSummary) } }
        } else {
            Group {
                Item { Text(text = item.optString("name").ifBlank { "资源详情" }, kind = CouiTextKind.ItemTitle) }
                val info = item.optJSONArray("info")
                if (info != null) {
                    for (index in 0 until info.length()) {
                        val field = info.optJSONObject(index) ?: continue
                        val label = field.optString("label")
                        val value = field.optString("value")
                        if (label.isNotBlank() && value.isNotBlank()) Item {
                            Text(text = "$label：$value", kind = CouiTextKind.ItemSummary)
                        }
                    }
                } else {
                    val legacyFields = listOf(
                        "版本" to "version",
                        "文件大小" to "size",
                        "适用内核" to "kernel",
                        "Android 版本" to "android",
                        "SHA-256" to "sha256",
                        "签名指纹" to "signature",
                    )
                    legacyFields.forEach { (label, key) ->
                        val value = item.optString(key)
                        if (value.isNotBlank()) Item {
                            Text(text = "$label：$value", kind = CouiTextKind.ItemSummary)
                        }
                    }
                }
                val description = item.optString("description")
                if (description.isNotBlank()) Item { Text(text = description, kind = CouiTextKind.ItemSummary) }
            }

            val links = buildList {
                val releaseUrl = item.optString("official_url")
                if (releaseUrl.startsWith("https://")) add("作者 GitHub Release" to releaseUrl)
                val cloudLinks = item.optJSONArray("links")
                if (cloudLinks != null) {
                    for (index in 0 until cloudLinks.length()) {
                        val link = cloudLinks.optJSONObject(index) ?: continue
                        val url = link.optString("url")
                        if (url.startsWith("https://")) {
                            add(link.optString("label").removePrefix("打开").trim().ifBlank { "官方链接" } to url)
                        }
                    }
                }
                if (isEmpty()) {
                    val repositoryUrl = item.optString("github_url")
                    val telegramUrl = item.optString("telegram_url")
                    if (repositoryUrl.startsWith("https://")) add("GitHub 仓库" to repositoryUrl)
                    if (telegramUrl.startsWith("https://")) add("Telegram 频道" to telegramUrl)
                }
            }.distinctBy { it.second }

            links.forEach { (label, url) ->
                Group {
                    Item(onClick = { uri.openUri(url) }) {
                        Text(text = "前往$label", kind = CouiTextKind.ItemTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun RootResourcesNoticePage() {
    Group(title = "使用须知") {
        Item {
            Text(
                text = "本专区仅提供第三方资源的索引与官方来源链接，不托管、不分发任何安装包；所有资源版权归原作者所有，请自行核对 SHA-256 与签名指纹；刷入模块存在设备损坏与数据丢失风险，操作前请完整备份，因使用第三方资源造成的后果由用户自行承担。",
                kind = CouiTextKind.ItemSummary,
            )
        }
    }
}
