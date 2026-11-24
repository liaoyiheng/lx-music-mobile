package cn.toside.music.liquid.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.SongListItem
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.EditorialHeroHeader
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val state by vm.discover.collectAsState()
    var showTagSheet by remember { mutableStateOf(false) }

    val featured = remember(state.items) { state.items.take(5) }
    val gridItems = remember(state.items) { if (state.items.size > 5) state.items.drop(5) else state.items }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(top = 12.dp, bottom = bottomClearanceDp.dp),
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
                    text = "EDITORIAL CURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    letterSpacing = 1.6.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "发现",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                )
            }
        }

        // Platform Source Pills (full readable labels, never truncated)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(MusicSources.ONLINE) { src ->
                    val active = state.source == src
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (active) colors.accent else colors.surface)
                            .border(
                                width = 0.8.dp,
                                color = if (active) Color.Transparent else colors.divider,
                                shape = CircleShape,
                            )
                            .bouncyClick(pressedScale = 0.94f) { vm.selectDiscoverSource(src) }
                            .padding(horizontal = 15.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = MusicSources.displayName(src),
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) Color.White else colors.textPrimary,
                        )
                    }
                }
            }
        }

        // Sort & Genre Tag Filter Bar (generous spacing, never ellipsized)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .bouncyClick(pressedScale = 0.94f) { showTagSheet = true }
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = state.selectedTagName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                items(state.sorts) { sort ->
                    val active = state.selectedSortId == sort.id
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (active) colors.textPrimary else colors.badgeBg)
                            .bouncyClick(pressedScale = 0.94f) { vm.selectDiscoverSort(sort.id) }
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = sort.name,
                            fontSize = 12.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) colors.background else colors.textSecondary,
                        )
                    }
                }
            }
        }

        // Loading or Error State
        if (state.loading && state.items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.6.dp)
                }
            }
        } else if (state.error != null && state.items.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.error ?: "加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { vm.loadDiscover(reset = true) }) {
                        Icon(Icons.Rounded.Refresh, null, tint = colors.accent)
                        Spacer(Modifier.width(6.dp))
                        Text("重新加载", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Magazine Editorial Hero Carousel
            if (featured.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)) {
                        Text(
                            text = "编辑精选",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )

                        val pagerState = rememberPagerState(pageCount = { featured.size })
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            pageSpacing = 14.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) { page ->
                            val item = featured[page]
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            EditorialFeatureCarouselCard(
                                item = item,
                                onClick = { vm.openOnlineSongList(item) },
                                modifier = Modifier.graphicsLayer {
                                    val s = 1f - (pageOffset.coerceIn(0f, 1f) * 0.04f)
                                    scaleX = s
                                    scaleY = s
                                },
                            )
                        }
                    }
                }
            }

            // Section Title for Grid
            if (gridItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = MusicSources.displayName(state.source).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                letterSpacing = 1.4.sp,
                            )
                            Text(
                                text = "人气歌单",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary,
                            )
                        }
                    }
                }

                val rows = gridItems.chunked(2)
                itemsIndexed(rows) { rowIdx, pair ->
                    if (rowIdx >= rows.lastIndex - 1 && state.hasMore && !state.loadingMore) {
                        LaunchedEffect(rows.size) {
                            vm.loadDiscover(reset = false)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        pair.forEach { item ->
                            SongListPosterCard(
                                item = item,
                                onClick = { vm.openOnlineSongList(item) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                if (state.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colors.accent,
                                strokeWidth = 2.2.dp,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTagSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showTagSheet = false },
            sheetState = sheetState,
            containerColor = colors.surfaceElevated,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "选择歌单风格",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                    )
                    TextButton(
                        onClick = {
                            vm.selectDiscoverTag("", "全部风格")
                            showTagSheet = false
                        }
                    ) {
                        Text("重置全部", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                }

                if (state.tags.hotTag.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "热门标签",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.tags.hotTag.forEach { tag ->
                            val selected = state.selectedTagId == tag.id
                            TagChoiceChip(
                                label = tag.name,
                                selected = selected,
                                onClick = {
                                    vm.selectDiscoverTag(tag.id, tag.name)
                                    showTagSheet = false
                                },
                            )
                        }
                    }
                }

                state.tags.tags.forEach { group ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        group.list.forEach { tag ->
                            val selected = state.selectedTagId == tag.id
                            TagChoiceChip(
                                label = tag.name,
                                selected = selected,
                                onClick = {
                                    vm.selectDiscoverTag(tag.id, tag.name)
                                    showTagSheet = false
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TagChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.accent else colors.badgeBg)
            .bouncyClick(pressedScale = 0.93f, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else colors.textPrimary,
        )
    }
}

@Composable
private fun EditorialFeatureCarouselCard(
    item: SongListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(228.dp)
            .clip(RoundedCornerShape(24.dp))
            .bouncyClick(pressedScale = 0.97f, onClick = onClick),
    ) {
        CoverArt(
            url = item.img,
            cornerRadius = 0.dp,
            placeholderSeed = item.id,
            modifier = Modifier.fillMaxSize(),
        )

        // Smooth cinema gradient scrim (never banded)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x18000000),
                        0.42f to Color(0x28000000),
                        0.76f to Color(0xB808080E),
                        1.0f to Color(0xEB08080E),
                    )
                )
        )

        // Top-left frosted badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .border(0.6.dp, Color(0x40FFFFFF), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "FEATURED · ${MusicSources.displayName(item.source)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.8.sp,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = item.name,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (item.author.isNotBlank()) {
                    Text(
                        text = item.author,
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                if (!item.playCount.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.78f),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = item.playCount,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongListPosterCard(
    item: SongListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    Column(
        modifier = modifier.bouncyClick(pressedScale = 0.965f, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            CoverArt(
                url = item.img,
                cornerRadius = 18.dp,
                placeholderSeed = "${item.source}_${item.id}",
                modifier = Modifier.fillMaxSize(),
            )
            if (!item.playCount.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color(0x8A000000))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = item.playCount,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.author.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.author,
                fontSize = 12.sp,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OnlinePlaylistDetailScreen(
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val detail by vm.onlineDetail.collectAsState()
    val playerState by vm.player.state.collectAsState()
    val settings by vm.settings.collectAsState()

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
                val eyebrow = if (detail.isLeaderboard) {
                    "OFFICIAL CHART · ${MusicSources.displayName(detail.source)}"
                } else {
                    "CURATED PLAYLIST · ${MusicSources.displayName(detail.source)}"
                }
                val subtitle = buildString {
                    if (detail.author.isNotBlank()) append(detail.author)
                    if (!detail.playCount.isNullOrBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append("${detail.playCount} 播放")
                    }
                    if (detail.total > 0) {
                        if (isNotEmpty()) append(" · ")
                        append("${detail.total} 首")
                    }
                }

                EditorialHeroHeader(
                    title = detail.title,
                    eyebrow = eyebrow,
                    subtitle = subtitle,
                    coverUrl = detail.coverUrl,
                    description = detail.description,
                    onPlayAll = {
                        if (detail.tracks.isNotEmpty()) {
                            vm.player.playList(detail.tracks, 0, detail.title, shuffle = false)
                        }
                    },
                    onShuffleAll = {
                        if (detail.tracks.isNotEmpty()) {
                            vm.player.playList(detail.tracks, 0, detail.title, shuffle = true)
                        }
                    },
                    trailingAction = {
                        if (detail.tracks.isNotEmpty()) {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .height(36.dp)
                                    .bouncyClick { vm.collectCurrentOnlineList() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkAdd,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "收藏整个歌单到资料库",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary,
                                    )
                                }
                            }
                        }
                    },
                )
            }

            if (detail.loading && detail.tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.6.dp)
                    }
                }
            } else if (detail.error != null && detail.tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = detail.error ?: "加载失败",
                            color = colors.textSecondary,
                        )
                    }
                }
            } else {
                itemsIndexed(detail.tracks, key = { idx, t -> "${t.id}_$idx" }) { idx, track ->
                    if (idx >= detail.tracks.lastIndex - 3 && detail.hasMore && !detail.loadingMore) {
                        LaunchedEffect(detail.tracks.size) {
                            vm.loadMoreOnlineDetail()
                        }
                    }
                    TrackRow(
                        music = track,
                        index = idx,
                        isCurrent = playerState.currentTrack?.id == track.id,
                        isPlaying = playerState.isPlaying,
                        showAlbum = settings.showAlbumName,
                        showDuration = settings.showTrackDuration,
                        onClick = { vm.player.playList(detail.tracks, idx, detail.title) },
                        onMoreClick = { vm.openTrackActions(track) },
                    )
                    if (idx < detail.tracks.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        )
                    }
                }

                if (detail.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colors.accent,
                                strokeWidth = 2.2.dp,
                            )
                        }
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
