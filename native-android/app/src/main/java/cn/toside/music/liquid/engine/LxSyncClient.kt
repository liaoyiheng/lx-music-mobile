package cn.toside.music.liquid.engine

import android.os.Build
import android.util.Base64
import cn.toside.music.liquid.crypto.CryptoUtils
import cn.toside.music.liquid.data.MusicRepository
import cn.toside.music.liquid.data.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 洛雪 LX Sync v4 客户端协议实现：
 * 支持 /hello 协议校验、/id 服务器标识获取、/ah RSA+AES 握手认证与 WebSocket 实时通道。
 */
class LxSyncClient(
    private val repository: MusicRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private var webSocket: WebSocket? = null

    @Serializable
    private data class KeyInfo(
        val clientId: String,
        val key: String,
        val serverName: String = "",
    )

    companion object {
        private const val HELLO_MSG = "Hello~::^-^::~v4~"
        private const val ID_PREFIX = "OjppZDo6"
        private const val AUTH_MSG = "lx-music auth::"
    }

    fun connect(serverUrlRaw: String, authCode: String = "") {
        scope.launch {
            val baseUrl = serverUrlRaw.trim().trimEnd('/')
            if (baseUrl.isBlank()) {
                _state.value = SyncState(statusText = "请先填写同步服务器地址")
                return@launch
            }
            disconnect()
            _state.value = SyncState(connecting = true, statusText = "正在连接同步服务...")
            try {
                val helloResp = httpGetText("$baseUrl/hello")
                if (helloResp != HELLO_MSG) {
                    throw RuntimeException("服务端协议不匹配: $helloResp")
                }
                val idResp = httpGetText("$baseUrl/id")
                if (!idResp.startsWith(ID_PREFIX)) {
                    throw RuntimeException("无法获取同步服务 ID")
                }
                val keyInfo = authenticate(baseUrl, authCode.trim())
                openWebSocket(baseUrl, keyInfo)
            } catch (e: Throwable) {
                AppLogger.error("Sync", "LX Sync 连接失败", e)
                _state.value = SyncState(
                    connected = false,
                    connecting = false,
                    statusText = "连接失败：${e.message}",
                )
            }
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _state.value = SyncState(connected = false, connecting = false, statusText = "已断开")
    }

    private suspend fun authenticate(baseUrl: String, authCode: String): KeyInfo = withContext(Dispatchers.IO) {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        if (authCode.isNotBlank()) {
            val md5Key = CryptoUtils.md5(authCode).substring(0, 16)
            val keyB64 = Base64.encodeToString(md5Key.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            val (pubKey, privKey) = CryptoUtils.generateRsaKeyPair()
            val plain = "$AUTH_MSG\n$pubKey\n$deviceName\nlx_music_mobile"
            val encrypted = CryptoUtils.aesEncryptUtf8(plain, keyB64)
            val resp = httpGetText("$baseUrl/ah", mapOf("m" to encrypted))
            val decryptedB64 = CryptoUtils.rsaDecrypt(resp, privKey, "RSA/ECB/OAEPWithSHA1AndMGF1Padding")
            val jsonStr = String(Base64.decode(decryptedB64, Base64.DEFAULT), StandardCharsets.UTF_8)
            val info = json.decodeFromString<KeyInfo>(jsonStr)
            repository.settingsStore.saveSyncKeyInfoJson(json.encodeToString(info))
            info
        } else {
            val savedJson = repository.settingsStore.getSyncKeyInfoJson()
                ?: throw RuntimeException("首次连接请输入连接码")
            val info = json.decodeFromString<KeyInfo>(savedJson)
            val msg = CryptoUtils.aesEncryptUtf8(AUTH_MSG + deviceName, info.key)
            val resp = httpGetText("$baseUrl/ah", mapOf("i" to info.clientId, "m" to msg))
            val check = CryptoUtils.aesDecryptUtf8(resp, info.key)
            if (check != HELLO_MSG) throw RuntimeException("已保存凭据失效，请重新输入连接码")
            info
        }
    }

    private fun openWebSocket(baseUrl: String, keyInfo: KeyInfo) {
        val wsUrl = baseUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://") +
            "/socket?i=${URLEncoder.encode(keyInfo.clientId, "UTF-8")}&t=${URLEncoder.encode(CryptoUtils.aesEncryptUtf8("lx-music connect", keyInfo.key), "UTF-8")}"

        val req = Request.Builder().url(wsUrl).build()
        webSocket = HttpClientProvider.okHttpClient.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                AppLogger.info("Sync", "LX Sync WebSocket 已连接: $baseUrl")
                _state.value = SyncState(
                    connected = true,
                    connecting = false,
                    statusText = "已连接 ${keyInfo.serverName.ifBlank { baseUrl }}",
                    lastSyncedAt = System.currentTimeMillis(),
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                AppLogger.info("Sync", "收到同步帧 (${text.length} chars)")
                _state.value = _state.value.copy(lastSyncedAt = System.currentTimeMillis())
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _state.value = SyncState(connected = false, connecting = false, statusText = "连接已关闭 ($code)")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                AppLogger.error("Sync", "WebSocket 断开", t)
                _state.value = SyncState(connected = false, connecting = false, statusText = "连接中断：${t.message}")
            }
        })
    }

    private fun httpGetText(url: String, headers: Map<String, String> = emptyMap()): String {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.header(k, v) }
        HttpClientProvider.okHttpClient.newCall(builder.build()).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw RuntimeException(body.ifBlank { "HTTP ${resp.code}" })
            return body
        }
    }
}
