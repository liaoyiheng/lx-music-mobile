import './polyfills.js'
import musicSdk, { init as sdkInit, findMusic as sdkFindMusic } from '../../src/utils/musicSdk'
import { toNewMusicInfo, toOldMusicInfo } from '../../src/utils'

const ONLINE_SOURCES = ['kw', 'kg', 'tx', 'wy', 'mg']

const normalizeList = (list) => {
  if (!Array.isArray(list)) return []
  const seen = new Set()
  const out = []
  for (const item of list) {
    if (!item) continue
    const n = item.meta ? item : toNewMusicInfo(item)
    if (!n || !n.id || seen.has(n.id)) continue
    seen.add(n.id)
    out.push(n)
  }
  return out
}

const ensureOldInfo = (info) => {
  if (!info) return {}
  return info.meta ? toOldMusicInfo(info) : info
}

const unwrapPromise = (val) => {
  if (val && typeof val.then === 'function') return val
  if (val && val.promise && typeof val.promise.then === 'function') return val.promise
  return Promise.resolve(val)
}

const handlers = {
  async init() {
    await sdkInit()
    return { ok: true }
  },

  async searchMusic({ source = 'wy', keyword = '', page = 1, limit = 30 }) {
    const q = String(keyword || '').trim()
    if (!q) return { list: [], allPage: 0, limit, total: 0, source }

    if (source === 'all') {
      const results = await Promise.all(
        ONLINE_SOURCES.map(async (src) => {
          try {
            const res = await unwrapPromise(musicSdk[src]?.musicSearch?.search(q, page, Math.min(limit, 20)))
            return normalizeList(res?.list)
          } catch {
            return []
          }
        })
      )
      // Interleave top results from each source for balanced aggregate ranking
      const merged = []
      const maxLen = Math.max(0, ...results.map(r => r.length))
      for (let i = 0; i < maxLen; i++) {
        for (const arr of results) {
          if (i < arr.length) merged.push(arr[i])
        }
      }
      return {
        list: merged,
        allPage: merged.length > 0 ? page + 1 : page,
        limit,
        total: merged.length,
        source: 'all',
      }
    }

    const mod = musicSdk[source]
    if (!mod?.musicSearch) throw new Error(`Unsupported search source: ${source}`)
    const res = await unwrapPromise(mod.musicSearch.search(q, page, limit))
    return {
      list: normalizeList(res?.list),
      allPage: Number(res?.allPage ?? 0),
      limit: Number(res?.limit ?? limit),
      total: Number(res?.total ?? 0),
      source,
    }
  },

  async searchSongList({ source = 'wy', keyword = '', page = 1, limit = 20 }) {
    const q = String(keyword || '').trim()
    if (!q) return { list: [], total: 0, limit, source }

    if (source === 'all') {
      const results = await Promise.all(
        ONLINE_SOURCES.map(async (src) => {
          try {
            const res = await unwrapPromise(musicSdk[src]?.songList?.search(q, page, 10))
            return Array.isArray(res?.list) ? res.list : []
          } catch {
            return []
          }
        })
      )
      const merged = []
      const maxLen = Math.max(0, ...results.map(r => r.length))
      for (let i = 0; i < maxLen; i++) {
        for (const arr of results) {
          if (i < arr.length) merged.push(arr[i])
        }
      }
      return { list: merged, total: merged.length, limit, source: 'all' }
    }

    const mod = musicSdk[source]
    if (!mod?.songList?.search) return { list: [], total: 0, limit, source }
    const res = await unwrapPromise(mod.songList.search(q, page, limit))
    const rawList = Array.isArray(res?.list) ? res.list : []
    return {
      list: rawList.map(item => (item && !item.source ? { ...item, source } : item)),
      total: Number(res?.total ?? 0),
      limit: Number(res?.limit ?? limit),
      source,
    }
  },

  async searchTips({ source = 'kw', keyword = '' }) {
    const q = String(keyword || '').trim()
    if (!q) return []
    const target = musicSdk[source]?.tipSearch ? source : 'kw'
    const res = await unwrapPromise(musicSdk[target].tipSearch.search(q))
    return Array.isArray(res) ? res.map(String) : []
  },

  async hotSearch({ source = 'all' }) {
    if (source === 'all') {
      const results = await Promise.all(
        ONLINE_SOURCES.map(async (src) => {
          try {
            const res = await unwrapPromise(musicSdk[src]?.hotSearch?.getList())
            return Array.isArray(res?.list) ? res.list : []
          } catch {
            return []
          }
        })
      )
      const seen = new Set()
      const merged = []
      const maxLen = Math.max(0, ...results.map(r => r.length))
      for (let i = 0; i < maxLen; i++) {
        for (const arr of results) {
          const w = arr[i]
          if (w && !seen.has(w)) {
            seen.add(w)
            merged.push(String(w))
          }
        }
      }
      return { source: 'all', list: merged.slice(0, 30) }
    }
    const res = await unwrapPromise(musicSdk[source]?.hotSearch?.getList())
    return { source, list: Array.isArray(res?.list) ? res.list.map(String) : [] }
  },

  async getSongListSorts({ source = 'wy' }) {
    const mod = musicSdk[source]?.songList
    return Array.isArray(mod?.sortList) ? mod.sortList : []
  },

  async getSongListTags({ source = 'wy' }) {
    const mod = musicSdk[source]?.songList
    if (!mod?.getTags) return { hotTag: [], tags: [] }
    return await unwrapPromise(mod.getTags())
  },

  async getSongList({ source = 'wy', sortId = '', tagId = '', page = 1 }) {
    const mod = musicSdk[source]?.songList
    if (!mod?.getList) throw new Error(`Unsupported songList source: ${source}`)
    const effectiveSort = sortId || (mod.sortList?.[0]?.id ?? 'hot')
    const res = await unwrapPromise(mod.getList(effectiveSort, tagId || '', page))
    const rawList = Array.isArray(res?.list) ? res.list : []
    return {
      list: rawList.map(item => (item && !item.source ? { ...item, source } : item)),
      total: Number(res?.total ?? 0),
      page: Number(res?.page ?? page),
      limit: Number(res?.limit ?? 30),
      source,
    }
  },

  async getSongListDetail({ source = 'wy', id = '', page = 1 }) {
    const mod = musicSdk[source]?.songList
    if (!mod?.getListDetail) throw new Error(`Unsupported songList detail source: ${source}`)
    const res = await unwrapPromise(mod.getListDetail(String(id), page))
    return {
      list: normalizeList(res?.list),
      page: Number(res?.page ?? page),
      limit: Number(res?.limit ?? 100),
      total: Number(res?.total ?? (res?.list?.length ?? 0)),
      source,
      info: res?.info || {},
    }
  },

  async getLeaderboards({ source = 'wy' }) {
    const mod = musicSdk[source]?.leaderboard
    if (!mod) return { list: [], source }
    const normalizeBoards = (arr) => (Array.isArray(arr) ? arr : []).map(item => ({
      ...item,
      bangid: String(item?.bangid ?? ''),
      source: item?.source || source,
    }))
    if (typeof mod.getBoards === 'function') {
      const res = await unwrapPromise(mod.getBoards())
      return { list: normalizeBoards(res?.list), source }
    }
    return { list: normalizeBoards(mod.list), source }
  },

  async getLeaderboardDetail({ source = 'wy', bangid = '', page = 1 }) {
    const mod = musicSdk[source]?.leaderboard
    if (!mod?.getList) throw new Error(`Unsupported leaderboard source: ${source}`)
    const res = await unwrapPromise(mod.getList(String(bangid), page))
    return {
      list: normalizeList(res?.list),
      total: Number(res?.total ?? (res?.list?.length ?? 0)),
      limit: Number(res?.limit ?? 100),
      page: Number(res?.page ?? page),
      source,
    }
  },

  async getLyric({ musicInfo }) {
    const oldInfo = ensureOldInfo(musicInfo)
    const mod = musicSdk[oldInfo.source]
    if (!mod?.getLyric) return { lyric: '', tlyric: '', rlyric: '', lxlyric: '' }
    const res = await unwrapPromise(mod.getLyric(oldInfo))
    return {
      lyric: String(res?.lyric ?? ''),
      tlyric: String(res?.tlyric ?? ''),
      rlyric: String(res?.rlyric ?? ''),
      lxlyric: String(res?.lxlyric ?? ''),
    }
  },

  async getPic({ musicInfo }) {
    const oldInfo = ensureOldInfo(musicInfo)
    const mod = musicSdk[oldInfo.source]
    if (!mod?.getPic) return oldInfo.img || ''
    const url = await unwrapPromise(mod.getPic(oldInfo))
    return String(url || oldInfo.img || '')
  },

  async getComments({ musicInfo, page = 1, limit = 20, type = 'hot' }) {
    const oldInfo = ensureOldInfo(musicInfo)
    const mod = musicSdk[oldInfo.source]?.comment
    if (!mod) return { source: oldInfo.source, comments: [], total: 0, page, limit, maxPage: 1 }
    const fn = type === 'latest' ? mod.getComment : mod.getHotComment
    if (typeof fn !== 'function') return { source: oldInfo.source, comments: [], total: 0, page, limit, maxPage: 1 }
    const res = await unwrapPromise(fn.call(mod, oldInfo, page, limit))
    return {
      source: oldInfo.source,
      comments: Array.isArray(res?.comments) ? res.comments : [],
      total: Number(res?.total ?? 0),
      page: Number(res?.page ?? page),
      limit: Number(res?.limit ?? limit),
      maxPage: Number(res?.maxPage ?? 1),
    }
  },

  async findMusic({ musicInfo }) {
    const oldInfo = ensureOldInfo(musicInfo)
    const res = await sdkFindMusic(oldInfo)
    return normalizeList(res)
  },

  async toOldMusicInfo({ musicInfo }) {
    return ensureOldInfo(musicInfo)
  },
}

globalThis.__sdk_dispatch = (taskId, method, argsJson) => {
  const handler = handlers[method]
  if (!handler) {
    globalThis.__sdk_task_reject(taskId, `Unknown SDK method: ${method}`)
    return
  }
  let args = {}
  try {
    args = argsJson ? JSON.parse(argsJson) : {}
  } catch (e) {
    globalThis.__sdk_task_reject(taskId, `Invalid args JSON: ${e.message}`)
    return
  }
  Promise.resolve()
    .then(() => handler(args))
    .then((res) => {
      globalThis.__sdk_task_resolve(taskId, JSON.stringify(res ?? null))
    })
    .catch((err) => {
      globalThis.__sdk_task_reject(taskId, String(err?.message || err || 'SDK error'))
    })
}
