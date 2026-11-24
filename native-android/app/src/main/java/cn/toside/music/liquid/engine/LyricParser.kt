package cn.toside.music.liquid.engine

import cn.toside.music.liquid.data.model.LyricData
import cn.toside.music.liquid.data.model.LyricLine
import kotlin.math.abs

object LyricParser {
    private val timeTagRegex = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val wordTagRegex = Regex("""<\d+,\d+(?:,\d+)?>""")

    fun parse(data: LyricData): List<LyricLine> {
        val primary = parseSingle(data.lyric.ifBlank { data.lxlyric })
        if (primary.isEmpty()) return emptyList()

        val transMap = parseSingle(data.tlyric)
        val romaMap = parseSingle(data.rlyric)

        return primary.map { (timeMs, text) ->
            val trans = findNearestText(transMap, timeMs)
            val roma = findNearestText(romaMap, timeMs)
            LyricLine(
                timeMs = timeMs,
                text = text,
                translation = trans?.takeIf { it.isNotBlank() && it != text },
                romaji = roma?.takeIf { it.isNotBlank() && it != text },
            )
        }
    }

    private fun parseSingle(raw: String): List<Pair<Long, String>> {
        if (raw.isBlank()) return emptyList()
        val entries = mutableListOf<Pair<Long, String>>()
        raw.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach
            val matches = timeTagRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            val text = line
                .replace(timeTagRegex, "")
                .replace(wordTagRegex, "")
                .trim()
            if (text.isEmpty()) return@forEach
            matches.forEach { m ->
                val min = m.groupValues[1].toLongOrNull() ?: 0L
                val sec = m.groupValues[2].toLongOrNull() ?: 0L
                val fracStr = m.groupValues.getOrNull(3).orEmpty()
                val ms = when (fracStr.length) {
                    0 -> 0L
                    1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                    else -> fracStr.take(3).toLongOrNull() ?: 0L
                }
                val totalMs = min * 60_000L + sec * 1_000L + ms
                entries.add(totalMs to text)
            }
        }
        return entries.sortedBy { it.first }
    }

    private fun findNearestText(sorted: List<Pair<Long, String>>, targetMs: Long): String? {
        if (sorted.isEmpty()) return null
        var best: String? = null
        var bestDiff = 600L
        for ((t, text) in sorted) {
            val diff = abs(t - targetMs)
            if (diff <= bestDiff) {
                bestDiff = diff
                best = text
            }
            if (t > targetMs + 600L) break
        }
        return best
    }

    fun findActiveIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var lo = 0
        var hi = lines.lastIndex
        var ans = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (lines[mid].timeMs <= positionMs + 120L) {
                ans = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return ans
    }
}
