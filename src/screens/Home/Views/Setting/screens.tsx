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

/** 分组（iOS 26 设置的一张张圆角卡片） */
export const SETTING_GROUPS: SettingScreenIds[][] = [
  ['basic', 'player', 'lyric_desktop'],
  ['search', 'list'],
  ['sync', 'backup'],
  ['other'],
  ['version', 'about'],
]

/** 每个分类左侧的高饱和 iOS 风格彩色圆角图标（Ionicons） */
export const SETTING_ICONS: Record<SettingScreenIds, { icon: string, color: string }> = {
  basic: { icon: 'settings-outline', color: '#007AFF' },
  player: { icon: 'play', color: '#FA243C' },
  lyric_desktop: { icon: 'chatbox-ellipses-outline', color: '#AF52DE' },
  search: { icon: 'search-outline', color: '#34C759' },
  list: { icon: 'musical-notes-outline', color: '#FF9500' },
  sync: { icon: 'sync-outline', color: '#34C759' },
  backup: { icon: 'cloud-upload-outline', color: '#5856D6' },
  other: { icon: 'ellipsis-horizontal', color: '#8E8E93' },
  version: { icon: 'arrow-up-circle-outline', color: '#007AFF' },
  about: { icon: 'information-circle-outline', color: '#8E8E93' },
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
