import { memo, useMemo } from 'react'
import { View } from 'react-native'
import { useI18n } from '@/lang'
import LargeTitle from '@/components/apple/LargeTitle'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import { pagePadding, spacing } from '@/theme/apple'
import { setNavActiveId } from '@/core/common'
import { createStyle } from '@/utils/tools'
import Search from '../Views/Search'
import SongList from '../Views/SongList'
import Mylist from '../Views/Mylist'
import Leaderboard from '../Views/Leaderboard'
import Setting from '../Views/Setting'
import KeepAlive from './KeepAlive'
import type { BrowseId } from './tabs'

export const LibraryPage = memo(() => <View style={styles.body}><Mylist /></View>)

export const SearchPage = memo(() => {
  const t = useI18n()
  return (
    <>
      <LargeTitle title={t('nav_search')} />
      <View style={styles.body}><Search /></View>
    </>
  )
})

export const SettingPage = memo(() => <View style={styles.body}><Setting /></View>)

/**
 * 浏览：歌单广场 + 排行榜
 * 竖屏用分段控件切换；宽屏由侧边栏分别进入，不显示分段控件
 */
export const BrowsePage = memo(({ browseId, isWide }: { browseId: BrowseId, isWide: boolean }) => {
  const t = useI18n()
  const items = useMemo<Array<SegmentItem<BrowseId>>>(() => [
    { id: 'nav_songlist', label: t('nav_songlist') },
    { id: 'nav_top', label: t('nav_top') },
  ], [t])

  return (
    <>
      <LargeTitle title={isWide ? t(browseId) : t('apple_tab_browse')} />
      {isWide
        ? null
        : (
            <View style={styles.segment}>
              <SegmentedControl items={items} value={browseId} onChange={setNavActiveId} />
            </View>
          )}
      <View style={styles.body}>
        <KeepAlive active={browseId == 'nav_songlist'}><SongList /></KeepAlive>
        <KeepAlive active={browseId == 'nav_top'}><Leaderboard /></KeepAlive>
      </View>
    </>
  )
})

const styles = createStyle({
  body: {
    flex: 1,
  },
  segment: {
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.sm,
  },
})
