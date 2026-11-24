import { memo } from 'react'
import { View } from 'react-native'
import { createStyle } from '@/utils/tools'
import Search from '../Views/Search'
import SongList from '../Views/SongList'
import Mylist from '../Views/Mylist'
import Leaderboard from '../Views/Leaderboard'
import Setting from '../Views/Setting'
import KeepAlive from './KeepAlive'
import type { BrowseId } from './tabs'

export const LibraryPage = memo(() => <View style={styles.body}><Mylist /></View>)

export const SearchPage = memo(() => <View style={styles.body}><Search /></View>)

export const SettingPage = memo(() => <View style={styles.body}><Setting /></View>)

/**
 * 浏览：歌单广场 + 排行榜
 * 由各自视图承载「浏览」大标题 + 顶部液态玻璃音源胶囊 + 分段控件，保持与设计稿一致
 */
export const BrowsePage = memo(({ browseId }: { browseId: BrowseId, isWide: boolean }) => {
  return (
    <View style={styles.body}>
      <KeepAlive active={browseId == 'nav_songlist'}><SongList /></KeepAlive>
      <KeepAlive active={browseId == 'nav_top'}><Leaderboard /></KeepAlive>
    </View>
  )
})

const styles = createStyle({
  body: {
    flex: 1,
  },
})
