import { memo } from 'react'

import Basic from './settings/Basic'
import Player from './settings/Player'
import LyricDesktop from './settings/LyricDesktop'
import Search from './settings/Search'
import List from './settings/List'
import Sync from './settings/Sync'
import Backup from './settings/Backup'
import Other from './settings/Other'
import Version from './settings/Version'
import About from './settings/About'

export const SETTING_SCREENS = [
  'basic',
  'player',
  'lyric_desktop',
  'search',
  'list',
  'sync',
  'backup',
  'other',
  'version',
  'about',
] as const

export type SettingScreenIds = typeof SETTING_SCREENS[number]

/** 分组（iOS 设置的一张张圆角卡片） */
export const SETTING_GROUPS: SettingScreenIds[][] = [
  ['basic', 'player', 'lyric_desktop'],
  ['search', 'list'],
  ['sync', 'backup'],
  ['other'],
  ['version', 'about'],
]

/** 每个分类左侧的彩色圆角图标 */
export const SETTING_ICONS: Record<SettingScreenIds, { icon: string, color: string }> = {
  basic: { icon: 'setting', color: '#8E8E93' },
  player: { icon: 'play', color: '#FA2D48' },
  lyric_desktop: { icon: 'lyric-on', color: '#AF52DE' },
  search: { icon: 'search-2', color: '#007AFF' },
  list: { icon: 'album', color: '#FF9500' },
  sync: { icon: 'list-loop', color: '#34C759' },
  backup: { icon: 'sd-card', color: '#30B0C7' },
  other: { icon: 'dots-vertical', color: '#8E8E93' },
  version: { icon: 'available_updates', color: '#007AFF' },
  about: { icon: 'help', color: '#8E8E93' },
}

export const SettingContent = memo(({ id }: { id: SettingScreenIds }) => {
  switch (id) {
    case 'player': return <Player />
    case 'lyric_desktop': return <LyricDesktop />
    case 'search': return <Search />
    case 'list': return <List />
    case 'sync': return <Sync />
    case 'backup': return <Backup />
    case 'other': return <Other />
    case 'version': return <Version />
    case 'about': return <About />
    case 'basic':
    default: return <Basic />
  }
})
