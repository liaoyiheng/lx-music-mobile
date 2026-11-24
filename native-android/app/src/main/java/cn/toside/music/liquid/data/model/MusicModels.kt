package cn.toside.music.liquid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object MusicSources {
    const val ALL = "all"
    const val KW = "kw"
    const val KG = "kg"
    const val TX = "tx"
    const val WY = "wy"
    const val MG = "mg"
    const val LOCAL = "local"

    val ONLINE = listOf(WY, TX, KW, KG, MG)
    val SEARCH_SOURCES = listOf(ALL, WY, TX, KW, KG, MG)

    fun displayName(id: String): String = when (id) {
        ALL -> "聚合"
        WY -> "网易云"
        TX -> "QQ音乐"
        KW -> "酷我"
        KG -> "酷狗"
        MG -> "咪咕"
        LOCAL -> "本地"
        else -> id.uppercase()
    }

    fun shortBadge(id: String): String = when (id) {
        WY -> "WY"
        TX -> "TX"
        KW -> "KW"
        KG -> "KG"
        MG -> "MG"
        LOCAL -> "LOCAL"
        else -> id.uppercase()
    }
}

object PlaylistIds {
    const val DEFAULT = "default"
    const val LOVE = "love"
    const val TEMP = "temp"
}

@Serializable
data class QualityItem(
    val type: String = "128k",
    val size: String = "",
)

@Serializable
data class MusicMeta(
    val songId: String = "",
    val albumName: String = "",
    val picUrl: String? = null,
    val qualitys: List<QualityItem> = emptyList(),
    @SerialName("_qualitys")
    val qualityMap: Map<String, Map<String, String>> = emptyMap(),
    val albumId: String? = null,
    val hash: String? = null,
    val strMediaMid: String? = null,
    val albumMid: String? = null,
    val id: String? = null,
    val copyrightId: String? = null,
    val lrcUrl: String? = null,
    val mrcUrl: String? = null,
    val trcUrl: String? = null,
    val filePath: String? = null,
    val ext: String? = null,
)

@Serializable
data class MusicInfo(
    val id: String,
    val name: String,
    val singer: String = "",
    val source: String = MusicSources.WY,
    val interval: String? = null,
    val meta: MusicMeta = MusicMeta(),
) {
    val picUrl: String? get() = meta.picUrl?.takeIf { it.isNotBlank() }
    val albumName: String get() = meta.albumName

    fun bestAvailableQuality(preferred: AudioQuality): String {
        val supported = meta.qualitys.map { it.type }.toSet()
        if (supported.isEmpty()) return preferred.key
        val order = AudioQuality.fallbackOrder(preferred)
        return order.firstOrNull { it in supported } ?: meta.qualitys.last().type
    }

    fun highestQualityBadge(): String {
        val types = meta.qualitys.map { it.type }.toSet()
        return when {
            "flac24bit" in types -> "Hi-Res"
            "flac" in types -> "无损"
            "320k" in types -> "HQ"
            else -> "SQ"
        }
    }
}

@Serializable
data class SongListItem(
    val id: String,
    val name: String,
    val author: String = "",
    val img: String? = null,
    val desc: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
    val total: Int? = null,
    val source: String = MusicSources.WY,
    val time: String? = null,
)

@Serializable
data class SongListSort(
    val id: String,
    val name: String,
    val tid: String? = null,
)

@Serializable
data class SongListTag(
    val id: String,
    val name: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    @SerialName("parent_name")
    val parentName: String? = null,
    val source: String? = null,
)

@Serializable
data class SongListTagGroup(
    val name: String,
    val list: List<SongListTag> = emptyList(),
)

@Serializable
data class SongListTagsResult(
    val hotTag: List<SongListTag> = emptyList(),
    val tags: List<SongListTagGroup> = emptyList(),
)

@Serializable
data class SongListPageResult(
    val list: List<SongListItem> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 30,
    val source: String = MusicSources.WY,
)

@Serializable
data class SongListDetailInfo(
    val name: String? = null,
    val img: String? = null,
    val desc: String? = null,
    val author: String? = null,
    @SerialName("play_count")
    val playCount: String? = null,
)

@Serializable
data class SongListDetailResult(
    val list: List<MusicInfo> = emptyList(),
    val page: Int = 1,
    val limit: Int = 100,
    val total: Int = 0,
    val source: String = MusicSources.WY,
    val info: SongListDetailInfo = SongListDetailInfo(),
)

@Serializable
data class SearchMusicResult(
    val list: List<MusicInfo> = emptyList(),
    val allPage: Int = 0,
    val limit: Int = 30,
    val total: Int = 0,
    val source: String = MusicSources.WY,
)

@Serializable
data class LeaderboardItem(
    val id: String,
    val name: String,
    val bangid: String,
    val coverImg: String? = null,
    val source: String = MusicSources.WY,
)

@Serializable
data class CommentItem(
    val id: String = "",
    val text: String = "",
    val timeStr: String = "",
    val userName: String = "",
    val avatar: String? = null,
    val likedCount: Int = 0,
    val reply: List<CommentItem> = emptyList(),
)

@Serializable
data class CommentsPageResult(
    val source: String = "",
    val comments: List<CommentItem> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val maxPage: Int = 1,
)

@Serializable
data class LyricData(
    val lyric: String = "",
    val tlyric: String = "",
    val rlyric: String = "",
    val lxlyric: String = "",
)

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val translation: String? = null,
    val romaji: String? = null,
)

data class UserPlaylist(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean = false,
    val source: String? = null,
    val sourceListId: String? = null,
    val coverUrl: String? = null,
    val trackCount: Int = 0,
    val position: Int = 0,
    val updatedAt: Long = 0L,
)

data class UserApiScript(
    val id: String,
    val name: String,
    val description: String = "",
    val version: String = "",
    val author: String = "",
    val homepage: String = "",
    val script: String = "",
    val enabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

data class UserApiStatus(
    val activeScriptId: String? = null,
    val activeScriptName: String? = null,
    val initialized: Boolean = false,
    val errorMessage: String? = null,
    val supportedSources: Map<String, List<String>> = emptyMap(),
)

enum class PlayMode(val label: String) {
    ORDER("顺序播放"),
    SHUFFLE("随机播放"),
    SINGLE("单曲循环"),
    SINGLE_STOP("播完暂停");

    fun next(): PlayMode = entries[(ordinal + 1) % entries.size]
}

enum class AudioQuality(val key: String, val label: String, val badge: String) {
    Q128("128k", "标准音质 (128k)", "SQ"),
    Q320("320k", "极高音质 (320k)", "HQ"),
    FLAC("flac", "无损音质 (FLAC)", "无损"),
    FLAC24("flac24bit", "母带音质 (Hi-Res)", "Hi-Res");

    companion object {
        fun fromKey(key: String): AudioQuality = entries.firstOrNull { it.key == key } ?: FLAC

        fun fallbackOrder(preferred: AudioQuality): List<String> {
            val all = listOf("flac24bit", "flac", "320k", "128k")
            val idx = all.indexOf(preferred.key).coerceAtLeast(0)
            return all.subList(idx, all.size) + all.subList(0, idx)
        }
    }
}

enum class ThemeMode(val label: String) {
    LIGHT("浅色亮面"),
    DARK("深色暗夜"),
    SYSTEM("跟随系统"),
}

enum class MaterialStyle(val label: String, val desc: String) {
    LIQUID_GLASS("液态玻璃", "Android 12+ 实时 GPU 高斯模糊与 1.65× 色彩折射"),
    CLASSIC_FROST("经典毛玻璃", "柔和半透明磨砂，低饱和稳重质感"),
    SOLID_MATTE("固态哑光", "纯色哑光悬浮卡片，极致省电清晰"),
}

data class DesktopLyricConfig(
    val enabled: Boolean = false,
    val locked: Boolean = false,
    val showTranslation: Boolean = true,
    val showRoma: Boolean = false,
    val fontSizeSp: Float = 18f,
    val opacity: Float = 0.92f,
    val activeColorHex: String = "#FA243C",
    val inactiveColorHex: String = "#FFFFFF",
    val posYFraction: Float = 0.14f,
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val materialStyle: MaterialStyle = MaterialStyle.LIQUID_GLASS,
    val preferredQuality: AudioQuality = AudioQuality.FLAC,
    val playMode: PlayMode = PlayMode.ORDER,
    val autoFallbackSource: Boolean = true,
    val audioCacheMaxMb: Int = 512,
    val showLyricTranslation: Boolean = true,
    val showLyricRoma: Boolean = false,
    val showAlbumName: Boolean = true,
    val showTrackDuration: Boolean = true,
    val addMusicToTop: Boolean = false,
    val enableSearchHistory: Boolean = true,
    val enableHotSearch: Boolean = true,
    val enableSearchTips: Boolean = true,
    val pauseOnNoisy: Boolean = true,
    val lockScreenEnabled: Boolean = true,
    val notificationShowLyric: Boolean = false,
    val activeUserApiId: String? = null,
    val syncEnabled: Boolean = false,
    val syncServerUrl: String = "",
    val syncAuthCode: String = "",
    val desktopLyric: DesktopLyricConfig = DesktopLyricConfig(),
)

data class SyncState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val statusText: String = "未连接",
    val lastSyncedAt: Long = 0L,
)

data class AppLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO",
    val tag: String = "App",
    val message: String,
)
