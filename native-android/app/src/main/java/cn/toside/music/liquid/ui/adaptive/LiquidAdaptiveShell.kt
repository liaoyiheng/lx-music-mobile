package cn.toside.music.liquid.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.toside.music.liquid.data.model.UserPlaylist
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.DetailRoute
import cn.toside.music.liquid.ui.components.LiquidFloatingDock
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidMiniPlayerCapsule
import cn.toside.music.liquid.ui.components.MainTab
import cn.toside.music.liquid.ui.components.primaryTabs
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

private val SidebarWidth = 256.dp
private val SidebarGutter = 16.dp

@Composable
internal fun LiquidAdaptiveShell(
    window: LiquidWindowClass,
    hazeState: HazeState,
    activeTab: MainTab,
    detailRoute: DetailRoute?,
    playlists: List<UserPlaylist>,
    playerState: PlayerUiState,
    playerExpanded: Boolean,
    onSelectTab: (MainTab) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onOpenPlayer: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    content: @Composable (bottomClearanceDp: Float) -> Unit,
) {
    var sidebarExpanded by rememberSaveable { mutableStateOf(true) }
    val mode = window.resolveNavigationMode(sidebarExpanded)
    val canToggleSidebar = window.widthSizeClass == LiquidWidthSizeClass.Expanded &&
        !window.isCompactHeight
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val showChrome = !playerExpanded && !keyboardVisible
    val compact = mode == LiquidNavigationMode.BottomDock
    val hasTrack = playerState.currentTrack != null
    var dockCollapsed by remember(mode, activeTab, detailRoute) { mutableStateOf(false) }
    val immersiveTint = remember(detailRoute) { mutableStateOf<Color?>(null) }
    val reportImmersiveTint: (Color?) -> Unit = remember(immersiveTint) {
        { tint -> immersiveTint.value = tint }
    }
    val scrollConnection = remember(density, showChrome, compact, activeTab, detailRoute) {
        if (showChrome && compact) {
            DockScrollConnection(
                collapseThreshold = with(density) { 32.dp.toPx() },
                expandThreshold = with(density) { 16.dp.toPx() },
                isCollapsed = { dockCollapsed },
                setCollapsed = { dockCollapsed = it },
            )
        } else null
    }
    val sidebarSpace = if (mode == LiquidNavigationMode.LeftSidebar && !keyboardVisible) {
        SidebarWidth + SidebarGutter
    } else 0.dp
    val topHeight = if (window.isCompactHeight) 48.dp else 54.dp
    val topMargin = if (window.isCompactHeight) 6.dp else 10.dp
    val topClearance = if (mode == LiquidNavigationMode.TopFloatingTabs && !keyboardVisible) {
        topHeight + topMargin + 8.dp
    } else 0.dp
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomClearance = if (keyboardVisible) 0.dp else navBottom + when {
        compact && dockCollapsed -> 56.dp + 20.dp
        compact && hasTrack -> 56.dp + 10.dp + 58.dp + 20.dp
        compact -> 58.dp + 20.dp
        hasTrack -> (if (window.isCompactHeight) 48.dp else 56.dp) + 16.dp
        else -> 0.dp
    }
    val colors = LocalLiquidColors.current

    CompositionLocalProvider(LocalLiquidWindowClass provides window) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
            // Chrome is a sibling of this source. In-page surfaces use their tint-only fallback.
            Box(Modifier.fillMaxSize().haze(hazeState).background(colors.background)) {
                CompositionLocalProvider(
                    LocalHazeState provides null,
                    // The shell already reserves the top capsule's space for existing screen headers.
                    LocalTopBarClearance provides 0.dp,
                    LocalImmersiveTintReporter provides reportImmersiveTint,
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(start = sidebarSpace, top = topClearance)
                            .imePadding()
                            .let { if (scrollConnection != null) it.nestedScroll(scrollConnection) else it },
                    ) {
                        content(bottomClearance.value)
                    }
                }
            }

            if (showChrome) {
                when (mode) {
                    LiquidNavigationMode.BottomDock -> LiquidFloatingDock(
                        activeTab = activeTab,
                        onSelectTab = onSelectTab,
                        playerState = playerState,
                        onMiniPlayerClick = onOpenPlayer,
                        onTogglePlay = onTogglePlay,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        immersiveTint = immersiveTint.value,
                        isCollapsed = dockCollapsed,
                        onExpand = { dockCollapsed = false },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                    LiquidNavigationMode.TopFloatingTabs -> LiquidTopTabs(
                        activeTab = activeTab,
                        onSelectTab = onSelectTab,
                        onShowSidebar = if (canToggleSidebar) ({ sidebarExpanded = true }) else null,
                        height = topHeight,
                        modifier = Modifier.align(Alignment.TopCenter)
                            .statusBarsPadding().padding(top = topMargin, start = 16.dp, end = 16.dp),
                    )
                    LiquidNavigationMode.LeftSidebar -> LiquidSidebar(
                        activeTab = activeTab,
                        selectedPlaylistId = (detailRoute as? DetailRoute.LocalPlaylist)?.playlistId,
                        playlists = playlists,
                        onSelectTab = onSelectTab,
                        onOpenPlaylist = onOpenPlaylist,
                        onHideSidebar = { sidebarExpanded = false },
                        modifier = Modifier.align(Alignment.TopStart).statusBarsPadding()
                            .navigationBarsPadding().padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
                    )
                }

                if (!compact && hasTrack) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = sidebarSpace)
                            .navigationBarsPadding()
                            .padding(horizontal = window.horizontalMargin, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LiquidMiniPlayerCapsule(
                            playerState = playerState,
                            onMiniPlayerClick = onOpenPlayer,
                            onTogglePlay = onTogglePlay,
                            onSkipNext = onSkipNext,
                            onSwipeNext = onSkipNext,
                            onSwipePrevious = onSkipPrevious,
                            immersiveTint = immersiveTint.value,
                            wide = true,
                            height = if (window.isCompactHeight) 48.dp else 56.dp,
                            modifier = Modifier.widthIn(
                                max = if (window.widthSizeClass == LiquidWidthSizeClass.Medium) 580.dp else 760.dp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private class DockScrollConnection(
    private val collapseThreshold: Float,
    private val expandThreshold: Float,
    private val isCollapsed: () -> Boolean,
    private val setCollapsed: (Boolean) -> Unit,
) : NestedScrollConnection {
    private var distance = 0f
    private var lastCollapsed = isCollapsed()

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        // Observe actual user-driven list travel. Leave all scroll deltas to the list and its parents.
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        val collapsed = isCollapsed()
        if (lastCollapsed != collapsed) {
            distance = 0f
            lastCollapsed = collapsed
        }
        if (collapsed && available.y > 0f && consumed.y >= 0f) {
            setCollapsed(false)
            lastCollapsed = false
            distance = 0f
            return Offset.Zero
        }
        val delta = if (collapsed) consumed.y else -consumed.y
        if (delta < 0f) distance = 0f
        if (delta > 0f) {
            distance += delta
            val threshold = if (collapsed) expandThreshold else collapseThreshold
            if (distance >= threshold) {
                setCollapsed(!collapsed)
                lastCollapsed = !collapsed
                distance = 0f
            }
        }
        return Offset.Zero
    }
}

@Composable
private fun LiquidTopTabs(
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onShowSidebar: (() -> Unit)?,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        dense = true,
    ) {
        Row(
            modifier = Modifier.height(height).padding(horizontal = 6.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (onShowSidebar != null) {
                SidebarToggle(expanded = false, onClick = onShowSidebar)
            }
            navigationTabs.forEach { tab ->
                ShellNavigationItem(
                    icon = tab.icon,
                    label = tab.label,
                    selected = tab == activeTab,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.fillMaxHeight(),
                    iconOnly = tab == MainTab.SEARCH,
                    horizontalPadding = if (tab == MainTab.SEARCH) 12.dp else 10.dp,
                )
            }
        }
    }
}

@Composable
private fun LiquidSidebar(
    activeTab: MainTab,
    selectedPlaylistId: String?,
    playlists: List<UserPlaylist>,
    onSelectTab: (MainTab) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onHideSidebar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        modifier = modifier.width(SidebarWidth).fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        dense = true,
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = colors.accent)
                Text(
                    "洛雪音乐",
                    modifier = Modifier.weight(1f),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                SidebarToggle(expanded = true, onClick = onHideSidebar)
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 4.dp),
            ) {
                items(navigationTabs, key = { "tab:${it.name}" }) { tab ->
                    ShellNavigationItem(
                        icon = tab.icon,
                        label = tab.label,
                        selected = selectedPlaylistId == null && tab == activeTab,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    )
                }
                if (playlists.isNotEmpty()) {
                    item(key = "playlists") {
                        Text(
                            "本地歌单",
                            modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 4.dp),
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    items(playlists, key = { "playlist:${it.id}" }) { playlist ->
                        ShellNavigationItem(
                            icon = Icons.Rounded.QueueMusic,
                            label = playlist.name,
                            selected = playlist.id == selectedPlaylistId,
                            onClick = { onOpenPlaylist(playlist.id) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarToggle(expanded: Boolean, onClick: () -> Unit) {
    val colors = LocalLiquidColors.current
    Box(
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
            .background(colors.chipBackground)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.ViewSidebar,
            contentDescription = if (expanded) "收起侧栏" else "展开侧栏",
            tint = colors.textPrimary,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun ShellNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false,
    horizontalPadding: Dp = 12.dp,
) {
    val colors = LocalLiquidColors.current
    val tint = if (selected) colors.accent else colors.textPrimary
    Row(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.accentSoft else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = if (iconOnly) label else null, Modifier.size(22.dp), tint = tint)
        if (!iconOnly) {
            Text(
                text = label,
                color = tint,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val navigationTabs = primaryTabs + MainTab.SEARCH
