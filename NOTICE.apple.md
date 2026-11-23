# 修改说明

本项目基于 [lyswhut/lx-music-mobile](https://github.com/lyswhut/lx-music-mobile)（Apache License 2.0）修改，原版权与许可证保持不变，见 LICENSE。

本分支（apple-style）所做的修改：

- 新增 `src/theme/apple.ts`：Apple Music 风格配色、圆角、字号规范
- 通用控件（按钮、复选框、滑块、输入框、弹窗、底部面板、菜单、设置分组）改为 Apple 风格
- 底部迷你播放条改为悬浮圆角卡片
- 主界面顶部栏改为大标题样式
- 播放详情页：模糊封面背景、大圆角封面、灰白进度条
- 包名改为 `cn.toside.music.mobile.apple`，应用名改为 `LX Music Apple`，可与官方版同时安装
- 新增 `.github/workflows/apple-build.yml` 云端打包流程

功能逻辑（`src/core`、`src/store`）未做修改。
