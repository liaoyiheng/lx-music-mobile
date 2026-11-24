package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.data.model.UserPlaylist
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.EditorialHeroHeader
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.MainTab
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

@Composable
fun LibraryScreen(
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val playlists by vm.playlists.collectAsState()
    val apiStatus by vm.userApiStatus.collectAsState()

    val defaultList = playlists.firstOrNull { it.id == PlaylistIds.DEFAULT }
    val loveList = playlists.firstOrNull { it.id == PlaylistIds.LOVE }
    val customLists = playlists.filter { !it.isBuiltIn }

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<UserPlaylist?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = bottomClearanceDp.dp,
        ),
    ) {
        // Editorial Masthead
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "YOUR COLLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    letterSpacing = 1.6.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "资料库",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )

                    // Source Status Glass Pill
                    LiquidGlassSurface(
                        shape = CircleShape,
                        modifier = Modifier
                            .height(34.dp)
                            .bouncyClick { vm.selectTab(MainTab.SETTINGS) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = if (apiStatus.initialized) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = if (apiStatus.initialized) colors.success else colors.accent,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = if (apiStatus.initialized) {
                                    apiStatus.activeScriptName?.take(10) ?: "音源就绪"
                                } else {
                                    "导入音源"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Magazine Featured Cards: 试听列表 & 我的收藏
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LibraryFeaturePosterCard(
                    title = "试听列表",
                    eyebrow = "NOW LISTENING",
                    count = defaultList?.trackCount ?: 0,
                    coverUrl = defaultList?.coverUrl,
                    icon = Icons.Rounded.Headphones,
                    gradient = listOf(Color(0xFFFA243C), Color(0xFFFF6E7F)),
                    onClick = { vm.openLocalPlaylist(PlaylistIds.DEFAULT) },
                    modifier = Modifier.weight(1f),
                )
                LibraryFeaturePosterCard(
                    title = "我的收藏",
                    eyebrow = "FAVORITES",
                    count = loveList?.trackCount ?: 0,
                    coverUrl = loveList?.coverUrl,
                    icon = Icons.Rounded.Favorite,
                    gradient = listOf(Color(0xFF5E5CE6), Color(0xFFBF5AF2)),
                    onClick = { vm.openLocalPlaylist(PlaylistIds.LOVE) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Section Header: 我的歌单
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "PLAYLISTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        letterSpacing = 1.4.sp,
                    )
                    Text(
                        text = "我的歌单",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.accentSoft)
                        .bouncyClick { showCreateDialog = true }
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "新建歌单",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                        )
                    }
                }
            }
        }

        if (customLists.isEmpty()) {
            item {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "打造你的专属音乐杂志",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "你可以新建自建歌单、在「发现」收藏在线歌单，或在「设置」一键导入原版洛雪 .lxmc 备份。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        } else {
            val rows = customLists.chunked(2)
            itemsIndexed(rows) { _, pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    pair.forEach { playlist ->
                        UserPlaylistCard(
                            playlist = playlist,
                            onClick = { vm.openLocalPlaylist(playlist.id) },
                            onRename = { renameTarget = playlist },
                            onDelete = { vm.deletePlaylist(playlist.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建歌单", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("输入歌单名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            vm.createPlaylist(nameInput.trim())
                            showCreateDialog = false
                        }
                    }
                ) { Text("创建", color = colors.accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("取消") }
            },
        )
    }

    renameTarget?.let { target ->
        var nameInput by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名歌单", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            vm.renamePlaylist(target.id, nameInput.trim())
                            renameTarget = null
                        }
                    }
                ) { Text("保存", color = colors.accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun LibraryFeaturePosterCard(
    title: String,
    eyebrow: String,
    count: Int,
    coverUrl: String?,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(152.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradient))
            .bouncyClick(pressedScale = 0.96f, onClick = onClick),
    ) {
        if (!coverUrl.isNullOrBlank()) {
            CoverArt(
                url = coverUrl,
                cornerRadius = 0.dp,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to gradient[0].copy(alpha = 0.42f),
                            0.65f to Color(0x990C0C14),
                            1.0f to Color(0xD80A0A12),
                        )
                    )
            )
        } else {
            // Editorial decorative emblem in top-right (never clipped awkwardly)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(54.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column {
                Text(
                    text = eyebrow,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.78f),
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                )
                Text(
                    text = "$count 首歌曲",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun UserPlaylistCard(
    playlist: UserPlaylist,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.bouncyClick(pressedScale = 0.965f, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            CoverArt(
                url = playlist.coverUrl,
                cornerRadius = 18.dp,
                placeholderSeed = playlist.id,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x78000000))
                        .bouncyClick(pressedScale = 0.88f) { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "歌单选项",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("重命名") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("删除歌单", color = colors.destructive) },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = colors.destructive) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "${playlist.trackCount} 首",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 14.5.sp,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun LocalPlaylistDetailScreen(
    playlistId: String,
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val playlists by vm.playlists.collectAsState()
    val playlist = playlists.firstOrNull { it.id == playlistId }
    val tracks by remember(playlistId) { vm.repo.observePlaylistTracks(playlistId) }
        .collectAsState(initial = emptyList())
    val playerState by vm.player.state.collectAsState()
    val settings by vm.settings.collectAsState()

    var filterText by remember { mutableStateOf("") }
    val filteredTracks = remember(tracks, filterText) {
        if (filterText.isBlank()) tracks
        else {
            val q = filterText.trim().lowercase()
            tracks.filter {
                it.name.lowercase().contains(q) ||
                    it.singer.lowercase().contains(q) ||
                    it.albumName.lowercase().contains(q)
            }
        }
    }

    val title = playlist?.name ?: when (playlistId) {
        PlaylistIds.DEFAULT -> "试听列表"
        PlaylistIds.LOVE -> "我的收藏"
        else -> "歌单详情"
    }
    val heroCover = playlist?.coverUrl ?: tracks.firstNotNullOfOrNull { it.picUrl }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomClearanceDp.dp),
        ) {
            item {
                EditorialHeroHeader(
                    title = title,
                    eyebrow = if (playlist?.isBuiltIn == true) "LX LIBRARY" else "PERSONAL PLAYLIST",
                    subtitle = "${tracks.size} 首歌曲",
                    coverUrl = heroCover,
                    onPlayAll = {
                        if (filteredTracks.isNotEmpty()) {
                            vm.player.playList(filteredTracks, 0, title, shuffle = false)
                        }
                    },
                    onShuffleAll = {
                        if (filteredTracks.isNotEmpty()) {
                            vm.player.playList(filteredTracks, 0, title, shuffle = true)
                        }
                    },
                )
            }

            if (tracks.size >= 6) {
                item {
                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        placeholder = { Text("在歌单内搜索歌曲或歌手...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = colors.textTertiary) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }

            if (filteredTracks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp, horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (tracks.isEmpty()) "歌单还是空的" else "未找到匹配歌曲",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "去「发现」或「搜索」添加喜欢的曲目吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textTertiary,
                        )
                    }
                }
            } else {
                itemsIndexed(filteredTracks, key = { _, t -> t.id }) { idx, track ->
                    TrackRow(
                        music = track,
                        index = idx,
                        isCurrent = playerState.currentTrack?.id == track.id,
                        isPlaying = playerState.isPlaying,
                        showAlbum = settings.showAlbumName,
                        showDuration = settings.showTrackDuration,
                        onClick = { vm.player.playList(filteredTracks, idx, title) },
                        onMoreClick = { vm.openTrackActions(track) },
                    )
                    if (idx < filteredTracks.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        )
                    }
                }
            }
        }

        // Floating Liquid Glass Back Button
        GlassCircleIconButton(
            onClick = { vm.closeDetail() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 10.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = colors.textPrimary,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}
