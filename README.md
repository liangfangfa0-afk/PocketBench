# PocketBench

PocketBench 是一个专注于 Android 实时动态（Live Updates）的 Compose 应用，提供可编辑的通知标题、胶囊短文本与进度，并通过同一通知 ID 实时更新。

## 功能

- 使用 Android 原生通知模板，不绘制或覆盖系统胶囊。
- 编辑标题、短文本和进度后，立即更新同一条常驻通知。
- 请求系统实时动态提升，并读取实际的 `FLAG_PROMOTED_ONGOING` 状态。
- 系统未授权时，跳转到实时动态授权设置页。
- 使用 COUI-Kit 官方 `Scaffold`、`NavigationBar`、`Group`、`Item`、`Slider` 与 `Switch` 组件构建界面。

## 项目信息

- Application ID：`com.ljj.PocketBench`
- Target SDK：36
- Minimum SDK：31
- UI：Jetpack Compose + COUI-Kit

## 构建

使用仓库根目录的 Gradle Wrapper，或直接通过 Android Studio 打开项目。

## COUI-Kit

界面组件 API 以 [COUI-Kit 官方仓库](https://github.com/ItosEO/COUI-Kit) 的组件文档与示例为准。
