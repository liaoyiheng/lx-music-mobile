package cn.toside.music.liquid.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.ui.DetailRoute
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.adaptive.LiquidWidthSizeClass
import cn.toside.music.liquid.ui.adaptive.LocalDockScrollConnection
import cn.toside.music.liquid.ui.adaptive.LocalImmersiveTintReporter
import cn.toside.music.liquid.ui.adaptive.LocalLiquidWindowClass
import cn.toside.music.liquid.ui.adaptive.LocalTopBarClearance
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidPopoverItem
import cn.toside.music.liquid.ui.components.LiquidPopoverMenu
import cn.toside.music.liquid.ui.components.PlayingEqualizer
import cn.toside.music.liquid.ui.components.liquidPress
import cn.toside.music.liquid.ui.components.liquidRowPress
import cn.toside.music.liquid.ui.components.rememberCoverPalette
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

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

    var showMoreMenu by remember(route) { mutableStateOf(false) }
    var showRenameDialog by remember(route) { mutableStateOf(false) }
    var showDeleteConfirm by remember(route) { mutableStateOf(false) }
    var menuTrackIndex by remember(route) { mutableStateOf<Int?>(null) }

    val window = LocalLiquidWindowClass.current
    val topClearance = LocalTopBarClearance.current
    val dockScroll = LocalDockScrollConnection.current
    val reportTint = LocalImmersiveTintReporter.current
    val palette = rememberCoverPalette(coverUrl, colors.isDark)
    val pageColor = lerp(colors.background, palette.dominant, if (colors.isDark) 0.10f else 0.06f)
    val artColor = if (colors.isDark) palette.darkMuted else palette.dominant
    val topColor = lerp(pageColor, artColor, if (colors.isDark) 0.58f else 0.29f)
    val middleColor = lerp(pageColor, palette.darkVibrant, if (colors.isDark) 0.31f else 0.12f)
    SideEffect { reportTint(topColor) }
    DisposableEffect(reportTint) { onDispose { reportTint(null) } }

    val hasTracks = tracks.isNotEmpty()
    val onCollect: (() -> Unit)? = if (isOnline) ({ vm.collectCurrentOnlineList() }) else null
    val onPlayAll = { if (hasTracks) vm.player.playList(tracks, 0, title, shuffle = false) }
    val onShuffleAll = { if (hasTracks) vm.player.playList(tracks, 0, title, shuffle = true) }
    val retry: () -> Unit = {
        when (route) {
            is DetailRoute.OnlineSongList -> vm.openOnlineSongList(route.item)
            is DetailRoute.Leaderboard -> vm.openLeaderboard(route.item)
            is DetailRoute.LocalPlaylist -> Unit
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(pageColor),
    ) {
        // The navigation sidebar can leave substantially less width than the window class implies.
        val short = window.isCompactHeight || maxHeight < 480.dp
        val split = if (short) {
            maxWidth >= 610.dp && window.widthSizeClass != LiquidWidthSizeClass.Compact
        } else {
            window.prefersTwoPaneDetail && maxWidth >= 660.dp
        }
        val horizontal = !split && maxWidth >= 420.dp && (
            window.widthSizeClass != LiquidWidthSizeClass.Compact || short
        )
        val margin = when {
            maxWidth < 460.dp || short -> 16.dp
            horizontal || split -> window.horizontalMargin.coerceIn(20.dp, 32.dp)
            else -> 20.dp
        }
        val gap = if (short) 16.dp else 24.dp
        val leftWidth = if (short) {
            (maxWidth * 0.34f).coerceIn(212.dp, 244.dp)
        } else {
            (maxWidth * 0.37f).coerceIn(246.dp, 344.dp)
        }
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomClearance = navBottom + when {
            window.widthSizeClass == LiquidWidthSizeClass.Compact && playerState.currentTrack != null -> 144.dp
            window.widthSizeClass == LiquidWidthSizeClass.Compact -> 84.dp
            playerState.currentTrack != null -> if (short) 68.dp else 80.dp
            else -> 16.dp
        }
        val backdrop = Brush.verticalGradient(
            0f to topColor,
            0.36f to lerp(topColor, middleColor, 0.45f),
            0.68f to middleColor,
            1f to pageColor,
        )
        val listState = key(route, split) { rememberLazyListState() }
        val collapseDistance = with(LocalDensity.current) { if (horizontal) 196.dp.toPx() else 260.dp.toPx() }
        val reveal by remember(listState, split, collapseDistance) {
            derivedStateOf {
                when {
                    split || listState.firstVisibleItemIndex > 0 -> 1f
                    else -> (listState.firstVisibleItemScrollOffset / collapseDistance).coerceIn(0f, 1f)
                }
            }
        }
        val lastTrackIndex = tracks.size + if (split) 0 else 1
        val nearEnd by remember(listState, lastTrackIndex) {
            derivedStateOf {
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it >= lastTrackIndex - 2 } == true
            }
        }
        val canLoadMore = isOnline && hasTracks && onlineDetail.hasMore &&
            !onlineDetail.loading && !onlineDetail.loadingMore && onlineDetail.error == null
        LaunchedEffect(route, nearEnd, canLoadMore, onlineDetail.page) {
            if (nearEnd && canLoadMore) vm.loadMoreOnlineDetail()
        }

        val hero: @Composable (Modifier, Boolean) -> Unit = { heroModifier, horizontalHero ->
            DetailHero(
                title = title,
                eyebrow = eyebrow,
                subtitle = subtitle,
                coverUrl = coverUrl,
                description = description,
                horizontal = horizontalHero,
                short = short,
                split = split,
                hasTracks = hasTracks,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
                onCollect = onCollect,
                scrollOffset = {
                    if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 0
                },
                modifier = heroModifier,
            )
        }
        val scrollConnectionModifier = dockScroll?.let { Modifier.nestedScroll(it) } ?: Modifier
        val trackList: @Composable (Modifier, Boolean) -> Unit = { listModifier, includeHero ->
            BoxWithConstraints(listModifier) {
                val rowWidth = maxWidth - if (split) 0.dp else margin * 2
                val showAlbumColumn = settings.showAlbumName && rowWidth >= 500.dp && !short
                val showDurationColumn = settings.showTrackDuration && rowWidth >= 326.dp
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().then(scrollConnectionModifier),
                    contentPadding = PaddingValues(bottom = bottomClearance),
                ) {
                    if (includeHero) {
                        item(key = "detail_hero") {
                            Column(Modifier.fillMaxWidth().background(backdrop)) {
                                Spacer(Modifier.statusBarsPadding().height(topClearance + DetailNavHeight))
                                hero(Modifier.padding(horizontal = margin), horizontal)
                            }
                        }
                    }
                    detailTrackItems(
                        tracks = tracks,
                        horizontalPadding = if (split) 0.dp else margin,
                        isOnline = isOnline,
                        isLeaderboard = isLeaderboard,
                        loading = onlineDetail.loading,
                        loadingMore = onlineDetail.loadingMore,
                        error = onlineDetail.error,
                        showAlbum = settings.showAlbumName,
                        showDuration = settings.showTrackDuration,
                        showAlbumColumn = showAlbumColumn,
                        showDurationColumn = showDurationColumn,
                        currentTrack = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        menuTrackIndex = menuTrackIndex,
                        onPlayTrack = { vm.player.playList(tracks, it, title) },
                        onOpenMenu = { menuTrackIndex = it },
                        onCloseMenu = { menuTrackIndex = null },
                        onRetry = retry,
                        onLoadMore = { vm.loadMoreOnlineDetail() },
                        vm = vm,
                    )
                }
            }
        }

        if (split) {
            Box(Modifier.matchParentSize().background(backdrop))
            Row(
                modifier = Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = topClearance + DetailNavHeight)
                    .padding(horizontal = margin),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                hero(
                    Modifier.width(leftWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = bottomClearance),
                    false,
                )
                trackList(Modifier.weight(1f).fillMaxHeight(), false)
            }
        } else {
            trackList(Modifier.fillMaxSize(), true)
        }

        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            if (!split) {
                Box(
                    Modifier.matchParentSize()
                        .graphicsLayer { alpha = reveal }
                        .background(
                            Brush.verticalGradient(
                                0f to pageColor,
                                0.80f to pageColor.copy(alpha = 0.94f),
                                1f to pageColor.copy(alpha = 0f),
                            ),
                        ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = topClearance)
                    .height(DetailNavHeight)
                    .padding(horizontal = margin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCircleIconButton(onClick = { vm.closeDetail() }, size = 46.dp, useBlur = false) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回", tint = colors.textPrimary)
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                        .graphicsLayer { alpha = if (split) 0f else ((reveal - 0.25f) / 0.75f).coerceIn(0f, 1f) }
                        .then(if (split || reveal <= 0.25f) Modifier.clearAndSetSemantics {} else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank() && !short) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (isCustomLocalPlaylist) {
                    Box {
                        GlassCircleIconButton(onClick = { showMoreMenu = true }, size = 46.dp, useBlur = false) {
                            Icon(Icons.Rounded.MoreHoriz, "歌单操作", tint = colors.textPrimary)
                        }
                        LiquidPopoverMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            width = 184.dp,
                            items = listOf(
                                LiquidPopoverItem(id = "rename", title = "重命名歌单", icon = Icons.Rounded.Edit) {
                                    showRenameDialog = true
                                },
                                LiquidPopoverItem(
                                    id = "delete", title = "删除歌单", icon = Icons.Rounded.DeleteOutline,
                                    destructive = true, showDividerAbove = true,
                                ) { showDeleteConfirm = true },
                            ),
                        )
                    }
                } else {
                    Spacer(Modifier.size(46.dp))
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

private val DetailNavHeight = 62.dp

@Composable
private fun DetailHero(
    title: String,
    eyebrow: String,
    subtitle: String,
    coverUrl: String?,
    description: String?,
    horizontal: Boolean,
    short: Boolean,
    split: Boolean,
    hasTracks: Boolean,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onCollect: (() -> Unit)?,
    scrollOffset: () -> Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    var descriptionExpanded by rememberSaveable(title, description) { mutableStateOf(false) }
    var descriptionOverflows by remember(description) { mutableStateOf(false) }
    val textAlign = if (horizontal || split) TextAlign.Start else TextAlign.Center
    val align = if (horizontal || split) Alignment.Start else Alignment.CenterHorizontally

    val details: @Composable (Modifier) -> Unit = { detailsModifier ->
        Column(modifier = detailsModifier, horizontalAlignment = align) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                letterSpacing = 0.9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = (if (short) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall)
                    .copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = textAlign,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(if (short) 8.dp else 14.dp))
            DetailHeroActions(hasTracks, onPlayAll, onShuffleAll, onCollect)

            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(if (short) 8.dp else 12.dp))
                Column(Modifier.fillMaxWidth().animateContentSize(), horizontalAlignment = align) {
                    Text(
                        text = description.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = if (descriptionExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (!descriptionExpanded) descriptionOverflows = it.hasVisualOverflow },
                    )
                    if (descriptionOverflows || descriptionExpanded) {
                        Row(
                            modifier = Modifier.align(Alignment.End)
                                .heightIn(min = 40.dp)
                                .liquidPress { descriptionExpanded = !descriptionExpanded }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (descriptionExpanded) "收起" else "更多",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.accent,
                            )
                            Icon(
                                if (descriptionExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (horizontal) {
        BoxWithConstraints(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 22.dp)) {
            val coverSize = if (short) 120.dp else (maxWidth * 0.33f).coerceIn(146.dp, 204.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (short) 16.dp else 24.dp),
            ) {
                DetailCover(coverUrl, title, Modifier.size(coverSize))
                details(Modifier.weight(1f))
            }
        }
    } else {
        BoxWithConstraints(modifier.fillMaxWidth().padding(top = 6.dp, bottom = 20.dp)) {
            val artWidth = when {
                short && split -> 112.dp
                short -> 114.dp
                split -> (maxWidth - 20.dp).coerceAtMost(278.dp)
                else -> (maxWidth * 0.70f).coerceAtMost(250.dp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clipToBounds()) {
                    DetailCover(
                        url = coverUrl,
                        title = title,
                        modifier = Modifier.size(artWidth).graphicsLayer {
                            if (!split && !short) {
                                val scrolled = scrollOffset().toFloat()
                                translationY = scrolled * 0.27f
                                alpha = (1f - scrolled / size.height.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            }
                        },
                    )
                }
                Spacer(Modifier.height(if (short) 8.dp else 16.dp))
                details(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DetailCover(url: String?, title: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier.border(
            0.8.dp,
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.Transparent)),
            shape,
        ),
    ) {
        if (url.isNullOrBlank()) {
            GlassPlaceholderCover(seed = title, modifier = Modifier.fillMaxSize())
        } else {
            CoverArt(
                url = url,
                cornerRadius = 20.dp,
                placeholderSeed = title,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DetailHeroActions(
    enabled: Boolean,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onCollect: (() -> Unit)?,
) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier.widthIn(max = 330.dp).fillMaxWidth()
            .graphicsLayer { alpha = if (enabled) 1f else 0.52f },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DetailSmallAction(Icons.Rounded.Shuffle, "随机播放", enabled, onShuffleAll)
        Row(
            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                .clip(CircleShape)
                .background(colors.accent)
                .liquidPress(shape = CircleShape, enabled = enabled, onClick = onPlayAll)
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(
                text = "播放全部",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        if (onCollect != null) {
            DetailSmallAction(Icons.Rounded.BookmarkAdd, "收藏歌单", enabled, onCollect)
        }
    }
}

@Composable
private fun DetailSmallAction(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = CircleShape,
        useBlur = false,
        dense = true,
        modifier = Modifier.size(48.dp).liquidPress(shape = CircleShape, enabled = enabled, onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = colors.accent, modifier = Modifier.size(22.dp))
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
            useBlur = false,
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

private fun LazyListScope.detailTrackItems(
    tracks: List<MusicInfo>,
    horizontalPadding: Dp,
    isOnline: Boolean,
    isLeaderboard: Boolean,
    loading: Boolean,
    loadingMore: Boolean,
    error: String?,
    showAlbum: Boolean,
    showDuration: Boolean,
    showAlbumColumn: Boolean,
    showDurationColumn: Boolean,
    currentTrack: MusicInfo?,
    isPlaying: Boolean,
    menuTrackIndex: Int?,
    onPlayTrack: (Int) -> Unit,
    onOpenMenu: (Int) -> Unit,
    onCloseMenu: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    vm: MainViewModel,
) {
    if (tracks.isEmpty()) {
        item(key = "detail_status") {
            DetailTrackStatus(isOnline, loading, isLeaderboard, error, onRetry)
        }
        return
    }

    item(key = "detail_tracks_header") {
        val colors = LocalLiquidColors.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding + 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (isLeaderboard) "榜单排行 · ${tracks.size} 首" else "曲目列表 · ${tracks.size} 首",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showAlbumColumn) {
                DetailTrackColumnLabel("专辑", Modifier.weight(0.47f))
            }
            if (showDurationColumn) {
                DetailTrackColumnLabel("时长", Modifier.width(52.dp), TextAlign.End)
            }
            if (showAlbumColumn || showDurationColumn) Spacer(Modifier.width(48.dp))
        }
    }

    itemsIndexed(tracks, key = { index, track -> "${track.source}:${track.id}:$index" }) { index, track ->
        val colors = LocalLiquidColors.current
        val menuOpen = menuTrackIndex == index
        if (menuOpen) {
            DisposableEffect(track) { onDispose(onCloseMenu) }
        }
        DetailTrackRow(
            track = track,
            position = index + 1,
            isLeaderboard = isLeaderboard,
            isCurrent = currentTrack?.let { it.id == track.id && it.source == track.source } == true,
            isPlaying = isPlaying,
            showAlbum = showAlbum,
            showAlbumColumn = showAlbumColumn,
            showDurationColumn = showDurationColumn,
            showInlineDuration = showDuration && !showDurationColumn,
            modifier = Modifier.padding(horizontal = horizontalPadding),
            onClick = { onPlayTrack(index) },
            onMore = { onOpenMenu(index) },
            menu = {
                if (menuOpen) DetailTrackActionPopover(vm, track, onCloseMenu)
            },
        )
        if (index < tracks.lastIndex) {
            HorizontalDivider(
                color = colors.divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = horizontalPadding + 92.dp, end = horizontalPadding + 8.dp),
            )
        }
    }

    if (isOnline && (loadingMore || error != null)) {
        item(key = "detail_pagination_status") {
            DetailTrackStatus(isOnline = true, loading = loadingMore, isLeaderboard = isLeaderboard,
                error = error, onRetry = onLoadMore, isPagination = true)
        }
    }
}

@Composable
private fun DetailTrackColumnLabel(text: String, modifier: Modifier, alignment: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = LocalLiquidColors.current.textSecondary,
        textAlign = alignment,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DetailTrackRow(
    track: MusicInfo,
    position: Int,
    isLeaderboard: Boolean,
    isCurrent: Boolean,
    isPlaying: Boolean,
    showAlbum: Boolean,
    showAlbumColumn: Boolean,
    showDurationColumn: Boolean,
    showInlineDuration: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    menu: @Composable () -> Unit,
) {
    val colors = LocalLiquidColors.current
    val hovered = remember { MutableInteractionSource() }
    val isHovered by hovered.collectIsHoveredAsState()
    val rankColor = when {
        isLeaderboard && position == 1 -> colors.accent
        isLeaderboard && position <= 3 -> if (colors.isDark) Color(0xFFFDB44C) else Color(0xFF9F5700)
        else -> colors.textSecondary
    }
    val supporting = listOfNotNull(
        track.singer.ifBlank { "未知艺人" },
        track.albumName.takeIf { showAlbum && !showAlbumColumn && it.isNotBlank() },
    ).joinToString(" · ")

    Row(
        modifier = modifier.fillMaxWidth()
            .hoverable(hovered)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHovered) colors.rowHighlight else Color.Transparent)
            .liquidRowPress(onClick = onClick, onLongClick = onMore)
            .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(26.dp), contentAlignment = Alignment.Center) {
                if (isCurrent) {
                    PlayingEqualizer(isPlaying = isPlaying, color = colors.accent)
                } else {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isLeaderboard && position <= 3) FontWeight.Bold else FontWeight.Normal,
                        color = rankColor,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            CoverArt(url = track.picUrl, cornerRadius = 7.dp, placeholderSeed = track.name, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isCurrent) colors.accent else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = supporting,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showInlineDuration && !track.interval.isNullOrBlank()) {
                        Text(
                            text = " · ${track.interval}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (showAlbumColumn) {
            DetailTrackColumnLabel(track.albumName.ifBlank { "–" }, Modifier.weight(0.47f))
        }
        if (showDurationColumn) {
            DetailTrackColumnLabel(track.interval.orEmpty().ifBlank { "–" }, Modifier.width(52.dp), TextAlign.End)
        }
        Box {
            Box(
                modifier = Modifier.size(48.dp).liquidPress(shape = CircleShape, onClick = onMore),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MoreHoriz, "曲目操作：${track.name}", tint = colors.textSecondary, modifier = Modifier.size(22.dp))
            }
            menu()
        }
    }
}

@Composable
private fun DetailTrackStatus(
    isOnline: Boolean,
    loading: Boolean,
    isLeaderboard: Boolean,
    error: String?,
    onRetry: () -> Unit,
    isPagination: Boolean = false,
) {
    val colors = LocalLiquidColors.current
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (isPagination) 16.dp else 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading && isOnline) {
            CircularProgressIndicator(Modifier.size(24.dp), color = colors.accent, strokeWidth = 2.dp)
        }
        Text(
            text = when {
                loading && isOnline && isPagination -> "正在加载更多歌曲…"
                loading && isOnline && isLeaderboard -> "正在同步官方榜单…"
                loading && isOnline -> "正在加载精选曲目…"
                isOnline && error != null && isPagination -> "加载更多歌曲失败"
                isOnline && error != null -> "加载内容失败"
                isOnline -> "暂无可展示曲目"
                else -> "歌单还是空的"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (isOnline && !loading && error != null) {
            if (!isPagination) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, textAlign = TextAlign.Center)
            }
            TextButton(onClick = onRetry) {
                Icon(Icons.Rounded.Refresh, null, tint = colors.accent, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("重试", color = colors.accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
