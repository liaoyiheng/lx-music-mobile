package cn.toside.music.liquid.engine

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import cn.toside.music.liquid.crypto.CryptoUtils
import cn.toside.music.liquid.data.model.LyricData
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.UserApiScript
import cn.toside.music.liquid.data.model.UserApiStatus
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 100% 兼容洛雪自定义音源 (.js) 脚本的原生 QuickJS 引擎。
 * 直接复用官方 assets/script/user-api-preload.js 协议，支持所有现有洛雪自定义源脚本。
 */
class UserApiEngine(
    private val context: Context,
    private val musicSdkEngine: MusicSdkEngine,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jsThread = HandlerThread("LxUserApiJs").apply { start() }
    private val jsHandler = Handler(jsThread.looper)

    @Volatile
    private var jsContext: QuickJSContext? = null

    @Volatile
    private var sessionKey: String = ""

    private val _status = MutableStateFlow(UserApiStatus())
    val status: StateFlow<UserApiStatus> = _status.asStateFlow()

    private val _updateAlert = MutableStateFlow<Pair<String, String>?>(null)
    val updateAlert: StateFlow<Pair<String, String>?> = _updateAlert.asStateFlow()

    private val pendingApiRequests = ConcurrentHashMap<String, CompletableDeferred<JsonElement>>()
    private val activeHttpCalls = ConcurrentHashMap<String, Call>()

    fun dismissUpdateAlert() {
        _updateAlert.value = null
    }

    fun unload() {
        jsHandler.post {
            failAllPending("自定义源已卸载")
            runCatching { jsContext?.destroy() }
            jsContext = null
            _status.value = UserApiStatus()
        }
    }

    fun loadScript(script: UserApiScript) {
        jsHandler.post {
            failAllPending("正在切换自定义源")
            runCatching { jsContext?.destroy() }
            jsContext = null

            _status.value = UserApiStatus(
                activeScriptId = script.id,
                activeScriptName = script.name,
                initialized = false,
                errorMessage = "正在初始化...",
            )

            try {
                QuickJSLoader.init()
                val ctx = QuickJSContext.create()
                sessionKey = UUID.randomUUID().toString()
                ctx.setConsole(object : QuickJSContext.Console {
                    override fun log(info: String?) {
                        if (!info.isNullOrBlank()) AppLogger.info("UserApi", info)
                    }
                    override fun info(info: String?) {
                        if (!info.isNullOrBlank()) AppLogger.info("UserApi", info)
                    }
                    override fun warn(info: String?) {
                        if (!info.isNullOrBlank()) AppLogger.warn("UserApi", info)
                    }
                    override fun error(info: String?) {
                        if (!info.isNullOrBlank()) AppLogger.error("UserApi", info)
                    }
                })

                val preload = context.assets.open("script/user-api-preload.js").use {
                    String(it.readBytes(), StandardCharsets.UTF_8)
                }

                bindPreloadEnv(ctx, script)
                ctx.evaluate(preload)
                ctx.globalObject.getJSFunction("lx_setup").call(
                    sessionKey,
                    script.id,
                    script.name,
                    script.description,
                    script.version,
                    script.author,
                    script.homepage,
                    script.script,
                )
                jsContext = ctx
                ctx.evaluate(script.script)
                AppLogger.info("UserApi", "已加载脚本: ${script.name} (${script.version})")
            } catch (e: Throwable) {
                AppLogger.error("UserApi", "加载自定义源失败: ${script.name}", e)
                _status.value = UserApiStatus(
                    activeScriptId = script.id,
                    activeScriptName = script.name,
                    initialized = false,
                    errorMessage = e.message ?: "脚本执行异常",
                )
            }
        }
    }

    private fun bindPreloadEnv(ctx: QuickJSContext, script: UserApiScript) {
        val g = ctx.globalObject

        g.setProperty("__lx_native_call__") { args ->
            val key = args.getOrNull(0)?.toString().orEmpty()
            if (key == sessionKey) {
                val action = args.getOrNull(1)?.toString().orEmpty()
                val data = args.getOrNull(2)?.toString().orEmpty()
                handleScriptAction(script, action, data)
            }
            null
        }
        g.setProperty("__lx_native_call__utils_str2b64") { args ->
            runCatching {
                val str = args.getOrNull(0)?.toString().orEmpty()
                Base64.encodeToString(str.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            }.getOrDefault("")
        }
        g.setProperty("__lx_native_call__utils_b642buf") { args ->
            runCatching {
                val bytes = Base64.decode(args.getOrNull(0)?.toString().orEmpty(), Base64.NO_WRAP)
                bytes.joinToString(",", "[", "]") { it.toInt().toString() }
            }.getOrDefault("[]")
        }
        g.setProperty("__lx_native_call__utils_str2md5") { args ->
            runCatching {
                val decoded = URLDecoder.decode(args.getOrNull(0)?.toString().orEmpty(), "UTF-8")
                CryptoUtils.md5(decoded)
            }.getOrDefault("")
        }
        g.setProperty("__lx_native_call__utils_aes_encrypt") { args ->
            runCatching {
                CryptoUtils.aesEncrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                    args.getOrNull(3)?.toString().orEmpty(),
                )
            }.getOrDefault("")
        }
        g.setProperty("__lx_native_call__utils_rsa_encrypt") { args ->
            runCatching {
                CryptoUtils.rsaEncrypt(
                    args.getOrNull(0)?.toString().orEmpty(),
                    args.getOrNull(1)?.toString().orEmpty(),
                    args.getOrNull(2)?.toString().orEmpty(),
                )
            }.getOrDefault("")
        }
        g.setProperty("__lx_native_call__set_timeout") { args ->
            val timerId = args.getOrNull(0)
            val delay = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
            jsHandler.postDelayed({
                callJs("__set_timeout__", timerId)
            }, delay.coerceAtLeast(0L))
            null
        }
    }

    private fun handleScriptAction(script: UserApiScript, action: String, dataJson: String) {
        try {
            when (action) {
                "init" -> {
                    val obj = json.parseToJsonElement(dataJson).jsonObject
                    val ok = obj["status"]?.jsonPrimitive?.booleanOrNull == true
                    val err = obj["errorMessage"]?.jsonPrimitive?.contentOrNull
                    val sourcesMap = mutableMapOf<String, List<String>>()
                    obj["info"]?.jsonObject?.get("sources")?.jsonObject?.forEach { (src, v) ->
                        val q = v.jsonObject["qualitys"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                            ?: emptyList()
                        sourcesMap[src] = q
                    }
                    _status.value = UserApiStatus(
                        activeScriptId = script.id,
                        activeScriptName = script.name,
                        initialized = ok,
                        errorMessage = if (ok) null else (err ?: "初始化失败"),
                        supportedSources = sourcesMap,
                    )
                    AppLogger.info("UserApi", "初始化${if (ok) "成功" else "失败"}: ${script.name}, 支持平台=${sourcesMap.keys}")
                }
                "request" -> {
                    val obj = json.parseToJsonElement(dataJson).jsonObject
                    val reqKey = obj["requestKey"]?.jsonPrimitive?.contentOrNull ?: return
                    val url = obj["url"]?.jsonPrimitive?.contentOrNull ?: return
                    val options = obj["options"] as? JsonObject
                    executeScriptHttp(reqKey, url, options)
                }
                "cancelRequest" -> {
                    val obj = json.parseToJsonElement(dataJson).jsonObject
                    val reqKey = obj["requestKey"]?.jsonPrimitive?.contentOrNull ?: return
                    activeHttpCalls.remove(reqKey)?.cancel()
                }
                "response" -> {
                    val obj = json.parseToJsonElement(dataJson).jsonObject
                    val reqKey = obj["requestKey"]?.jsonPrimitive?.contentOrNull ?: return
                    val ok = obj["status"]?.jsonPrimitive?.booleanOrNull == true
                    val deferred = pendingApiRequests.remove(reqKey) ?: return
                    if (ok) {
                        deferred.complete(obj["result"] ?: JsonNull)
                    } else {
                        val msg = obj["errorMessage"]?.jsonPrimitive?.contentOrNull ?: "自定义源返回失败"
                        deferred.completeExceptionally(RuntimeException(msg))
                    }
                }
                "showUpdateAlert" -> {
                    val obj = json.parseToJsonElement(dataJson).jsonObject
                    val log = obj["log"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val url = obj["updateUrl"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    if (log.isNotBlank() || url.isNotBlank()) {
                        _updateAlert.value = log to url
                    }
                }
            }
        } catch (e: Throwable) {
            AppLogger.error("UserApi", "处理脚本动作 $action 失败", e)
        }
    }

    private fun executeScriptHttp(reqKey: String, url: String, options: JsonObject?) {
        ioScope.launch {
            try {
                val method = (options?.get("method")?.jsonPrimitive?.contentOrNull ?: "GET").uppercase()
                val timeout = options?.get("timeout")?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 20_000L
                val reqBuilder = Request.Builder().url(url)

                var contentType: String? = null
                options?.get("headers")?.let { hEl ->
                    if (hEl is JsonObject) {
                        hEl.forEach { (k, v) ->
                            val value = v.jsonPrimitive.contentOrNull ?: v.toString()
                            reqBuilder.header(k, value)
                            if (k.equals("Content-Type", ignoreCase = true)) contentType = value
                        }
                    }
                }
                if (reqBuilder.build().header("User-Agent") == null) {
                    reqBuilder.header("User-Agent", HttpClientProvider.DESKTOP_UA)
                }

                val bodyElem = options?.get("body")
                val formElem = options?.get("form")
                val reqBody = when {
                    method == "GET" || method == "HEAD" -> null
                    formElem is JsonObject -> {
                        val encoded = formElem.entries.joinToString("&") { (k, v) ->
                            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v.jsonPrimitive.contentOrNull ?: v.toString(), "UTF-8")}"
                        }
                        encoded.toRequestBody((contentType ?: "application/x-www-form-urlencoded").toMediaTypeOrNull())
                    }
                    bodyElem != null && bodyElem !is JsonNull -> {
                        val raw = if (bodyElem is JsonPrimitive && bodyElem.isString) bodyElem.content else bodyElem.toString()
                        raw.toRequestBody((contentType ?: "application/json; charset=utf-8").toMediaTypeOrNull())
                    }
                    else -> "".toRequestBody((contentType ?: "application/json; charset=utf-8").toMediaTypeOrNull())
                }
                reqBuilder.method(method, reqBody)

                val client = HttpClientProvider.okHttpClient.newBuilder()
                    .callTimeout(timeout.coerceIn(5_000L, 60_000L), TimeUnit.MILLISECONDS)
                    .build()
                val call = client.newCall(reqBuilder.build())
                activeHttpCalls[reqKey] = call
                val resp = call.execute()
                activeHttpCalls.remove(reqKey)

                val rawBody = resp.body?.string().orEmpty()
                val parsedBody: JsonElement = runCatching { json.parseToJsonElement(rawBody) }
                    .getOrElse { JsonPrimitive(rawBody) }
                val headersObj = buildJsonObject {
                    resp.headers.names().forEach { name ->
                        put(name.lowercase(), resp.headers.values(name).joinToString("; "))
                    }
                }
                val responsePayload = buildJsonObject {
                    put("error", JsonNull)
                    put("requestKey", reqKey)
                    put("response", buildJsonObject {
                        put("statusCode", resp.code)
                        put("statusMessage", resp.message)
                        put("headers", headersObj)
                        put("body", parsedBody)
                        put("raw", rawBody)
                    })
                }.toString()

                jsHandler.post { callJs("response", responsePayload) }
            } catch (e: Throwable) {
                activeHttpCalls.remove(reqKey)
                val errPayload = buildJsonObject {
                    put("error", e.message ?: "Request failed")
                    put("requestKey", reqKey)
                    put("response", JsonNull)
                }.toString()
                jsHandler.post { callJs("response", errPayload) }
            }
        }
    }

    private fun callJs(action: String, vararg extraArgs: Any?) {
        val ctx = jsContext ?: return
        try {
            val args = Array<Any?>(extraArgs.size + 2) { idx ->
                when (idx) {
                    0 -> sessionKey
                    1 -> action
                    else -> extraArgs[idx - 2]
                }
            }
            ctx.globalObject.getJSFunction("__lx_native__").call(*args)
        } catch (e: Throwable) {
            AppLogger.error("UserApi", "调用脚本方法 $action 异常", e)
        }
    }

    private fun failAllPending(reason: String) {
        pendingApiRequests.keys.toList().forEach { k ->
            pendingApiRequests.remove(k)?.completeExceptionally(RuntimeException(reason))
        }
    }

    suspend fun getMusicUrl(musicInfo: MusicInfo, quality: String): String {
        val st = _status.value
        if (!st.initialized || jsContext == null) {
            throw IllegalStateException("未启用可用的自定义音源，请在 设置 → 自定义音源 中导入并启用 .js 脚本")
        }
        val oldInfo = musicSdkEngine.toOldMusicInfoJson(musicInfo)
        val reqKey = "uapi_${UUID.randomUUID().toString().take(12)}"
        val payload = buildJsonObject {
            put("requestKey", reqKey)
            put("data", buildJsonObject {
                put("source", musicInfo.source)
                put("action", "musicUrl")
                put("info", buildJsonObject {
                    put("type", quality)
                    put("musicInfo", oldInfo)
                })
            })
        }.toString()

        val deferred = CompletableDeferred<JsonElement>()
        pendingApiRequests[reqKey] = deferred
        jsHandler.post { callJs("request", payload) }

        val res = try {
            withTimeout(22_000L) { deferred.await() }
        } catch (e: Throwable) {
            pendingApiRequests.remove(reqKey)
            throw e
        }

        val data = res.jsonObject["data"]
        val url = when (data) {
            is JsonPrimitive -> data.contentOrNull
            is JsonObject -> data["url"]?.jsonPrimitive?.contentOrNull
            else -> res.jsonObject["url"]?.jsonPrimitive?.contentOrNull
        }
        if (url.isNullOrBlank() || !url.startsWith("http")) {
            throw RuntimeException("音源未返回有效播放链接")
        }
        return url
    }

    suspend fun getLyricOverride(musicInfo: MusicInfo): LyricData? {
        if (!_status.value.initialized || jsContext == null) return null
        return runCatching {
            val oldInfo = musicSdkEngine.toOldMusicInfoJson(musicInfo)
            val reqKey = "uapi_lrc_${UUID.randomUUID().toString().take(10)}"
            val payload = buildJsonObject {
                put("requestKey", reqKey)
                put("data", buildJsonObject {
                    put("source", musicInfo.source)
                    put("action", "lyric")
                    put("info", buildJsonObject {
                        put("type", "music")
                        put("musicInfo", oldInfo)
                    })
                })
            }.toString()
            val deferred = CompletableDeferred<JsonElement>()
            pendingApiRequests[reqKey] = deferred
            jsHandler.post { callJs("request", payload) }
            val res = withTimeout(12_000L) { deferred.await() }
            val data = res.jsonObject["data"]?.jsonObject ?: return@runCatching null
            LyricData(
                lyric = data["lyric"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                tlyric = data["tlyric"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                rlyric = data["rlyric"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                lxlyric = data["lxlyric"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            ).takeIf { it.lyric.isNotBlank() }
        }.getOrNull()
    }

    companion object {
        /**
         * 解析洛雪自定义源 .js 头部注释元数据 (@name, @description, @version, @author, @homepage)
         */
        fun parseScriptMeta(rawScript: String, fallbackName: String = "自定义音源"): UserApiScript {
            val header = rawScript.take(4096)
            fun extract(tag: String): String {
                val regex = Regex("""@$tag\s+([^\r\n]+)""")
                return regex.find(header)?.groupValues?.getOrNull(1)?.trim().orEmpty()
            }
            val name = extract("name").ifBlank { fallbackName }
            val id = "user_api_${CryptoUtils.md5("$name:${rawScript.take(256)}").take(12)}"
            return UserApiScript(
                id = id,
                name = name,
                description = extract("description"),
                version = extract("version").ifBlank { "1.0.0" },
                author = extract("author"),
                homepage = extract("homepage"),
                script = rawScript,
                enabled = false,
                createdAt = System.currentTimeMillis(),
            )
        }
    }
}
