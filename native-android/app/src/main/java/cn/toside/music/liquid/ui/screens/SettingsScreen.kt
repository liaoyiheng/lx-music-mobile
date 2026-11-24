package cn.toside.music.liquid.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.BuildConfig
import cn.toside.music.liquid.data.model.AppLogEntry
import cn.toside.music.liquid.data.model.AudioQuality
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.SyncState
import cn.toside.music.liquid.data.model.ThemeMode
import cn.toside.music.liquid.data.model.UserApiScript
import cn.toside.music.liquid.data.model.UserApiStatus
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidSegmentedControl
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.lockscreen.LockScreenActivity
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val settings by vm.settings.collectAsState()
    val userApiScripts by vm.userApiScripts.collectAsState()
    val userApiStatus by vm.userApiStatus.collectAsState()
    val syncState by vm.syncState.collectAsState()
    val appLogs by vm.appLogs.collectAsState()
    val dislikeRules by vm.dislikeRules.collectAsState()

    var showUrlImportDialog by remember { mutableStateOf(false) }
    var showTextImportDialog by remember { mutableStateOf(false) }
    var deleteScriptTarget by remember { mutableStateOf<UserApiScript?>(null) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showDislikeDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }

    val importScriptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            vm.importUserApiFromUri(uri)
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            vm.exportBackup(uri)
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            vm.importBackup(uri)
        }
    }

    val orderedThemeModes = remember {
        listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
    }
    val themeLabels = remember {
        listOf("跟随系统", "浅色", "深色")
    }
    val orderedQualities = remember {
        listOf(AudioQuality.Q128, AudioQuality.Q320, AudioQuality.FLAC, AudioQuality.FLAC24)
    }
    val qualityLabels = remember {
        listOf("128k", "320k", "FLAC", "Hi-Res")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(top = 12.dp, bottom = 170.dp),
    ) {
        // 1. 顶部苹果风大标题 + 副标题
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "PREFERENCES & SOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    letterSpacing = 1.6.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "自定义音源管理、液态玻璃材质与多端同步",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }

        // 2. 自定义音源管理（置于顶部醒目位置）
        item {
            SettingsSectionTitle(
                title = "自定义音源管理",
                trailing = if (userApiScripts.isNotEmpty()) "${userApiScripts.size} 个脚本" else null,
            )
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // 状态指示胶囊
                    UserApiStatusCapsule(status = userApiStatus)

                    // 三个紧凑玻璃胶囊导入入口
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompactImportPill(
                            icon = Icons.Rounded.FolderOpen,
                            label = "导入文件",
                            onClick = {
                                importScriptLauncher.launch(
                                    arrayOf(
                                        "application/javascript",
                                        "text/javascript",
                                        "text/plain",
                                        "*/*",
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                        CompactImportPill(
                            icon = Icons.Rounded.CloudDownload,
                            label = "在线导入",
                            onClick = { showUrlImportDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                        CompactImportPill(
                            icon = Icons.Rounded.ContentPaste,
                            label = "粘贴脚本",
                            onClick = { showTextImportDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // 已导入音源列表
                    if (userApiScripts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.badgeBg)
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text = "暂未导入任何自定义音源脚本。支持标准洛雪音乐 .js 音源脚本，点击上方按钮即可导入并自动启用。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (colors.isDark) Color(0x28000000) else Color(0x08000000))
                                .border(0.6.dp, colors.divider, RoundedCornerShape(18.dp)),
                        ) {
                            userApiScripts.forEachIndexed { index, script ->
                                val isActive = if (userApiStatus.activeScriptId != null) {
                                    userApiStatus.activeScriptId == script.id
                                } else {
                                    settings.activeUserApiId == script.id || script.enabled
                                }
                                UserApiScriptRow(
                                    script = script,
                                    isActive = isActive,
                                    activeStatus = if (isActive) userApiStatus else null,
                                    onToggle = {
                                        vm.activateUserApi(if (isActive) null else script.id)
                                    },
                                    onDelete = { deleteScriptTarget = script },
                                )
                                if (index < userApiScripts.lastIndex) {
                                    HorizontalDivider(
                                        color = colors.divider,
                                        thickness = 0.6.dp,
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. 外观与液态玻璃材质
        item {
            SettingsSectionTitle(title = "外观与液态玻璃材质")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                SettingsIconBadge(icon = Icons.Rounded.Palette, tint = colors.accent)
                                Text(
                                    text = "主题模式",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                )
                            }
                            Text(
                                text = settings.themeMode.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary,
                            )
                        }

                        LiquidSegmentedControl(
                            items = themeLabels,
                            selectedIndex = orderedThemeModes.indexOf(settings.themeMode).coerceAtLeast(0),
                            onSelect = { idx ->
                                val nextMode = orderedThemeModes.getOrElse(idx) { ThemeMode.SYSTEM }
                                vm.updateSettings { it.copy(themeMode = nextMode) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    HorizontalDivider(color = colors.divider, thickness = 0.6.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "材质风格",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        MaterialStyle.entries.forEach { style ->
                            val selected = settings.materialStyle == style
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) colors.accentSoft else colors.badgeBg)
                                    .border(
                                        width = if (selected) 1.dp else 0.5.dp,
                                        color = if (selected) colors.accent.copy(alpha = 0.45f) else colors.divider,
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    .bouncyClick(pressedScale = 0.985f) {
                                        vm.updateSettings { it.copy(materialStyle = style) }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = style.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 14.5.sp,
                                        color = if (selected) colors.accent else colors.textPrimary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = style.desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(1.5.dp, colors.textTertiary.copy(alpha = 0.45f), CircleShape),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 播放与音质
        item {
            SettingsSectionTitle(title = "播放与音质")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                SettingsIconBadge(icon = Icons.Rounded.HighQuality, tint = Color(0xFF0A84FF))
                                Column {
                                    Text(
                                        text = "默认音质",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary,
                                    )
                                    Text(
                                        text = settings.preferredQuality.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = settings.preferredQuality.badge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accent,
                                )
                            }
                        }

                        LiquidSegmentedControl(
                            items = qualityLabels,
                            selectedIndex = orderedQualities.indexOf(settings.preferredQuality).coerceAtLeast(0),
                            onSelect = { idx ->
                                val quality = orderedQualities.getOrElse(idx) { AudioQuality.FLAC }
                                vm.updateSettings { it.copy(preferredQuality = quality) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.SwapHoriz,
                        iconTint = Color(0xFF30D158),
                        title = "播放失败自动换源",
                        subtitle = "当前音源不可用时自动匹配同曲目可用音源",
                        checked = settings.autoFallbackSource,
                        onCheckedChange = { checked ->
                            vm.updateSettings { it.copy(autoFallbackSource = checked) }
                        },
                    )

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.Search,
                        iconTint = Color(0xFFFF9F0A),
                        title = "搜索联想建议",
                        subtitle = "输入关键词时实时展示热门联想词条",
                        checked = settings.enableSearchTips,
                        onCheckedChange = { checked ->
                            vm.updateSettings { it.copy(enableSearchTips = checked) }
                        },
                    )

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.Subtitles,
                        iconTint = Color(0xFFBF5AF2),
                        title = "桌面悬浮歌词",
                        subtitle = if (settings.desktopLyric.enabled) "已在桌面顶层显示实时歌词" else "在桌面或其它应用上方显示悬浮歌词",
                        checked = settings.desktopLyric.enabled,
                        onCheckedChange = { checked ->
                            if (checked && !Settings.canDrawOverlays(context)) {
                                showOverlayPermissionDialog = true
                            } else {
                                val nextDesktopLyric = settings.desktopLyric.copy(enabled = checked)
                                vm.updateSettings { it.copy(desktopLyric = nextDesktopLyric) }
                                vm.app.desktopLyricManager.applyConfig(nextDesktopLyric)
                            }
                        },
                    )

                    AnimatedVisibility(visible = settings.desktopLyric.enabled) {
                        Column {
                            HorizontalDivider(
                                color = colors.divider,
                                thickness = 0.6.dp,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            SettingsSwitchRow(
                                icon = Icons.Rounded.Lock,
                                iconTint = colors.textSecondary,
                                title = "锁定悬浮歌词位置",
                                subtitle = "锁定后悬浮窗将穿透触摸事件，不再响应拖动",
                                checked = settings.desktopLyric.locked,
                                onCheckedChange = { locked ->
                                    val nextDesktopLyric = settings.desktopLyric.copy(locked = locked)
                                    vm.updateSettings { it.copy(desktopLyric = nextDesktopLyric) }
                                    vm.app.desktopLyricManager.applyConfig(nextDesktopLyric)
                                },
                            )
                        }
                    }

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.PhoneIphone,
                        iconTint = Color(0xFFFF375F),
                        title = "液态玻璃锁屏播放页",
                        subtitle = if (settings.lockScreenEnabled) {
                            "播放时息屏自动启用全屏液态玻璃锁屏（建议开启悬浮窗权限以确保稳定弹出）"
                        } else {
                            "关闭后仅保留系统锁屏媒体卡片控制"
                        },
                        checked = settings.lockScreenEnabled,
                        onCheckedChange = { checked ->
                            vm.updateSettings { it.copy(lockScreenEnabled = checked) }
                            if (checked && !Settings.canDrawOverlays(context)) {
                                vm.showToast("已开启锁屏页，授权悬浮窗权限可确保 Android 12+ 息屏稳定唤起")
                            }
                        },
                    )

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Preview,
                        iconTint = Color(0xFF64D2FF),
                        title = "预览锁屏播放页",
                        subtitle = "无需锁屏，立即体验液态玻璃锁屏时钟、封面与实时歌词交互",
                        onClick = {
                            runCatching {
                                context.startActivity(LockScreenActivity.createPreviewIntent(context))
                            }.onFailure {
                                vm.showToast("无法打开锁屏预览: ${it.message}")
                            }
                        },
                    )

                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.NotificationsActive,
                        iconTint = Color(0xFF5E5CE6),
                        title = "通知栏显示实时歌词",
                        subtitle = "将系统通知栏与锁屏媒体卡片副标题实时同步为当前歌词",
                        checked = settings.notificationShowLyric,
                        onCheckedChange = { checked ->
                            vm.updateSettings { it.copy(notificationShowLyric = checked) }
                        },
                    )
                }
            }
        }

        // 5. 数据同步（LX Sync Server）
        item {
            SettingsSectionTitle(title = "数据同步")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SyncStatusHeader(syncState = syncState, savedServerUrl = settings.syncServerUrl)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.accent)
                                .bouncyClick { showSyncDialog = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "配置并连接",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 13.5.sp,
                                color = Color.White,
                            )
                        }

                        val canDisconnect = syncState.connected || syncState.connecting || settings.syncEnabled
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (canDisconnect) colors.badgeBg else colors.badgeBg.copy(alpha = 0.45f))
                                .border(0.6.dp, colors.divider, RoundedCornerShape(14.dp))
                                .bouncyClick(enabled = canDisconnect) { vm.disconnectSync() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "断开连接",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 13.5.sp,
                                color = if (canDisconnect) colors.destructive else colors.textTertiary,
                            )
                        }
                    }
                }
            }
        }

        // 6. 备份与恢复 / 不喜欢列表
        item {
            SettingsSectionTitle(title = "备份与恢复")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsActionRow(
                        icon = Icons.Rounded.FileUpload,
                        iconTint = Color(0xFF0A84FF),
                        title = "导出歌单备份",
                        subtitle = "保存全部歌单、收藏与不喜欢规则 (.lxmc)",
                        onClick = { exportBackupLauncher.launch("lx-music-backup.lxmc") },
                    )
                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.FileDownload,
                        iconTint = Color(0xFF30D158),
                        title = "导入歌单备份",
                        subtitle = "从本地 .lxmc 或 JSON 备份文件恢复歌单",
                        onClick = { importBackupLauncher.launch(arrayOf("*/*")) },
                    )
                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Block,
                        iconTint = colors.destructive,
                        title = "不喜欢列表规则",
                        subtitle = "已配置 ${dislikeRules.size} 条过滤规则，每行一条",
                        onClick = { showDislikeDialog = true },
                    )
                }
            }
        }

        // 7. 存储与运行日志
        item {
            SettingsSectionTitle(title = "存储与诊断")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsActionRow(
                        icon = Icons.Rounded.CleaningServices,
                        iconTint = Color(0xFFFF9F0A),
                        title = "一键清空缓存",
                        subtitle = "清理本地歌曲 URL、歌词与图片缓存",
                        onClick = { vm.clearCaches() },
                    )
                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Terminal,
                        iconTint = Color(0xFF5E5CE6),
                        title = "查看运行日志",
                        subtitle = "共 ${appLogs.size} 条记录 · 支持一键复制",
                        onClick = { showLogsDialog = true },
                    )
                }
            }
        }

        // 8. 关于卡片
        item {
            SettingsSectionTitle(title = "关于")
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                dense = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.accent, Color(0xFFFF6B81))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "洛雪音乐 · Liquid Glass Native Edition",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.5.sp,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Kotlin Native · Jetpack Compose · QuickJS · Media3",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = colors.textTertiary,
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    // ---- 弹窗区 ----

    if (showUrlImportDialog) {
        var urlInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showUrlImportDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "在线导入自定义音源",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "输入洛雪自定义音源脚本 (.js) 的直链 URL，导入后将自动启用。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("脚本 URL") },
                        placeholder = { Text("https://example.com/lx-source.js") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = urlInput.trim()
                        if (trimmed.isNotEmpty()) {
                            vm.importUserApiFromUrl(trimmed)
                            showUrlImportDialog = false
                        } else {
                            vm.showToast("请输入有效的脚本 URL")
                        }
                    },
                ) {
                    Text("导入并启用", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlImportDialog = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    if (showTextImportDialog) {
        var scriptName by remember { mutableStateOf("") }
        var scriptCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTextImportDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "粘贴音源脚本代码",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = scriptName,
                        onValueChange = { scriptName = it },
                        label = { Text("脚本名称（可选）") },
                        placeholder = { Text("例如：我的自定义音源") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = scriptCode,
                        onValueChange = { scriptCode = it },
                        label = { Text("JavaScript 脚本内容") },
                        placeholder = { Text("// @name ...\nconst { EVENT_NAMES, on, send } = globalThis.lx") },
                        minLines = 6,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (scriptCode.isNotBlank()) {
                            vm.importUserApiFromText(
                                rawScript = scriptCode,
                                fallbackName = scriptName.trim().ifBlank { "自定义音源" },
                            )
                            showTextImportDialog = false
                        } else {
                            vm.showToast("请先粘贴脚本代码")
                        }
                    },
                ) {
                    Text("导入并启用", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextImportDialog = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    deleteScriptTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteScriptTarget = null },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "删除自定义音源",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = "确认删除音源脚本「${target.name}」吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteUserApi(target.id)
                        deleteScriptTarget = null
                    },
                ) {
                    Text("删除", color = colors.destructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteScriptTarget = null }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "需要悬浮窗权限",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = "开启桌面悬浮歌词需要系统「显示在其他应用上层」权限。请前往系统设置授权后返回再打开开关。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                            .onFailure { vm.showToast("无法打开悬浮窗权限设置页") }
                    },
                ) {
                    Text("前往授权", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    if (showSyncDialog) {
        var serverUrl by remember(settings.syncServerUrl) { mutableStateOf(settings.syncServerUrl) }
        var authCode by remember(settings.syncAuthCode) { mutableStateOf(settings.syncAuthCode) }
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "配置 LX Sync Server",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "连接桌面端或自托管的洛雪同步服务端（LX Sync v4）。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("服务器地址") },
                        placeholder = { Text("http://192.168.1.100:9527") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = authCode,
                        onValueChange = { authCode = it },
                        label = { Text("认证码（首次连接必填）") },
                        placeholder = { Text("请输入同步认证码") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanUrl = serverUrl.trim()
                        if (cleanUrl.isNotEmpty()) {
                            vm.connectSync(cleanUrl, authCode.trim())
                            showSyncDialog = false
                        } else {
                            vm.showToast("请填写同步服务器地址")
                        }
                    },
                ) {
                    Text("连接", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    if (showDislikeDialog) {
        var rulesText by remember(dislikeRules) {
            mutableStateOf(dislikeRules.joinToString("\n"))
        }
        AlertDialog(
            onDismissRequest = { showDislikeDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = "不喜欢列表规则",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "每行一条过滤规则，支持歌名、歌手或「歌名@歌手」格式，匹配的曲目将自动跳过或屏蔽。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                    OutlinedTextField(
                        value = rulesText,
                        onValueChange = { rulesText = it },
                        label = { Text("规则列表") },
                        placeholder = { Text("例如：\n某首不想听的歌\n歌名@歌手") },
                        minLines = 6,
                        maxLines = 12,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.saveDislikeRules(rulesText)
                        showDislikeDialog = false
                    },
                ) {
                    Text("保存规则", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDislikeDialog = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            },
        )
    }

    if (showLogsDialog) {
        val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showLogsDialog = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(26.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "运行日志 (${appLogs.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                    )
                    Row {
                        IconButton(
                            onClick = {
                                val text = appLogs.joinToString("\n") { entry ->
                                    "[${timeFormatter.format(Date(entry.timestamp))}] [${entry.level}/${entry.tag}] ${entry.message}"
                                }
                                clipboard.setText(AnnotatedString(text))
                                vm.showToast("已复制 ${appLogs.size} 条日志到剪贴板")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "复制日志",
                                tint = colors.accent,
                            )
                        }
                        IconButton(
                            onClick = {
                                AppLogger.clear()
                                vm.showToast("已清空运行日志")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "清空日志",
                                tint = colors.textSecondary,
                            )
                        }
                    }
                }
            },
            text = {
                if (appLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无运行日志",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textTertiary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(appLogs.asReversed()) { entry ->
                            LogEntryItem(entry = entry, formatter = timeFormatter)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val text = appLogs.joinToString("\n") { entry ->
                            "[${timeFormatter.format(Date(entry.timestamp))}] [${entry.level}/${entry.tag}] ${entry.message}"
                        }
                        clipboard.setText(AnnotatedString(text))
                        vm.showToast("已复制运行日志到剪贴板")
                    },
                ) {
                    Text("一键复制", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogsDialog = false }) {
                    Text("关闭", color = colors.textSecondary)
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    trailing: String? = null,
) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 13.sp,
            color = colors.textSecondary,
        )
        if (!trailing.isNullOrBlank()) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserApiStatusCapsule(status: UserApiStatus) {
    val colors = LocalLiquidColors.current
    val isNone = status.activeScriptId == null
    val isReady = status.initialized
    val isInitializing = !isNone && !isReady && (status.errorMessage == null || status.errorMessage == "正在初始化...")
    val isError = !isNone && !isReady && !isInitializing

    val badgeColor = when {
        isReady -> colors.success
        isInitializing -> Color(0xFFFF9F0A)
        isError -> colors.destructive
        else -> colors.textTertiary
    }
    val statusLabel = when {
        isReady -> "已就绪"
        isInitializing -> "初始化中"
        isError -> "异常"
        else -> "未启用"
    }
    val detailText = when {
        isReady -> "支持 ${status.supportedSources.size} 个平台 · 引擎运行正常"
        isInitializing -> status.errorMessage ?: "正在加载 QuickJS 运行环境..."
        isError -> status.errorMessage ?: "自定义音源初始化异常"
        else -> "请导入或启用任意自定义音源脚本 (.js)"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(badgeColor.copy(alpha = if (colors.isDark) 0.16f else 0.10f))
            .border(0.8.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isInitializing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = badgeColor,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = status.activeScriptName ?: "未挂载自定义音源",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.5.sp,
                    color = if (isError) colors.destructive else colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (isReady && status.supportedSources.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                status.supportedSources.forEach { (sourceId, qualities) ->
                    val qText = if (qualities.isNotEmpty()) " · ${qualities.last()}" else ""
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.78f))
                            .border(0.5.dp, colors.divider, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "${MusicSources.displayName(sourceId)}$qText",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactImportPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(44.dp)
            .bouncyClick(pressedScale = 0.94f, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.5.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserApiScriptRow(
    script: UserApiScript,
    isActive: Boolean,
    activeStatus: UserApiStatus?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.985f, onClick = onToggle)
            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isActive) colors.accentSoft else colors.badgeBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isActive) Icons.Rounded.Check else Icons.Rounded.Code,
                contentDescription = null,
                tint = if (isActive) colors.accent else colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = script.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    color = if (isActive) colors.accent else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (script.version.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.badgeBg)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = "v${script.version.removePrefix("v")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) colors.accentSoft else colors.badgeBg)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = if (isActive) "已启用" else "未启用",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) colors.accent else colors.textTertiary,
                    )
                }
            }

            val metaLine = buildList {
                if (script.author.isNotBlank()) add("作者: ${script.author}")
                if (script.description.isNotBlank()) add(script.description)
            }.joinToString(" · ")
            if (metaLine.isNotBlank()) {
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val sourceKeys = activeStatus?.supportedSources?.keys?.toList().orEmpty()
            if (sourceKeys.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    sourceKeys.forEach { src ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = MusicSources.displayName(src),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "删除音源",
                tint = colors.destructive.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SyncStatusHeader(
    syncState: SyncState,
    savedServerUrl: String,
) {
    val colors = LocalLiquidColors.current
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val tint = when {
        syncState.connected -> colors.success
        syncState.connecting -> Color(0xFFFF9F0A)
        else -> colors.textTertiary
    }
    val icon = when {
        syncState.connected -> Icons.Rounded.CloudDone
        syncState.connecting -> Icons.Rounded.Sync
        else -> Icons.Rounded.CloudOff
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBadge(icon = icon, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "LX Sync Server",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.16f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = when {
                            syncState.connected -> "已连接"
                            syncState.connecting -> "连接中"
                            else -> "未连接"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = syncState.statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.5.sp,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (savedServerUrl.isNotBlank() || syncState.lastSyncedAt > 0L) {
                val extra = buildList {
                    if (savedServerUrl.isNotBlank()) add(savedServerUrl)
                    if (syncState.lastSyncedAt > 0L) {
                        add("最近同步 ${timeFormatter.format(Date(syncState.lastSyncedAt))}")
                    }
                }.joinToString(" · ")
                Text(
                    text = extra,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.99f) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBadge(icon = icon, tint = iconTint)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = colors.textSecondary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.textTertiary.copy(alpha = 0.35f),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.988f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBadge(icon = icon, tint = iconTint)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun LogEntryItem(
    entry: AppLogEntry,
    formatter: SimpleDateFormat,
) {
    val colors = LocalLiquidColors.current
    val levelColor = when (entry.level) {
        "ERROR" -> colors.destructive
        "WARN" -> Color(0xFFFF9F0A)
        else -> colors.accent
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.badgeBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.level,
                style = MaterialTheme.typography.labelSmall,
                color = levelColor,
            )
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textPrimary,
            )
            Text(
                text = formatter.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
        Text(
            text = entry.message,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            color = colors.textSecondary,
        )
    }
}
