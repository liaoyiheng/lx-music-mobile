package cn.toside.music.liquid.data

import android.content.Context
import cn.toside.music.liquid.data.db.AppDatabase
import cn.toside.music.liquid.data.db.DislikeRuleEntity
import cn.toside.music.liquid.data.db.KvCacheEntity
import cn.toside.music.liquid.data.db.PlaylistEntity
import cn.toside.music.liquid.data.db.PlaylistTrackEntity
import cn.toside.music.liquid.data.db.SearchHistoryEntity
import cn.toside.music.liquid.data.db.UserApiScriptEntity
import cn.toside.music.liquid.data.model.LyricData
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.data.model.UserApiScript
import cn.toside.music.liquid.data.model.UserPlaylist
import cn.toside.music.liquid.data.prefs.SettingsStore
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.engine.HttpClientProvider
import cn.toside.music.liquid.engine.MusicSdkEngine
import cn.toside.music.liquid.engine.UserApiEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File
import java.util.UUID

class MusicRepository(
    private val context: Context,
    val db: AppDatabase,
    val settingsStore: SettingsStore,
    val sdk: MusicSdkEngine,
    val userApi: UserApiEngine,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun ensureBuiltInPlaylists() = withContext(Dispatchers.IO) {
        val dao = db.playlistDao()
        if (dao.getPlaylist(PlaylistIds.DEFAULT) == null) {
            dao.upsertPlaylist(
                PlaylistEntity(
                    id = PlaylistIds.DEFAULT,
                    name = "试听列表",
                    isBuiltIn = true,
                    position = 0,
                )
            )
        }
        if (dao.getPlaylist(PlaylistIds.LOVE) == null) {
            dao.upsertPlaylist(
                PlaylistEntity(
                    id = PlaylistIds.LOVE,
                    name = "我的收藏",
                    isBuiltIn = true,
                    position = 1,
                )
            )
        }
        // Auto-load saved active user API script on boot
        val activeId = settingsStore.settings.value.activeUserApiId
        val scripts = db.userApiDao().getAllScripts()
        val target = scripts.firstOrNull { it.id == activeId } ?: scripts.firstOrNull { it.enabled }
        if (target != null) {
            userApi.loadScript(target.toModel())
        }
    }

    fun observePlaylists(): Flow<List<UserPlaylist>> {
        val dao = db.playlistDao()
        return dao.observePlaylists().map { list ->
            list.map { entity ->
                val count = dao.getTrackCount(entity.id)
                val cover = entity.coverUrl ?: dao.getTracks(entity.id)
                    .firstOrNull()
                    ?.let { decodeMusic(it.musicJson)?.picUrl }
                UserPlaylist(
                    id = entity.id,
                    name = entity.name,
                    isBuiltIn = entity.isBuiltIn,
                    source = entity.source,
                    sourceListId = entity.sourceListId,
                    coverUrl = cover,
                    trackCount = count,
                    position = entity.position,
                    updatedAt = entity.updatedAt,
                )
            }
        }
    }

    fun observePlaylistTracks(playlistId: String): Flow<List<MusicInfo>> {
        return db.playlistDao().observeTracks(playlistId).map { entities ->
            entities.mapNotNull { decodeMusic(it.musicJson) }
        }
    }

    suspend fun getPlaylistTracks(playlistId: String): List<MusicInfo> = withContext(Dispatchers.IO) {
        db.playlistDao().getTracks(playlistId).mapNotNull { decodeMusic(it.musicJson) }
    }

    suspend fun getAllPlaylistsWithTracks(): List<Pair<UserPlaylist, List<MusicInfo>>> = withContext(Dispatchers.IO) {
        val dao = db.playlistDao()
        dao.getAllPlaylists().map { p ->
            val tracks = dao.getTracks(p.id).mapNotNull { decodeMusic(it.musicJson) }
            UserPlaylist(
                id = p.id,
                name = p.name,
                isBuiltIn = p.isBuiltIn,
                source = p.source,
                sourceListId = p.sourceListId,
                coverUrl = p.coverUrl ?: tracks.firstOrNull()?.picUrl,
                trackCount = tracks.size,
                position = p.position,
                updatedAt = p.updatedAt,
            ) to tracks
        }
    }

    suspend fun createPlaylist(
        name: String,
        tracks: List<MusicInfo> = emptyList(),
        source: String? = null,
        sourceListId: String? = null,
        coverUrl: String? = null,
        explicitId: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val dao = db.playlistDao()
        val id = explicitId ?: "userlist_${UUID.randomUUID().toString().take(12)}"
        val pos = dao.getAllPlaylists().size + 1
        dao.upsertPlaylist(
            PlaylistEntity(
                id = id,
                name = name.trim().ifBlank { "新建歌单" },
                isBuiltIn = false,
                source = source,
                sourceListId = sourceListId,
                coverUrl = coverUrl ?: tracks.firstOrNull()?.picUrl,
                position = pos,
                updatedAt = System.currentTimeMillis(),
            )
        )
        if (tracks.isNotEmpty()) {
            addTracksToPlaylist(id, tracks)
        }
        id
    }

    suspend fun renamePlaylist(id: String, newName: String) = withContext(Dispatchers.IO) {
        if (newName.isBlank()) return@withContext
        db.playlistDao().renamePlaylist(id, newName.trim())
    }

    suspend fun deletePlaylist(id: String) = withContext(Dispatchers.IO) {
        if (id == PlaylistIds.DEFAULT || id == PlaylistIds.LOVE) return@withContext
        db.playlistDao().clearPlaylistTracks(id)
        db.playlistDao().deleteCustomPlaylist(id)
    }

    suspend fun addTracksToPlaylist(playlistId: String, tracks: List<MusicInfo>) = withContext(Dispatchers.IO) {
        if (tracks.isEmpty()) return@withContext
        val dao = db.playlistDao()
        val addToTop = settingsStore.settings.value.addMusicToTop
        val base = if (addToTop) dao.getMinSortOrder(playlistId) - tracks.size - 1 else dao.getMaxSortOrder(playlistId) + 1
        val entities = tracks.mapIndexed { index, music ->
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = music.id,
                musicJson = json.encodeToString(music),
                sortOrder = base + index,
            )
        }
        dao.upsertTracks(entities)
        val p = dao.getPlaylist(playlistId)
        if (p != null && p.coverUrl.isNullOrBlank()) {
            tracks.firstNotNullOfOrNull { it.picUrl }?.let { dao.updateCover(playlistId, it) }
        }
    }

    suspend fun replacePlaylistTracks(playlistId: String, tracks: List<MusicInfo>) = withContext(Dispatchers.IO) {
        val dao = db.playlistDao()
        dao.clearPlaylistTracks(playlistId)
        val entities = tracks.mapIndexed { index, music ->
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = music.id,
                musicJson = json.encodeToString(music),
                sortOrder = index.toLong(),
            )
        }
        dao.upsertTracks(entities)
        tracks.firstNotNullOfOrNull { it.picUrl }?.let { dao.updateCover(playlistId, it) }
    }

    suspend fun removeTracksFromPlaylist(playlistId: String, trackIds: List<String>) = withContext(Dispatchers.IO) {
        if (trackIds.isEmpty()) return@withContext
        db.playlistDao().deleteTracks(playlistId, trackIds)
    }

    fun observeIsFavorite(trackId: String): Flow<Boolean> =
        db.playlistDao().observeContainsTrack(PlaylistIds.LOVE, trackId)

    suspend fun isFavorite(trackId: String): Boolean = withContext(Dispatchers.IO) {
        db.playlistDao().containsTrack(PlaylistIds.LOVE, trackId)
    }

    suspend fun toggleFavorite(music: MusicInfo): Boolean = withContext(Dispatchers.IO) {
        val dao = db.playlistDao()
        val exists = dao.containsTrack(PlaylistIds.LOVE, music.id)
        if (exists) {
            dao.deleteTracks(PlaylistIds.LOVE, listOf(music.id))
            false
        } else {
            addTracksToPlaylist(PlaylistIds.LOVE, listOf(music))
            true
        }
    }

    // ---- Custom User API Scripts ----
    fun observeUserApiScripts(): Flow<List<UserApiScript>> =
        db.userApiDao().observeScripts().map { list -> list.map { it.toModel() } }

    suspend fun importUserApiScript(rawScript: String, fallbackName: String = "自定义音源", activate: Boolean = true): UserApiScript =
        withContext(Dispatchers.IO) {
            val parsed = UserApiEngine.parseScriptMeta(rawScript, fallbackName)
            val entity = UserApiScriptEntity(
                id = parsed.id,
                name = parsed.name,
                description = parsed.description,
                version = parsed.version,
                author = parsed.author,
                homepage = parsed.homepage,
                script = parsed.script,
                enabled = activate,
                createdAt = parsed.createdAt,
            )
            db.userApiDao().upsert(entity)
            if (activate) {
                activateUserApiScript(entity.id)
            }
            entity.toModel()
        }

    suspend fun importUserApiFromUrl(url: String): UserApiScript = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url.trim())
            .header("User-Agent", HttpClientProvider.DESKTOP_UA)
            .build()
        val body = HttpClientProvider.okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("下载失败 (HTTP ${resp.code})")
            resp.body?.string().orEmpty()
        }
        if (body.length < 20) throw RuntimeException("脚本内容过短或无效")
        val fallback = url.substringAfterLast('/').substringBefore('?').ifBlank { "在线自定义源" }
        importUserApiScript(body, fallbackName = fallback, activate = true)
    }

    suspend fun activateUserApiScript(scriptId: String?) = withContext(Dispatchers.IO) {
        val all = db.userApiDao().getAllScripts()
        all.forEach { s ->
            val shouldEnable = s.id == scriptId
            if (s.enabled != shouldEnable) {
                db.userApiDao().upsert(s.copy(enabled = shouldEnable))
            }
        }
        settingsStore.update { it.copy(activeUserApiId = scriptId) }
        if (scriptId == null) {
            userApi.unload()
        } else {
            val target = db.userApiDao().getScript(scriptId)?.toModel()
            if (target != null) userApi.loadScript(target)
        }
    }

    suspend fun deleteUserApiScript(scriptId: String) = withContext(Dispatchers.IO) {
        if (settingsStore.settings.value.activeUserApiId == scriptId) {
            activateUserApiScript(null)
        }
        db.userApiDao().delete(scriptId)
    }

    // ---- Search History ----
    fun observeSearchHistory(): Flow<List<String>> = db.searchHistoryDao().observeHistory()

    suspend fun recordSearchHistory(keyword: String) = withContext(Dispatchers.IO) {
        if (!settingsStore.settings.value.enableSearchHistory) return@withContext
        val q = keyword.trim()
        if (q.isNotEmpty()) {
            db.searchHistoryDao().upsert(SearchHistoryEntity(q, System.currentTimeMillis()))
        }
    }

    suspend fun deleteSearchHistory(keyword: String) = withContext(Dispatchers.IO) {
        db.searchHistoryDao().delete(keyword)
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        db.searchHistoryDao().clearAll()
    }

    // ---- Dislike Rules ----
    fun observeDislikeRules(): Flow<List<String>> = db.dislikeDao().observeRules()

    suspend fun saveDislikeRulesText(rawLines: String) = withContext(Dispatchers.IO) {
        val rules = rawLines.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .distinct()
            .map { DislikeRuleEntity(it) }
            .toList()
        db.dislikeDao().clearAll()
        if (rules.isNotEmpty()) db.dislikeDao().upsertAll(rules)
    }

    suspend fun addDislikeRule(music: MusicInfo) = withContext(Dispatchers.IO) {
        val rule = if (music.singer.isNotBlank()) "${music.name}@${music.singer}" else music.name
        db.dislikeDao().upsertAll(listOf(DislikeRuleEntity(rule)))
    }

    suspend fun isDisliked(music: MusicInfo): Boolean = withContext(Dispatchers.IO) {
        val rules = db.dislikeDao().getAllRules()
        if (rules.isEmpty()) return@withContext false
        val nameL = music.name.lowercase()
        val singerL = music.singer.lowercase()
        rules.any { raw ->
            val r = raw.trim().lowercase()
            when {
                r.contains("@") -> {
                    val (n, s) = r.split("@", limit = 2)
                    (n.isBlank() || nameL == n) && (s.isBlank() || singerL.contains(s))
                }
                else -> nameL == r
            }
        }
    }

    // ---- Cached Lyric & Pic & URL ----
    suspend fun getCachedLyric(music: MusicInfo): LyricData = withContext(Dispatchers.IO) {
        val key = "lrc:${music.id}"
        val cached = db.kvCacheDao().get(key)
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            runCatching { json.decodeFromString<LyricData>(cached.value) }.getOrNull()?.let { return@withContext it }
        }
        val fetched = userApi.getLyricOverride(music)
            ?: runCatching { sdk.getLyric(music) }.getOrDefault(LyricData())
        if (fetched.lyric.isNotBlank()) {
            db.kvCacheDao().put(
                KvCacheEntity(
                    cacheKey = key,
                    value = json.encodeToString(fetched),
                    expiresAt = System.currentTimeMillis() + 14L * 24 * 3600 * 1000,
                )
            )
        }
        fetched
    }

    suspend fun resolvePicUrl(music: MusicInfo): String? = withContext(Dispatchers.IO) {
        music.picUrl?.let { return@withContext it }
        val key = "pic:${music.id}"
        val cached = db.kvCacheDao().get(key)
        if (cached != null && cached.expiresAt > System.currentTimeMillis() && cached.value.isNotBlank()) {
            return@withContext cached.value
        }
        val fetched = runCatching { sdk.getPic(music) }.getOrNull()?.takeIf { it.startsWith("http") }
        if (fetched != null) {
            db.kvCacheDao().put(
                KvCacheEntity(key, fetched, System.currentTimeMillis() + 30L * 24 * 3600 * 1000)
            )
        }
        fetched
    }

    suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        db.kvCacheDao().clearAll()
        runCatching { File(context.cacheDir, "image_cache").deleteRecursively() }
        runCatching { File(context.cacheDir, "media_cache").deleteRecursively() }
        AppLogger.info("Cache", "已清除全部图片、歌词与音频缓存")
    }

    fun calculateCacheSizeBytes(): Long {
        fun dirSize(f: File): Long = if (!f.exists()) 0L else if (f.isFile) f.length() else f.listFiles()?.sumOf { dirSize(it) } ?: 0L
        return dirSize(File(context.cacheDir, "image_cache")) + dirSize(File(context.cacheDir, "media_cache"))
    }

    private fun decodeMusic(raw: String): MusicInfo? =
        runCatching { json.decodeFromString<MusicInfo>(raw) }.getOrNull()

    private fun UserApiScriptEntity.toModel() = UserApiScript(
        id = id,
        name = name,
        description = description,
        version = version,
        author = author,
        homepage = homepage,
        script = script,
        enabled = enabled,
        createdAt = createdAt,
    )
}
