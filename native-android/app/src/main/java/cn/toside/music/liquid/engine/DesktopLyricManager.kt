package cn.toside.music.liquid.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import cn.toside.music.liquid.data.model.DesktopLyricConfig
import cn.toside.music.liquid.data.model.LyricLine
import kotlin.math.roundToInt

/**
 * 纯原生 Android 桌面悬浮歌词窗口控制器：
 * 支持拖动定位、位置锁定、单/双行译文/罗马音、字号、透明度与配色实时生效。
 */
class DesktopLyricManager(
    private val context: Context,
    private val onPositionChanged: (Float) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var rootView: LinearLayout? = null
    private var primaryText: TextView? = null
    private var secondaryText: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var currentConfig = DesktopLyricConfig()
    private var currentLine: LyricLine? = null

    fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    fun requestOverlayPermissionIntent(): Intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun applyConfig(config: DesktopLyricConfig) {
        currentConfig = config
        mainHandler.post {
            if (config.enabled) {
                if (!hasOverlayPermission()) {
                    AppLogger.warn("DesktopLyric", "缺少悬浮窗权限，无法显示桌面歌词")
                    return@post
                }
                if (rootView == null) {
                    attachWindow()
                }
                updateAppearance()
                renderLine(currentLine)
            } else {
                detachWindow()
            }
        }
    }

    fun updateLyricLine(line: LyricLine?) {
        currentLine = line
        if (!currentConfig.enabled || rootView == null) return
        mainHandler.post { renderLine(line) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachWindow() {
        val dm = context.resources.displayMetrics
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = buildFlags(currentConfig.locked)
        val params = WindowManager.LayoutParams(
            (dm.widthPixels * 0.9f).roundToInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (dm.heightPixels * currentConfig.posYFraction).roundToInt()
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val padH = dp(16)
            val padV = dp(8)
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.argb(128, 16, 16, 22))
            }
        }

        val tvMain = TextView(context).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(6f, 0f, 2f, Color.argb(190, 0, 0, 0))
            maxLines = 2
        }
        val tvSub = TextView(context).apply {
            gravity = Gravity.CENTER
            setShadowLayer(4f, 0f, 1.5f, Color.argb(180, 0, 0, 0))
            maxLines = 2
            visibility = View.GONE
        }

        container.addView(tvMain)
        container.addView(tvSub)

        var startY = 0
        var touchY = 0f
        container.setOnTouchListener { _, event ->
            if (currentConfig.locked) return@setOnTouchListener false
            val lp = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = lp.y
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = (event.rawY - touchY).roundToInt()
                    lp.y = (startY + dy).coerceIn(0, dm.heightPixels - dp(60))
                    runCatching { windowManager.updateViewLayout(container, lp) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val fraction = (lp.y.toFloat() / dm.heightPixels.coerceAtLeast(1)).coerceIn(0.02f, 0.88f)
                    onPositionChanged(fraction)
                    true
                }
                else -> false
            }
        }

        runCatching {
            windowManager.addView(container, params)
            rootView = container
            primaryText = tvMain
            secondaryText = tvSub
            layoutParams = params
        }.onFailure {
            AppLogger.error("DesktopLyric", "挂载桌面悬浮歌词窗口失败", it)
        }
    }

    private fun updateAppearance() {
        val container = rootView ?: return
        val lp = layoutParams ?: return
        lp.flags = buildFlags(currentConfig.locked)
        container.alpha = currentConfig.opacity.coerceIn(0.3f, 1f)
        val activeColor = runCatching { Color.parseColor(currentConfig.activeColorHex) }.getOrDefault(0xFFFA243C.toInt())
        val subColor = runCatching { Color.parseColor(currentConfig.inactiveColorHex) }.getOrDefault(Color.WHITE)

        primaryText?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, currentConfig.fontSizeSp.coerceIn(12f, 30f))
            setTextColor(activeColor)
        }
        secondaryText?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, (currentConfig.fontSizeSp * 0.78f).coerceIn(10f, 24f))
            setTextColor(subColor)
        }
        runCatching { windowManager.updateViewLayout(container, lp) }
    }

    private fun renderLine(line: LyricLine?) {
        val main = primaryText ?: return
        val sub = secondaryText ?: return
        if (line == null || line.text.isBlank()) {
            main.text = "洛雪音乐 · 液态玻璃"
            sub.visibility = View.GONE
            return
        }
        main.text = line.text
        val extra = buildList {
            if (currentConfig.showTranslation && !line.translation.isNullOrBlank()) add(line.translation)
            if (currentConfig.showRoma && !line.romaji.isNullOrBlank()) add(line.romaji)
        }.joinToString("\n")
        if (extra.isNotBlank()) {
            sub.text = extra
            sub.visibility = View.VISIBLE
        } else {
            sub.visibility = View.GONE
        }
    }

    private fun detachWindow() {
        val view = rootView ?: return
        runCatching { windowManager.removeView(view) }
        rootView = null
        primaryText = null
        secondaryText = null
        layoutParams = null
    }

    private fun buildFlags(locked: Boolean): Int {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (locked) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        return flags
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).roundToInt()
}
