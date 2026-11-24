package cn.toside.music.liquid.screenshot

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.EditorialHeroHeader
import cn.toside.music.liquid.ui.components.FluidMeshBackground
import cn.toside.music.liquid.ui.components.GlassCapsuleButton
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.LiquidFavoriteHeartButton
import cn.toside.music.liquid.ui.components.LiquidFloatingDock
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidPlaybackControlButton
import cn.toside.music.liquid.ui.components.LiquidPlaybackScrubber
import cn.toside.music.liquid.ui.components.LiquidPopoverItem
import cn.toside.music.liquid.ui.components.LiquidPopoverSurface
import cn.toside.music.liquid.ui.components.LiquidSegmentedControl
import cn.toside.music.liquid.ui.components.MainTab
import cn.toside.music.liquid.ui.components.PlayingEqualizer
import cn.toside.music.liquid.ui.components.TrackRow
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LocalMaterialStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * LX Music Liquid V2 组件样板截图（不是业务页面回归测试）：
 * - 以离线测试数据组合生产玻璃、导航与播放控件，覆盖各页面的代表性视觉场景。
 * - 每组用例输出浅色 (light/) 与深色 (dark/) 对照图；平板用例使用生产自适应导航，内容区仍为样板。
 * - 业务页面仍依赖 MainViewModel，接入无 ViewModel 的页面入口后才能做整页回归。
 */
class LiquidScreensScreenshotTest : ScreenshotTestBase() {

    @Test
    fun render01_v2FoundationGlassTokens() {
        captureLightAndDark("01_v2_foundation_glass_tokens") {
            V2FoundationTokensShowcase()
        }
    }

    @Test
    fun render02_libraryScreen() {
        captureLightAndDark("02_library_screen") { hazeState ->
            LibraryScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    fun render03_discoverScreen() {
        captureLightAndDark("03_discover_screen") { hazeState ->
            DiscoverScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    fun render04_chartsScreen() {
        captureLightAndDark("04_charts_screen") { hazeState ->
            ChartsScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    fun render05_searchMorphScreen() {
        captureLightAndDark("05_search_morph_screen") { hazeState ->
            SearchScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    fun render06_playlistDetailScreen() {
        captureLightAndDark("06_playlist_detail_screen") { hazeState ->
            PlaylistDetailScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    fun render07_playerCoverMode() {
        captureLightAndDark("07_player_cover_mode") {
            PlayerOverlayShowcase(lyricsFocusMode = false)
        }
    }

    @Test
    fun render08_playerLyricsMode() {
        captureLightAndDark("08_player_lyrics_mode") {
            PlayerOverlayShowcase(lyricsFocusMode = true)
        }
    }

    @Test
    fun render09_settingsScreen() {
        captureLightAndDark("09_settings_screen") { hazeState ->
            SettingsScreenShowcase(hazeState = hazeState)
        }
    }

    @Test
    @Config(qualifiers = "w840dp-h1180dp-xhdpi")
    fun render10_tabletPortraitSkeleton() {
        captureLightAndDark(
            baseName = "10_tablet_wide",
            device = DeviceProfile.TABLET_PORTRAIT,
        ) { hazeState ->
            AdaptiveNavigationShowcase(hazeState = hazeState)
        }
    }

    @Test
    @Config(qualifiers = "w1180dp-h820dp-xhdpi-land")
    fun render11_tabletLandscapeSkeleton() {
        captureLightAndDark(
            baseName = "11_tablet_wide",
            device = DeviceProfile.TABLET_LANDSCAPE,
        ) { hazeState ->
            AdaptiveNavigationShowcase(hazeState = hazeState)
        }
    }

    /*
     * ============================================================================
     * TODO(Wave-2): 待业务页面提供无 ViewModel 依赖的 Content Composable 后，
     * 直接替换上面各 Showcase 内部实现为真实生产页面，例如：
     * - LibraryScreenContent(playlists = LiquidMockFixtures.sampleUserPlaylists, ...)
     * - DiscoverScreenContent(state = LiquidMockFixtures.sampleDiscoverState, ...)
     * - ChartsScreenContent(state = LiquidMockFixtures.sampleChartsState, ...)
     * - SearchScreenContent(state = LiquidMockFixtures.sampleSearchState, ...)
     * - PlaylistDetailContent(state = LiquidMockFixtures.sampleOnlineDetailState, ...)
     * - PlayerOverlayContent(playerState = LiquidMockFixtures.samplePlayerState, ...)
     * - SettingsScreenContent(settings = LiquidMockFixtures.sampleSettings, ...)
     * ============================================================================
     */

    @Composable
    private fun V2FoundationTokensShowcase() {
        val colors = LocalLiquidColors.current
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.linearGradient(
                            if (colors.isDark) {
                                listOf(Color(0xFF42183B), Color(0xFF1A2A52), Color(0xFF0B0B0E))
                            } else {
                                listOf(Color(0xFFFFD6DF), Color(0xFFD8E6FF), Color(0xFFF5F5F8))
                            },
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "LX Liquid Design V2",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = "液态玻璃材质体系 · 触感控件 · 悬浮菜单矩阵",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )

                LiquidSegmentedControl(
                    items = listOf("液态玻璃", "经典磨砂", "固态哑光"),
                    selectedIndex = 0,
                    onSelect = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MaterialStyle.entries.forEach { style ->
                        CompositionLocalProvider(
                            LocalMaterialStyle provides style,
                            LocalHazeState provides null,
                        ) {
                            LiquidGlassSurface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(92.dp),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = style.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary,
                                    )
                                    Text(
                                        text = "Specular Rim",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }

                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    dense = true,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        GlassCircleIconButton(onClick = {}) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = colors.textPrimary)
                        }
                        GlassCapsuleButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "全部播放",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary,
                            )
                        }
                        LiquidFavoriteHeartButton(
                            isFavorite = true,
                            onClick = {},
                            inactiveTint = colors.textSecondary,
                            containerColor = colors.chipBackground,
                        )
                        PlayingEqualizer(isPlaying = true)
                        LiquidPlaybackControlButton(
                            icon = Icons.Rounded.Pause,
                            contentDescription = "暂停",
                            onClick = {},
                            tint = colors.textPrimary,
                            haloColor = colors.accent.copy(alpha = 0.25f),
                            containerColor = colors.chipBackground,
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    LiquidPopoverSurface(
                        items = listOf(
                            LiquidPopoverItem("q", "音质：Hi-Res 24bit", icon = Icons.Rounded.GraphicEq, selected = true, onClick = {}),
                            LiquidPopoverItem("dl", "缓存无损到本地", icon = Icons.Rounded.Download, onClick = {}),
                            LiquidPopoverItem("share", "分享歌曲卡片", icon = Icons.Rounded.Share, showDividerAbove = true, onClick = {}),
                        ),
                        onDismissRequest = {},
                        useBlur = false,
                        width = 224.dp,
                    )
                }
            }
        }
    }

    @Composable
    private fun LibraryScreenShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "资料库",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.textPrimary,
                            )
                            GlassCapsuleButton(onClick = {}) {
                                Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("新建歌单", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LiquidMockFixtures.sampleUserPlaylists.take(2).forEachIndexed { idx, pl ->
                                LiquidGlassSurface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(136.dp),
                                    shape = RoundedCornerShape(22.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (idx == 0) colors.accentSoft else colors.chipBackground),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (idx == 0) Icons.Rounded.Favorite else Icons.Rounded.QueueMusic,
                                                contentDescription = null,
                                                tint = if (idx == 0) colors.accent else colors.textPrimary,
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = pl.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${pl.trackCount} 首曲目",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.textSecondary,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "自建与同步歌单",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LiquidMockFixtures.sampleUserPlaylists.drop(2).forEach { pl ->
                                Column(modifier = Modifier.weight(1f)) {
                                    CoverArt(
                                        url = pl.coverUrl,
                                        placeholderSeed = pl.id,
                                        cornerRadius = 18.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f),
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = pl.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${pl.trackCount} 首 · ${MusicSources.displayName(pl.source ?: "local")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }

                        Text(
                            text = "最近常听",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                        )
                        LiquidMockFixtures.sampleTracks.take(3).forEachIndexed { idx, track ->
                            TrackRow(
                                music = track,
                                index = idx + 1,
                                isCurrent = idx == 0,
                                isPlaying = idx == 0,
                                onClick = {},
                                onMoreClick = {},
                            )
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.LIBRARY,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun DiscoverScreenShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        val discover = LiquidMockFixtures.sampleDiscoverState
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "发现",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.textPrimary,
                            )
                            GlassCapsuleButton(onClick = {}) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(MusicSources.displayName(discover.source), style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                            }
                        }

                        LiquidSegmentedControl(
                            items = discover.sorts.map { it.name },
                            selectedIndex = 0,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            discover.tags.hotTag.forEachIndexed { idx, tag ->
                                LiquidGlassSurface(
                                    shape = CircleShape,
                                    tintOverride = if (idx == 0) colors.accentSoft else null,
                                    modifier = Modifier.height(34.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = tag.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (idx == 0) colors.accent else colors.textPrimary,
                                        )
                                    }
                                }
                            }
                        }

                        discover.items.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                rowItems.forEach { item ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        CoverArt(
                                            url = item.img,
                                            placeholderSeed = item.id,
                                            cornerRadius = 18.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = 13.5.sp,
                                            color = colors.textPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${item.playCount ?: ""} · ${item.author}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.DISCOVER,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun ChartsScreenShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        val charts = LiquidMockFixtures.sampleChartsState
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "排行榜",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.textPrimary,
                            )
                            GlassCapsuleButton(onClick = {}) {
                                Icon(Icons.Rounded.Equalizer, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(MusicSources.displayName(charts.source), style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                            }
                        }

                        // Featured top-2 boards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            charts.boards.take(2).forEach { board ->
                                LiquidGlassSurface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(22.dp),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        CoverArt(
                                            url = board.coverImg,
                                            placeholderSeed = board.id,
                                            cornerRadius = 16.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = board.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "每日实时更新",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.textSecondary,
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "全站风向榜",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                        )
                        charts.boards.forEachIndexed { idx, board ->
                            LiquidGlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (idx < 2) colors.accent else colors.textSecondary,
                                        modifier = Modifier.width(26.dp),
                                    )
                                    CoverArt(
                                        url = board.coverImg,
                                        placeholderSeed = board.id,
                                        cornerRadius = 10.dp,
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = board.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary,
                                        )
                                        Text(
                                            text = MusicSources.displayName(board.source),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    PlayingEqualizer(isPlaying = idx == 0)
                                }
                            }
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.CHARTS,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun SearchScreenShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        val search = LiquidMockFixtures.sampleSearchState
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 28.dp, bottom = 180.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Text(
                                text = "搜索",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(6.dp))
                            LiquidSegmentedControl(
                                items = listOf("单曲", "歌单"),
                                selectedIndex = search.modeIndex,
                                onSelect = {},
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                search.hotKeywords.take(4).forEach { kw ->
                                    LiquidGlassSurface(
                                        shape = CircleShape,
                                        modifier = Modifier.height(34.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = kw,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = colors.textPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        itemsIndexed(search.musicResults) { index, track ->
                            TrackRow(
                                music = track,
                                index = index + 1,
                                isCurrent = index == 0,
                                isPlaying = index == 0,
                                onClick = {},
                                onMoreClick = {},
                            )
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.SEARCH,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                isSearchActive = true,
                searchQuery = search.query,
                onSearchQueryChange = {},
                onSearchSubmit = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun PlaylistDetailScreenShowcase(hazeState: HazeState) {
        val detail = LiquidMockFixtures.sampleOnlineDetailState
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        EditorialHeroHeader(
                            title = detail.title,
                            eyebrow = "EDITORIAL PLAYLIST · 网易云",
                            subtitle = "${detail.author} · ${detail.playCount} 次播放",
                            coverUrl = detail.coverUrl,
                            description = detail.description,
                            onPlayAll = {},
                            onShuffleAll = {},
                        )

                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            detail.tracks.forEachIndexed { idx, track ->
                                TrackRow(
                                    music = track,
                                    index = idx + 1,
                                    isCurrent = idx == 0,
                                    isPlaying = idx == 0,
                                    onClick = {},
                                    onMoreClick = {},
                                )
                            }
                            Spacer(Modifier.height(170.dp))
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.DISCOVER,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                immersiveTint = Color(0xFF3A1A28),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun PlayerOverlayShowcase(lyricsFocusMode: Boolean) {
        val state = LiquidMockFixtures.samplePlayerState
        val track = state.currentTrack!!

        Box(modifier = Modifier.fillMaxSize()) {
            FluidMeshBackground(
                colors = state.meshColors,
                animateMovement = false,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x3D000000)),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f)),
                )
                Spacer(Modifier.height(20.dp))

                if (!lyricsFocusMode) {
                    CoverArt(
                        url = state.resolvedCoverUrl,
                        placeholderSeed = track.id,
                        cornerRadius = 24.dp,
                        modifier = Modifier
                            .size(288.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp)),
                    )
                    Spacer(Modifier.height(26.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CoverArt(
                            url = state.resolvedCoverUrl,
                            placeholderSeed = track.id,
                            cornerRadius = 12.dp,
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${track.singer} · ${track.albumName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        LiquidFavoriteHeartButton(
                            isFavorite = state.isFavorite,
                            onClick = {},
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        state.lyrics.forEachIndexed { idx, line ->
                            val isActive = idx == state.activeLyricIndex
                            Column {
                                Text(
                                    text = line.text,
                                    fontSize = if (isActive) 24.sp else 18.sp,
                                    lineHeight = if (isActive) 31.sp else 24.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.44f),
                                )
                                line.translation?.let { trans ->
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = trans,
                                        fontSize = 14.sp,
                                        color = if (isActive) Color.White.copy(alpha = 0.80f) else Color.White.copy(alpha = 0.32f),
                                    )
                                }
                            }
                        }
                    }
                }

                if (!lyricsFocusMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${track.singer} — ${track.albumName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.74f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        LiquidFavoriteHeartButton(
                            isFavorite = state.isFavorite,
                            onClick = {},
                        )
                        Spacer(Modifier.width(8.dp))
                        GlassCircleIconButton(
                            onClick = {},
                            size = 36.dp,
                            darkOverride = true,
                            useBlur = false,
                        ) {
                            Icon(Icons.Rounded.MoreHoriz, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                LiquidPlaybackScrubber(
                    progress = state.positionMs.toFloat() / state.durationMs.toFloat(),
                    onScrubbingChange = { _, _ -> },
                    onScrubFinished = {},
                    durationMs = state.durationMs,
                    positionMs = state.positionMs,
                    centerBadge = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "${state.activeQualityBadge} · ${state.activeSourceBadge}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                            )
                        }
                    },
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidPlaybackControlButton(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "上一首",
                        onClick = {},
                        iconSize = 36.dp,
                    )
                    LiquidPlaybackControlButton(
                        icon = Icons.Rounded.Pause,
                        contentDescription = "暂停",
                        onClick = {},
                        iconSize = 44.dp,
                        buttonSize = 72.dp,
                        haloSize = 68.dp,
                    )
                    LiquidPlaybackControlButton(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "下一首",
                        onClick = {},
                        iconSize = 36.dp,
                    )
                }

                if (!lyricsFocusMode) {
                    Spacer(Modifier.weight(1f))
                    val activeLine = state.lyrics[state.activeLyricIndex]
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(18.dp),
                        darkOverride = true,
                        useBlur = false,
                        tintOverride = Color(0x3314141C),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = activeLine.text,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            activeLine.translation?.let { trans ->
                                Text(
                                    text = trans,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.72f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingsScreenShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        val settings = LiquidMockFixtures.sampleSettings
        val apiStatus = LiquidMockFixtures.sampleUserApiStatus
        val syncState = LiquidMockFixtures.sampleSyncState

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "设置",
                            style = MaterialTheme.typography.displaySmall,
                            color = colors.textPrimary,
                        )

                        LiquidGlassSurface(
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "外观主题与玻璃质感",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                )
                                LiquidSegmentedControl(
                                    items = listOf("浅色亮面", "深色暗夜", "跟随系统"),
                                    selectedIndex = if (colors.isDark) 1 else 0,
                                    onSelect = {},
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                LiquidSegmentedControl(
                                    items = listOf("液态玻璃", "经典磨砂", "固态哑光"),
                                    selectedIndex = 0,
                                    onSelect = {},
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        LiquidGlassSurface(
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "自定义音源引擎 (QuickJS)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                )
                                Text(
                                    text = "${apiStatus.activeScriptName} · 运行正常",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.success,
                                )
                                HorizontalDivider(color = colors.divider)
                                Text(
                                    text = "默认下载与在线试听音质：${settings.preferredQuality.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                )
                                Text(
                                    text = "LX Sync 桌面同步：${syncState.statusText}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.SETTINGS,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Composable
    private fun TabletWideScaffoldShowcase(hazeState: HazeState) {
        val colors = LocalLiquidColors.current
        val detail = LiquidMockFixtures.sampleOnlineDetailState
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
            ) {
                CompositionLocalProvider(LocalHazeState provides null) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(0.42f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text(
                                text = "LX Liquid · Tablet",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.textPrimary,
                            )
                            CoverArt(
                                url = detail.coverUrl,
                                placeholderSeed = detail.sourceListId,
                                cornerRadius = 24.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                            )
                            Text(
                                text = detail.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary,
                            )
                            Text(
                                text = detail.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }

                        LiquidGlassSurface(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxSize(),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidSegmentedControl(
                                    items = listOf("曲目列表", "同步歌词", "音频设置"),
                                    selectedIndex = 0,
                                    onSelect = {},
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(6.dp))
                                detail.tracks.forEachIndexed { idx, track ->
                                    TrackRow(
                                        music = track,
                                        index = idx + 1,
                                        isCurrent = idx == 0,
                                        isPlaying = idx == 0,
                                        onClick = {},
                                        onMoreClick = {},
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LiquidFloatingDock(
                activeTab = MainTab.DISCOVER,
                onSelectTab = {},
                playerState = LiquidMockFixtures.samplePlayerState,
                onMiniPlayerClick = {},
                onTogglePlay = {},
                onSkipNext = {},
                onSkipPrevious = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(540.dp),
            )
        }
    }
}
