package cn.toside.music.liquid

import android.app.Application
import cn.toside.music.liquid.data.MusicRepository
import cn.toside.music.liquid.data.db.AppDatabase
import cn.toside.music.liquid.data.prefs.SettingsStore
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.engine.BackupManager
import cn.toside.music.liquid.engine.DesktopLyricManager
import cn.toside.music.liquid.engine.HttpClientProvider
import cn.toside.music.liquid.engine.LxSyncClient
import cn.toside.music.liquid.engine.MusicSdkEngine
import cn.toside.music.liquid.engine.UserApiEngine
import cn.toside.music.liquid.player.PlayerController
import coil.ImageLoader
import coil.ImageLoaderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LxLiquidApp : Application(), ImageLoaderFactory {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var sdkEngine: MusicSdkEngine
        private set
    lateinit var userApiEngine: UserApiEngine
        private set
    lateinit var repository: MusicRepository
        private set
    lateinit var desktopLyricManager: DesktopLyricManager
        private set
    lateinit var backupManager: BackupManager
        private set
    lateinit var syncClient: LxSyncClient
        private set
    lateinit var playerController: PlayerController
        private set
    lateinit var coilImageLoader: ImageLoader
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppLogger.info("App", "LX Music Liquid (Kotlin Native) 启动")

        coilImageLoader = HttpClientProvider.createImageLoader(this)
        database = AppDatabase.build(this)
        settingsStore = SettingsStore(this)
        sdkEngine = MusicSdkEngine(this)
        userApiEngine = UserApiEngine(this, sdkEngine)
        repository = MusicRepository(
            context = this,
            db = database,
            settingsStore = settingsStore,
            sdk = sdkEngine,
            userApi = userApiEngine,
        )
        desktopLyricManager = DesktopLyricManager(this) { newFraction ->
            appScope.launch {
                settingsStore.update { s ->
                    s.copy(desktopLyric = s.desktopLyric.copy(posYFraction = newFraction))
                }
            }
        }
        backupManager = BackupManager(this, repository)
        syncClient = LxSyncClient(repository)
        playerController = PlayerController(
            context = this,
            repository = repository,
            imageLoader = coilImageLoader,
            desktopLyricManager = desktopLyricManager,
        )

        appScope.launch {
            repository.ensureBuiltInPlaylists()
        }
    }

    override fun newImageLoader(): ImageLoader = coilImageLoader

    companion object {
        lateinit var instance: LxLiquidApp
            private set
    }
}
