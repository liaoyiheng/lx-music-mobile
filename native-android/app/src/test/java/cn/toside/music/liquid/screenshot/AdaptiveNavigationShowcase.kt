package cn.toside.music.liquid.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.toside.music.liquid.ui.adaptive.LiquidAdaptiveShell
import cn.toside.music.liquid.ui.adaptive.rememberLiquidWindowClass
import cn.toside.music.liquid.ui.components.MainTab
import dev.chrisbanes.haze.HazeState

@Composable
internal fun AdaptiveNavigationShowcase(hazeState: HazeState) {
    LiquidAdaptiveShell(
        window = rememberLiquidWindowClass(),
        hazeState = hazeState,
        activeTab = MainTab.DISCOVER,
        detailRoute = null,
        playlists = LiquidMockFixtures.sampleUserPlaylists,
        playerState = LiquidMockFixtures.samplePlayerState,
        playerExpanded = false,
        onSelectTab = {},
        onOpenPlaylist = {},
        onOpenPlayer = {},
        onTogglePlay = {},
        onSkipNext = {},
        onSkipPrevious = {},
    ) { bottomClearance ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("LX Music · 自适应内容区域 · 底部安全间距 ${bottomClearance.toInt()}dp")
        }
    }
}
