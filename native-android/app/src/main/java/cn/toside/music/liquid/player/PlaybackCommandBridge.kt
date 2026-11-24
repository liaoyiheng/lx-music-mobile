package cn.toside.music.liquid.player

import cn.toside.music.liquid.data.model.PlayMode
import kotlinx.coroutines.flow.StateFlow

/**
 * 播放控制与后台 MediaSessionService 之间的解耦桥接契约：
 * - 让通知栏、系统锁屏卡片、蓝牙耳机/车载 AVRCP、线控按键无缝接入
 *   LX Music 的自定义音源解析、跨源换源、随机/循环与不喜欢过滤管线。
 */
interface PlaybackSessionDelegate {
    val uiState: StateFlow<PlayerUiState>
    fun hasQueue(): Boolean
    fun isFavorite(): Boolean
    fun playMode(): PlayMode
    fun skipNextFromSystem()
    fun skipPreviousFromSystem()
    fun toggleFavoriteFromSystem()
    fun cyclePlayModeFromSystem()
}

/**
 * 进程内轻量级弱耦合注册表：
 * `PlayerController` 启动时注册，`LxPlaybackService` 通过它转发系统媒体事件与同步自定义按钮状态。
 */
object PlaybackCommandBridge {
    @Volatile
    var delegate: PlaybackSessionDelegate? = null
}
