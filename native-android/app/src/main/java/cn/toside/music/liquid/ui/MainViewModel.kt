package cn.toside.music.liquid.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.toside.music.liquid.LxLiquidApp
import cn.toside.music.liquid.data.model.AppSettings
import cn.toside.music.liquid.data.model.CommentItem
import cn.toside.music.liquid.data.model.LeaderboardItem
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlaylistIds
import cn.toside.music.liquid.data.model.SongListDetailResult
import cn.toside.music.liquid.data.model.SongListItem
import cn.toside.music.liquid.data.model.SongListSort
import cn.toside.music.liquid.data.model.SongListTagsResult
import cn.toside.music.liquid.data.model.UserApiScript
import cn.toside.music.liquid.data.model.UserPlaylist
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.ui.components.MainTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DetailRoute {
    data class LocalPlaylist(val playlistId: String) : DetailRoute
    data class OnlineSongList(val item: SongListItem) : DetailRoute
    data class Leaderboard(val item: LeaderboardItem) : DetailRoute
}

data class DiscoverUiState(
    val source: String = MusicSources.WY,
    val sorts: List<SongListSort> = emptyList(),
    val selectedSortId: String = "",
    val tags: SongListTagsResult = SongListTagsResult(),
    val selectedTagId: String = "",
    val selectedTagName: String = "全部风格",
    val items: List<SongListItem> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
)

data class ChartsUiState(
    val source: String = MusicSources.WY,
    val boards: List<LeaderboardItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

data class SearchUiState(
    val query: String = "",
    val committedQuery: String = "",
    val source: String = MusicSources.ALL,
    val modeIndex: Int = 0, // 0 = 歌曲, 1 = 歌单
    val tips: List<String> = emptyList(),
    val hotKeywords: List<String> = emptyList(),
    val musicResults: List<MusicInfo> = emptyList(),
    val songListResults: List<SongListItem> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = false,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
)

data class OnlineDetailUiState(
    val title: String = "",
    val author: String = "",
    val coverUrl: String? = null,
    val description: String? = null,
    val playCount: String? = null,
    val source: String = MusicSources.WY,
    val sourceListId: String = "",
    val isLeaderboard: Boolean = false,
    val tracks: List<MusicInfo> = emptyList(),
    val page: Int = 1,
    val total: Int = 0,
    val hasMore: Boolean = false,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
)

data class CommentsUiState(
    val music: MusicInfo? = null,
    val tabIndex: Int = 0, // 0 = 热评, 1 = 最新
    val comments: List<CommentItem> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val maxPage: Int = 1,
    val loading: Boolean = false,
    val error: String? = null,
)

class MainViewModel : ViewModel() {
    val app: LxLiquidApp = LxLiquidApp.instance
    val repo = app.repository
    val player = app.playerController
    val userApiStatus = app.userApiEngine.status
    val syncState = app.syncClient.state
    val appLogs = AppLogger.logs

    val settings: StateFlow<AppSettings> = app.settingsStore.settings
    val playlists: StateFlow<List<UserPlaylist>> = repo.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val userApiScripts: StateFlow<List<UserApiScript>> = repo.observeUserApiScripts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val searchHistory: StateFlow<List<String>> = repo.observeSearchHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val dislikeRules: StateFlow<List<String>> = repo.observeDislikeRules()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _activeTab = MutableStateFlow(MainTab.DISCOVER)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _detailRoute = MutableStateFlow<DetailRoute?>(null)
    val detailRoute: StateFlow<DetailRoute?> = _detailRoute.asStateFlow()

    private val _playerExpanded = MutableStateFlow(false)
    val playerExpanded: StateFlow<Boolean> = _playerExpanded.asStateFlow()

    private val _discover = MutableStateFlow(DiscoverUiState())
    val discover: StateFlow<DiscoverUiState> = _discover.asStateFlow()

    private val _charts = MutableStateFlow(ChartsUiState())
    val charts: StateFlow<ChartsUiState> = _charts.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val _onlineDetail = MutableStateFlow(OnlineDetailUiState())
    val onlineDetail: StateFlow<OnlineDetailUiState> = _onlineDetail.asStateFlow()

    private val _actionTrack = MutableStateFlow<MusicInfo?>(null)
    val actionTrack: StateFlow<MusicInfo?> = _actionTrack.asStateFlow()

    private val _playlistPickerTrack = MutableStateFlow<MusicInfo?>(null)
    val playlistPickerTrack: StateFlow<MusicInfo?> = _playlistPickerTrack.asStateFlow()

    private val _comments = MutableStateFlow(CommentsUiState())
    val comments: StateFlow<CommentsUiState> = _comments.asStateFlow()

    private val _findAlternatesTrack = MutableStateFlow<MusicInfo?>(null)
    val findAlternatesTrack: StateFlow<MusicInfo?> = _findAlternatesTrack.asStateFlow()

    private val _alternatesList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val alternatesList: StateFlow<List<MusicInfo>> = _alternatesList.asStateFlow()

    private val _alternatesLoading = MutableStateFlow(false)
    val alternatesLoading: StateFlow<Boolean> = _alternatesLoading.asStateFlow()

    private val _showSleepTimer = MutableStateFlow(false)
    val showSleepTimer: StateFlow<Boolean> = _showSleepTimer.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toast = _toast.asSharedFlow()

    private var discoverJob: Job? = null
    private var chartsJob: Job? = null
    private var searchJob: Job? = null
    private var tipsJob: Job? = null
    private var detailJob: Job? = null

    init {
        loadDiscover(MusicSources.WY, reset = true)
        loadCharts(MusicSources.WY)
        loadHotSearch()
    }

    fun showToast(msg: String) {
        _toast.tryEmit(msg)
    }

    fun selectTab(tab: MainTab) {
        _activeTab.value = tab
        _detailRoute.value = null
    }

    fun openPlayer() {
        if (player.state.value.currentTrack != null) {
            _playerExpanded.value = true
        }
    }

    fun closePlayer() {
        _playerExpanded.value = false
    }

    fun openLocalPlaylist(playlistId: String) {
        _detailRoute.value = DetailRoute.LocalPlaylist(playlistId)
    }

    fun openOnlineSongList(item: SongListItem) {
        _detailRoute.value = DetailRoute.OnlineSongList(item)
        loadOnlineSongListDetail(item, page = 1)
    }

    fun openLeaderboard(item: LeaderboardItem) {
        _detailRoute.value = DetailRoute.Leaderboard(item)
        loadLeaderboardDetail(item, page = 1)
    }

    fun closeDetail() {
        _detailRoute.value = null
    }

    // ---- Discover ----
    fun selectDiscoverSource(source: String) {
        if (_discover.value.source == source && _discover.value.items.isNotEmpty()) return
        loadDiscover(source = source, sortId = "", tagId = "", tagName = "全部风格", reset = true)
    }

    fun selectDiscoverSort(sortId: String) {
        val d = _discover.value
        loadDiscover(source = d.source, sortId = sortId, tagId = d.selectedTagId, tagName = d.selectedTagName, reset = true)
    }

    fun selectDiscoverTag(tagId: String, tagName: String) {
        val d = _discover.value
        loadDiscover(source = d.source, sortId = d.selectedSortId, tagId = tagId, tagName = tagName, reset = true)
    }

    fun loadDiscover(
        source: String = _discover.value.source,
        sortId: String = _discover.value.selectedSortId,
        tagId: String = _discover.value.selectedTagId,
        tagName: String = _discover.value.selectedTagName,
        reset: Boolean = true,
    ) {
        discoverJob?.cancel()
        val nextPage = if (reset) 1 else _discover.value.page + 1
        _discover.update {
            if (reset) {
                it.copy(
                    source = source,
                    selectedSortId = sortId,
                    selectedTagId = tagId,
                    selectedTagName = tagName,
                    loading = true,
                    error = null,
                    page = 1,
                )
            } else {
                it.copy(loadingMore = true)
            }
        }
        discoverJob = viewModelScope.launch {
            if (reset) {
                val sorts = runCatching { repo.sdk.getSongListSorts(source) }.getOrDefault(emptyList())
                val tags = runCatching { repo.sdk.getSongListTags(source) }.getOrDefault(SongListTagsResult())
                val effectiveSort = sortId.ifBlank { sorts.firstOrNull()?.id.orEmpty() }
                _discover.update {
                    it.copy(sorts = sorts, selectedSortId = effectiveSort, tags = tags)
                }
                val res = runCatching {
                    repo.sdk.getSongList(source, effectiveSort, tagId, 1)
                }
                res.onSuccess { pageRes ->
                    _discover.update {
                        it.copy(
                            items = pageRes.list,
                            page = 1,
                            hasMore = pageRes.list.size >= 15,
                            loading = false,
                            error = null,
                        )
                    }
                }.onFailure { e ->
                    _discover.update { it.copy(loading = false, error = e.message ?: "加载歌单失败") }
                }
            } else {
                val res = runCatching {
                    repo.sdk.getSongList(source, _discover.value.selectedSortId, _discover.value.selectedTagId, nextPage)
                }
                res.onSuccess { pageRes ->
                    _discover.update {
                        val merged = (it.items + pageRes.list).distinctBy { item -> "${item.source}_${item.id}" }
                        it.copy(
                            items = merged,
                            page = nextPage,
                            hasMore = pageRes.list.isNotEmpty(),
                            loadingMore = false,
                        )
                    }
                }.onFailure {
                    _discover.update { it.copy(loadingMore = false) }
                }
            }
        }
    }

    // ---- Charts ----
    fun selectChartsSource(source: String) {
        if (_charts.value.source == source && _charts.value.boards.isNotEmpty()) return
        loadCharts(source)
    }

    fun loadCharts(source: String = _charts.value.source) {
        chartsJob?.cancel()
        _charts.update { it.copy(source = source, loading = true, error = null) }
        chartsJob = viewModelScope.launch {
            runCatching { repo.sdk.getLeaderboards(source) }
                .onSuccess { list ->
                    _charts.update { it.copy(boards = list, loading = false, error = null) }
                }
                .onFailure { e ->
                    _charts.update { it.copy(loading = false, error = e.message ?: "加载排行榜失败") }
                }
        }
    }

    // ---- Online SongList & Leaderboard Detail ----
    private fun loadOnlineSongListDetail(item: SongListItem, page: Int) {
        detailJob?.cancel()
        val reset = page == 1
        _onlineDetail.update {
            if (reset) {
                OnlineDetailUiState(
                    title = item.name,
                    author = item.author,
                    coverUrl = item.img,
                    description = item.desc,
                    playCount = item.playCount,
                    source = item.source,
                    sourceListId = item.id,
                    isLeaderboard = false,
                    loading = true,
                )
            } else {
                it.copy(loadingMore = true)
            }
        }
        detailJob = viewModelScope.launch {
            runCatching { repo.sdk.getSongListDetail(item.source, item.id, page) }
                .onSuccess { res -> applyOnlineDetailResult(res, reset) }
                .onFailure { e ->
                    _onlineDetail.update {
                        it.copy(loading = false, loadingMore = false, error = e.message ?: "加载详情失败")
                    }
                }
        }
    }

    private fun loadLeaderboardDetail(item: LeaderboardItem, page: Int) {
        detailJob?.cancel()
        val reset = page == 1
        _onlineDetail.update {
            if (reset) {
                OnlineDetailUiState(
                    title = item.name,
                    author = MusicSources.displayName(item.source) + "官方榜单",
                    coverUrl = item.coverImg,
                    source = item.source,
                    sourceListId = item.bangid,
                    isLeaderboard = true,
                    loading = true,
                )
            } else {
                it.copy(loadingMore = true)
            }
        }
        detailJob = viewModelScope.launch {
            runCatching { repo.sdk.getLeaderboardDetail(item.source, item.bangid, page) }
                .onSuccess { res -> applyOnlineDetailResult(res, reset) }
                .onFailure { e ->
                    _onlineDetail.update {
                        it.copy(loading = false, loadingMore = false, error = e.message ?: "加载榜单失败")
                    }
                }
        }
    }

    private fun applyOnlineDetailResult(res: SongListDetailResult, reset: Boolean) {
        _onlineDetail.update { cur ->
            val merged = if (reset) res.list else (cur.tracks + res.list).distinctBy { it.id }
            cur.copy(
                title = res.info.name?.takeIf { it.isNotBlank() } ?: cur.title,
                author = res.info.author?.takeIf { it.isNotBlank() } ?: cur.author,
                coverUrl = res.info.img?.takeIf { it.isNotBlank() }
                    ?: cur.coverUrl
                    ?: merged.firstNotNullOfOrNull { it.picUrl },
                description = res.info.desc?.takeIf { it.isNotBlank() } ?: cur.description,
                playCount = res.info.playCount?.takeIf { it.isNotBlank() } ?: cur.playCount,
                tracks = merged,
                page = res.page,
                total = maxOf(res.total, merged.size),
                hasMore = res.list.isNotEmpty() && merged.size < res.total,
                loading = false,
                loadingMore = false,
                error = null,
            )
        }
    }

    fun loadMoreOnlineDetail() {
        val d = _onlineDetail.value
        if (d.loading || d.loadingMore || !d.hasMore) return
        val route = _detailRoute.value ?: return
        when (route) {
            is DetailRoute.OnlineSongList -> loadOnlineSongListDetail(route.item, d.page + 1)
            is DetailRoute.Leaderboard -> loadLeaderboardDetail(route.item, d.page + 1)
            else -> Unit
        }
    }

    fun collectCurrentOnlineList() {
        val d = _onlineDetail.value
        if (d.tracks.isEmpty()) return
        viewModelScope.launch {
            repo.createPlaylist(
                name = d.title,
                tracks = d.tracks,
                source = d.source,
                sourceListId = d.sourceListId,
                coverUrl = d.coverUrl,
            )
            showToast("已收藏歌单《${d.title}》到资料库 (${d.tracks.size} 首)")
        }
    }

    // ---- Search ----
    private fun loadHotSearch() {
        viewModelScope.launch {
            val hot = runCatching { repo.sdk.hotSearch("all") }.getOrDefault(emptyList())
            _search.update { it.copy(hotKeywords = hot.take(18)) }
        }
    }

    fun updateSearchQuery(query: String) {
        _search.update { it.copy(query = query) }
        if (query.isBlank()) {
            tipsJob?.cancel()
            _search.update {
                it.copy(
                    committedQuery = "",
                    tips = emptyList(),
                    musicResults = emptyList(),
                    songListResults = emptyList(),
                    error = null,
                )
            }
            return
        }
        if (settings.value.enableSearchTips) {
            tipsJob?.cancel()
            tipsJob = viewModelScope.launch {
                delay(240L)
                val tipSource = _search.value.source.let { if (it == MusicSources.ALL) MusicSources.WY else it }
                val tips = runCatching { repo.sdk.searchTips(tipSource, query) }.getOrDefault(emptyList())
                _search.update { it.copy(tips = tips.take(8)) }
            }
        }
    }

    fun selectSearchSource(source: String) {
        _search.update { it.copy(source = source) }
        val q = _search.value.committedQuery
        if (q.isNotBlank()) submitSearch(q)
    }

    fun selectSearchMode(modeIndex: Int) {
        _search.update { it.copy(modeIndex = modeIndex) }
        val q = _search.value.committedQuery
        if (q.isNotBlank()) submitSearch(q)
    }

    fun submitSearch(rawQuery: String = _search.value.query) {
        val q = rawQuery.trim()
        if (q.isBlank()) return
        searchJob?.cancel()
        tipsJob?.cancel()
        _search.update {
            it.copy(
                query = q,
                committedQuery = q,
                tips = emptyList(),
                loading = true,
                error = null,
                page = 1,
            )
        }
        searchJob = viewModelScope.launch {
            repo.recordSearchHistory(q)
            val st = _search.value
            if (st.modeIndex == 0) {
                runCatching { repo.sdk.searchMusic(st.source, q, page = 1, limit = 30) }
                    .onSuccess { res ->
                        _search.update {
                            it.copy(
                                musicResults = res.list,
                                page = 1,
                                hasMore = res.allPage > 1 || res.list.size >= 20,
                                loading = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { e ->
                        _search.update { it.copy(loading = false, error = e.message ?: "搜索失败") }
                    }
            } else {
                val src = if (st.source == MusicSources.ALL) MusicSources.WY else st.source
                runCatching { repo.sdk.searchSongList(src, q, page = 1, limit = 20) }
                    .onSuccess { res ->
                        _search.update {
                            it.copy(
                                songListResults = res.list,
                                page = 1,
                                hasMore = res.list.size >= 15,
                                loading = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { e ->
                        _search.update { it.copy(loading = false, error = e.message ?: "搜索歌单失败") }
                    }
            }
        }
    }

    fun loadMoreSearch() {
        val st = _search.value
        if (st.loading || st.loadingMore || !st.hasMore || st.committedQuery.isBlank()) return
        val nextPage = st.page + 1
        _search.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            if (st.modeIndex == 0) {
                runCatching { repo.sdk.searchMusic(st.source, st.committedQuery, page = nextPage, limit = 30) }
                    .onSuccess { res ->
                        _search.update { cur ->
                            cur.copy(
                                musicResults = (cur.musicResults + res.list).distinctBy { it.id },
                                page = nextPage,
                                hasMore = res.list.isNotEmpty() && nextPage < res.allPage.coerceAtLeast(nextPage + 1),
                                loadingMore = false,
                            )
                        }
                    }
                    .onFailure { _search.update { it.copy(loadingMore = false) } }
            } else {
                val src = if (st.source == MusicSources.ALL) MusicSources.WY else st.source
                runCatching { repo.sdk.searchSongList(src, st.committedQuery, page = nextPage, limit = 20) }
                    .onSuccess { res ->
                        _search.update { cur ->
                            cur.copy(
                                songListResults = (cur.songListResults + res.list).distinctBy { "${it.source}_${it.id}" },
                                page = nextPage,
                                hasMore = res.list.isNotEmpty(),
                                loadingMore = false,
                            )
                        }
                    }
                    .onFailure { _search.update { it.copy(loadingMore = false) } }
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repo.clearSearchHistory() }
    }

    fun deleteSearchHistory(kw: String) {
        viewModelScope.launch { repo.deleteSearchHistory(kw) }
    }

    // ---- Track / Playlist Actions ----
    fun openTrackActions(track: MusicInfo) {
        _actionTrack.value = track
    }

    fun closeTrackActions() {
        _actionTrack.value = null
    }

    fun openPlaylistPicker(track: MusicInfo) {
        _actionTrack.value = null
        _playlistPickerTrack.value = track
    }

    fun closePlaylistPicker() {
        _playlistPickerTrack.value = null
    }

    fun addTrackToPlaylist(playlistId: String, track: MusicInfo, playlistName: String) {
        viewModelScope.launch {
            repo.addTracksToPlaylist(playlistId, listOf(track))
            _playlistPickerTrack.value = null
            showToast("已添加到「$playlistName」")
        }
    }

    fun createPlaylist(name: String, initialTrack: MusicInfo? = null) {
        viewModelScope.launch {
            val tracks = listOfNotNull(initialTrack)
            repo.createPlaylist(name, tracks)
            _playlistPickerTrack.value = null
            showToast("已创建歌单「$name」")
        }
    }

    fun renamePlaylist(id: String, name: String) {
        viewModelScope.launch {
            repo.renamePlaylist(id, name)
            showToast("已重命名为「$name」")
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            repo.deletePlaylist(id)
            if ((_detailRoute.value as? DetailRoute.LocalPlaylist)?.playlistId == id) {
                _detailRoute.value = null
            }
            showToast("已删除歌单")
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repo.removeTracksFromPlaylist(playlistId, listOf(trackId))
            showToast("已从歌单移除")
        }
    }

    fun toggleFavoriteTrack(track: MusicInfo) {
        viewModelScope.launch {
            val added = repo.toggleFavorite(track)
            showToast(if (added) "已添加到「我的收藏」" else "已取消收藏")
        }
    }

    fun addDislike(track: MusicInfo) {
        viewModelScope.launch {
            repo.addDislikeRule(track)
            showToast("已将《${track.name}》加入不喜欢列表")
        }
    }

    fun saveDislikeRules(rawText: String) {
        viewModelScope.launch {
            repo.saveDislikeRulesText(rawText)
            showToast("不喜欢规则已保存")
        }
    }

    // ---- Comments ----
    fun openComments(music: MusicInfo) {
        _actionTrack.value = null
        _comments.value = CommentsUiState(music = music, tabIndex = 0, loading = true)
        loadComments(music, tabIndex = 0, page = 1)
    }

    fun closeComments() {
        _comments.value = CommentsUiState()
    }

    fun switchCommentsTab(tabIndex: Int) {
        val m = _comments.value.music ?: return
        loadComments(m, tabIndex = tabIndex, page = 1)
    }

    fun loadMoreComments() {
        val c = _comments.value
        val m = c.music ?: return
        if (c.loading || c.page >= c.maxPage) return
        loadComments(m, tabIndex = c.tabIndex, page = c.page + 1)
    }

    private fun loadComments(music: MusicInfo, tabIndex: Int, page: Int) {
        val reset = page == 1
        _comments.update {
            it.copy(
                music = music,
                tabIndex = tabIndex,
                loading = true,
                error = null,
                comments = if (reset) emptyList() else it.comments,
            )
        }
        viewModelScope.launch {
            val type = if (tabIndex == 0) "hot" else "latest"
            runCatching { repo.sdk.getComments(music, page = page, limit = 20, type = type) }
                .onSuccess { res ->
                    _comments.update { cur ->
                        cur.copy(
                            comments = if (reset) res.comments else cur.comments + res.comments,
                            total = res.total,
                            page = res.page,
                            maxPage = res.maxPage.coerceAtLeast(1),
                            loading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _comments.update { it.copy(loading = false, error = e.message ?: "获取评论失败") }
                }
        }
    }

    // ---- Alternate Source Switcher (FindMusic) ----
    fun openAlternateSources(track: MusicInfo) {
        _actionTrack.value = null
        _findAlternatesTrack.value = track
        _alternatesList.value = emptyList()
        _alternatesLoading.value = true
        viewModelScope.launch {
            val list = runCatching { repo.sdk.findMusic(track) }.getOrDefault(emptyList())
            _alternatesList.value = list
            _alternatesLoading.value = false
        }
    }

    fun closeAlternateSources() {
        _findAlternatesTrack.value = null
    }

    fun openSleepTimer() { _showSleepTimer.value = true }
    fun closeSleepTimer() { _showSleepTimer.value = false }

    // ---- Settings & Custom User API & Backup & Sync ----
    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { app.settingsStore.update(transform) }
    }

    fun importUserApiFromText(rawScript: String, fallbackName: String = "自定义音源") {
        viewModelScope.launch {
            runCatching { repo.importUserApiScript(rawScript, fallbackName, activate = true) }
                .onSuccess { showToast("已导入并启用音源「${it.name}」") }
                .onFailure { showToast("导入失败：${it.message}") }
        }
    }

    fun importUserApiFromUrl(url: String) {
        viewModelScope.launch {
            showToast("正在下载自定义音源脚本...")
            runCatching { repo.importUserApiFromUrl(url) }
                .onSuccess { showToast("已在线导入并启用「${it.name}」") }
                .onFailure { showToast("在线导入失败：${it.message}") }
        }
    }

    fun importUserApiFromUri(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val raw = app.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalStateException("无法读取脚本文件")
                val name = uri.lastPathSegment?.substringAfterLast('/')?.removeSuffix(".js") ?: "本地音源"
                repo.importUserApiScript(raw, name, activate = true)
            }.onSuccess {
                showToast("已导入并启用音源「${it.name}」")
            }.onFailure {
                showToast("脚本导入失败：${it.message}")
            }
        }
    }

    fun activateUserApi(scriptId: String?) {
        viewModelScope.launch {
            repo.activateUserApiScript(scriptId)
            showToast(if (scriptId == null) "已停用自定义音源" else "正在切换音源脚本...")
        }
    }

    fun deleteUserApi(scriptId: String) {
        viewModelScope.launch {
            repo.deleteUserApiScript(scriptId)
            showToast("已删除音源脚本")
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { app.backupManager.exportPlaylistsToUri(uri, compressGzip = true) }
                .onSuccess { showToast("已导出 ${it.playlistCount} 个歌单（共 ${it.trackCount} 首）") }
                .onFailure { showToast("导出失败：${it.message}") }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { app.backupManager.importFromUri(uri) }
                .onSuccess { showToast("成功导入 ${it.playlistCount} 个歌单（${it.trackCount} 首歌曲）") }
                .onFailure { showToast("导入失败：${it.message}") }
        }
    }

    fun clearCaches() {
        viewModelScope.launch {
            repo.clearAllCaches()
            showToast("已清空全部图片、歌词与音频缓存")
        }
    }

    fun connectSync(url: String, code: String) {
        viewModelScope.launch {
            app.settingsStore.update { it.copy(syncServerUrl = url, syncAuthCode = code, syncEnabled = true) }
            app.syncClient.connect(url, code)
        }
    }

    fun disconnectSync() {
        app.syncClient.disconnect()
        viewModelScope.launch { app.settingsStore.update { it.copy(syncEnabled = false) } }
    }
}
