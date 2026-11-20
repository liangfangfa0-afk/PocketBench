# PocketBench

PocketBench 是一个专注于 Android 实时动态（Live Updates）的 Compose 应用，提供可编辑的通知标题、胶囊短文本与进度，并通过同一通知 ID 实时更新。当前版本：1.0.0.11。

## 功能

- 使用 Android 原生通知模板，不绘制或覆盖系统胶囊。
- 编辑标题、短文本和进度后，立即更新同一条常驻通知。
- 请求系统实时动态提升，并读取实际的 `FLAG_PROMOTED_ONGOING` 状态。
- 系统未授权时，跳转到实时动态授权设置页。
- 应用内主题、颜色、文字与所有可见交互控件统一使用 COUI-Kit，包括导航栏、顶栏按钮、列表、开关、滑块、输入弹窗与 COUI 图标。
- 设置页提供可持久保存的 COUI 跟随系统、浅色模式、深色模式三种外观选择。
- 各页面在有界转场视口内独立滚动，避免设置分组在页面切换测量时发生重叠。
- 新增电池与温度设备监测页，读取电量、电池温度、电压、CPU 核心数和 Android 系统热状态。
- 可选 Root 授权仅进行 `su -c id` 只读身份确认；不执行写入、改权限、挂载、刷写、删除或调频操作。
- 首页仅展示工具入口；实况通知使用说明归入实况通知子页面。
- 新增 ColorOS 17 与 HarmonyOS 7 官方机型适配页面，按厂商公开名单和计划批次展示。
- 设置页新增项目开源与 COUI-Kit 官方依赖/组件示例说明；公告使用 COUI 原生 Dialog 弹窗。

## 项目信息

- Application ID：`com.ljj.PocketBench`
- Target SDK：36
- Minimum SDK：34（Android 14）
- UI：Jetpack Compose + COUI-Kit

## 构建

使用仓库根目录的 Gradle Wrapper，或直接通过 Android Studio 打开项目。

## COUI-Kit

界面组件 API 以 [COUI-Kit 官方仓库](https://github.com/ItosEO/COUI-Kit) 的组件文档与示例为准。当前引用版本为 `0.0.1-alpha12`。官方组件文档：[组件目录](https://github.com/ItosEO/COUI-Kit/tree/main/docs/components)、[弹窗](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/dialog.md)、[Group](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/group.md)、[Item](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/item.md)。

## 远程适配配置

APP 从 `https://raw.githubusercontent.com/liangfangfa0-afk/PocketBench/main/data/device_adaptation.json` 读取公开只读 JSON，保留上次成功获取的本地缓存。远程文件只包含展示数据，不执行代码或系统命令。适配名单与时间表以文件内列出的官方来源为准；未公布的时间标记为未公布，不自行推测。

## 官方适配信息来源

- [ColorOS 17 官方升级计划](https://www.coloros.com/upgrade/)
- [华为 HarmonyOS 7 官方支持机型](https://consumer.huawei.com/cn/support/harmonyos/models-7/)

适配名单与计划节点按公开信息展示。ColorOS 计划启动日期不代表全量推送完成；HarmonyOS 官方未公布整体适配百分比，PocketBench 不自行推算或虚构完成率。
