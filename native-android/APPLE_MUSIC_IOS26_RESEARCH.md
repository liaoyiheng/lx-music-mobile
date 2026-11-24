# Apple Music on iOS 26 / iPadOS 26 原生界面、动效、交互与宽屏适配工程规范

> **标杆与边界声明**：本规范以 **原生 iOS 26 / iPadOS 26 Apple Music** 为唯一设计标杆（严禁参考官方 Android 版 Apple Music 或 Web 版布局）。文中所有条目标注实证来源；面向 Jetpack Compose 的具体数值、签名与断点参数标注 `[工程推导]`，待结合本地仓库其余源码进一步校准。

---

## 一、iOS 26 / iPadOS 26 Liquid Glass 核心视觉与物理规范

### 1.1 分层架构原则：控件层 vs 内容层严格分离
- **只用于浮动导航与控件层** `[实证: HIG 实图 + MacStories p.3 + HIG/WWDC25 摘要]`：
  - Liquid Glass 仅承载**悬浮于内容之上**的功能层：底部 Tab 胶囊、MiniPlayer 胶囊、顶部悬浮 Tab 胶囊、左侧 Sidebar、弹出气泡菜单（Context Popover）、圆形动作按钮（关闭 `×`、完成 `✓`、搜索、侧栏切换钮）与拖动中的滑块放大气泡（Loupe）。
  - **严禁将大片内容卡片、列表行、专辑墙背景做成糊玻璃**（内容卡片如 `Top Picks`、`Recently Played` 在白色/深色底色上保持实色或高不透明度封面/色块表面）。
- **禁止“玻璃叠玻璃”（No Glass-on-Glass）** `[实证: HIG/WWDC25 摘要]`：
  - Liquid Glass 控件下方延伸的是**应用内容层**（专辑封面、歌单列表、流光背景），不得在常规玻璃胶囊内再嵌套第二层独立模糊玻璃容器；激活 Tab 指示器是同一玻璃基底内部的**高光内嵌浮片（elevated pill indicator）**，而非双层独立实时高斯模糊叠加 `[实证: HIG 实图]`。

### 1.2 材质双变体与明暗自适应：`Regular` vs `Clear`
- **`Regular` 变体（默认首选）** `[实证: HIG/WWDC25 摘要]`：
  - 用于常规浅色/深色导航栏、Tab Bar、Sidebar、MiniPlayer、气泡菜单。
  - 根据底层内容自动自适应调节透明度与对比度，确保图标与单行文字在滚动经过高反差封面时仍具可读性。
  - 在深色模式下（见 `tab-bar-with-accessory-expanded~dark` 实图），呈深炭灰半透明磨砂感，胶囊上下弧边带细腻高光描边（Specular Rim Highlight）；在浅色模式下（见 `ipad-tab-bar-music-app` 实图），呈高通透亮白玻璃感，配合极轻的描边与弥散阴影区分纯白页面底色 `[实证: HIG 实图]`。
- **`Clear` 变体（仅用于富媒体强背景之上）** `[实证: HIG/WWDC25 摘要]`：
  - 用于全屏播放页（Now Playing）流动光影之上或全出血大封面之上的浮动控制按钮（返回、更多、播控胶囊、歌词/队列切换钮），追求极致通透度与色彩透射。
  - **强制暗化兜底（35% 基准）** `[实证: HIG/WWDC25 摘要]`：当底层专辑封面/背景亮度较高时，必须在 `Clear` 玻璃控件后方或底层媒体边缘叠加约 **35% 不透明度的暗化护栏/遮罩（Dimming Layer）**，否则白色图标/文字会丢失对比度。

### 1.3 Scroll Edge Effect（滚动边缘渐变模糊）与 Background Extension Effect
- **Scroll Edge Effect（Soft / Hard）** `[实证: MacStories p.3 + HIG/WWDC25 摘要]`：
  - iOS 26 取消了传统固定不动的“硬横线分割标题栏”。页面向上滚动时，顶部导航区域采用 **Scroll Edge Effect**：内容穿越顶栏下方时产生**由下至上渐进增强的变量模糊（Variable Blur）与柔和遮罩渐隐**，而顶部的悬浮玻璃按钮组保持锐利浮现于最前。
  - **Soft Edge**：用于普通可滚动内容页（发现页、资料库、歌单列表）顶部，模糊梯度平缓过渡；**Hard Edge**：用于需要更明确视觉边界的工具栏/表头固定区 `[实证: HIG/WWDC25 摘要]`。
- **Background Extension Effect（全出血底层延伸）** `[实证: HIG 实图 + HIG/WWDC25 摘要]`：
  - 从 `ipad-sidebar-music-app` 与 `ipad-tab-bar-music-app` 实图可见：`Recently Played` 卡片与页面底色/内容**直接延伸至顶部 Tab 胶囊、底部宽体 MiniPlayer 胶囊以及左侧 Sidebar 下方**，而非在底部/侧边留死板的实色遮挡带，从而让玻璃的折射、透色与模糊效果真正“活起来”。

### 1.4 几何与触控形态：Concentricity（同心圆角）、Toolbar 与 Sliders
- **Concentricity（同心圆角规律）** `[实证: HIG 实图 + MacStories p.3 + HIG/WWDC25 摘要]`：
  - 嵌套组件遵循同心圆角关系：`内层圆角 = 外层圆角 - 内外间距`（`innerRadius = max(minRadius, outerRadius - padding)`）。
  - 例：Expanded Tab Bar 外层全圆角胶囊（高度约 `56dp–62dp` → 半径 `28dp–31dp`），内部选中 Tab 高光浮片存在约 `4dp–5dp` 内边距，因此选中胶囊自然保持与外胶囊同心的全圆角形态 `[实证: HIG 实图 + 工程推导]`。
  - 列表分组（Table View / Grouped List）圆角进一步加大，分组块与外边缘留出清晰 Inset 边距 `[实证: MacStories p.3]`。
- **Buttons & Monochrome Toolbars（有形按钮与单色工具栏）** `[实证: MacStories p.3 + HIG 实图 + HIG/WWDC25 摘要]`：
  - **告别裸文字按钮**：iOS 26 全面淘汰无边框裸文字导航按钮，操作项均被包裹在圆形或胶囊形 Liquid Glass 容器中（如 `Done` → 圆形 `✓`，`Close` → 圆形 `×`，`Edit` 在侧栏保留轻量形态或置于独立组，`Sidebar Toggle` 为独立圆钮）`[实证: MacStories p.3 + HIG 实图]`。
  - **单色工具栏 + `.prominent` 唯一强调**：Toolbar 内默认图标统一单色（深色模式下纯白、浅色模式下深灰/黑），**仅对当前激活 Tab 或唯一主操作（如 Play/主强调按钮）使用主题红 `#FA2D48` / `.prominent` 强调色**，避免满屏彩色图标抢戏 `[实证: HIG 实图 + HIG/WWDC25 摘要]`。
  - 按压反馈：按下时控件呈现轻微“抬升放大 + 高光提亮”的弹性态 `[实证: MacStories p.3]`。
- **Sliders（Loupe 气泡滑块与惯性甩动）& Thumbless 进度条** `[实证: MacStories p.3 + HIG/WWDC25 摘要]`：
  - 常规滑块拇指改为**横向拉长的药丸形（Pill-shaped）**。
  - 手指按住拖动时，拇指即时形变为放大的 **Liquid Glass 折射气泡（Loupe）**，使进度条/调节值在指尖下方依然清晰可见并折射底层光线。
  - **支持惯性甩动（Momentum Flick）**：快速拨动滑块后松手，滑块具备物理动量滑行与平滑衰减停靠 `[实证: MacStories p.3]`。
  - **媒体进度条 Thumbless 态**：全屏播放页进度条常态为无圆点/无拇指的极简细胶囊（Thumbless Style），手指触碰/拖拽时轨道垂直扩粗并点亮高光（或浮现微光指示），松手后弹性回缩 `[实证: HIG/WWDC25 摘要 + 工程推导]`。

### 1.5 物理动效系统与弹簧参数映射（Jetpack Compose 对齐）
本仓库 [`LiquidMotion.kt`](file:///tmp/fork/native-android/app/src/main/java/cn/toside/music/liquid/ui/components/LiquidMotion.kt) 已确立与 Apple HIG 对齐的四档核心弹簧常数 `[实证: 代码]`，第二波重构须严格复用，禁止随意另起 `tween` 僵硬插值：

| 动效档位 | 已定稿参数 (`LiquidMotion.kt`) | 适用 Apple Music 交互场景 | 来源属性 |
| :--- | :--- | :--- | :--- |
| **`SpringSnappy`** | `dampingRatio = 0.78f`, `stiffness = 520f` | 按钮按压放大回弹、Tab 选中浮片滑行与形变、上下文气泡菜单（`0.84f → 1.0f`）、底栏折叠/展开切换、Slider 气泡弹出 | `[实证: 代码 + 工程推导]` |
| **`SpringGentle`** | `dampingRatio = 0.86f`, `stiffness = 320f` | 详情页推入/返回（底层联动左移 `-22%`）、MiniPlayer ↔ 全屏播放页垂直/共享容器展开、搜索胶囊展开变形、Sidebar ↔ Top Tab 切换 | `[实证: 代码 + 工程推导]` |
| **`SpringBouncy`** | `dampingRatio = 0.58f`, `stiffness = 420f` | 全屏封面**播放 `1.0x` ↔ 暂停 `0.82x` 呼吸缩放**（及对应阴影半径收敛）、红心收藏跳动、切歌封面轻弹反馈 | `[实证: 代码 + 工程推导]` |
| **`SpringFluid`** | `dampingRatio = 0.92f`, `stiffness = 220f` | 逐行歌词垂直聚焦滑动、流光背景主色平滑过渡（`MeshColorCrossfadeMs = 650`）、`immersiveTint` 沉浸着色切换 | `[实证: 代码 + 工程推导]` |

**关键动效补充工程参数** `[工程推导（注：开源库 AMLL/MeloX 因环境超时未拉取，此处为 Compose 物理推导参数）]`：
- **MiniPlayer ↔ FullPlayer 手势展开**：拖拽阻尼跟随手指 `translationY`，圆角从 MiniPlayer 胶囊半径（`~28dp`）平滑插值到屏幕外框/全屏圆角（`0dp` 或设备圆角），内容元素错峰透明度（MiniPlayer 内容在前 `0%–30%` 进度淡出，全屏大封面同步缩放定位，播控区在 `40%–100%` 渐显）。
- **逐行歌词波浪式瀑布滚动（Cascade Effect）**：当激活行切换时，利用 `SpringFluid(0.92f, 220f)` 驱动滚动位移，可视区相邻行按距当前行索引差 `|i - activeIndex|` 施加约 `18ms–28ms` 递进延迟（上限 ~6 行），形成“弹性瀑布”跟手波浪感；非当前行按距离阶梯降低透明度（`0.55f → 0.32f → 0.18f`）并在 Android 12+（API 31+）施加渐进柔焦（`0.dp → 1.5.dp → 3.5.dp`），当前行微放大（`1.0f → 1.04f`）+ 柔和发光，未播放/点击预览歌词时瞬时去模糊至清晰态。
- **全出血详情页下拉 Overscroll 与视差**：下拉超过顶部时，Hero 封面以阻尼系数 `0.5f–0.6f` 向上跟随放大（`scale = 1 + overscroll / heroHeight`），避免露白边；上滑收起 Hero 时封面以 `0.45x` 速率产生视差位移，同时顶栏 Scroll Edge Effect 渐显模糊与居中紧凑标题。

---

## 二、原生 iOS 26 Apple Music 招牌交互与功能清单

### 2.1 Scroll-Driven Tab Bar Minimize（下滑收缩 + Inline MiniPlayer）
依据 Apple HIG 实机配图（`tab-bar-with-accessory-expanded~dark` 与 `tab-bar-with-accessory-collapsed~dark`）与 MacStories 实测 `[实证: HIG 实图 + MacStories p.3]`：

1. **默认展开态（Expanded）**：
   - 上层：独立悬浮 MiniPlayer 胶囊（左侧圆角封面缩略图 + 曲目主标题/副标题 + 右侧 `播放/暂停` 与 `下一首` 两个图标按钮）。
   - 下层：底部主 Tab 栏胶囊（本仓库 `LiquidFloatingDock.kt` 采用左侧 `4-Tab 胶囊 [资料库/发现/排行/设置]` + 右侧 `独立搜索玻璃圆钮`，与 HIG 允许独立搜索圆钮形态一致）。
   - 两层之间保持清晰间距（约 `8dp–10dp`），互不粘连。
2. **下滑收缩态（Collapsed on Scroll Down）**：
   - 触发阈值：列表向下滚动累计位移超过约 `24dp–36dp`（建议加入方向滞回防抖，防止手指轻触抖动误触发）`[工程推导]`。
   - **视觉变形（实图核实）**：两行合并压缩为屏幕最底部的**单行三段式悬浮玻璃栏**：
     - **左侧段**：缩为**单个圆形 Liquid Glass 按钮**（直径与中段高度一致，约 `52dp–56dp`），仅显示当前激活 Tab 的主题红实心图标，隐藏文字标签；
     - **中间段**：MiniPlayer 变形为**Inline 紧凑胶囊**嵌入左右圆钮之间（横向间隔约 `8dp`），保留 `圆角小封面 + 单行主标题 + 播放/暂停钮`，**隐藏“下一首”按钮与副标题**以确保标题横向可读空间；
     - **右侧段**：独立圆形「搜索」Liquid Glass 按钮原地保留。
3. **展开还原触发**：
   - 列表向上滚动、滚回顶部，或**轻点左侧单个激活 Tab 圆钮**时，立即以 `SpringSnappy` 展开还原为双层展开态 `[实证: MacStories p.3 + HIG]`。

### 2.2 MiniPlayer 交互矩阵
- **点击 / 上滑展开全屏**：`[实证: LiquidFloatingDock.kt + HIG]` 点击胶囊非按钮区域，或自胶囊向上拖曳超过阈值，弹性展开全屏播放页。
- **左右滑动切歌**：`[实证: LiquidFloatingDock.kt]` 在 MiniPlayer 文本/封面区域左右横向滑动，跟手阻尼位移并触发触感反馈（Haptic）切换上/下一首。
- **详情页上下文色渗透（Immersive Tint）**：`[实证: LiquidFloatingDock.kt]` 传入 `immersiveTint` 时，底部护栏渐变与玻璃基底柔和吸收歌单/专辑封面主色，使底栏与沉浸式详情页浑然一体。

### 2.3 Library Pin（资料库置顶）
`[实证: MacStories p.15 + HIG 实图]`
- **支持对象**：任意**歌单、专辑、艺人或单曲**，通过长按/更多气泡菜单点击 **`Pin（置顶）`** 加入。
- **数量上限**：iOS 26 / iPadOS 26 原生限制最多置顶 **6 项**（MacStories 实测对 6 项上限有明确记录；本项目可默认展示最多 6 项紧凑置顶网格，并可视需要提供设置项或保持 6 项经典双排/三列格）。
- **展示形态**：
  - **手机端（Compact）**：位于「资料库」页最顶部优先区，呈现紧凑圆角封面磁贴网格（如 3 列×2 行或 2 列×3 行）或横排玻璃卡片；
  - **平板端（Expanded Sidebar）**：自动同步显示在左侧 Liquid Glass Sidebar 的 `Library` 分区顶部，点击一键跳转或播放。
- **Tap Actions（自定义点击动作）**：置顶项支持为单击配置默认动作（打开详情 / 直接播放 / 随机播放），长按弹出气泡菜单可快速切换动作或取消置顶（Unpin）`[实证: MacStories p.15]`。

### 2.4 Playlist Folders（歌单文件夹管理）
`[实证: MacStories p.15]`
- 移动端原生支持创建与管理歌单文件夹（不再依赖桌面端）：
  - 点击歌单页 `+` 按钮弹出选择：「新建歌单」或「新建文件夹」；
  - 长按已有歌单可通过上下文菜单 `Move to Folder（移至文件夹）` 归类；
  - 在平板 Sidebar 的 `Playlists` 分区中支持直接在侧栏内折叠/展开歌单文件夹 `[实证: MacStories p.15]`。

### 2.5 Lyrics Translation & Pronunciation（歌词翻译 + 注音/音译双显）
`[实证: Apple 官网]`
- **功能定位**：Apple Music Sing / 实时歌词内原生支持外语歌词的**逐行翻译（Translation）**与**音译注音（Pronunciation，如罗马音）**同步显示，辅助跟唱与理解。
- **排版与交互规范** `[实证: Apple 官网 + 工程推导]`：
  - 播放页底部（常规在歌词视图左下角气泡按钮或更多菜单内）提供独立开关：仅原词 / 原词+译文 / 原词+音译 / 双显；
  - 视觉层级：主歌词大号加粗（`24sp–28sp` 手机 / `28sp–34sp` 平板），音译/罗马音紧邻主歌词或作为次级辅助行（`13sp–15sp`，约 `0.72f` 透明度），译文位于下方（`15sp–17sp`，约 `0.80f` 透明度），同组词行内部行距紧凑（`4dp`），组间保留呼吸间距（`18dp–24dp`）。

### 2.6 AutoMix / Crossfade（智能混音过渡）
`[实证: Apple 官网 + MacStories p.15]`
- **AutoMix**：通过**节拍匹配（Beat Matching）与时间拉伸（Time Stretching）**在歌曲结尾与下首开头自动实现 DJ 级无缝混音衔接。
- **界面状态指示**：正在执行 AutoMix 过渡时，全屏播放页中**原本显示音频品质标签（如 Hi-Res Lossless / Dolby Atmos）的位置会短暂切换显示 `"Mixing"` 提示** `[实证: MacStories p.15]`。
- **用户可切换**：设置页提供过渡模式切换（`关闭` / `Crossfade 交叉淡入淡出` / `AutoMix 智能混音`）`[实证: MacStories p.15]`。

### 2.7 全屏动态流光封面、无边框 Hero 详情页与 Context Popover Menu
- **全屏播放页动态流光**：`[实证: LiquidMotion.kt + Apple 官网]` 提取当前专辑封面多组主色生成实时慢速流动的 Mesh Gradient 光影背景，封面图在播放态（`1.0x` + 柔和彩色弥散投影）与暂停态（`0.82x` + 投影收拢）间以 `SpringBouncy` 切换。
- **无边框全出血 Hero 详情页**：歌单/专辑/榜单详情页顶部封面直通状态栏顶部（Edge-to-Edge），下方平滑渐变融入封面提取主色底板，标题、副标题、简介与「播放 / 随机播放」双胶囊主按钮直接浮于色彩渐变层之上，无需僵硬横线分割。
- **Context Popover Menu（长按/更多气泡菜单）**：`[实证: MacStories p.3 + LiquidMotion.kt]` 使用 `popoverEnterTransition`（`0.84f → 1.0f` 锚点弹出 + `SpringSnappy`），采用 Liquid Glass `Regular` 材质、大圆角分组与清晰图标+文案行（顶部常置歌曲缩略信息头：封面+歌名+歌手），支持置顶、移入文件夹、加队列、查艺人/专辑等高频操作。

---

## 三、原生 iPadOS 26 Apple Music 与 Android 宽屏适配规范

> 本章结构严格对照 Apple HIG 官方 iPadOS 26 Apple Music 实机双态图（`ipad-tab-bar-music-app` 与 `ipad-sidebar-music-app`）制定，适用于 Android 平板、大折叠屏（展开态）及桌面分屏窗口。

### 3.1 三档响应式断点策略（对齐 Jetpack Compose `WindowSizeClass`）

| 断点档位 | 宽度判定 (`widthDp`) | 典型设备形态 | 导航骨架形态 | 底部 MiniPlayer 形态 |
| :--- | :--- | :--- | :--- | :--- |
| **`Compact`** | `< 600dp` | 手机竖屏、折叠屏外屏、极窄分屏窗口 | 底部悬浮 `4-Tab 胶囊 + 搜索圆钮`（下拉可缩为单行三段式） | 手机悬浮胶囊（位于底栏上方，收缩时内联于底栏正中） |
| **`Medium`** | `600dp – 839dp` | 大折叠屏展开、8–11 寸平板竖屏、半屏分屏 | **顶部居中悬浮玻璃 Tab 胶囊**（含侧栏抽屉呼出钮）+ 可临时呼出叠加态 Sidebar | **底部居中限宽悬浮宽体 MiniPlayer**（`maxWidth = 580dp` 左右） |
| **`Expanded`** | `≥ 840dp` | 平板横屏、桌面全屏/大窗模式 | **默认展开左侧悬浮/半透明 Liquid Glass Sidebar**；点击侧栏右上角切换钮可**收起 Sidebar 并形变为顶部居中悬浮玻璃 Tab 胶囊** | **底部居中悬浮宽体 MiniPlayer**（当 Sidebar 展开时，在**右侧内容区**内水平居中；收起时在全屏内水平居中） |

#### 关键兜底规则：横屏手机与矮屏分屏（`Compact Height` 保护）`[工程推导]`
- **严禁仅凭 `widthDp >= 600dp` 就直接套用平板宽屏双栏竖排结构**！典型手机横屏（如 `840dp × 390dp`）虽宽度进入 `Medium`/`Expanded`，但高度 `heightDp < 480dp` 属于 **Compact Height**。
- **兜底判定建议**：定义 `isCompactHeight = heightDp < 480.dp`（且 `widthDp < 960.dp` 或结合屏幕最小边长 `smallestScreenWidthDp < 600`）：
  1. **导航兜底**：手机横屏（`isCompactHeight`）不常驻占宽 `260dp` 的左侧 Sidebar，改用**顶部居中紧凑悬浮 Tab 胶囊**或侧滑抽屉，把宝贵的横向内容面积留给列表/卡片；
  2. **详情页兜底**：左右双栏（左固定封面列仅 `220dp–260dp`，取消大段纵向留白，按钮紧贴封面下方）+ 右侧独立滚动曲目表；
  3. **全屏播放页兜底**：`isCompactHeight` 下**强制左右双栏**（左侧正方形小封面缩至 `180dp–230dp` + 精简进度条与播控，右侧呈现歌词/队列或曲目信息，绝不上下堆叠导致封面溢出屏幕）。

---

### 3.2 iPadOS 26 导航双态规范（Sidebar ↔ 顶部悬浮 Tab 胶囊）

#### 状态 A：顶部悬浮 Liquid Glass Tab 胶囊（`Medium` 默认 / `Expanded` 收起态）`[实证: HIG 实图 ipad-tab-bar-music-app]`
- **位置**：顶部水平居中悬浮于状态栏下方（距顶部安全区约 `10dp–14dp`），高度约 `48dp–52dp` `[工程推导]`，全圆角 Liquid Glass `Regular` 胶囊。
- **内部元素（实图严格核对，由左至右）**：
  1. **左侧侧栏切换钮**（`sidebar.leading` 图标）：点击呼出/展开左侧 Sidebar；
  2. **主 Tab 项**：`发现`（对应 HIG `Home`）/ `排行`（或 `New`）/ `资料库`（或若保留 4 项则为 `资料库 · 发现 · 排行 · 设置`，建议精选 3–4 项保持胶囊紧凑不超宽）——当前激活项包裹在**高亮内嵌圆角浮片**中且图标/文字呈主题红 `#FA2D48`；
  3. **尾部搜索图标**（放大镜）：集成于顶部 Tab 胶囊最右侧（点击后胶囊可横向扩展为玻璃搜索输入框）。
- **右上角独立区**：与页面大标题（`"Home"` / `"发现"`）同行最右侧，放置独立圆形**个人头像/设置快捷圆钮**（对应 HIG 实图右上角红色用户图标）。

#### 状态 B：左侧 Liquid Glass Sidebar（`Expanded` 默认态）`[实证: HIG 实图 ipad-sidebar-music-app + MacStories p.15]`
- **尺寸与视觉**：
  - 推荐宽度：**`248dp – 268dp`**（约占 11–13 寸平板横屏宽度的 22%–24%）`[实证: HIG 实图比例 + 工程推导]`。
  - **底层内容透射**：`Expanded` 布局采用覆盖/分栏结合架构，右侧横向滚动卡片、详情页渐变背景等可延伸至 Sidebar 半透明玻璃下方（Background Extension Effect），让 Sidebar 拥有真实折射深度。
- **内部结构自上而下（严格对照 HIG 实图 + MacStories p.15）**：
  1. **Sidebar 顶栏**：左侧 `"Edit"`（编辑侧栏/置顶项按钮），右侧独立圆形 **Liquid Glass 侧栏收起按钮**（带微阴影与玻璃描边，点击收起回到顶部 Tab 胶囊态）；
  2. **顶部导航组**：
     - `搜索`（放大镜，点击展开搜索页或内联搜索框）
     - `发现` / `排行` / `设置`（主功能导航；**当前激活项被包裹在明亮的悬浮圆角白/亮玻璃胶囊内**，图标与文字呈主题红 `#FA2D48`，其余项单色深灰/纯白）；
  3. **可折叠分区 `资料库（Library）`**（带右侧折叠箭头 `⌄`）：
     - **置顶区（Pins，最多 6 项）** `[实证: MacStories p.15]`：直接展示用户置顶的歌单/专辑/艺人（带小缩略图），支持一触直达；
     - 资料库子入口：`最近添加`、`艺人`、`专辑`、`歌曲`、`已下载`；
  4. **可折叠分区 `歌单（Playlists）`**（带右侧折叠箭头 `⌄`）：
     - `全部歌单`、`我喜欢的歌曲`、`歌单文件夹`（支持在侧栏直接展开/折叠文件夹）及用户歌单列表。

---

### 3.3 iPadOS 26 宽屏悬浮宽体 MiniPlayer 规范 `[实证: HIG 实图]`
对照 `ipad-tab-bar-music-app` 与 `ipad-sidebar-music-app` 实图，平板宽屏底部的 MiniPlayer 彻底重构为**单行宽体三段式 Liquid Glass 胶囊**（`Medium` / `Expanded` 通用）：
- **定位与对齐规则（实图关键发现）**：
  - 悬浮于底部安全区上方约 `12dp–16dp` 处，高度约 `56dp–60dp`，全圆角大胶囊；
  - **Sidebar 收起时**：在整个屏幕宽度下水平居中，两侧各留约 `8%–10%` 呼吸边距（或 `maxWidth = 760dp–840dp`）；
  - **Sidebar 展开时**：**在右侧主内容区域内部水平居中**（两侧各留约 `28dp–40dp` 边距），绝不跨越左侧 Sidebar，不遮挡侧栏底部歌单列表！
- **内部三段式控件布局（由左至右）**：
  1. **左区（播控 5 键组）**：`随机播放`、`上一首`、`播放/暂停`（视觉加粗突出）、`下一首`、`循环模式`；
  2. **中区（曲目与封面中心区）**：居中的圆角小封面（`~36dp`）+ 歌名与艺人（空闲态居中显示品牌音符/Logo，对应实图居中苹果 Logo），点击展开全屏播放页；
  3. **右区（快捷工具 4 键组）**：`歌词开关`（气泡引号图标）、`待播队列`（列表图标）、`投屏/设备输出`、`音量提示/静音控制`（在 Android 平板上可映射为音量滑块 Popover 或音频输出弹窗）。
- **底层内容**：下方页面内容（如 `Recently Played` 卡片和列表尾部）继续延伸滚动穿过其背后，内容区底部预留约 `88dp` `contentPadding(bottom)` 以保证最末项可滚出不被永久遮挡。

---

### 3.4 核心业务页面宽屏自适应规格 `[实证: HIG 实图 + 工程推导]`

| 页面 | `Compact (<600dp)` | `Medium (600–839dp)` | `Expanded (≥840dp)` |
| :--- | :--- | :--- | :--- |
| **发现页 / 排行** | 水平边距 `20dp`；`Top Picks` 大卡单卡占 ~82% 屏宽（露下一张边缘）；双列或单列榜单 | 水平边距 `28dp`；`Top Picks` 卡片固定宽度区间 `240dp–270dp`（横屏约露 2.4–3 卡）；网格 `GridCells.Adaptive(164.dp)` | 水平边距 `36dp`；`Top Picks` 维持 `260dp–290dp` 比例（Sidebar 开启时约 **2.7 卡**、关闭时约 **3.4 卡**，完全还原 HIG 实图）；网格 `GridCells.Adaptive(172.dp–180.dp)`；排行榜多列并行卡片矩阵 |
| **资料库页** | 水平边距 `20dp`；顶部 Pin（2×3 或 3×2）+ 纵向分类列表 + 2 列最近添加封面墙 | 水平边距 `28dp`；Pin 横向 3–6 列网格；最近添加 `GridCells.Adaptive(156.dp)` | 水平边距 `36dp`；主内容区直接承接侧栏所选二级页（如全部专辑/最近添加采用 4–5 列网格 `GridCells.Adaptive(172.dp)`，歌曲列表带表头与多列信息） |
| **歌单 / 专辑 / 榜单详情页** | 无边框全出血纵向 Hero（封面居中 + 下方渐变标题与双按钮）→ 下方单列曲目 | **横向 Hero Header**（左方正封面 `180dp–210dp` + 右侧标题/作者/简介与「播放/随机」胶囊按钮）→ 下方全宽曲目表（增补时长列） | **左右双栏（Master-Detail Split）**：<br>• **左栏（固定 Sticky，宽 `320dp–360dp`）**：高清大封面（`260dp–300dp`）+ 标题 + 艺人/更新信息 + 简介 +「播放/随机」双胶囊 + 收藏/置顶/菜单；<br>• **右栏（`weight(1f)` 独立滚动）**：完整曲目表，增补**专辑名列**与**时长列**，支持鼠标悬停/触摸圆角行高亮 |
| **全屏播放页 Now Playing（核心识别度！）** | 纵向排布：封面（播放 `1.0x` / 暂停 `0.82x`）→ 曲目信息与红心/更多 → 进度条 → 主播控 → 底部功能栏（歌词 / 队列 / 音效）；切歌词模式封面缩至顶部小条 | 居中限宽（`maxWidth = 460dp`）；展开歌词或队列时可选择双栏或平滑切换（宽度足够 `≥720dp` 时建议提前启用双栏） | **双栏动态联动**：<br>• **纯封面模式**：封面 + 进度条 + 播控居中限宽（`maxWidth = 440dp`），四周满屏流光背景；<br>• **展开「歌词」或「待播队列」模式**：左侧封面+进度条+完整播控**以 `SpringGentle` 平滑左移至左栏（占 `42%–45%` 宽，保持完整播控而不压扁成迷你条）**；右侧（`55%–58%` 宽）全高展开大字逐行歌词（含翻译/音译）或待播队列！ |
| **搜索页** | 全宽搜索框（底栏变形）+ 2 列音乐流派分类卡 + 单列搜索结果 | 顶部居中胶囊搜索框 + 3 列流派分类网格 + 限宽结果列表 | 左侧分类/历史或 4 列流派发现网格；搜索结果采用**双栏分块**（左栏最佳匹配 Top Result 大卡 + 艺人/专辑，右栏歌曲列表） |
| **设置页** | 全宽圆角分组列表（水平边距 `20dp`） | 居中限宽分组列表（`maxWidth = 640dp`） | 居中限宽圆角分组列表（**`maxWidth = 680dp`**），避免开关与滑动条跨满 `1280dp` 屏幕导致视觉失衡 |

---

## 四、对接 `native-android` 代码库的差距清单与第二波落地蓝图

> **严谨性核注**：本轮工具输出中直接核实了 [`LiquidMotion.kt`](file:///tmp/fork/native-android/app/src/main/java/cn/toside/music/liquid/ui/components/LiquidMotion.kt)（全部 229 行）与 [`LiquidFloatingDock.kt`](file:///tmp/fork/native-android/app/src/main/java/cn/toside/music/liquid/ui/components/LiquidFloatingDock.kt)（1–160 行）；以下针对 `DESIGN_V2_SPEC.md`、`MusicModels.kt`、`LiquidTheme.kt`、`LiquidGlassComponents.kt` 及业务页面的对接建议，基于任务上下文已知字段（`tlyric`、`rlyric`、歌单、榜单等）与已读取的底座 API 进行对齐，具体符号命名在派发第二波页面子代理前请再快速核对一次 `MusicModels.kt` 与 `LiquidGlassComponents.kt`。

### 4.1 现状对照与差距清单

| 能力域 | 已有基础（已核实 / 已知） | 对标 iOS/iPadOS 26 关键差距 | 优先级 |
| :--- | :--- | :--- | :--- |
| **物理动效底座** | `LiquidMotion.kt` 已齐备 4 档弹簧、层次推入 `-22%`、Tab 切换、Popover `0.84f` 展开、错峰入场 `[实证: 代码]` | 需补充：**Scroll-Driven 收缩阈值阻尼逻辑**、**歌词波浪级联延迟函数**、**宽屏双栏左移布局动画规格** | **P0** |
| **底部浮动 Dock（手机）** | `LiquidFloatingDock.kt` 已具备双层胶囊、4-Tab + 独立搜索圆钮、搜索展开变形、`immersiveTint` 融色、左右滑切歌 `[实证: 代码]` | 缺少 **Scroll-Driven 下滑收缩态**（缩为单行三段式：`[当前Tab圆钮 + Inline MiniPlayer + 搜索圆钮]`）与轻点左侧圆钮还原 | **P0** |
| **平板/折叠屏宽屏骨架** | 目前仅见单屏手机 `LiquidFloatingDock` 形态 `[实证: 代码]` | 缺少三档断点体系、`LiquidSidebar`（含 `Edit`/收起钮/可折叠 Library & Playlists）、顶部居中悬浮 Tab 胶囊、宽体三段式 `LiquidWideMiniPlayer` | **P0** |
| **Scroll Edge & 材质分离** | 据上下文 `LiquidTheme.kt` / `LiquidGlassComponents.kt` 已建液态玻璃基础组件 `[未直接读取]` | 需核查并严格收口：**内容卡片禁用糊玻璃**、加入顶栏 `ScrollEdgeEffect` 渐隐模糊、加强 `Clear` 玻璃在亮图下的 **35% 暗化保护**、检查同心圆角公式 | **P0** |
| **全屏播放页（含双栏）** | 已知项目目标含全屏流光与逐行歌词；据上下文模型含 `tlyric`/`rlyric` `[未直接读取]` | 需落地：**宽屏左右双栏联动**（左封面+完整播控平滑左移 42–45%，右全高歌词/队列）、**翻译+音译双显开关与排版**、播放/暂停 `1.0x ↔ 0.82x` 弹簧呼吸、**Thumbless 细轨进度条**与拖拽气泡放大 | **P0** |
| **详情页自适应双栏** | 据上下文存在歌单/专辑/榜单页 `[未直接读取]` | 需按三档断点拆分：**全出血 Hero**（Compact）↔ **横向 Hero**（Medium）↔ **左右 Sticky 双栏 + 专辑/时长扩展列**（Expanded）+ 下拉 Overscroll 拉伸与 `0.45x` 视差 | **P0** |
| **Library Pin（置顶）** | 尚无独立置顶模型与入口 `[未直接读取]` | 新增本地持久化置顶列表（上限 6 项，支持歌单/专辑/艺人/单曲）、长按气泡置顶/Tap Action 设置、同步呈现在资料库顶部与平板 Sidebar 顶部 | **P1** |
| **Playlist Folders** | 仅常规歌单列表 `[未直接读取]` | 本地文件夹分组结构、`+` 创建文件夹、长按 `Move to Folder`、Sidebar 折叠展开 | **P1** |
| **AutoMix / Crossfade** | 依赖播放器底层引擎 `[未直接读取]` | 第一阶段可先补全设置页与播放页标签位契约；真实节拍匹配与时间拉伸需 ExoPlayer 音频管线深度扩展，建议放 P2（Crossfade 可先行） | **P2（Crossfade 可 P1）** |
| **桌面 Widget / 跨设备同步** | Android 桌面小组件暂非第二波核心阻塞项 | 与 iOS Pin Widget 类似，延后至第三波打磨 | **P2** |

---

### 4.2 建议新增宽屏共享底座与核心签名（第二波前置契约）

建议在 `cn.toside.music.liquid.ui.adaptive`（或 `ui.components`）下新建 **`AdaptiveLayout.kt`**，作为所有第二波子代理必须遵守的唯一宽屏契约 `[工程推导]`：

```kotlin
package cn.toside.music.liquid.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class LiquidWidthSizeClass { Compact, Medium, Expanded }
enum class LiquidHeightSizeClass { Compact, Medium, Expanded }

enum class LiquidNavigationMode {
    BottomDock,      // Compact (<600dp) 或手机横屏兜底
    TopFloatingTabs, // Medium (600-839dp) 或 Expanded 下用户手动收起 Sidebar
    LeftSidebar      // Expanded (>=840dp 且非矮屏) 默认常驻左侧悬浮玻璃侧栏
}

@Immutable
data class LiquidWindowClass(
    val widthSizeClass: LiquidWidthSizeClass,
    val heightSizeClass: LiquidHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp,
    val isCompactHeight: Boolean = heightDp < 480.dp,
    val horizontalMargin: Dp = when (widthSizeClass) {
        LiquidWidthSizeClass.Compact -> 20.dp
        LiquidWidthSizeClass.Medium -> 28.dp
        LiquidWidthSizeClass.Expanded -> 36.dp
    },
    val gridMinCellSize: Dp = when (widthSizeClass) {
        LiquidWidthSizeClass.Compact -> 156.dp
        LiquidWidthSizeClass.Medium -> 164.dp
        LiquidWidthSizeClass.Expanded -> 176.dp
    },
    val prefersTwoPaneDetail: Boolean = widthSizeClass == LiquidWidthSizeClass.Expanded && !isCompactHeight,
    val prefersTwoPanePlayer: Boolean = (widthSizeClass == LiquidWidthSizeClass.Expanded ||
        (widthSizeClass == LiquidWidthSizeClass.Medium && widthDp >= 720.dp) ||
        isCompactHeight),
)

val LocalLiquidWindowClass = compositionLocalOf<LiquidWindowClass> {
    error("LocalLiquidWindowClass not provided")
}
```

#### 配套新增组件拆分建议
1. **`AdaptiveScaffold.kt`（或整合进 `AdaptiveLayout.kt`）**：
   - `LiquidAdaptiveScaffold(...)`：统一根据 `LiquidWindowClass` 与用户偏好状态 `isSidebarExpanded` 切换：
     - `BottomDock`：渲染现有 `LiquidFloatingDock`（新增 `isCollapsed: Boolean` 参数驱动滚落单行三段式态）；
     - `TopFloatingTabs`：顶部渲染 `LiquidTopTabCapsule`（含 Sidebar 切换钮 + Tab 胶囊 + 尾部搜索钮），底部渲染居中限宽 `LiquidWideMiniPlayer`；
     - `LeftSidebar`：左侧渲染 `LiquidSidebar`（宽 `256.dp`），右侧内容区顶部留出透明状态栏 + 个人头像按钮，底部在右栏内部居中渲染 `LiquidWideMiniPlayer`。
2. **`LiquidSidebar.kt`**：
   - 分区：`Edit` + 右上角圆形收起钮；主导航（搜索、发现、排行、设置，激活态高光白胶囊 + 主题红）；可折叠 `资料库`（先置顶 Pins，后子项）；可折叠 `歌单`（文件夹 + 自建歌单）。
3. **`LiquidTopTabCapsule.kt`**：
   - 严格复刻 `ipad-tab-bar-music-app` 实图：`[SidebarToggleIcon | ActivePill Tab | Other Tabs | SearchIcon]` + 搜索激活时无缝横向展成搜索框。
4. **`LiquidWideMiniPlayer.kt`**：
   - 平板专属三段式宽体悬浮玻璃胶囊：`[随机/上曲/播放/下曲/循环]` | `[居中封面+曲名/艺人]` | `[歌词/队列/输出设备/音量]`。
5. **`LiquidScrollEdge.kt`**：
   - 提供 `Modifier.liquidScrollEdgeOverlay(...)` 与 `rememberDockCollapseNestedScrollConnection(...)`：统一处理顶栏边缘柔焦渐隐与手机底栏向下滚动收缩（建议阈值 `collapseThresholdPx = 32.dp`，回滚展开阈值 `expandThresholdPx = 16.dp`）。

---

### 4.3 第二波业务页面并行改造要点
- **发现页 / 排行榜**：去掉内容卡片多余模糊，改用高质感封面与底部渐变文字条；`Top Picks` 横滑卡随断点计算固定合适宽度保证右侧露边；排行榜列表在 `Expanded` 下可采用 2 列或自适应卡片网格。
- **资料库页**：顶部新增 **置顶区（最多 6 项）** + 长按气泡置顶/Tap Action；下方在宽屏下采用大圆角分类块 + 自适应列数专辑网格。
- **歌单 / 专辑 / 榜单详情页**：基于 `LocalLiquidWindowClass.current` 分发三种 Header（纵向全出血 Hero / 横向 Hero / 左固定 Sticky 双栏），右栏补齐**专辑列**与**时长列**，滚动衔接 `ScrollEdgeEffect` 顶栏。
- **全屏播放页（Now Playing）**：对接模型中的翻译歌词（`tlyric`）与罗马音（`rlyric`），左下角气泡按钮控制双显切换；宽屏时用 `AnimatedContent` / `animateFloatAsState(SpringGentle)` 实现封面+播控平顺左移（`42%–45%`），右栏展示歌词（`SpringFluid` 逐行级联 + Android 12+ 渐进柔焦）或待播队列。
- **搜索页 & 设置页**：搜索页在宽屏下区分左栏最佳匹配与右栏曲目表；设置页统一套用 `widthIn(max = 680.dp)` 居中分组圆角卡片，新增 `Song Transitions（关闭 / 交叉淡入淡出）` 与 `置顶管理` 选项。

---

### 4.4 Roborazzi 云端截图验证矩阵扩充建议 `[工程推导]`
建议在现有手机截图回归之外，新增以下关键设备配置与交互状态快照：
1. **手机竖屏（`Compact` — 如 `w411dp-h915dp-xxhdpi`）**：
   - 底栏 **Expanded 双层态** vs **Collapsed 单行三段式态**；
   - 全屏播放页（翻译 + 音译双显态、拖拽进度条态）；
   - 长按歌曲 Liquid Glass Context Popover 态。
2. **手机横屏 / 极窄分屏（`Compact Height` — `w840dp-h390dp-xxhdpi`）**：
   - 验证底栏未错误触发占宽 Sidebar、全屏播放页强制左右分栏无纵向溢出。
3. **折叠屏 / 平板竖屏（`Medium` — `w700dp-h840dp-xhdpi`）**：
   - 顶部居中悬浮 Tab 胶囊 + 底部居中宽体 MiniPlayer + 横向 Hero 详情页。
4. **平板横屏（`Expanded` — `w1280dp-h800dp-xhdpi`）**：
   - 发现页 **Sidebar 展开态**（含可折叠 Library/Pins 与右栏居中宽体 MiniPlayer）；
   - 发现页 **Sidebar 收起态**（切换为顶部居中 Tab 胶囊 + `Top Picks` ~3.4 卡露边）；
   - 歌单/专辑详情页 **左右双栏（左 Sticky 封面 + 右含专辑/时长列曲目表）**；
   - 全屏播放页 **纯封面居中态** vs **左封面播控 + 右逐字翻译歌词双栏态**；
   - 设置页 `maxWidth = 680dp` 居中分组态。
