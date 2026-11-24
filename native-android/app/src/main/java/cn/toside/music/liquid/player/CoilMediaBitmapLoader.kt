package cn.toside.music.liquid.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future

/**
 * 基于 Coil + 平台防盗链 OkHttpClient 的 Media3 高清封面加载器：
 * - 解决系统默认 DataSourceBitmapLoader 因缺少 Referer / Desktop UA 导致网易云/QQ/酷狗封面 403 空白问题
 * - 复用 Coil 内存与磁盘缓存，通知栏与系统锁屏卡片切歌瞬间呈现高清专辑封面
 */
@OptIn(UnstableApi::class)
class CoilMediaBitmapLoader(
    private val context: Context,
    private val imageLoader: ImageLoader,
) : BitmapLoader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = scope.future {
        BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: throw IllegalArgumentException("无法解码封面字节流 (${data.size} bytes)")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> = scope.future {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(640)
            .allowHardware(false)
            .build()
        val result = imageLoader.execute(request)
        val bitmap = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
            ?: throw IllegalStateException("封面加载失败: $uri")
        // 返回不可变副本或安全位图供 Notification / MediaSession 跨进程序列化
        if (bitmap.isRecycled) {
            throw IllegalStateException("封面位图已回收: $uri")
        }
        bitmap
    }
}
