package cn.toside.music.liquid.engine

import android.content.Context
import android.net.Uri
import cn.toside.music.liquid.data.MusicRepository
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicMeta
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.data.model.QualityItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * 100% 兼容原版洛雪 .lxmc (GZIP/JSON) 与 .json 备份文件的导入/导出管理器。
 * 支持 playList_v2 / playList / allData_v2 / allData / playListPart / defautlList。
 */
class BackupManager(
    private val context: Context,
    private val repository: MusicRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    data class ImportSummary(
        val playlistCount: Int,
        val trackCount: Int,
    )

    suspend fun exportPlaylistsToUri(uri: Uri, compressGzip: Boolean = true): ImportSummary = withContext(Dispatchers.IO) {
        val all = repository.getAllPlaylistsWithTracks()
        var totalTracks = 0
        val dataArr = buildJsonArray {
            all.forEach { (playlist, tracks) ->
                totalTracks += tracks.size
                add(buildJsonObject {
                    put("id", playlist.id)
                    put("name", playlist.name)
                    playlist.source?.let { put("source", it) }
                    playlist.sourceListId?.let { put("sourceListId", it) }
                    put("list", buildJsonArray {
                        tracks.forEach { t -> add(json.encodeToJsonElement(t)) }
                    })
                })
            }
        }
        val root = buildJsonObject {
            put("type", "playList_v2")
            put("data", dataArr)
        }.toString()

        val rawBytes = root.toByteArray(StandardCharsets.UTF_8)
        val outBytes = if (compressGzip) {
            ByteArrayOutputStream().use { bos ->
                GZIPOutputStream(bos).use { it.write(rawBytes) }
                bos.toByteArray()
            }
        } else rawBytes

        context.contentResolver.openOutputStream(uri)?.use { it.write(outBytes) }
            ?: throw IllegalStateException("无法写入目标文件")
        AppLogger.info("Backup", "导出歌单成功: ${all.size} 个歌单, $totalTracks 首歌曲")
        ImportSummary(all.size, totalTracks)
    }

    suspend fun importFromUri(uri: Uri): ImportSummary = withContext(Dispatchers.IO) {
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("无法读取所选文件")
        val text = decodePossiblyGzipped(rawBytes)
        val root = json.parseToJsonElement(text).jsonObject
        val type = root["type"]?.jsonPrimitive?.contentOrNull.orEmpty()

        val listsToImport: List<JsonObject> = when (type) {
            "playList_v2", "playList" -> root["data"]?.jsonArray?.map { it.jsonObject }.orEmpty()
            "allData_v2", "allData" -> root["playList"]?.jsonArray?.map { it.jsonObject }.orEmpty()
            "playListPart", "playListPart_v2" -> listOfNotNull(root["data"]?.jsonObject)
            "defautlList" -> listOfNotNull(root["data"]?.jsonObject)
            else -> throw IllegalArgumentException("不支持的备份文件类型: ${type.ifBlank { "未知" }}")
        }

        var importedPlaylists = 0
        var importedTracks = 0

        for (listObj in listsToImport) {
            val id = listObj["id"]?.jsonPrimitive?.contentOrNull ?: continue
            val name = listObj["name"]?.jsonPrimitive?.contentOrNull ?: "导入歌单"
            val source = listObj["source"]?.jsonPrimitive?.contentOrNull
            val sourceListId = listObj["sourceListId"]?.jsonPrimitive?.contentOrNull
            val rawTracks = listObj["list"]?.jsonArray ?: JsonArray(emptyList())

            val parsedTracks = rawTracks.mapNotNull { el -> parseFlexibleMusicInfo(el.jsonObject) }
            when (id) {
                PlaylistIds.DEFAULT, PlaylistIds.LOVE -> {
                    repository.addTracksToPlaylist(id, parsedTracks)
                }
                else -> {
                    val existing = repository.db.playlistDao().getPlaylist(id)
                    if (existing != null) {
                        repository.addTracksToPlaylist(id, parsedTracks)
                    } else {
                        repository.createPlaylist(
                            name = name,
                            tracks = parsedTracks,
                            source = source,
                            sourceListId = sourceListId,
                            explicitId = id,
                        )
                    }
                }
            }
            importedPlaylists++
            importedTracks += parsedTracks.size
        }

        // Also import dislike rules if present in allData
        root["dislikeInfo"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
            repository.saveDislikeRulesText(it)
        }

        AppLogger.info("Backup", "导入备份成功 ($type): $importedPlaylists 个歌单, $importedTracks 首歌曲")
        ImportSummary(importedPlaylists, importedTracks)
    }

    private fun decodePossiblyGzipped(bytes: ByteArray): String {
        val isGzip = bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()
        return if (isGzip) {
            GZIPInputStream(ByteArrayInputStream(bytes)).use { String(it.readBytes(), StandardCharsets.UTF_8) }
        } else {
            String(bytes, StandardCharsets.UTF_8)
        }
    }

    private fun parseFlexibleMusicInfo(obj: JsonObject): MusicInfo? {
        // v2 format has nested "meta"
        if (obj.containsKey("meta")) {
            return runCatching { json.decodeFromString<MusicInfo>(obj.toString()) }.getOrNull()
        }
        // v1 legacy format
        val source = obj["source"]?.jsonPrimitive?.contentOrNull ?: return null
        val songmid = obj["songmid"]?.jsonPrimitive?.contentOrNull ?: return null
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return null
        val singer = obj["singer"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val interval = obj["interval"]?.jsonPrimitive?.contentOrNull
        val hash = obj["hash"]?.jsonPrimitive?.contentOrNull
        val id = if (source == "kg" && !hash.isNullOrBlank()) "${songmid}_$hash" else "${source}_$songmid"
        val types = obj["types"]?.jsonArray?.mapNotNull { t ->
            val to = t.jsonObject
            val typeStr = to["type"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            QualityItem(typeStr, to["size"]?.jsonPrimitive?.contentOrNull.orEmpty())
        }.orEmpty()
        return MusicInfo(
            id = id,
            name = name,
            singer = singer,
            source = source,
            interval = interval,
            meta = MusicMeta(
                songId = songmid,
                albumName = obj["albumName"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                picUrl = obj["img"]?.jsonPrimitive?.contentOrNull,
                qualitys = types,
                albumId = obj["albumId"]?.jsonPrimitive?.contentOrNull,
                hash = hash,
                strMediaMid = obj["strMediaMid"]?.jsonPrimitive?.contentOrNull,
                albumMid = obj["albumMid"]?.jsonPrimitive?.contentOrNull,
                id = obj["songId"]?.jsonPrimitive?.contentOrNull,
                copyrightId = obj["copyrightId"]?.jsonPrimitive?.contentOrNull,
                lrcUrl = obj["lrcUrl"]?.jsonPrimitive?.contentOrNull,
                mrcUrl = obj["mrcUrl"]?.jsonPrimitive?.contentOrNull,
                trcUrl = obj["trcUrl"]?.jsonPrimitive?.contentOrNull,
            ),
        )
    }
}
