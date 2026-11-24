package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.SongListItem
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidSegmentedControl
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

private data class BrowseCategory(
    val title: String,
    val subtitle: String,
    val keyword: String,
    val icon: ImageVector,
    val colors: List<Color>,
)

private val EditorialCategories = listOf(
    BrowseCategory("华语流行", "MANDOPOP", "华语流行", Icons.Rounded.MusicNote, listOf(Color(0xFFFA243C), Color(0xFFFF6B81))),
    BrowseCategory("民谣诗篇", "ACOUSTIC", "民谣", Icons.Rounded.Spa, listOf(Color(0xFF1D976C), Color(0xFF38EF7D))),
    BrowseCategory("电子律动", "ELECTRONIC", "电子", Icons.Rounded.GraphicEq, listOf(Color(0xFF3A5AFE), Color(0xFF00C6FF))),
    BrowseCategory("欧美精选", "GLOBAL POP", "欧美", Icons.Rounded.Public, listOf(Color(0xFF7F00FF), Color(0xFFE100FF))),
    BrowseCategory("摇滚现场", "ROCK LIVE", "摇滚", Icons.Rounded.Radio, listOf(Color(0xFFFF512F), Color(0xFFF09819))),
    BrowseCategory("ACG 燃曲", "ANIME", "ACG", Icons.Rounded.Headphones, listOf(Color(0xFFEC008C), Color(0xFFFC6767))),
    BrowseCategory("轻音乐", "AMBIENT", "轻音乐", Icons.Rounded.Piano, listOf(Color(0xFF11998E), Color(0xFF2BC0E4))),
    BrowseCategory("粤语经典", "CANTOPOP", "粤语经典", Icons.Rounded.Nightlight, listOf(Color(0xFF283C86), Color(0xFF45A247))),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val state by vm.search.collectAsState()
    val history by vm.searchHistory.collectAsState()
    val settings by vm.settings.collectAsState()
    val playerState by vm.player.state.collectAsState()
    val focusManager = LocalFocusManager.current

    val isShowingResults = state.committedQuery.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = bottomClearanceDp.dp),
    ) {
        // Large Editorial Title + Glass Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 10.dp),
            ) {
                Text(
                    text = "SEARCH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp,
                    ),
                    color = colors.accent,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "搜索",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp,
                    ),
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(14.dp))

                // Liquid Glass Pill Search Input
                LiquidGlassSurface(
                    shape = CircleShape,
                    useBlur = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "搜索",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (state.query.isEmpty()) {
                                Text(
                                    text = "艺人、歌曲、歌词或歌单",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.textTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            BasicTextField(
                                value = state.query,
                                onValueChange = { vm.updateSearchQuery(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = colors.textPrimary,
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        vm.submitSearch()
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (state.query.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(colors.chipBackground)
                                    .bouncyClick {
                                        vm.updateSearchQuery("")
                                        focusManager.clearFocus()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "清除",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Source Selector Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            ) {
                items(MusicSources.SEARCH_SOURCES, key = { it }) { src ->
                    val selected = state.source == src
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) colors.accent else colors.chipBackground)
                            .border(
                                width = 0.7.dp,
                                color = if (selected) Color.Transparent else colors.glassBorder,
                                shape = CircleShape,
                            )
                            .bouncyClick { vm.selectSearchSource(src) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = MusicSources.displayName(src),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (selected) Color.White else colors.textPrimary,
                        )
                    }
                }
            }
        }

        // Live Search Suggestions Floating Glass Card
        if (state.tips.isNotEmpty() && !isShowingResults) {
            item {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    useBlur = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        state.tips.forEachIndexed { idx, tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick {
                                        focusManager.clearFocus()
                                        vm.submitSearch(tip)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (idx < state.tips.lastIndex) {
                                HorizontalDivider(
                                    color = colors.divider,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 46.dp, end = 16.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (!isShowingResults) {
            // Recent Searches
            if (settings.enableSearchHistory && history.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "最近搜索",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )
                        Text(
                            text = "清除",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accent,
                            modifier = Modifier
                                .clip(CircleShape)
                                .bouncyClick { vm.clearSearchHistory() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        history.take(12).forEach { kw ->
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.chipBackground)
                                    .border(0.6.dp, colors.glassBorder, CircleShape)
                                    .bouncyClick {
                                        focusManager.clearFocus()
                                        vm.submitSearch(kw)
                                    }
                                    .padding(start = 13.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = kw,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "删除历史",
                                    tint = colors.textTertiary,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .bouncyClick { vm.deleteSearchHistory(kw) },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            // Hot Search Keywords
            if (settings.enableHotSearch && state.hotKeywords.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "全网热搜",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.hotKeywords.forEachIndexed { idx, kw ->
                            val isTop3 = idx < 3
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isTop3) colors.accentSoft else colors.chipBackground
                                    )
                                    .border(
                                        0.6.dp,
                                        if (isTop3) colors.accent.copy(alpha = 0.25f) else colors.glassBorder,
                                        CircleShape,
                                    )
                                    .bouncyClick {
                                        focusManager.clearFocus()
                                        vm.submitSearch(kw)
                                    }
                                    .padding(horizontal = 13.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isTop3) colors.accent else colors.textTertiary,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = kw,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isTop3) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Editorial Browse Categories Poster Grid
            item {
                Text(
                    text = "浏览类别",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }

            val catRows = EditorialCategories.chunked(2)
            items(catRows.size, key = { "cat_row_$it" }) { rowIdx ->
                val pair = catRows[rowIdx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEach { cat ->
                        CategoryPosterCard(
                            category = cat,
                            onClick = {
                                focusManager.clearFocus()
                                vm.submitSearch(cat.keyword)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        } else {
            // Search Results Mode Switcher
            item {
                LiquidSegmentedControl(
                    items = listOf("歌曲", "歌单"),
                    selectedIndex = state.modeIndex,
                    onSelect = { vm.selectSearchMode(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.6.dp)
                    }
                }
            } else if (state.error != null && state.musicResults.isEmpty() && state.songListResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.error ?: "搜索失败",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                        )
                    }
                }
            } else if (state.modeIndex == 0) {
                // Music Results
                if (state.musicResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("未找到相关歌曲", color = colors.textSecondary)
                        }
                    }
                } else {
                    val topResult = state.musicResults.first()
                    item(key = "top_result_${topResult.id}") {
                        BestMatchCard(
                            track = topResult,
                            onPlay = {
                                vm.player.playList(
                                    state.musicResults,
                                    0,
                                    "搜索: ${state.committedQuery}",
                                )
                            },
                            onMore = { vm.openTrackActions(topResult) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                        Text(
                            text = "歌曲",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        )
                    }

                    itemsIndexed(state.musicResults, key = { idx, t -> "sr_${t.id}_$idx" }) { idx, track ->
                        if (idx >= state.musicResults.lastIndex - 3 && state.hasMore && !state.loadingMore) {
                            LaunchedEffect(state.musicResults.size) {
                                vm.loadMoreSearch()
                            }
                        }
                        TrackRow(
                            music = track,
                            index = idx,
                            isCurrent = playerState.currentTrack?.id == track.id,
                            isPlaying = playerState.isPlaying,
                            showAlbum = settings.showAlbumName,
                            showDuration = settings.showTrackDuration,
                            onClick = {
                                vm.player.playList(
                                    state.musicResults,
                                    idx,
                                    "搜索: ${state.committedQuery}",
                                )
                            },
                            onMoreClick = { vm.openTrackActions(track) },
                        )
                        if (idx < state.musicResults.lastIndex) {
                            HorizontalDivider(
                                color = colors.divider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            )
                        }
                    }

                    if (state.loadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
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
            } else {
                // SongList Results (2-column visual grid)
                if (state.songListResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("未找到相关歌单", color = colors.textSecondary)
                        }
                    }
                } else {
                    val rows = state.songListResults.chunked(2)
                    items(rows.size, key = { "sl_sr_$it" }) { rowIdx ->
                        if (rowIdx >= rows.lastIndex - 1 && state.hasMore && !state.loadingMore) {
                            LaunchedEffect(state.songListResults.size) {
                                vm.loadMoreSearch()
                            }
                        }
                        val pair = rows[rowIdx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            pair.forEach { item ->
                                SearchSongListCard(
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
                                    .padding(vertical = 18.dp),
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
    }
}

@Composable
private fun CategoryPosterCard(
    category: BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(98.dp)
            .clip(shape)
            .background(Brush.linearGradient(category.colors))
            .bouncyClick(onClick = onClick),
    ) {
        // Subtle editorial translucent geometric arcs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = size.height * 0.72f,
                center = Offset(size.width * 0.92f, size.height * 0.18f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = size.height * 0.48f,
                center = Offset(size.width * 0.72f, size.height * 0.88f),
            )
        }

        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.28f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp,
                    fontSize = 9.sp,
                ),
                color = Color.White.copy(alpha = 0.82f),
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BestMatchCard(
    track: MusicInfo,
    onPlay: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        useBlur = false,
        modifier = modifier.bouncyClick(onClick = onPlay),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverArt(
                url = track.picUrl,
                contentDescription = track.name,
                cornerRadius = 14.dp,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "最佳匹配 · ${MusicSources.displayName(track.source)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = colors.accent,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                val metaLine = buildString {
                    append(track.singer.ifBlank { "未知艺人" })
                    if (track.albumName.isNotBlank()) {
                        append(" · ")
                        append(track.albumName)
                    }
                }
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .bouncyClick(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.chipBackground)
                    .bouncyClick(onClick = onMore),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "更多",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchSongListCard(
    item: SongListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    Column(modifier = modifier.bouncyClick(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            CoverArt(
                url = item.img,
                contentDescription = item.name,
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxSize(),
            )
            if (!item.playCount.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = item.playCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.author.isNotBlank()) {
            Text(
                text = item.author,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
