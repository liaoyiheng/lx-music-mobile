package cn.toside.music.liquid.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cn.toside.music.liquid.data.model.AppSettings
import cn.toside.music.liquid.data.model.AudioQuality
import cn.toside.music.liquid.data.model.DesktopLyricConfig
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.data.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lx_liquid_settings")

class SettingsStore(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MATERIAL_STYLE = stringPreferencesKey("material_style")
        val QUALITY = stringPreferencesKey("preferred_quality")
        val PLAY_MODE = stringPreferencesKey("play_mode")
        val AUTO_FALLBACK = booleanPreferencesKey("auto_fallback")
        val CACHE_MAX_MB = intPreferencesKey("cache_max_mb")
        val LYRIC_TRANS = booleanPreferencesKey("lyric_trans")
        val LYRIC_ROMA = booleanPreferencesKey("lyric_roma")
        val SHOW_ALBUM = booleanPreferencesKey("show_album")
        val SHOW_DURATION = booleanPreferencesKey("show_duration")
        val ADD_TO_TOP = booleanPreferencesKey("add_to_top")
        val SEARCH_HISTORY = booleanPreferencesKey("search_history")
        val HOT_SEARCH = booleanPreferencesKey("hot_search")
        val SEARCH_TIPS = booleanPreferencesKey("search_tips")
        val PAUSE_NOISY = booleanPreferencesKey("pause_noisy")
        val LOCK_SCREEN_ENABLED = booleanPreferencesKey("lock_screen_enabled")
        val NOTIFICATION_SHOW_LYRIC = booleanPreferencesKey("notification_show_lyric")
        val ACTIVE_API_ID = stringPreferencesKey("active_user_api_id")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val SYNC_URL = stringPreferencesKey("sync_url")
        val SYNC_CODE = stringPreferencesKey("sync_code")
        val SYNC_CLIENT_KEY_JSON = stringPreferencesKey("sync_client_key_json")
        val DL_ENABLED = booleanPreferencesKey("dl_enabled")
        val DL_LOCKED = booleanPreferencesKey("dl_locked")
        val DL_TRANS = booleanPreferencesKey("dl_trans")
        val DL_ROMA = booleanPreferencesKey("dl_roma")
        val DL_FONT = floatPreferencesKey("dl_font")
        val DL_OPACITY = floatPreferencesKey("dl_opacity")
        val DL_ACTIVE_COLOR = stringPreferencesKey("dl_active_color")
        val DL_INACTIVE_COLOR = stringPreferencesKey("dl_inactive_color")
        val DL_POS_Y = floatPreferencesKey("dl_pos_y")
    }

    val settings: StateFlow<AppSettings> = context.dataStore.data
        .map { p ->
            AppSettings(
                themeMode = p[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.LIGHT,
                materialStyle = p[Keys.MATERIAL_STYLE]?.let { runCatching { MaterialStyle.valueOf(it) }.getOrNull() } ?: MaterialStyle.LIQUID_GLASS,
                preferredQuality = p[Keys.QUALITY]?.let { AudioQuality.fromKey(it) } ?: AudioQuality.FLAC,
                playMode = p[Keys.PLAY_MODE]?.let { runCatching { PlayMode.valueOf(it) }.getOrNull() } ?: PlayMode.ORDER,
                autoFallbackSource = p[Keys.AUTO_FALLBACK] ?: true,
                audioCacheMaxMb = p[Keys.CACHE_MAX_MB] ?: 512,
                showLyricTranslation = p[Keys.LYRIC_TRANS] ?: true,
                showLyricRoma = p[Keys.LYRIC_ROMA] ?: false,
                showAlbumName = p[Keys.SHOW_ALBUM] ?: true,
                showTrackDuration = p[Keys.SHOW_DURATION] ?: true,
                addMusicToTop = p[Keys.ADD_TO_TOP] ?: false,
                enableSearchHistory = p[Keys.SEARCH_HISTORY] ?: true,
                enableHotSearch = p[Keys.HOT_SEARCH] ?: true,
                enableSearchTips = p[Keys.SEARCH_TIPS] ?: true,
                pauseOnNoisy = p[Keys.PAUSE_NOISY] ?: true,
                lockScreenEnabled = p[Keys.LOCK_SCREEN_ENABLED] ?: true,
                notificationShowLyric = p[Keys.NOTIFICATION_SHOW_LYRIC] ?: false,
                activeUserApiId = p[Keys.ACTIVE_API_ID]?.takeIf { it.isNotBlank() },
                syncEnabled = p[Keys.SYNC_ENABLED] ?: false,
                syncServerUrl = p[Keys.SYNC_URL] ?: "",
                syncAuthCode = p[Keys.SYNC_CODE] ?: "",
                desktopLyric = DesktopLyricConfig(
                    enabled = p[Keys.DL_ENABLED] ?: false,
                    locked = p[Keys.DL_LOCKED] ?: false,
                    showTranslation = p[Keys.DL_TRANS] ?: true,
                    showRoma = p[Keys.DL_ROMA] ?: false,
                    fontSizeSp = p[Keys.DL_FONT] ?: 18f,
                    opacity = p[Keys.DL_OPACITY] ?: 0.92f,
                    activeColorHex = p[Keys.DL_ACTIVE_COLOR] ?: "#FA243C",
                    inactiveColorHex = p[Keys.DL_INACTIVE_COLOR] ?: "#FFFFFF",
                    posYFraction = p[Keys.DL_POS_Y] ?: 0.14f,
                ),
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, AppSettings())

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(settings.value)
        context.dataStore.edit { p ->
            p[Keys.THEME_MODE] = next.themeMode.name
            p[Keys.MATERIAL_STYLE] = next.materialStyle.name
            p[Keys.QUALITY] = next.preferredQuality.key
            p[Keys.PLAY_MODE] = next.playMode.name
            p[Keys.AUTO_FALLBACK] = next.autoFallbackSource
            p[Keys.CACHE_MAX_MB] = next.audioCacheMaxMb
            p[Keys.LYRIC_TRANS] = next.showLyricTranslation
            p[Keys.LYRIC_ROMA] = next.showLyricRoma
            p[Keys.SHOW_ALBUM] = next.showAlbumName
            p[Keys.SHOW_DURATION] = next.showTrackDuration
            p[Keys.ADD_TO_TOP] = next.addMusicToTop
            p[Keys.SEARCH_HISTORY] = next.enableSearchHistory
            p[Keys.HOT_SEARCH] = next.enableHotSearch
            p[Keys.SEARCH_TIPS] = next.enableSearchTips
            p[Keys.PAUSE_NOISY] = next.pauseOnNoisy
            p[Keys.LOCK_SCREEN_ENABLED] = next.lockScreenEnabled
            p[Keys.NOTIFICATION_SHOW_LYRIC] = next.notificationShowLyric
            p[Keys.ACTIVE_API_ID] = next.activeUserApiId.orEmpty()
            p[Keys.SYNC_ENABLED] = next.syncEnabled
            p[Keys.SYNC_URL] = next.syncServerUrl
            p[Keys.SYNC_CODE] = next.syncAuthCode
            p[Keys.DL_ENABLED] = next.desktopLyric.enabled
            p[Keys.DL_LOCKED] = next.desktopLyric.locked
            p[Keys.DL_TRANS] = next.desktopLyric.showTranslation
            p[Keys.DL_ROMA] = next.desktopLyric.showRoma
            p[Keys.DL_FONT] = next.desktopLyric.fontSizeSp
            p[Keys.DL_OPACITY] = next.desktopLyric.opacity
            p[Keys.DL_ACTIVE_COLOR] = next.desktopLyric.activeColorHex
            p[Keys.DL_INACTIVE_COLOR] = next.desktopLyric.inactiveColorHex
            p[Keys.DL_POS_Y] = next.desktopLyric.posYFraction
        }
    }

    suspend fun getSyncKeyInfoJson(): String? = context.dataStore.data.map { it[Keys.SYNC_CLIENT_KEY_JSON] }
        .stateIn(scope).value

    suspend fun saveSyncKeyInfoJson(json: String) {
        context.dataStore.edit { it[Keys.SYNC_CLIENT_KEY_JSON] = json }
    }
}
