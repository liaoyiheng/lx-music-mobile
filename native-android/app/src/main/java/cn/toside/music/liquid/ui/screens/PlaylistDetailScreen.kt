package cn.toside.music.liquid.ui.screens

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.ui.DetailRoute
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import coil.compose.SubcomposeAsyncImage

@Composable
fun PlaylistDetailScreen(
    vm: MainViewModel,
    route: DetailRoute,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val playerState by vm.player.state.collectAsState()
    val settings by vm.settings.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val onlineDetail by vm.onlineDetail.collectAsState()

    val localPlaylistId = (route as? DetailRoute.LocalPlaylist)?.playlistId
    val localTracks by remember(localPlaylistId) {
        if (localPlaylistId != null) {
            vm.repo.observePlaylistTracks(localPlaylistId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val localPlaylist = remember(playlists, localPlaylistId) {
        localPlaylistId?.let { id -> playlists.firstOrNull { it.id == id } }
    }

    val isOnline = route !is DetailRoute.LocalPlaylist
    val isLeaderboard = route is DetailRoute.Leaderboard

    val tracks: List<MusicInfo> = if (isOnline) onlineDetail.tracks else localTracks

    val title: String = when (route) {
        is DetailRoute.LocalPlaylist -> localPlaylist?.name ?: when (route.playlistId) {
            PlaylistIds.DEFAULT -> "试听列表"
            PlaylistIds.LOVE -> "我的收藏"
            else -> "歌单详情"
        }
        is DetailRoute.OnlineSongList -> onlineDetail.title.ifBlank { route.item.name }
        is DetailRoute.Leaderboard -> onlineDetail.title.ifBlank { route.item.name }
    }

    val coverUrl: String? = when (route) {
        is DetailRoute.LocalPlaylist -> {
            localPlaylist?.coverUrl?.takeIf { it.isNotBlank() }
                ?: localTracks.firstNotNullOfOrNull { it.picUrl }
        }
        is DetailRoute.OnlineSongList -> {
            onlineDetail.coverUrl?.takeIf { it.isNotBlank() }
                ?: route.item.img?.takeIf { it.isNotBlank() }
                ?: onlineDetail.tracks.firstNotNullOfOrNull { it.picUrl }
        }
        is DetailRoute.Leaderboard -> {
            onlineDetail.coverUrl?.takeIf { it.isNotBlank() }
                ?: route.item.coverImg?.takeIf { it.isNotBlank() }
                ?: onlineDetail.tracks.firstNotNullOfOrNull { it.picUrl }
        }
    }

    val eyebrow: String = when (route) {
        is DetailRoute.LocalPlaylist -> {
            val collectedSource = localPlaylist?.source?.takeIf { it.isNotBlank() }
            when {
                route.playlistId == PlaylistIds.DEFAULT -> "LX COLLECTION · QUEUE"
                route.playlistId == PlaylistIds.LOVE -> "LX COLLECTION · FAVORITES"
                collectedSource != null -> "COLLECTED · ${MusicSources.displayName(collectedSource)}"
                else -> "PERSONAL PLAYLIST"
            }
        }
        is DetailRoute.OnlineSongList ->
            "CURATED PLAYLIST · ${MusicSources.displayName(onlineDetail.source.ifBlank { route.item.source })}"
        is DetailRoute.Leaderboard ->
            "OFFICIAL CHART · ${MusicSources.displayName(onlineDetail.source.ifBlank { route.item.source })}"
    }

    val subtitle: String = when (route) {
        is DetailRoute.LocalPlaylist -> buildList {
            localPlaylist?.source?.takeIf { it.isNotBlank() }?.let {
                add("来自 ${MusicSources.displayName(it)}")
            }
            add("${tracks.size} 首歌曲")
        }.joinToString(" · ")
        is DetailRoute.OnlineSongList -> buildList {
            val author = onlineDetail.author.ifBlank { route.item.author }
            if (author.isNotBlank()) add(author)
            val count = maxOf(onlineDetail.total, tracks.size, route.item.total ?: 0)
            if (count > 0) add("$count 首")
            val playCount = onlineDetail.playCount?.takeIf { it.isNotBlank() }
                ?: route.item.playCount?.takeIf { it.isNotBlank() }
            if (playCount != null) add("$playCount 播放")
        }.joinToString(" · ")
        is DetailRoute.Leaderboard -> buildList {
            val author = onlineDetail.author.ifBlank {
                "${MusicSources.displayName(route.item.source)}官方榜单"
            }
            add(author)
            val count = maxOf(onlineDetail.total, tracks.size)
            if (count > 0) add("$count 首")
            val playCount = onlineDetail.playCount?.takeIf { it.isNotBlank() }
            if (playCount != null) add("$playCount 播放")
        }.joinToString(" · ")
    }

    val description: String? = when (route) {
        is DetailRoute.LocalPlaylist -> null
        is DetailRoute.OnlineSongList -> onlineDetail.description?.takeIf { it.isNotBlank() }
            ?: route.item.desc?.takeIf { it.isNotBlank() }
        is DetailRoute.Leaderboard -> onlineDetail.description?.takeIf { it.isNotBlank() }
    }

    val isCustomLocalPlaylist = route is DetailRoute.LocalPlaylist &&
        localPlaylist != null &&
        !localPlaylist.isBuiltIn &&
        route.playlistId != PlaylistIds.DEFAULT &&
        route.playlistId != PlaylistIds.LOVE

    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scrolledPastHero by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 440
        }
    }
    val topBarReveal by animateFloatAsState(
        targetValue = if (scrolledPastHero) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "detail_topbar_reveal",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 170.dp),
        ) {
            item(key = "editorial_hero") {
                MagazineHeroHeader(
                    title = title,
                    eyebrow = eyebrow,
                    subtitle = subtitle,
                    coverUrl = coverUrl,
                    description = description,
                    hasTracks = tracks.isNotEmpty(),
                    onPlayAll = {
                        if (tracks.isNotEmpty()) {
                            vm.player.playList(tracks, 0, title, shuffle = false)
                        }
                    },
                    onShuffleAll = {
                        if (tracks.isNotEmpty()) {
                            vm.player.playList(tracks, 0, title, shuffle = true)
                        }
                    },
                )
            }

            if (isOnline && onlineDetail.loading && tracks.isEmpty()) {
                item(key = "loading_initial") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(
                            color = colors.accent,
                            strokeWidth = 2.6.dp,
                            modifier = Modifier.size(30.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isLeaderboard) "正在同步官方榜单..." else "正在加载精选曲目...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            } else if (isOnline && onlineDetail.error != null && tracks.isEmpty()) {
                item(key = "error_retry") {
                    DetailErrorCard(
                        message = onlineDetail.error ?: "加载失败",
                        onRetry = {
                            when (route) {
                                is DetailRoute.OnlineSongList -> vm.openOnlineSongList(route.item)
                                is DetailRoute.Leaderboard -> vm.openLeaderboard(route.item)
                                else -> Unit
                            }
                        },
                    )
                }
            } else if (tracks.isEmpty()) {
                item(key = "empty_tracks") {
                    DetailEmptyCard(isOnline = isOnline)
                }
            } else {
                item(key = "tracks_section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (isLeaderboard) "榜单排行" else "曲目列表",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textTertiary,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = "${tracks.size} 首",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textTertiary,
                        )
                    }
                }

                itemsIndexed(
                    items = tracks,
                    key = { idx, track -> "${track.id}_$idx" },
                ) { idx, track ->
                    if (isOnline && idx >= tracks.lastIndex - 3 && onlineDetail.hasMore && !onlineDetail.loadingMore) {
                        LaunchedEffect(tracks.size) {
                            vm.loadMoreOnlineDetail()
                        }
                    }

                    val isCurrent = playerState.currentTrack?.id == track.id
                    val onPlayTrack = {
                        vm.player.playList(tracks, idx, title)
                    }

                    if (isLeaderboard) {
                        LeaderboardTrackRow(
                            rank = idx + 1,
                            track = track,
                            isCurrent = isCurrent,
                            isPlaying = playerState.isPlaying,
                            showAlbum = settings.showAlbumName,
                            showDuration = settings.showTrackDuration,
                            onClick = onPlayTrack,
                            onMoreClick = { vm.openTrackActions(track) },
                        )
                    } else {
                        TrackRow(
                            music = track,
                            index = idx,
                            isCurrent = isCurrent,
                            isPlaying = playerState.isPlaying,
                            showAlbum = settings.showAlbumName,
                            showDuration = settings.showTrackDuration,
                            onClick = onPlayTrack,
                            onMoreClick = { vm.openTrackActions(track) },
                        )
                    }

                    if (idx < tracks.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(
                                start = if (isLeaderboard) 106.dp else 76.dp,
                                end = 16.dp,
                            ),
                        )
                    }
                }

                if (isOnline && onlineDetail.loadingMore) {
                    item(key = "loading_more") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 22.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.accent,
                                strokeWidth = 2.2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "加载更多歌曲...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }
                    }
                } else if (isOnline && onlineDetail.error != null) {
                    item(key = "load_more_error") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(onClick = { vm.loadMoreOnlineDetail() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "加载更多失败，点击重试",
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Floating Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        ) {
            if (topBarReveal > 0.02f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = topBarReveal }
                        .background(
                            Brush.verticalGradient(
                                0.0f to colors.background.copy(alpha = 0.94f),
                                0.78f to colors.background.copy(alpha = 0.86f),
                                1.0f to Color.Transparent,
                            )
                        ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCircleIconButton(onClick = { vm.closeDetail() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(21.dp),
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .graphicsLayer { alpha = topBarReveal },
                )

                when {
                    isOnline -> {
                        val canCollect = tracks.isNotEmpty()
                        LiquidGlassSurface(
                            shape = CircleShape,
                            dense = true,
                            modifier = Modifier
                                .height(40.dp)
                                .graphicsLayer { alpha = if (canCollect) 1f else 0.55f }
                                .bouncyClick(pressedScale = 0.92f, enabled = canCollect) {
                                    vm.collectCurrentOnlineList()
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdd,
                                    contentDescription = "收藏",
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "收藏",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }

                    isCustomLocalPlaylist -> {
                        Box {
                            GlassCircleIconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = "歌单操作",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("重命名歌单") },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showRenameDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("删除歌单", color = colors.destructive) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = null,
                                            tint = colors.destructive,
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteConfirm = true
                                    },
                                )
                            }
                        }
                    }

                    else -> {
                        Spacer(Modifier.size(40.dp))
                    }
                }
            }
        }
    }

    if (showRenameDialog && localPlaylist != null) {
        var nameInput by remember(localPlaylist.id, localPlaylist.name) {
            mutableStateOf(localPlaylist.name)
        }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("重命名歌单", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("输入新的歌单名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            vm.renamePlaylist(localPlaylist.id, trimmed)
                            showRenameDialog = false
                        }
                    },
                ) {
                    Text("保存", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (showDeleteConfirm && localPlaylist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除歌单", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "确定要删除歌单「${localPlaylist.name}」吗？歌单内的曲目列表将被移除，此操作不可撤销。",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        vm.deletePlaylist(localPlaylist.id)
                    },
                ) {
                    Text("删除", color = colors.destructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun MagazineHeroHeader(
    title: String,
    eyebrow: String,
    subtitle: String,
    coverUrl: String?,
    description: String?,
    hasTracks: Boolean,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    var descExpanded by remember(title, description) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Soft blurred cover color-bleed backdrop fading seamlessly into page background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp),
        ) {
            if (!coverUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f
                            alpha = if (colors.isDark) 0.50f else 0.36f
                        }
                        .let { mod ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                mod.blur(54.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            } else {
                                mod
                            }
                        },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.accent.copy(alpha = if (colors.isDark) 0.34f else 0.18f),
                                    Color.Transparent,
                                ),
                            )
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to colors.background.copy(alpha = 0.14f),
                            0.42f to colors.background.copy(alpha = 0.52f),
                            0.78f to colors.background.copy(alpha = 0.90f),
                            1.0f to colors.background,
                        )
                    ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 66.dp, start = 20.dp, end = 20.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 220.dp elevated magazine cover with soft colored shadow and glass rim
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.glassShadow.copy(alpha = if (colors.isDark) 0.62f else 0.26f),
                            topLeft = Offset(10f, 24f),
                            size = size.copy(width = size.width - 20f, height = size.height - 10f),
                            cornerRadius = CornerRadius(26.dp.toPx()),
                        )
                    },
            ) {
                if (!coverUrl.isNullOrBlank()) {
                    CoverArt(
                        url = coverUrl,
                        cornerRadius = 22.dp,
                        placeholderSeed = title,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    GlassPlaceholderCover(
                        seed = title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (colors.isDark) 0.24f else 0.55f),
                                    Color.Transparent,
                                )
                            ),
                            shape = RoundedCornerShape(22.dp),
                        ),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                letterSpacing = 1.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!description.isNullOrBlank()) {
                val cleanDesc = description.trim()
                val interactionSource = remember { MutableInteractionSource() }
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { descExpanded = !descExpanded }
                        .animateContentSize()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = cleanDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center,
                        maxLines = if (descExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (cleanDesc.length > 36 || cleanDesc.contains('\n')) {
                        Spacer(Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = if (descExpanded) "收起简介" else "展开简介",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent,
                            )
                            Icon(
                                imageVector = if (descExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Dual equal-width Apple Red Liquid Glass action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = if (hasTracks) 1f else 0.5f },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.accent,
                                    colors.accent.copy(alpha = 0.88f),
                                )
                            )
                        )
                        .border(
                            width = 0.9.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.42f),
                                    Color.White.copy(alpha = 0.08f),
                                )
                            ),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .bouncyClick(enabled = hasTracks, onClick = onPlayAll),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = "播放全部",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                        )
                    }
                }

                LiquidGlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    dense = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .bouncyClick(enabled = hasTracks, onClick = onShuffleAll),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "随机播放",
                            color = colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassPlaceholderCover(
    seed: String,
    modifier: Modifier = Modifier,
) {
    val palette = remember(seed) {
        val options = listOf(
            listOf(Color(0xFFFA243C), Color(0xFFFF6B81), Color(0xFFFF9A8B)),
            listOf(Color(0xFF5E5CE6), Color(0xFF8B5CF6), Color(0xFF64D2FF)),
            listOf(Color(0xFF0A84FF), Color(0xFF5AC8FA), Color(0xFF66D4CF)),
            listOf(Color(0xFFFF9F0A), Color(0xFFFF5E3A), Color(0xFFFF375F)),
        )
        options[(seed.hashCode() and 0x7fffffff) % options.size]
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            darkOverride = false,
            modifier = Modifier.size(76.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardTrackRow(
    rank: Int,
    track: MusicInfo,
    isCurrent: Boolean,
    isPlaying: Boolean,
    showAlbum: Boolean,
    showDuration: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    val rankColor = when (rank) {
        1 -> colors.accent
        2 -> Color(0xFFFF9F0A)
        3 -> Color(0xFFFFB340)
        else -> colors.textTertiary
    }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(30.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank.toString(),
                fontSize = if (rank <= 3) 16.sp else 13.5.sp,
                fontWeight = if (rank <= 3) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isCurrent) colors.accent else rankColor,
                maxLines = 1,
            )
        }

        TrackRow(
            music = track,
            index = null,
            isCurrent = isCurrent,
            isPlaying = isPlaying,
            showAlbum = showAlbum,
            showDuration = showDuration,
            onClick = onClick,
            onMoreClick = onMoreClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DetailErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "加载内容失败",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accent)
                    .bouncyClick(onClick = onRetry)
                    .padding(horizontal = 22.dp, vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "重新加载",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailEmptyCard(isOnline: Boolean) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isOnline) "暂无可展示曲目" else "歌单还是空的",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isOnline) "请稍后刷新或切换其他歌单试试" else "去「发现」或「搜索」添加喜欢的歌曲吧",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
