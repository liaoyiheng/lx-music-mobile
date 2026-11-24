@file:OptIn(ExperimentalRoborazziApi::class)

package cn.toside.music.liquid.screenshot

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import cn.toside.music.liquid.data.model.AppLogEntry
import cn.toside.music.liquid.data.model.AppSettings
import cn.toside.music.liquid.data.model.AudioQuality
import cn.toside.music.liquid.data.model.CommentItem
import cn.toside.music.liquid.data.model.LeaderboardItem
import cn.toside.music.liquid.data.model.LyricLine
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicMeta
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.data.model.QualityItem
import cn.toside.music.liquid.data.model.SongListItem
import cn.toside.music.liquid.data.model.SongListSort
import cn.toside.music.liquid.data.model.SongListTag
import cn.toside.music.liquid.data.model.SongListTagGroup
import cn.toside.music.liquid.data.model.SongListTagsResult
import cn.toside.music.liquid.data.model.SyncState
import cn.toside.music.liquid.data.model.ThemeMode
import cn.toside.music.liquid.data.model.UserApiScript
import cn.toside.music.liquid.data.model.UserApiStatus
import cn.toside.music.liquid.data.model.UserPlaylist
import cn.toside.music.liquid.player.DefaultMeshPalette
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.ChartsUiState
import cn.toside.music.liquid.ui.CommentsUiState
import cn.toside.music.liquid.ui.DiscoverUiState
import cn.toside.music.liquid.ui.OnlineDetailUiState
import cn.toside.music.liquid.ui.SearchUiState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LxLiquidTheme
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeCaptureOption
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.background
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.size
import com.github.takahirom.roborazzi.uiMode
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowChoreographer
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * 100% 离线确定性 Coil ImageLoader：
 * - 拦截全部封面图片请求，绝不发起任何网络/磁盘请求，保证 CI 无头环境零白块、零超时。
 * - 使用 Android Canvas 绘制 320x320 ARGB_8888 彩色渐变 + 黑胶唱片几何纹理位图，
 *   确保 Palette 取色 (`rememberCoverPalette`) 与 `CoverArt` / `EditorialHeroHeader` 稳定呈现。
 */
object FakeCoilImageLoader {

    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()

    private val coverPalettes: List<IntArray> = listOf(
        intArrayOf(AndroidColor.rgb(250, 45, 72), AndroidColor.rgb(122, 22, 58), AndroidColor.rgb(255, 138, 128)),
        intArrayOf(AndroidColor.rgb(58, 92, 245), AndroidColor.rgb(20, 28, 78), AndroidColor.rgb(125, 185, 255)),
        intArrayOf(AndroidColor.rgb(245, 130, 32), AndroidColor.rgb(110, 38, 18), AndroidColor.rgb(255, 210, 110)),
        intArrayOf(AndroidColor.rgb(24, 184, 148), AndroidColor.rgb(12, 62, 68), AndroidColor.rgb(138, 245, 210)),
        intArrayOf(AndroidColor.rgb(176, 62, 224), AndroidColor.rgb(54, 18, 86), AndroidColor.rgb(255, 140, 220)),
        intArrayOf(AndroidColor.rgb(14, 142, 238), AndroidColor.rgb(12, 46, 88), AndroidColor.rgb(128, 232, 255)),
    )

    fun install(context: Context) {
        val appContext = context.applicationContext
        val loader = ImageLoader.Builder(appContext)
            .crossfade(false)
            .allowHardware(false)
            .interceptorDispatcher(Dispatchers.Main.immediate)
            .fetcherDispatcher(Dispatchers.Main.immediate)
            .decoderDispatcher(Dispatchers.Main.immediate)
            .transformationDispatcher(Dispatchers.Main.immediate)
            .components {
                add(OfflineVinylInterceptor(appContext))
            }
            .build()
        Coil.setImageLoader(loader)
    }

    fun reset() {
        Coil.reset()
        bitmapCache.clear()
    }

    fun getOrCreateBitmap(seed: String, sizePx: Int = 320): Bitmap {
        val cacheKey = "$seed#$sizePx"
        bitmapCache[cacheKey]?.let { if (!it.isRecycled) return it }
        val created = renderVinylCoverBitmap(seed, sizePx)
        bitmapCache[cacheKey] = created
        return created
    }

    private fun renderVinylCoverBitmap(seed: String, sizePx: Int): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val w = sizePx.toFloat()
        val h = sizePx.toFloat()

        val idx = abs(seed.hashCode()) % coverPalettes.size
        val palette = coverPalettes[idx]
        val cPrimary = palette[0]
        val cDeep = palette[1]
        val cHighlight = palette[2]

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w, h,
                intArrayOf(cPrimary, cDeep),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.28f, h * 0.24f, w * 0.65f,
                intArrayOf(
                    AndroidColor.argb(
                        185,
                        AndroidColor.red(cHighlight),
                        AndroidColor.green(cHighlight),
                        AndroidColor.blue(cHighlight),
                    ),
                    AndroidColor.TRANSPARENT,
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w, h, glowPaint)

        val cx = w * 0.5f
        val cy = h * 0.5f
        val discRadius = w * 0.37f
        val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(218, 16, 16, 22)
        }
        canvas.drawCircle(cx, cy, discRadius, discPaint)

        val groovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (w / 320f).coerceAtLeast(1f)
            color = AndroidColor.argb(46, 255, 255, 255)
        }
        for (ring in 1..4) {
            val r = discRadius * (0.48f + ring * 0.11f)
            canvas.drawCircle(cx, cy, r, groovePaint)
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - discRadius * 0.35f,
                cy - discRadius * 0.35f,
                cx + discRadius * 0.35f,
                cy + discRadius * 0.35f,
                cHighlight,
                cPrimary,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(cx, cy, discRadius * 0.36f, labelPaint)

        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.025f
            strokeCap = Paint.Cap.ROUND
            color = AndroidColor.argb(95, 255, 255, 255)
        }
        val arcRect = RectF(
            cx - discRadius * 0.84f,
            cy - discRadius * 0.84f,
            cx + discRadius * 0.84f,
            cy + discRadius * 0.84f,
        )
        canvas.drawArc(arcRect, -55f, 48f, false, arcPaint)
        canvas.drawArc(arcRect, 125f, 42f, false, arcPaint)

        val spindlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(235, 248, 248, 252)
        }
        canvas.drawCircle(cx, cy, discRadius * 0.09f, spindlePaint)

        return bmp
    }

    private class OfflineVinylInterceptor(
        private val context: Context,
    ) : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
            val request = chain.request
            val seed = request.data?.toString()?.takeIf { it.isNotBlank() } ?: "default_cover"
            val bitmap = getOrCreateBitmap(seed)
            return SuccessResult(
                drawable = BitmapDrawable(context.resources, bitmap),
                request = request,
                dataSource = DataSource.MEMORY,
                memoryCacheKey = null,
                diskCacheKey = null,
                isSampled = false,
                isPlaceholderCached = false,
            )
        }
    }
}

/**
 * 确定性中文音乐场景 Mock 数据集，严格对齐 `MusicModels.kt` 与 `MainViewModel.kt` UI State 字段。
 */
object LiquidMockFixtures {

    private val fullQualities = listOf(
        QualityItem(type = "128k", size = "3.8M"),
        QualityItem(type = "320k", size = "9.6M"),
        QualityItem(type = "flac", size = "28.4M"),
        QualityItem(type = "flac24bit", size = "64.2M"),
    )

    private val losslessQualities = listOf(
        QualityItem(type = "128k", size = "3.5M"),
        QualityItem(type = "320k", size = "8.9M"),
        QualityItem(type = "flac", size = "26.1M"),
    )

    val sampleTracks: List<MusicInfo> = listOf(
        MusicInfo(
            id = "wy_186016",
            name = "晴天",
            singer = "周杰伦",
            source = MusicSources.WY,
            interval = "04:29",
            meta = MusicMeta(
                songId = "186016",
                albumName = "叶惠美",
                picUrl = "offline://cover/album_yehuimei",
                qualitys = fullQualities,
            ),
        ),
        MusicInfo(
            id = "tx_001abc",
            name = "富士山下",
            singer = "陈奕迅",
            source = MusicSources.TX,
            interval = "04:19",
            meta = MusicMeta(
                songId = "001abc",
                albumName = "What's Going On...?",
                picUrl = "offline://cover/album_fushishanxia",
                qualitys = losslessQualities,
            ),
        ),
        MusicInfo(
            id = "wy_210049",
            name = "夜曲",
            singer = "周杰伦",
            source = MusicSources.WY,
            interval = "03:46",
            meta = MusicMeta(
                songId = "210049",
                albumName = "十一月的萧邦",
                picUrl = "offline://cover/album_chopin",
                qualitys = fullQualities,
            ),
        ),
        MusicInfo(
            id = "kw_98211",
            name = "海阔天空",
            singer = "Beyond",
            source = MusicSources.KW,
            interval = "05:26",
            meta = MusicMeta(
                songId = "98211",
                albumName = "乐与怒",
                picUrl = "offline://cover/album_beyond",
                qualitys = losslessQualities,
            ),
        ),
        MusicInfo(
            id = "kg_44091",
            name = "起风了",
            singer = "买辣椒也用券",
            source = MusicSources.KG,
            interval = "05:25",
            meta = MusicMeta(
                songId = "44091",
                albumName = "起风了",
                picUrl = "offline://cover/album_qifengle",
                qualitys = listOf(QualityItem("128k", "4.8M"), QualityItem("320k", "12.1M")),
            ),
        ),
        MusicInfo(
            id = "wy_526464293",
            name = "River Flows in You (Live)",
            singer = "Yiruma",
            source = MusicSources.MG,
            interval = "03:18",
            meta = MusicMeta(
                songId = "526464293",
                albumName = "First Love (20th Anniversary)",
                picUrl = "offline://cover/album_yiruma",
                qualitys = fullQualities,
            ),
        ),
    )

    val sampleLyrics: List<LyricLine> = listOf(
        LyricLine(timeMs = 15_000L, text = "故事的小黄花 从出生那年就飘着", translation = "The little yellow flower has drifted since birth"),
        LyricLine(timeMs = 28_000L, text = "童年的荡秋千 随记忆一直晃到现在", translation = "Childhood swings swaying softly through memory"),
        LyricLine(timeMs = 42_000L, text = "刮风这天 我试过握着你手", translation = "On that windy afternoon, I tried holding your hand"),
        LyricLine(timeMs = 56_000L, text = "但偏偏 雨渐渐 大到我看你不见", translation = "Yet the rain poured hard until you faded from sight"),
        LyricLine(timeMs = 71_000L, text = "还要多久 我才能在你身边", translation = "How much longer until I can stand beside you again"),
        LyricLine(timeMs = 86_000L, text = "等到放晴的那天 也许我会比较好一点", translation = "When the sky finally clears, maybe I'll feel alright"),
    )

    val samplePlayerState: PlayerUiState = PlayerUiState(
        currentTrack = sampleTracks.first(),
        resolvedCoverUrl = sampleTracks.first().picUrl,
        queue = sampleTracks,
        queueSourceName = "编辑精选 · 华语经典无损",
        queueIndex = 0,
        isPlaying = true,
        isBuffering = false,
        isResolvingUrl = false,
        positionMs = 112_000L,
        durationMs = 269_000L,
        playMode = PlayMode.ORDER,
        activeQualityBadge = "Hi-Res",
        activeSourceBadge = "WY",
        lyrics = sampleLyrics,
        activeLyricIndex = 3,
        isFavorite = true,
        statusMessage = null,
        sleepTimerRemainingSec = 0,
        meshColors = listOf(
            Color(0xFF9A1D3D),
            Color(0xFF283464),
            Color(0xFF5D2152),
            Color(0xFF15192B),
        ),
    )

    val sampleUserPlaylists: List<UserPlaylist> = listOf(
        UserPlaylist(
            id = PlaylistIds.LOVE,
            name = "我喜欢的音乐",
            isBuiltIn = true,
            coverUrl = "offline://cover/playlist_love",
            trackCount = 128,
            position = 0,
        ),
        UserPlaylist(
            id = PlaylistIds.DEFAULT,
            name = "默认试听列表",
            isBuiltIn = true,
            coverUrl = "offline://cover/playlist_default",
            trackCount = 46,
            position = 1,
        ),
        UserPlaylist(
            id = "custom_midnight",
            name = "深夜编码 · Liquid Synth",
            isBuiltIn = false,
            source = MusicSources.WY,
            sourceListId = "773829104",
            coverUrl = "offline://cover/playlist_synth",
            trackCount = 64,
            position = 2,
        ),
        UserPlaylist(
            id = "custom_cantopop",
            name = "港乐黄金时代 (24bit 母带)",
            isBuiltIn = false,
            source = MusicSources.TX,
            sourceListId = "889102341",
            coverUrl = "offline://cover/playlist_cantopop",
            trackCount = 92,
            position = 3,
        ),
    )

    val sampleSongLists: List<SongListItem> = listOf(
        SongListItem(
            id = "pl_01",
            name = "Apple 空间音频 · 华语新声代",
            author = "LX 编辑部",
            img = "offline://cover/discover_spatial",
            desc = "高解析度母带收录，清透声场与温润人声的完美碰撞。",
            playCount = "428.6万",
            total = 50,
            source = MusicSources.WY,
        ),
        SongListItem(
            id = "pl_02",
            name = "深夜霓虹：City Pop 与都市灵魂乐",
            author = "VinylClub",
            img = "offline://cover/discover_citypop",
            desc = "东京与香港八十年代末的浪漫合成器节拍。",
            playCount = "196.2万",
            total = 38,
            source = MusicSources.TX,
        ),
        SongListItem(
            id = "pl_03",
            name = "雨天咖啡馆 · 爵士钢琴独奏",
            author = "BlueNote 选集",
            img = "offline://cover/discover_jazz",
            desc = "适合专注阅读与轻声交谈的暖调器乐。",
            playCount = "112.4万",
            total = 42,
            source = MusicSources.KW,
        ),
        SongListItem(
            id = "pl_04",
            name = "周末公路旅行 · 独立民谣与摇滚",
            author = "RoadTripFM",
            img = "offline://cover/discover_road",
            desc = "把车窗摇下来，让风和吉他声灌满整个午后。",
            playCount = "89.3万",
            total = 60,
            source = MusicSources.KG,
        ),
    )

    val sampleLeaderboards: List<LeaderboardItem> = listOf(
        LeaderboardItem(
            id = "board_hot",
            name = "热歌总榜",
            bangid = "3778678",
            coverImg = "offline://cover/board_hot",
            source = MusicSources.WY,
        ),
        LeaderboardItem(
            id = "board_soar",
            name = "飙升新声榜",
            bangid = "19723756",
            coverImg = "offline://cover/board_soar",
            source = MusicSources.WY,
        ),
        LeaderboardItem(
            id = "board_original",
            name = "独立原创榜",
            bangid = "2884035",
            coverImg = "offline://cover/board_original",
            source = MusicSources.WY,
        ),
        LeaderboardItem(
            id = "board_hires",
            name = "Hi-Res 品质榜",
            bangid = "5059644681",
            coverImg = "offline://cover/board_hires",
            source = MusicSources.TX,
        ),
    )

    val sampleDiscoverState: DiscoverUiState = DiscoverUiState(
        source = MusicSources.WY,
        sorts = listOf(
            SongListSort(id = "hot", name = "最热"),
            SongListSort(id = "new", name = "最新"),
            SongListSort(id = "recommend", name = "编辑推荐"),
        ),
        selectedSortId = "hot",
        tags = SongListTagsResult(
            hotTag = listOf(
                SongListTag(id = "huayu", name = "华语"),
                SongListTag(id = "liuxing", name = "流行"),
                SongListTag(id = "minyao", name = "民谣"),
                SongListTag(id = "dianzi", name = "电子"),
            ),
            tags = listOf(
                SongListTagGroup(
                    name = "语种",
                    list = listOf(
                        SongListTag(id = "huayu", name = "华语"),
                        SongListTag(id = "yueyu", name = "粤语"),
                        SongListTag(id = "oumei", name = "欧美"),
                    ),
                ),
            ),
        ),
        selectedTagId = "huayu",
        selectedTagName = "华语",
        items = sampleSongLists,
        page = 1,
        hasMore = true,
    )

    val sampleChartsState: ChartsUiState = ChartsUiState(
        source = MusicSources.WY,
        boards = sampleLeaderboards,
    )

    val sampleSearchState: SearchUiState = SearchUiState(
        query = "周杰伦",
        committedQuery = "周杰伦",
        source = MusicSources.ALL,
        modeIndex = 0,
        hotKeywords = listOf("晴天", "富士山下", "海阔天空", "起风了", "夜曲", "稻香"),
        musicResults = sampleTracks,
        songListResults = sampleSongLists,
        page = 1,
        hasMore = true,
    )

    val sampleOnlineDetailState: OnlineDetailUiState = OnlineDetailUiState(
        title = "编辑精选 · 华语经典母带馆",
        author = "LX Liquid Studio",
        coverUrl = "offline://cover/discover_spatial",
        description = "精选华语乐坛跨时代代表作，覆盖 24bit/96kHz 无损音源与完整逐字歌词。",
        playCount = "428.6万",
        source = MusicSources.WY,
        sourceListId = "pl_01",
        isLeaderboard = false,
        tracks = sampleTracks,
        page = 1,
        total = sampleTracks.size,
        hasMore = false,
    )

    val sampleCommentsState: CommentsUiState = CommentsUiState(
        music = sampleTracks.first(),
        tabIndex = 0,
        comments = listOf(
            CommentItem(
                id = "c1",
                text = "前奏那两下木吉他扫弦一响起来，整年夏天的风就又吹回来了。",
                timeStr = "2026-08-14",
                userName = "浅水湾晚风",
                likedCount = 48210,
            ),
            CommentItem(
                id = "c2",
                text = "母带版人声细节太通透了，副歌鼓点出来瞬间鸡皮疙瘩起来。",
                timeStr = "2026-09-02",
                userName = "SynthLover",
                likedCount = 12904,
            ),
        ),
        total = 2,
    )

    val sampleSettings: AppSettings = AppSettings(
        themeMode = ThemeMode.LIGHT,
        materialStyle = MaterialStyle.LIQUID_GLASS,
        preferredQuality = AudioQuality.FLAC24,
        playMode = PlayMode.ORDER,
        autoFallbackSource = true,
        audioCacheMaxMb = 1024,
        showLyricTranslation = true,
        showAlbumName = true,
        showTrackDuration = true,
    )

    val sampleUserApiScripts: List<UserApiScript> = listOf(
        UserApiScript(
            id = "script_lx_pro",
            name = "星海聚合解析引擎 v3.2",
            description = "支持 WY / TX / KW / KG / MG 多品质无损解析与自动回退",
            version = "3.2.0",
            author = "LX Community",
            enabled = true,
        ),
    )

    val sampleUserApiStatus: UserApiStatus = UserApiStatus(
        activeScriptId = "script_lx_pro",
        activeScriptName = "星海聚合解析引擎 v3.2",
        initialized = true,
        supportedSources = mapOf(
            MusicSources.WY to listOf("128k", "320k", "flac", "flac24bit"),
            MusicSources.TX to listOf("128k", "320k", "flac"),
        ),
    )

    val sampleSyncState: SyncState = SyncState(
        connected = true,
        statusText = "已同步 · Desktop v2.9",
        lastSyncedAt = 1_763_990_400_000L,
    )

    val sampleLogs: List<AppLogEntry> = listOf(
        AppLogEntry(level = "INFO", tag = "Player", message = "Resolved Hi-Res FLAC stream (WY)"),
        AppLogEntry(level = "INFO", tag = "Palette", message = "Extracted adaptive mesh colors"),
    )

    val defaultMeshColors: List<Color> = DefaultMeshPalette
}

/**
 * Roborazzi + Robolectric Native Graphics 截图测试基类：
 * - 通过 `@Config(application = Application::class)` 绕过 [cn.toside.music.liquid.LxLiquidApp]，
 *   彻底避免在 x86_64 Linux CI 上加载 QuickJS `.so` / Room / Media3 原生库崩溃。
 * - 默认模拟旗舰手机视口 `w412dp-h915dp-xxhdpi` (API 34)，并可通过 [DeviceProfile] 切换平板竖屏/横屏。
 * - 也可在子类/测试方法上叠加方法级 `@Config(qualifiers = "w840dp-h1180dp-xhdpi")` 或 `"w1180dp-h820dp-xhdpi-land"`。
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
    sdk = [34],
    application = Application::class,
    qualifiers = "w412dp-h915dp-xxhdpi",
)
abstract class ScreenshotTestBase {

    enum class DeviceProfile(
        val widthDp: Int,
        val heightDp: Int,
    ) {
        PHONE_PORTRAIT(widthDp = 412, heightDp = 915),
        TABLET_PORTRAIT(widthDp = 840, heightDp = 1180),
        TABLET_LANDSCAPE(widthDp = 1180, heightDp = 820),
    }

    @Before
    fun installOfflineImageLoader() {
        ShadowChoreographer.setFrameDelay(Duration.ofMillis(16))
        ShadowChoreographer.setPaused(true)
        FakeCoilImageLoader.install(ApplicationProvider.getApplicationContext())
    }

    @After
    fun resetOfflineImageLoader() {
        FakeCoilImageLoader.reset()
    }

    protected fun captureScreen(
        name: String,
        themeMode: ThemeMode = ThemeMode.LIGHT,
        materialStyle: MaterialStyle = MaterialStyle.LIQUID_GLASS,
        device: DeviceProfile = DeviceProfile.PHONE_PORTRAIT,
        content: @Composable (hazeState: HazeState) -> Unit,
    ) {
        val isDark = themeMode == ThemeMode.DARK
        val relativePath = when (device) {
            DeviceProfile.PHONE_PORTRAIT -> {
                val modeFolder = if (isDark) "dark" else "light"
                "$modeFolder/$name.png"
            }
            DeviceProfile.TABLET_PORTRAIT -> {
                val suffix = if (isDark) "dark" else "light"
                "tablet/${name}_portrait_${suffix}.png"
            }
            DeviceProfile.TABLET_LANDSCAPE -> {
                val suffix = if (isDark) "dark" else "light"
                "tablet/${name}_landscape_${suffix}.png"
            }
        }

        val options = RoborazziComposeOptions {
            size(widthDp = device.widthDp, heightDp = device.heightDp)
            uiMode(
                if (isDark) Configuration.UI_MODE_NIGHT_YES
                else Configuration.UI_MODE_NIGHT_NO,
            )
            background(
                showBackground = true,
                backgroundColor = if (isDark) 0xFF0B0B0EL else 0xFFF5F5F8L,
            )
            addOption(SettleAsyncRenderOption())
        }

        captureRoboImage(
            filePath = relativePath,
            roborazziComposeOptions = options,
        ) {
            val hazeState = remember { HazeState() }
            LxLiquidTheme(
                themeMode = themeMode,
                materialStyle = materialStyle,
                hazeState = hazeState,
            ) {
                val colors = LocalLiquidColors.current
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background),
                ) {
                    content(hazeState)
                }
            }
        }
    }

    protected fun captureLightAndDark(
        baseName: String,
        materialStyle: MaterialStyle = MaterialStyle.LIQUID_GLASS,
        device: DeviceProfile = DeviceProfile.PHONE_PORTRAIT,
        content: @Composable (hazeState: HazeState) -> Unit,
    ) {
        captureScreen(
            name = baseName,
            themeMode = ThemeMode.LIGHT,
            materialStyle = materialStyle,
            device = device,
            content = content,
        )
        captureScreen(
            name = baseName,
            themeMode = ThemeMode.DARK,
            materialStyle = materialStyle,
            device = device,
            content = content,
        )
    }

    private class SettleAsyncRenderOption : RoborazziComposeCaptureOption {
        override fun beforeCapture() {
            // Advance simulated time in bounded steps so perpetual animations can render.
            val mainLooper = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
            repeat(20) { mainLooper.idleFor(Duration.ofMillis(16)) }
        }

        override fun afterCapture() = Unit
    }
}
