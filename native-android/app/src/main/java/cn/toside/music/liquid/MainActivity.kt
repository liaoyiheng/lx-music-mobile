package cn.toside.music.liquid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import cn.toside.music.liquid.data.model.ThemeMode
import cn.toside.music.liquid.ui.DetailRoute
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.adaptive.LiquidAdaptiveShell
import cn.toside.music.liquid.ui.adaptive.LocalLiquidWindowClass
import cn.toside.music.liquid.ui.adaptive.rememberLiquidWindowClass
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidMotion
import cn.toside.music.liquid.ui.components.MainTab
import cn.toside.music.liquid.ui.screens.ChartsScreen
import cn.toside.music.liquid.ui.screens.DiscoverScreen
import cn.toside.music.liquid.ui.screens.GlobalSheetsHost
import cn.toside.music.liquid.ui.screens.LibraryScreen
import cn.toside.music.liquid.ui.screens.PlayerOverlay
import cn.toside.music.liquid.ui.screens.PlaylistDetailScreen
import cn.toside.music.liquid.ui.screens.SearchScreen
import cn.toside.music.liquid.ui.screens.SettingsScreen
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LxLiquidTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        handleLaunchIntent(intent)

        setContent {
            LxLiquidRoot(vm = vm) { useDarkBars ->
                val barStyle = if (useDarkBars) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle,
                )
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !useDarkBars
                    isAppearanceLightNavigationBars = !useDarkBars
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER, false) == true) {
            if (vm.player.state.value.currentTrack != null) {
                vm.openPlayer()
            }
            intent.removeExtra(EXTRA_OPEN_PLAYER)
        }
    }

    companion object {
        const val EXTRA_OPEN_PLAYER = "cn.toside.music.liquid.EXTRA_OPEN_PLAYER"
    }
}

@Composable
private fun LxLiquidRoot(
    vm: MainViewModel,
    onSystemBarStyleChange: (useDarkBars: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsState()
    val activeTab by vm.activeTab.collectAsState()
    val detailRoute by vm.detailRoute.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val playerExpansionRequested by vm.playerExpanded.collectAsState()
    val playerState by vm.player.state.collectAsState()
    val hasTrack = playerState.currentTrack != null
    val playerExpanded = playerExpansionRequested && hasTrack
    LaunchedEffect(playerExpansionRequested, hasTrack) {
        if (playerExpansionRequested && !hasTrack) vm.closePlayer()
    }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    // Entering full-screen dark fluid player forces white status/nav bar icons.
    val useDarkBars = isDark || playerExpanded
    val view = LocalView.current
    SideEffect {
        onSystemBarStyleChange(useDarkBars)
        val activity = view.context as? ComponentActivity
        activity?.window?.let { w ->
            WindowCompat.getInsetsController(w, view).apply {
                isAppearanceLightStatusBars = !useDarkBars
                isAppearanceLightNavigationBars = !useDarkBars
            }
        }
    }

    // Optional lightweight Android 13+ notification permission request for media playback controls.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* non-blocking */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Surface background playback errors/notices as toast when mini-player is collapsed.
    LaunchedEffect(playerState.statusMessage, playerExpanded) {
        val msg = playerState.statusMessage
        if (!playerExpanded && !msg.isNullOrBlank()) {
            vm.showToast(msg)
            vm.player.clearStatusMessage()
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        vm.toast.collectLatest { msg ->
            toastMessage = msg
            delay(2400L)
            toastMessage = null
        }
    }

    val hazeState = remember { HazeState() }
    val windowClass = rememberLiquidWindowClass()

    LxLiquidTheme(
        themeMode = settings.themeMode,
        materialStyle = settings.materialStyle,
        hazeState = hazeState,
    ) {
        val colors = LocalLiquidColors.current
        BackHandler(
            enabled = playerExpanded || detailRoute != null || activeTab != MainTab.DISCOVER,
        ) {
            when {
                playerExpanded -> vm.closePlayer()
                detailRoute != null -> vm.closeDetail()
                activeTab != MainTab.DISCOVER -> vm.selectTab(MainTab.DISCOVER)
            }
        }

        var lastRenderedDetailRoute by remember { mutableStateOf<DetailRoute?>(null) }
        if (detailRoute != null) {
            lastRenderedDetailRoute = detailRoute
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            LiquidAdaptiveShell(
                window = windowClass,
                hazeState = hazeState,
                activeTab = activeTab,
                detailRoute = detailRoute,
                playlists = playlists,
                playerState = playerState,
                playerExpanded = playerExpanded,
                onSelectTab = vm::selectTab,
                onOpenPlaylist = vm::openLocalPlaylist,
                onOpenPlayer = vm::openPlayer,
                onTogglePlay = { vm.player.togglePlayPause() },
                onSkipNext = { vm.player.skipNext() },
                onSkipPrevious = { vm.player.skipPrevious() },
            ) { bottomClearanceDp ->
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { LiquidMotion.tabSwitchTransition() },
                    label = "main_tab_switch",
                    modifier = Modifier.fillMaxSize(),
                ) { tab ->
                    when (tab) {
                        MainTab.LIBRARY -> LibraryScreen(
                            vm = vm,
                            bottomClearanceDp = bottomClearanceDp,
                        )
                        MainTab.DISCOVER -> DiscoverScreen(
                            vm = vm,
                            bottomClearanceDp = bottomClearanceDp,
                        )
                        MainTab.CHARTS -> ChartsScreen(
                            vm = vm,
                            bottomClearanceDp = bottomClearanceDp,
                        )
                        MainTab.SETTINGS -> SettingsScreen(
                            vm = vm,
                            bottomClearanceDp = bottomClearanceDp,
                            modifier = Modifier.fillMaxSize(),
                        )
                        MainTab.SEARCH -> SearchScreen(
                            vm = vm,
                            bottomClearanceDp = bottomClearanceDp,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = detailRoute != null,
                    enter = LiquidMotion.detailEnterTransition(),
                    exit = LiquidMotion.detailExitTransition(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    lastRenderedDetailRoute?.let { route ->
                        PlaylistDetailScreen(
                            vm = vm,
                            route = route,
                            bottomClearanceDp = bottomClearanceDp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Full-Screen Fluid Mesh Player Overlay
            AnimatedVisibility(
                visible = playerExpanded && playerState.currentTrack != null,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = 0.88f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetY = { it },
                ) + fadeIn(animationSpec = tween(180)),
                exit = slideOutVertically(
                    animationSpec = tween(240),
                    targetOffsetY = { it },
                ) + fadeOut(animationSpec = tween(180)),
                modifier = Modifier.fillMaxSize(),
            ) {
                CompositionLocalProvider(LocalLiquidWindowClass provides windowClass) {
                    PlayerOverlay(vm = vm, onDismiss = vm::closePlayer)
                }
            }

            // Global Bottom Sheets (Track Actions, Playlist Picker, Comments, Alternates, Sleep Timer)
            CompositionLocalProvider(LocalLiquidWindowClass provides windowClass) {
                GlobalSheetsHost(vm = vm)
            }

            // Top Floating Liquid Glass Toast Capsule
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetY = { -it },
                ) + fadeIn(animationSpec = tween(180)),
                exit = slideOutVertically(
                    animationSpec = tween(180),
                    targetOffsetY = { -it },
                ) + fadeOut(animationSpec = tween(160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp),
            ) {
                LiquidToastCapsule(
                    message = toastMessage.orEmpty(),
                    forceDark = playerExpanded,
                )
            }
        }
    }
}

@Composable
private fun LiquidToastCapsule(
    message: String,
    forceDark: Boolean,
) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        shape = CircleShape,
        dense = true,
        darkOverride = if (forceDark) true else null,
        blurRadius = 28.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (forceDark) androidx.compose.ui.graphics.Color.White else colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
