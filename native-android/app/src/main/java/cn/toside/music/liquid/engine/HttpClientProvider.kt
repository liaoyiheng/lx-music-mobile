package cn.toside.music.liquid.engine

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object HttpClientProvider {
    const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    private val sessionCookieJar = object : CookieJar {
        private val store = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (cookies.isEmpty()) return
            val now = System.currentTimeMillis()
            for (cookie in cookies) {
                val domainKey = cookie.domain.lowercase()
                val bucket = store.computeIfAbsent(domainKey) { ConcurrentHashMap() }
                val key = "${cookie.name}|${cookie.path}"
                if (cookie.expiresAt < now || cookie.value.isEmpty()) {
                    bucket.remove(key)
                } else {
                    if (bucket.size > 64) {
                        bucket.entries.removeIf { it.value.expiresAt < now }
                    }
                    bucket[key] = cookie
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val now = System.currentTimeMillis()
            val matched = ArrayList<Cookie>()
            for ((_, bucket) in store) {
                val it = bucket.entries.iterator()
                while (it.hasNext()) {
                    val cookie = it.next().value
                    if (cookie.expiresAt < now) {
                        it.remove()
                    } else if (cookie.matches(url)) {
                        matched.add(cookie)
                    }
                }
            }
            return matched
        }
    }

    private val platformHeaderInterceptor = Interceptor { chain ->
        val orig = chain.request()
        val host = orig.url.host.lowercase()
        val builder = orig.newBuilder()

        if (orig.header("User-Agent") == null) {
            builder.header("User-Agent", DESKTOP_UA)
        }
        if (orig.header("Referer") == null) {
            when {
                host.contains("126.net") || host.contains("163.com") ->
                    builder.header("Referer", "https://music.163.com/")
                host.contains("gtimg.cn") || host.contains("qq.com") ->
                    builder.header("Referer", "https://y.qq.com/")
                host.contains("kuwo.cn") || host.contains("kwcdn") ->
                    builder.header("Referer", "https://www.kuwo.cn/")
                host.contains("kugou.com") ->
                    builder.header("Referer", "https://www.kugou.com/")
                host.contains("migu.cn") ->
                    builder.header("Referer", "https://music.migu.cn/")
            }
        }
        chain.proceed(builder.build())
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    val mediaOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .addInterceptor(platformHeaderInterceptor)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(mediaOkHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.22)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(128L * 1024L * 1024L)
                    .build()
            }
            .crossfade(220)
            .respectCacheHeaders(false)
            .build()
    }
}
