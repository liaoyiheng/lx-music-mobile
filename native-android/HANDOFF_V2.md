# 洛雪音乐纯原生 Kotlin + Jetpack Compose（v2 液态玻璃 & 全尺寸自适应）开发交接文档

> 生成时间：2026-11-24 23:57 (+08:00)  
> 仓库：`https://github.com/liaoyiheng/lx-music-mobile`  
> 主开发分支：`kotlin-native`（本交接提交之前基线为 `2fc4d21`）  
> CI 预览分支：`ci/native-v2-preview`（当前远端停在 `4a23cf3`，仅用于云端 Roborazzi 截图排障）

---

## 1. 项目概况与本轮已完成工作

本分支基于 `native-android/`（包名 `cn.toside.music.liquid`，Kotlin 2.0.21 + Jetpack Compose + Haze 1.1.1 + Media3 1.5.0 + QuickJS 2.4.0 + Room + Coil）推进了四条主线，**全部代码改动已落盘**：

### 1.1 iOS 26 液态玻璃（Liquid Glass）v2 设计系统与视觉交互重设计
- 设计参考与规范：
  - `native-android/APPLE_MUSIC_IOS26_RESEARCH.md`
  - `native-android/DESIGN_V2_SPEC.md`
- 主题与基础动效/触控/弹层组件：
  - `ui/theme/LiquidTheme.kt`：强化浅色/深色双层液态玻璃分离度与沉浸态语义 Token（`glassTint` / `glassTintDense` / `immersive*` 等），保留玫瑰红主色。
  - `ui/components/LiquidMotion.kt`（新增）：4 档统一弹簧参数（`SpringSnappy / SpringGentle / SpringBouncy / SpringFluid`）及页面视差推入、Tab 淡入缩放、气泡展开动效。
  - `ui/components/LiquidTouch.kt`（新增）：移除 Material 灰黑水波纹，实现 `liquidPress`、`liquidCardPress`、`liquidRowPress`、`LiquidPlaybackControlButton`、`LiquidFavoriteHeartButton` 物理弹性与光晕触控反馈。
  - `ui/components/LiquidPopoverMenu.kt`（新增）：液态玻璃气泡菜单。
  - `ui/components/LiquidGlassComponents.kt`、`ui/components/LiquidFloatingDock.kt`：重构玻璃胶囊、悬浮 Dock、MiniPlayer（新增收起态、滑动切歌、宽体上一首按钮等）。
- 业务页面全面翻新：
  - `ui/screens/StandardPageLayout.kt`（新增）：统一标准页骨架与顶部/底部安全避让。
  - `ui/screens/DiscoverScreen.kt`、`ChartsScreen.kt`、`LibraryScreen.kt`、`SearchScreen.kt`、`PlaylistDetailScreen.kt`、`SettingsScreen.kt`、`TrackSheetsAndDialogs.kt`、`PlayerOverlay.kt`。

### 1.2 全尺寸自适应导航与多端布局（手机 / 折叠屏 / 平板 / 横屏分屏）
- `ui/adaptive/AdaptiveLayout.kt`（新增）& `ui/adaptive/LiquidAdaptiveShell.kt`（新增）：
  - **Compact**（手机竖屏）：悬浮 Dock + 滚动 32dp 收起 / 反向 16dp 展开（不消费列表滚动）。
  - **Medium**（折叠屏/平板竖屏）：顶部导航胶囊 + 最大 580dp 限宽底部播控。
  - **Expanded**（平板横屏/桌面大屏）：可切换侧栏（含「洛雪音乐」标识、主导航、本地歌单入口）或顶部胶囊；侧栏模式下底部播控按内容区居中。
  - **矮屏横屏 / 分屏**（高度 `< 480dp`）：紧凑顶栏；键盘或全屏播放器出现时自动退出导航控件。
  - Haze blur source 与导航控件保持兄弟节点结构，避免嵌套模糊崩溃。
- **独立审查后已补齐的 P1 修复**（在主工作树已落盘，尚未同步到旧 `ci/native-v2-preview` 分支）：
  1. `MainActivity.kt`：在顶层调用一次 `rememberLiquidWindowClass()`，并通过 `CompositionLocalProvider(LocalLiquidWindowClass provides windowClass)` 同时提供给 `LiquidAdaptiveShell`、`PlayerOverlay`、`GlobalSheetsHost`，接通全局宽屏适配。
  2. `MainActivity.kt`：修复清空队列或移除最后一首歌后 `playerExpanded` 残留 `true` 导致导航栏消失的问题（`playerExpanded = playerExpansionRequested && hasTrack`，并在无曲目时自动 `vm.closePlayer()`）。
  3. `PlayerOverlay.kt`：补齐极矮分屏单栏模式（`!wide && maxWidth >= 360.dp && maxHeight <= 280.dp`）的 `veryShort` 布局分支（把歌词/队列/评论/定时入口上移至顶栏，精简进度条与播控同排，避免歌词/队列列表被压成 0 高度），并已补齐 `PlayerLyricsStage` / `PlayerQueueStage` 的 `veryShort` 参数签名。
  4. `PlaylistDetailScreen.kt`：补齐自适应细节调整。

### 1.3 网易云音源 `try max num` Bug 根治修改（已落盘，待真机实测）
- `crypto/CryptoUtils.kt`：新增 `normalizeIv`（16 字节零填充）、`aesDecryptBytes` / `aesDecryptUtf8`、`rsaDecryptBytes` / `rsaDecryptUtf8`。
- `engine/MusicSdkEngine.kt`：`__sdk_aes_decrypt` / `__sdk_rsa_decrypt` 改为返回 UTF-8 明文（修复原先返回 Base64 导致 SDK 解析异常不断重试直到 `try max num`）；POST 请求体显式按 `UTF-8` 字节构建。
- `engine/HttpClientProvider.kt`：为 `OkHttpClient` 接入内存级 `sessionCookieJar`，保留跨请求 Cookie。
- `sdk-bundler/{build.mjs,entry.js,shims/request.js}` & `app/src/main/assets/script/music-sdk-bundle.js`：同步更新垫片并重打 SDK bundle。

### 1.4 云端 Compose 截图渲染体系（Roborazzi + GitHub Actions）
- 新增文件：
  - `.github/workflows/compose-screenshots.yml`
  - `app/src/test/java/cn/toside/music/liquid/screenshot/LiquidScreenshotHarness.kt`
  - `app/src/test/java/cn/toside/music/liquid/screenshot/LiquidScreensScreenshotTest.kt`
  - `app/src/test/java/cn/toside/music/liquid/screenshot/AdaptiveNavigationShowcase.kt`
- `native-android/build.gradle.kts` & `native-android/app/build.gradle.kts` 已接入 Roborazzi 插件与依赖。

---

## 2. 验证现状与当前唯一阻塞点

### 2.1 编译状态
- 本机环境（Android PRoot Alpine）缺少完整 Android 35 SDK，未做本地 Gradle 编译。
- GitHub Actions 上（基于 `b00ef38` ~ `4a23cf3`）：`:app:compileDebugUnitTestKotlin` **已通过（BUILD SUCCESSFUL in 2m 35s）**。
- **注意**：`MainActivity.kt`、`LiquidAdaptiveShell.kt`、`PlayerOverlay.kt`、`PlaylistDetailScreen.kt` 的最后一批 P1 修复是在 `b00ef38` 之后于主工作树完成的，**尚未在 CI 重新编译验证**，接手后首件事就是触发一次编译校验。

### 2.2 Roborazzi 截图测试最新状态（Run `45423245174` / commit `4a23cf3`）
在 `ci/native-v2-preview` 分支上的排障已定位全部根因：
1. **已定位的超时根因**（Run `45421698619` 线程转储）：原版 `LiquidScreenshotHarness.kt` 在 `SettleAsyncRenderOption.beforeCapture()` 调用两次无界 `ShadowLooper.idleMainLooper()`，遇到 Compose 持续动画（无限重组/帧调度）时主线程无限跑帧直到 6 分钟超时。
2. **最新一次尝试**（`4a23cf3`，Run `45423245174`）：引入 `@LooperMode(LooperMode.Mode.PAUSED)` + `ShadowChoreographer.setPaused(true)` + `repeat(20) { mainLooper.idleFor(Duration.ofMillis(16)) }` 后，**超时已消除（31s 结束并生成完整测试报告）**，但暴露出 **2 个明确的小 Bug**（11/11 失败，均已定位到精确行号）：
   - **问题 A（10/11 用例 NPE）**：
     ```
     java.lang.NullPointerException
       at org.robolectric.android.controller.ActivityController.windowFocusChanged(ActivityController.java:258)
       at androidx.test.core.app.ActivityScenario.launch(ActivityScenario.java:237)
       at com.github.takahirom.roborazzi.RoborazziComposeKt.captureRoboImage(RoborazziCompose.kt:64)
     ```
     **根因**：`4a23cf3` 在 `@Before` 里过早执行了 `ShadowChoreographer.setPaused(true)`，导致 `ActivityScenario.launch` 启动 Activity 时主帧未完成挂载，`windowFocusChanged` 取 `ViewRootImpl` 为 null。  
     **建议修法**：不要在 `@Before` 暂停 Choreographer；改为在 `Activity` 完成启动 / Compose 内容挂载后再控制时钟推进（例如使用 `createAndroidComposeRule<ComponentActivity>()` + `composeRule.mainClock.autoAdvance = false` + `mainClock.advanceTimeBy(...)`，或在 Roborazzi `beforeCapture` 内配合 `ShadowChoreographer.setPaused(true)` 并在 `afterCapture` 恢复 `false`）。
   - **问题 B（`render11_tabletLandscapeSkeleton` 失败）**：
     ```
     java.lang.IllegalArgumentException: failed to parse qualifiers 'w1180dp-h820dp-xhdpi-land'
     ```
     **根因**：Android 资源限定符顺序规范要求屏幕方向（`land`）必须在像素密度（`xhdpi`）之前。  
     **修法**：将 `LiquidScreensScreenshotTest.kt` 中的 `w1180dp-h820dp-xhdpi-land` 改为 `w1180dp-h820dp-land-xhdpi`。

### 2.3 未验证项
- 最新的 4 个 P1 修复文件（`MainActivity.kt`、`LiquidAdaptiveShell.kt`、`PlayerOverlay.kt`、`PlaylistDetailScreen.kt`）尚未经过 Gradle 编译与 APK 打包验证。
- 网易云音源 `try max num` 修复尚未在真机上实测网易云搜索/歌单/排行榜/播放。
- 真机多尺寸实测未执行：
  - `411 × 915dp`（手机竖屏）：Dock 32dp 收起 / 16dp 展开、搜索键盘弹出避让。
  - `700 × 840dp`（折叠屏/平板竖屏）：顶部胶囊、580dp 限宽播控。
  - `1280 × 800dp`（平板横屏）：侧栏 ↔ 顶栏切换、本地歌单进入与返回、播控随内容区居中。
  - `840 × 390dp` / 极矮分屏（`≤ 280dp` 高）：紧凑顶栏、播放器 `veryShort` 歌词/队列可滚不归零、清空队列后导航栏正常恢复。

---

## 3. 接手后的推荐执行顺序（Checklist）

1. **编译验收**：在 `native-android/` 执行 `./gradlew :app:compileDebugKotlin :app:compileDebugUnitTestKotlin --no-daemon`（或触发 GitHub Actions `native-build.yml`），确认最新提交零编译错误。
2. **修通 Roborazzi 截图 CI**：
   - 修改 `LiquidScreensScreenshotTest.kt` 中 `w1180dp-h820dp-xhdpi-land` → `w1180dp-h820dp-land-xhdpi`。
   - 修复 `LiquidScreenshotHarness.kt` 中无界 `idleMainLooper()` 挂死问题（注意避开 `@Before` 里 `ShadowChoreographer.setPaused(true)` 引发的 `ActivityController.windowFocusChanged` NPE）。
   - 跑通 `./gradlew :app:recordRoborazziDebug` 或触发 `compose-screenshots.yml`，审阅生成的 Light / Dark / Tablet 截图与 contact sheet。
3. **打包 Debug / Release APK 并真机验收**：
   - 验证网易云音源是否已彻底消除 `try max num` 报错。
   - 验证上述 4 种屏幕尺寸 + 清空队列 + 极矮分屏播放器交互。
4. **清理临时分支**：确认 `kotlin-native` 编译、截图与真机全通后，可删除临时排障分支 `ci/native-v2-preview`。
