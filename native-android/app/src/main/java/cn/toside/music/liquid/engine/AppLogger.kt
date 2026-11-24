package cn.toside.music.liquid.engine

import android.util.Log
import cn.toside.music.liquid.data.model.AppLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AppLogger {
    private const val MAX_LOGS = 400
    private val _logs = MutableStateFlow<List<AppLogEntry>>(emptyList())
    val logs: StateFlow<List<AppLogEntry>> = _logs.asStateFlow()

    fun info(tag: String, message: String) {
        Log.i("LxLiquid/$tag", message)
        append("INFO", tag, message)
    }

    fun warn(tag: String, message: String) {
        Log.w("LxLiquid/$tag", message)
        append("WARN", tag, message)
    }

    fun error(tag: String, message: String, tr: Throwable? = null) {
        Log.e("LxLiquid/$tag", message, tr)
        val full = if (tr != null) "$message: ${tr.message}" else message
        append("ERROR", tag, full)
    }

    fun clear() {
        _logs.value = emptyList()
    }

    private fun append(level: String, tag: String, message: String) {
        val entry = AppLogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message.take(1200),
        )
        _logs.update { current ->
            val next = current + entry
            if (next.size > MAX_LOGS) next.takeLast(MAX_LOGS) else next
        }
    }
}
