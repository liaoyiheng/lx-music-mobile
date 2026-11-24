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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.LiquidSegmentedControl
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.components.liquidCardPress
import cn.toside.music.liquid.ui.components.liquidRowPress
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

private data class BrowseCategory(
    val title: String,
    val keyword: String,
    val icon: ImageVector,
    val colors: List<Color>,
)

private val EditorialCategories = listOf(
    BrowseCategory("华语流行", "华语流行", Icons.Rounded.MusicNote, listOf(Color(0xFFFA243C), Color(0xFFFF6B81))),
    BrowseCategory("民谣诗篇", "民谣", Icons.Rounded.Spa, listOf(Color(0xFF1D976C), Color(0xFF38EF7D))),
    BrowseCategory("电子律动", "电子", Icons.Rounded.GraphicEq, listOf(Color(0xFF3A5AFE), Color(0xFF00C6FF))),
    BrowseCategory("欧美精选", "欧美", Icons.Rounded.Public, listOf(Color(0xFF7F00FF), Color(0xFFE100FF))),
    BrowseCategory("摇滚现场", "摇滚", Icons.Rounded.Radio, listOf(Color(0xFFFF512F), Color(0xFFF09819))),
    BrowseCategory("ACG 燃曲", "ACG", Icons.Rounded.Headphones, listOf(Color(0xFFEC008C), Color(0xFFFC6767))),
    BrowseCategory("轻音乐", "轻音乐", Icons.Rounded.Piano, listOf(Color(0xFF11998E), Color(0xFF2BC0E4))),
    BrowseCategory("粤语经典", "粤语经典", Icons.Rounded.Nightlight, listOf(Color(0xFF283C86), Color(0xFF45A247))),
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

    StandardPage { availableWidth, window ->
        val columns = window.pageColumns(availableWidth)
        val categoryColumns = window.pageColumns(availableWidth, maxColumns = 4)
        val bestMatch = state.musicResults.firstOrNull()?.takeIf {
            isShowingResults && state.modeIndex == 0 && !state.loading && state.error == null
        }
        val twoPane = availableWidth >= 720.dp && bestMatch != null
        val playFrom: (Int) -> Unit = { index ->
            vm.player.playList(state.musicResults, index, "搜索: ${state.committedQuery}")
        }
        val submit: () -> Unit = {
            focusManager.clearFocus()
            vm.submitSearch()
        }
        Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
            // The navigation search button opens this screen. The text editor lives here in every window class.
            if (availableWidth >= 500.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(112.dp)) { PageHeading("搜索") }
                    SearchInput(
                        query = state.query,
                        onQueryChange = vm::updateSearchQuery,
                        onSubmit = submit,
                        modifier = Modifier.weight(1f).widthIn(max = 560.dp),
                    )
                }
            } else {
                PageHeading("搜索")
                SearchInput(state.query, vm::updateSearchQuery, submit, Modifier.fillMaxWidth())
            }
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (twoPane) 22.dp else 0.dp),
            ) {
                if (twoPane && bestMatch != null) {
                    Column(Modifier.width((availableWidth * 0.34f).coerceAtMost(360.dp)).padding(top = 12.dp)) {
                        BestMatchCard(bestMatch, { playFrom(0) }, { vm.openTrackActions(bestMatch) }, Modifier.fillMaxWidth())
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = bottomClearanceDp.dp + 16.dp),
                ) {
                    item("sources") {
                        Column(Modifier.padding(bottom = 8.dp)) {
                            PageSourcePicker(
                                MusicSources.SEARCH_SOURCES,
                                state.source,
                                wraps = !twoPane && availableWidth >= 500.dp,
                                onSelect = vm::selectSearchSource,
                            )
                        }
                    }

                    // Live Search Suggestions Floating Glass Card
                    if (state.tips.isNotEmpty() && (!isShowingResults || state.query != state.committedQuery)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    state.tips.forEachIndexed { idx, tip ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .liquidRowPress {
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
                                                modifier = Modifier.padding(start = 46.dp),
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
                                        .padding(vertical = 6.dp),
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
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    history.take(12).forEach { kw ->
                                        Row(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(colors.chipBackground)
                                                .border(0.6.dp, colors.glassBorder, CircleShape)
                                                .liquidRowPress {
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
                                        .padding(vertical = 6.dp),
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
                                        .padding(vertical = 6.dp),
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
                                                .liquidRowPress {
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
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }

                        itemsIndexed(EditorialCategories.chunked(categoryColumns), key = { index, _ -> "categories_$index" }) { _, entries ->
                            PageGridRow(entries, categoryColumns, Modifier.padding(vertical = 6.dp)) { category, cellModifier ->
                                CategoryPosterCard(category, onClick = {
                                    focusManager.clearFocus()
                                    vm.submitSearch(category.keyword)
                                }, modifier = cellModifier)
                            }
                        }
                    }

                    if (isShowingResults) {
                        item("result_mode") {
                            LiquidSegmentedControl(
                                items = listOf("歌曲", "歌单"),
                                selectedIndex = state.modeIndex,
                                onSelect = vm::selectSearchMode,
                                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(vertical = 8.dp),
                            )
                        }
                        val showTracks = state.modeIndex == 0
                        val resultCount = if (showTracks) state.musicResults.size else state.songListResults.size
                        when {
                            state.loading -> item("loading") { PageSpinner() }
                            state.error != null -> item("error") {
                                PageMessage(state.error ?: "搜索失败") { vm.submitSearch(state.committedQuery) }
                            }
                            resultCount == 0 -> item("empty") {
                                PageMessage(if (showTracks) "未找到相关歌曲" else "未找到相关歌单")
                            }
                            showTracks -> {
                                if (!twoPane && bestMatch != null) {
                                    item("best_match") {
                                        BestMatchCard(bestMatch, { playFrom(0) }, { vm.openTrackActions(bestMatch) }, Modifier.fillMaxWidth())
                                    }
                                }
                                item("track_heading") {
                                    Text(
                                        "歌曲",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                    )
                                }
                                itemsIndexed(state.musicResults, key = { index, track -> "track_${track.id}_$index" }) { index, track ->
                                    TrackRow(
                                        music = track,
                                        index = index,
                                        isCurrent = playerState.currentTrack?.id == track.id,
                                        isPlaying = playerState.isPlaying,
                                        showAlbum = settings.showAlbumName,
                                        showDuration = settings.showTrackDuration,
                                        onClick = { playFrom(index) },
                                        onMoreClick = { vm.openTrackActions(track) },
                                    )
                                    if (index < state.musicResults.lastIndex) {
                                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                                    }
                                }
                            }
                            else -> {
                                itemsIndexed(state.songListResults.chunked(columns), key = { index, _ -> "playlist_row_$index" }) { _, entries ->
                                    PageGridRow(entries, columns, Modifier.padding(vertical = 7.dp)) { playlist, cellModifier ->
                                        SongListPosterCard(playlist, { vm.openOnlineSongList(playlist) }, cellModifier)
                                    }
                                }
                            }
                        }
                        if (!state.loading && state.error == null && resultCount > 0 && (state.hasMore || state.loadingMore)) {
                            item("pagination") {
                                PageLoadMore(
                                    requestKey = "${state.committedQuery}:${state.source}:${state.modeIndex}:${state.page}",
                                    loading = state.loadingMore,
                                    onLoad = vm::loadMoreSearch,
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
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier.heightIn(min = 52.dp).clip(shape).background(colors.surfaceElevated).padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            modifier = Modifier.weight(1f).semantics { contentDescription = "搜索歌曲或歌单" },
            decorationBox = { field ->
                Box(Modifier.padding(vertical = 10.dp)) {
                    if (query.isEmpty()) {
                        Text("歌曲、艺人或歌单", color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    field()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Rounded.Close, "清除搜索词", tint = colors.textSecondary)
            }
        }
        IconButton(onClick = onSubmit, enabled = query.isNotBlank()) {
            Icon(Icons.Rounded.Search, "提交搜索", tint = if (query.isNotBlank()) colors.accent else colors.textTertiary)
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
            .liquidCardPress(shape = shape, onClick = onClick),
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
    val shape = RoundedCornerShape(18.dp)
    Box(modifier.clip(shape).background(colors.surfaceElevated).liquidCardPress(shape = shape, onClick = onPlay)) {
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
