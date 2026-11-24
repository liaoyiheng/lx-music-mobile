package cn.toside.music.liquid.engine

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import cn.toside.music.liquid.crypto.CryptoUtils
import cn.toside.music.liquid.data.model.CommentsPageResult
import cn.toside.music.liquid.data.model.LeaderboardItem
import cn.toside.music.liquid.data.model.LyricData
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.SearchMusicResult
import cn.toside.music.liquid.data.model.SongListDetailResult
import cn.toside.music.liquid.data.model.SongListPageResult
import cn.toside.music.liquid.data.model.SongListSort
import cn.toside.music.liquid.data.model.SongListTagsResult
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class MusicSdkEngine(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jsThread = HandlerThread("LxMusicSdkJs").apply { start() }
    private val jsHandler = Handler(jsThread.looper)

    @Volatile
    private var jsContext: QuickJSContext? = null

    @Volatile
    private var initialized = false

    private val taskSeq = AtomicInteger(0)
    private val pendingTasks = ConcurrentHashMap<String, CompletableDeferred<String>>()
    private val activeHttpCalls = ConcurrentHashMap<String, Call>()
    private val activeTimers = ConcurrentHashMap<String, Runnable>()

    private fun ensureInitializedOnJsThread() {
        if (initialized && jsContext != null) return
        QuickJSLoader.init()
        val ctx = QuickJSContext.create()
        ctx.setConsole(object : QuickJSContext.Console {
            override fun log(info: String?) {
                if (!info.isNullOrBlank()) AppLogger.info("MusicSDK", info)
            }
            override fun info(info: String?) {
                if (!info.isNullOrBlank()) AppLogger.info("MusicSDK", info)
            }
            override fun warn(info: String?) {
                if (!info.isNullOrBlank()) AppLogger.warn("MusicSDK", info)
            }
            override fun error(info: String?) {
                if (!info.isNullOrBlank()) AppLogger.error("MusicSDK", info)
            }
        })
        bindNativeBridges(ctx)
        val bundle = context.assets.open("script/music-sdk-bundle.js").use {
            String(it.readBytes(), StandardCharsets.UTF_8)
        }
        ctx.evaluate(bundle)
        jsContext = ctx
        initialized = true
        AppLogger.info("MusicSDK", "QuickJS music-sdk-bundle initialized")
    }

    private fun bindNativeBridges(ctx: QuickJSContext) {
        val g = ctx.globalObject

        g.setProperty("__sdk_md5") { args ->
            CryptoUtils.md5(args.firstOrNull()?.toString().orEmpty())
        }
        g.setProperty("__sdk_sha1") { args ->
            CryptoUtils.sha1(args.firstOrNull()?.toString().orEmpty())
        }
        g.setProperty("__sdk_btoa") { args ->
            CryptoUtils.btoa(args.firstOrNull()?.toString().orEmpty())
        }
        g.setProperty("__sdk_atob") { args ->
            CryptoUtils.atob(args.firstOrNull()?.toString().orEmpty())
        }
        g.setProperty("__sdk_aes_encrypt") { args ->
            runCatching {
                CryptoUtils.aesEncrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                    args.getOrNull(3)?.toString().orEmpty(),
                )
            }.getOrElse {
                AppLogger.error("MusicSDK", "aes_encrypt failed", it)
                ""
            }
        }
        g.setProperty("__sdk_aes_decrypt") { args ->
            runCatching {
                CryptoUtils.aesDecrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                    args.getOrNull(3)?.toString().orEmpty(),
                )
            }.getOrElse {
                AppLogger.error("MusicSDK", "aes_decrypt failed", it)
                ""
            }
        }
        g.setProperty("__sdk_rsa_encrypt") { args ->
            runCatching {
                CryptoUtils.rsaEncrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                )
            }.getOrElse {
                AppLogger.error("MusicSDK", "rsa_encrypt failed", it)
                ""
            }
        }
        g.setProperty("__sdk_rsa_decrypt") { args ->
            runCatching {
                CryptoUtils.rsaDecrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                )
            }.getOrElse {
                AppLogger.error("MusicSDK", "rsa_decrypt failed", it)
                ""
            }
        }
        g.setProperty("__sdk_set_timeout") { args ->
            val id = args.getOrNull(0)?.toString().orEmpty()
            val delay = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
            val runnable = Runnable {
                activeTimers.remove(id)
                runCatching {
                    jsContext?.globalObject?.getJSFunction("__sdk_timer_fire")?.call(id)
                }
            }
            activeTimers[id] = runnable
            jsHandler.postDelayed(runnable, delay.coerceAtLeast(0L))
            null
        }
        g.setProperty("__sdk_clear_timeout") { args ->
            val id = args.getOrNull(0)?.toString().orEmpty()
            activeTimers.remove(id)?.let { jsHandler.removeCallbacks(it) }
            null
        }
        g.setProperty("__sdk_http") { args ->
            val reqId = args.getOrNull(0)?.toString().orEmpty()
            val optJson = args.getOrNull(1)?.toString().orEmpty()
            performHttpAsync(reqId, optJson)
            null
        }
        g.setProperty("__sdk_http_cancel") { args ->
            val reqId = args.getOrNull(0)?.toString().orEmpty()
            activeHttpCalls.remove(reqId)?.cancel()
            null
        }
        g.setProperty("__sdk_task_resolve") { args ->
            val taskId = args.getOrNull(0)?.toString().orEmpty()
            val resultJson = args.getOrNull(1)?.toString() ?: "null"
            pendingTasks.remove(taskId)?.complete(resultJson)
            null
        }
        g.setProperty("__sdk_task_reject") { args ->
            val taskId = args.getOrNull(0)?.toString().orEmpty()
            val errMsg = args.getOrNull(1)?.toString() ?: "SDK error"
            pendingTasks.remove(taskId)?.completeExceptionally(RuntimeException(errMsg))
            null
        }
    }

    @Serializable
    private data class JsHttpOptions(
        val url: String,
        val method: String = "GET",
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val binary: Boolean = false,
        val timeout: Long = 15000L,
    )

    private fun performHttpAsync(reqId: String, optJson: String) {
        ioScope.launch {
            try {
                val opt = json.decodeFromString<JsHttpOptions>(optJson)
                val reqBuilder = Request.Builder().url(opt.url)
                var contentType: String? = null
                opt.headers.forEach { (k, v) ->
                    reqBuilder.header(k, v)
                    if (k.equals("Content-Type", ignoreCase = true)) contentType = v
                }

                val method = opt.method.uppercase()
                val reqBody = when {
                    method == "GET" || method == "HEAD" -> null
                    else -> (opt.body ?: "").toRequestBody(
                        (contentType ?: "application/json; charset=utf-8").toMediaTypeOrNull()
                    )
                }
                reqBuilder.method(method, reqBody)

                val client = if (opt.timeout != 15000L && opt.timeout > 0) {
                    HttpClientProvider.okHttpClient.newBuilder()
                        .callTimeout(opt.timeout, TimeUnit.MILLISECONDS)
                        .build()
                } else {
                    HttpClientProvider.okHttpClient
                }

                val call = client.newCall(reqBuilder.build())
                activeHttpCalls[reqId] = call
                val resp = call.execute()
                activeHttpCalls.remove(reqId)

                val respHeaders = buildJsonObject {
                    resp.headers.names().forEach { name ->
                        put(name.lowercase(), resp.headers.values(name).joinToString("; "))
                    }
                }
                val bodyString = resp.body?.use { b ->
                    if (opt.binary) {
                        Base64.encodeToString(b.bytes(), Base64.NO_WRAP)
                    } else {
                        b.string()
                    }
                } ?: ""

                val payload = buildJsonObject {
                    put("statusCode", resp.code)
                    put("statusMessage", resp.message)
                    put("url", resp.request.url.toString())
                    put("ok", resp.isSuccessful)
                    put("headers", respHeaders)
                    put("body", bodyString)
                }.toString()

                jsHandler.post {
                    runCatching {
                        jsContext?.globalObject?.getJSFunction("__sdk_http_resolve")?.call(reqId, payload)
                    }
                }
            } catch (e: Exception) {
                activeHttpCalls.remove(reqId)
                val msg = e.message ?: "Network error"
                jsHandler.post {
                    runCatching {
                        jsContext?.globalObject?.getJSFunction("__sdk_http_reject")?.call(reqId, msg)
                    }
                }
            }
        }
    }

    private suspend fun dispatch(method: String, argsJson: String = "{}", timeoutMs: Long = 22_000L): String {
        val taskId = "sdk_${taskSeq.incrementAndGet()}"
        val deferred = CompletableDeferred<String>()
        pendingTasks[taskId] = deferred

        jsHandler.post {
            try {
                ensureInitializedOnJsThread()
                jsContext?.globalObject?.getJSFunction("__sdk_dispatch")?.call(taskId, method, argsJson)
            } catch (e: Throwable) {
                pendingTasks.remove(taskId)?.completeExceptionally(e)
            }
        }

        return try {
            withTimeout(timeoutMs) { deferred.await() }
        } catch (e: Throwable) {
            pendingTasks.remove(taskId)
            throw e
        }
    }

    suspend fun searchMusic(source: String, keyword: String, page: Int = 1, limit: Int = 30): SearchMusicResult {
        val args = buildJsonObject {
            put("source", source)
            put("keyword", keyword)
            put("page", page)
            put("limit", limit)
        }.toString()
        return json.decodeFromString(dispatch("searchMusic", args))
    }

    suspend fun searchSongList(source: String, keyword: String, page: Int = 1, limit: Int = 20): SongListPageResult {
        val args = buildJsonObject {
            put("source", source)
            put("keyword", keyword)
            put("page", page)
            put("limit", limit)
        }.toString()
        return json.decodeFromString(dispatch("searchSongList", args))
    }

    suspend fun searchTips(source: String, keyword: String): List<String> {
        val args = buildJsonObject {
            put("source", source)
            put("keyword", keyword)
        }.toString()
        return json.decodeFromString(dispatch("searchTips", args, 8_000L))
    }

    suspend fun hotSearch(source: String = "all"): List<String> {
        val args = buildJsonObject { put("source", source) }.toString()
        val raw = dispatch("hotSearch", args, 12_000L)
        val obj = json.parseToJsonElement(raw).jsonObject
        return obj["list"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
    }

    suspend fun getSongListSorts(source: String): List<SongListSort> {
        val args = buildJsonObject { put("source", source) }.toString()
        return json.decodeFromString(dispatch("getSongListSorts", args, 8_000L))
    }

    suspend fun getSongListTags(source: String): SongListTagsResult {
        val args = buildJsonObject { put("source", source) }.toString()
        return json.decodeFromString(dispatch("getSongListTags", args))
    }

    suspend fun getSongList(source: String, sortId: String = "", tagId: String = "", page: Int = 1): SongListPageResult {
        val args = buildJsonObject {
            put("source", source)
            put("sortId", sortId)
            put("tagId", tagId)
            put("page", page)
        }.toString()
        return json.decodeFromString(dispatch("getSongList", args))
    }

    suspend fun getSongListDetail(source: String, id: String, page: Int = 1): SongListDetailResult {
        val args = buildJsonObject {
            put("source", source)
            put("id", id)
            put("page", page)
        }.toString()
        return json.decodeFromString(dispatch("getSongListDetail", args))
    }

    suspend fun getLeaderboards(source: String): List<LeaderboardItem> {
        val args = buildJsonObject { put("source", source) }.toString()
        val raw = dispatch("getLeaderboards", args)
        val obj = json.parseToJsonElement(raw).jsonObject
        val arr = obj["list"] ?: return emptyList()
        return json.decodeFromString(arr.toString())
    }

    suspend fun getLeaderboardDetail(source: String, bangid: String, page: Int = 1): SongListDetailResult {
        val args = buildJsonObject {
            put("source", source)
            put("bangid", bangid)
            put("page", page)
        }.toString()
        return json.decodeFromString(dispatch("getLeaderboardDetail", args))
    }

    suspend fun getLyric(musicInfo: MusicInfo): LyricData {
        val args = buildJsonObject {
            put("musicInfo", json.parseToJsonElement(json.encodeToString(musicInfo)))
        }.toString()
        return json.decodeFromString(dispatch("getLyric", args, 15_000L))
    }

    suspend fun getPic(musicInfo: MusicInfo): String {
        val args = buildJsonObject {
            put("musicInfo", json.parseToJsonElement(json.encodeToString(musicInfo)))
        }.toString()
        return json.decodeFromString<String>(dispatch("getPic", args, 10_000L))
    }

    suspend fun getComments(musicInfo: MusicInfo, page: Int = 1, limit: Int = 20, type: String = "hot"): CommentsPageResult {
        val args = buildJsonObject {
            put("musicInfo", json.parseToJsonElement(json.encodeToString(musicInfo)))
            put("page", page)
            put("limit", limit)
            put("type", type)
        }.toString()
        return json.decodeFromString(dispatch("getComments", args, 15_000L))
    }

    suspend fun findMusic(musicInfo: MusicInfo): List<MusicInfo> {
        val args = buildJsonObject {
            put("musicInfo", json.parseToJsonElement(json.encodeToString(musicInfo)))
        }.toString()
        return json.decodeFromString(dispatch("findMusic", args, 20_000L))
    }

    suspend fun toOldMusicInfoJson(musicInfo: MusicInfo): JsonObject {
        val args = buildJsonObject {
            put("musicInfo", json.parseToJsonElement(json.encodeToString(musicInfo)))
        }.toString()
        val raw = dispatch("toOldMusicInfo", args, 5_000L)
        return json.parseToJsonElement(raw).jsonObject
    }

    fun destroy() {
        jsHandler.post {
            runCatching { jsContext?.destroy() }
            jsContext = null
            initialized = false
            jsThread.quitSafely()
        }
    }
}
