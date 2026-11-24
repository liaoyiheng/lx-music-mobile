package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.CommentItem
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.LiquidSegmentedControl
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSheetsHost(vm: MainViewModel) {
    val actionTrack by vm.actionTrack.collectAsState()
    val pickerTrack by vm.playlistPickerTrack.collectAsState()
    val commentsState by vm.comments.collectAsState()
    val alternatesTrack by vm.findAlternatesTrack.collectAsState()
    val showSleepTimer by vm.showSleepTimer.collectAsState()

    actionTrack?.let { track ->
        TrackActionSheet(vm = vm, track = track)
    }
    pickerTrack?.let { track ->
        PlaylistPickerSheet(vm = vm, track = track)
    }
    if (commentsState.music != null) {
        CommentsSheet(vm = vm)
    }
    alternatesTrack?.let { track ->
        AlternateSourcesSheet(vm = vm, track = track)
    }
    if (showSleepTimer) {
        SleepTimerSheet(vm = vm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackActionSheet(
    vm: MainViewModel,
    track: MusicInfo,
) {
    val colors = LocalLiquidColors.current
    val clipboard = LocalClipboardManager.current
    var isFav by remember(track.id) { mutableStateOf(false) }

    LaunchedEffect(track.id) {
        isFav = vm.repo.isFavorite(track.id)
    }

    ModalBottomSheet(
        onDismissRequest = { vm.closeTrackActions() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            // Header Track Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverArt(
                    url = track.picUrl,
                    contentDescription = track.name,
                    cornerRadius = 12.dp,
                    modifier = Modifier.size(52.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${track.singer.ifBlank { "未知艺人" }} · ${MusicSources.displayName(track.source)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            HorizontalDivider(color = colors.divider, thickness = 0.6.dp)
            Spacer(Modifier.height(8.dp))

            SheetActionRow(
                icon = Icons.Rounded.PlayArrow,
                label = "立即播放",
                tint = colors.accent,
                onClick = {
                    vm.closeTrackActions()
                    vm.player.playSingleTrack(track)
                },
            )
            SheetActionRow(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                label = "下一首播放",
                onClick = {
                    vm.player.enqueueNext(track)
                    vm.showToast("已加入下一首播放")
                    vm.closeTrackActions()
                },
            )
            SheetActionRow(
                icon = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (isFav) "取消喜爱" else "添加到我的喜爱",
                tint = if (isFav) colors.accent else colors.textPrimary,
                onClick = {
                    vm.toggleFavoriteTrack(track)
                    vm.closeTrackActions()
                },
            )
            SheetActionRow(
                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                label = "收藏到歌单",
                onClick = { vm.openPlaylistPicker(track) },
            )
            SheetActionRow(
                icon = Icons.Rounded.ChatBubbleOutline,
                label = "查看歌曲评论",
                onClick = { vm.openComments(track) },
            )
            SheetActionRow(
                icon = Icons.Rounded.SwapHoriz,
                label = "跨平台换源 / 查找其他版本",
                onClick = { vm.openAlternateSources(track) },
            )
            SheetActionRow(
                icon = Icons.Rounded.ContentCopy,
                label = "复制歌曲名与歌手",
                onClick = {
                    clipboard.setText(AnnotatedString("${track.name} - ${track.singer}"))
                    vm.showToast("已复制到剪贴板")
                    vm.closeTrackActions()
                },
            )
            SheetActionRow(
                icon = Icons.Rounded.Block,
                label = "不喜欢这首歌（以后自动跳过）",
                tint = colors.destructive,
                onClick = {
                    vm.addDislike(track)
                    vm.closeTrackActions()
                },
            )
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = LocalLiquidColors.current.textPrimary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .bouncyClick(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = tint,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistPickerSheet(
    vm: MainViewModel,
    track: MusicInfo,
) {
    val colors = LocalLiquidColors.current
    val playlists by vm.playlists.collectAsState()
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { vm.closePlaylistPicker() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "收藏到歌单",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Text(
                text = "${track.name} - ${track.singer}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))

            if (creating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("输入新歌单名称") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                vm.createPlaylist(newName, initialTrack = track)
                                vm.closePlaylistPicker()
                            }
                        }
                    ) {
                        Text("创建", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accentSoft)
                        .bouncyClick { creating = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "新建歌单并收藏",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accent,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(playlists, key = { it.id }) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .bouncyClick {
                                vm.addTrackToPlaylist(pl.id, track, pl.name)
                                vm.closePlaylistPicker()
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.chipBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!pl.coverUrl.isNullOrBlank()) {
                                CoverArt(
                                    url = pl.coverUrl,
                                    contentDescription = pl.name,
                                    cornerRadius = 10.dp,
                                    modifier = Modifier.size(42.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${pl.trackCount} 首歌曲",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsSheet(vm: MainViewModel) {
    val colors = LocalLiquidColors.current
    val state by vm.comments.collectAsState()
    val track = state.music ?: return

    ModalBottomSheet(
        onDismissRequest = { vm.closeComments() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "歌曲评论",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Text(
                text = "${track.name} · ${track.singer} (${state.total})",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))

            LiquidSegmentedControl(
                items = listOf("热门评论", "最新评论"),
                selectedIndex = state.tabIndex,
                onSelect = { vm.switchCommentsTab(it) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            if (state.loading && state.comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.4.dp)
                }
            } else if (state.comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error ?: "暂无评论",
                        color = colors.textSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(state.comments, key = { idx, c -> "${c.id}_$idx" }) { idx, c ->
                        if (idx >= state.comments.lastIndex - 2 && state.page < state.maxPage && !state.loading) {
                            LaunchedEffect(state.comments.size) {
                                vm.loadMoreComments()
                            }
                        }
                        CommentCard(comment = c)
                    }
                    if (state.loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = colors.accent,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: CommentItem) {
    val colors = LocalLiquidColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.chipBackground)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoverArt(
                url = comment.avatar,
                contentDescription = comment.userName,
                cornerRadius = 18.dp,
                modifier = Modifier.size(34.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = comment.userName.ifBlank { "匿名乐迷" },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (comment.timeStr.isNotBlank()) {
                    Text(
                        text = comment.timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }
            if (comment.likedCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${comment.likedCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
            color = colors.textPrimary,
        )
        if (comment.reply.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.background.copy(alpha = 0.55f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                comment.reply.take(3).forEach { r ->
                    Text(
                        text = "${r.userName}: ${r.text}",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlternateSourcesSheet(
    vm: MainViewModel,
    track: MusicInfo,
) {
    val colors = LocalLiquidColors.current
    val candidates by vm.alternatesList.collectAsState()
    val loading by vm.alternatesLoading.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { vm.closeAlternateSources() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "跨平台换源",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Text(
                text = "为《${track.name}》查找其他平台可播版本",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(14.dp))

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.4.dp)
                }
            } else if (candidates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("未在其他平台找到匹配版本", color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(candidates, key = { "${it.source}_${it.id}" }) { alt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.chipBackground)
                                .bouncyClick {
                                    vm.closeAlternateSources()
                                    vm.player.playSingleTrack(alt, "换源播放")
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = MusicSources.displayName(alt.source),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accent,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = alt.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${alt.singer} ${alt.interval ?: ""}".trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = alt.highestQualityBadge(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(vm: MainViewModel) {
    val colors = LocalLiquidColors.current
    val playerState by vm.player.state.collectAsState()
    val options = listOf(0, 15, 30, 45, 60, 90)

    ModalBottomSheet(
        onDismissRequest = { vm.closeSleepTimer() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "定时停止播放",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
            }
            if (playerState.sleepTimerRemainingSec > 0) {
                val mins = playerState.sleepTimerRemainingSec / 60
                val secs = playerState.sleepTimerRemainingSec % 60
                Text(
                    text = "当前倒计时：%02d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.accent,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(14.dp))

            options.forEach { min ->
                val label = if (min == 0) "关闭定时停止" else "$min 分钟后停止播放"
                val isSelected = (min == 0 && playerState.sleepTimerRemainingSec == 0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bouncyClick {
                            vm.player.setSleepTimerMinutes(min)
                            vm.showToast(if (min == 0) "已关闭定时停止" else "已设置 $min 分钟后停止播放")
                            vm.closeSleepTimer()
                        }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) colors.accent else colors.textPrimary,
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
