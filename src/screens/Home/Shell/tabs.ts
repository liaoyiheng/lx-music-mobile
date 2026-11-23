import type { NAV_ID_Type } from '@/config/constant'
import { setNavActiveId } from '@/core/common'
import commonState from '@/store/common/state'

/**
 * 底部标签栏：资料库 / 浏览 / 搜索 / 设置
 * 内部仍沿用原版的 navActiveId（nav_love / nav_songlist / nav_top / nav_search / nav_setting），
 * 「浏览」同时承载歌单广场（nav_songlist）和排行榜（nav_top）
 */
export type TabId = 'library' | 'browse' | 'search' | 'setting'
export type BrowseId = Extract<NAV_ID_Type, 'nav_songlist' | 'nav_top'>

export interface TabInfo {
  id: TabId
  icon: string
  label: 'apple_tab_library' | 'apple_tab_browse' | 'nav_search' | 'nav_setting'
}

export const TABS: readonly TabInfo[] = [
  { id: 'library', icon: 'album', label: 'apple_tab_library' },
  { id: 'browse', icon: 'leaderboard', label: 'apple_tab_browse' },
  { id: 'search', icon: 'search-2', label: 'nav_search' },
  { id: 'setting', icon: 'setting', label: 'nav_setting' },
]

export const isBrowseId = (id: NAV_ID_Type): id is BrowseId => id == 'nav_songlist' || id == 'nav_top'

let lastBrowseId: BrowseId = 'nav_songlist'
export const rememberBrowseId = (id: BrowseId) => { lastBrowseId = id }
export const getLastBrowseId = () => lastBrowseId

export const getTabId = (navId: NAV_ID_Type): TabId => {
  switch (navId) {
    case 'nav_love': return 'library'
    case 'nav_search': return 'search'
    case 'nav_setting': return 'setting'
    default: return 'browse'
  }
}

export const selectTab = (id: TabId) => {
  switch (id) {
    case 'library':
      setNavActiveId('nav_love')
      break
    case 'browse':
      if (!isBrowseId(commonState.navActiveId)) setNavActiveId(lastBrowseId)
      break
    case 'search':
      setNavActiveId('nav_search')
      break
    case 'setting':
      setNavActiveId('nav_setting')
      break
  }
}
