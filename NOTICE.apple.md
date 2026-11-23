# 修改说明

本项目基于 [lyswhut/lx-music-mobile](https://github.com/lyswhut/lx-music-mobile)（Apache License 2.0）修改，原版权与许可证保持不变，见 LICENSE。

本分支（apple-style）所做的修改：

- 新增 `src/theme/apple.ts`：Apple Music 风格配色、圆角、字号规范
- 通用控件（按钮、复选框、滑块、输入框、弹窗、底部面板、菜单、设置分组）改为 Apple 风格
- 底部迷你播放条改为悬浮圆角卡片
- 主界面顶部栏改为大标题样式
- 播放详情页：模糊封面背景、大圆角封面、灰白进度条
- 包名改为 `cn.toside.music.mobile.apple`，应用名改为 `LX Music Apple`，可与官方版同时安装
- 新增 `.github/workflows/apple-build.yml` 云端打包流程（仅手动触发，类型检查通过才打包，打包后发布到 GitHub Releases）
- 上游自带的工作流（`release.yml`、`beta-pack.yml`、`build-test.yml`、`publish-version-info.yml`）保留不删，但加了 `if: github.repository == 'lyswhut/lx-music-mobile'`，在 fork 中自动跳过

功能逻辑（`src/core`、`src/store`）未做修改。

## 云端打包（GitHub Actions）

### 如何触发

打包只能手动触发，push 不会自动打包：

1. 打开仓库的 **Actions** 页 → 左侧选择 **Build Apple Style APK**
2. 点 **Run workflow**，选择要打包的分支（如 `apple-style`），按需填写参数：
   - **version**：版本号，如 `1.9.2`；留空则使用 `package.json` 里的 `version`。只影响本次构建的 versionName 和文件名，不会提交回仓库
   - **release**：打包成功后是否创建 GitHub Release（默认是）
   - **prerelease**：Release 是否标记为预发布（默认否）
   - **run_lint**：是否同时执行 ESLint 门禁（默认否，上游代码当前 lint 未通过）
3. 点绿色的 **Run workflow** 开始

### 流程

1. **Type check**：`npm ci` 后执行 `npx tsc --noEmit -p .`（勾选 run_lint 时再执行 `npm run lint`），失败则整个流程终止，不打包
2. **Build APK**：Node 版本取 `.nvmrc`，Java 17，带 npm 和 Gradle 缓存，执行 `assembleRelease`，生成 arm64-v8a / armeabi-v7a / x86 / x86_64 / universal 五个 APK，文件名为 `lx-music-apple-v<版本号>-<架构>.apk`，同时作为 workflow artifact 上传（附 md5）
3. **Release**：创建 tag `apple-v<版本号>`、标题 `LX Music Apple v<版本号>` 的 Release，直接上传各个 `.apk` 文件，说明里附 MD5 和签名方式。同一版本号再次打包会更新已有 Release 的附件

一般手机下载 `arm64-v8a`，不确定就下载 `universal`。

### 需要配置的 Secrets（可选）

在仓库 **Settings → Secrets and variables → Actions → New repository secret** 中添加：

| Secret | 说明 |
| --- | --- |
| `KEYSTORE_STORE_FILE_BASE64` | 签名文件（.jks / .keystore）的 base64，例如 `base64 -w0 my.keystore` |
| `KEYSTORE_KEY_ALIAS` | 密钥别名 |
| `KEYSTORE_PASSWORD` | 签名文件密码 |
| `KEYSTORE_KEY_PASSWORD` | 密钥密码 |

- 四个都不配置：使用仓库自带的公开 `debug.keystore` 签名，Release 说明里会注明“仅供个人使用”。
- 只配置了 `KEYSTORE_STORE_FILE_BASE64` 而其余不全：流程报错终止。
- 从 debug 签名换成自有签名后，手机上需要先卸载旧版才能安装（签名不同）。

### 仓库设置

- fork 默认可能禁用 Actions：在 **Actions** 页点 “I understand my workflows, go ahead and enable them”。
- 创建 Release 需要写权限：工作流里已为 Release 这一步单独声明 `contents: write`，一般无需改设置；如果组织/仓库把 **Settings → Actions → General → Workflow permissions** 锁成只读且不允许提升，请改为 **Read and write permissions**。
