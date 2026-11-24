import { useEffect, useMemo, useRef, useState } from 'react'
import { View } from 'react-native'
import { getSongListSetting, saveSongListSetting } from '@/utils/data'
import LargeTitle from '@/components/apple/LargeTitle'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import { setNavActiveId } from '@/core/common'
import { useI18n } from '@/lang'
import { useHorizontalMode } from '@/utils/hooks'
import songlistState, { type InitState, type SortInfo } from '@/store/songlist/state'
import { pagePadding, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import HeaderBar, { type HeaderBarProps, type HeaderBarType } from './HeaderBar'
import SourceSelector, { type SourceSelectorProps, type SourceSelectorType } from './HeaderBar/SourceSelector'
import OpenList, { type OpenListType } from './HeaderBar/OpenList'
import List, { type ListType } from './List'

interface SonglistInfo {
  source: InitState['sources'][number]
  sortId: SortInfo['id']
  tagName: string
  tagId: string
}

type BrowseTabId = 'nav_songlist' | 'nav_top'

export default () => {
  const t = useI18n()
  const isWide = useHorizontalMode()
  const headerBarRef = useRef<HeaderBarType>(null)
  const sourceSelectorRef = useRef<SourceSelectorType>(null)
  const openListRef = useRef<OpenListType>(null)
  const listRef = useRef<ListType>(null)
  const songlistInfo = useRef<SonglistInfo>({ source: 'kw', sortId: '5', tagName: '', tagId: '' })
  const [filterState, setFilterState] = useState<SonglistInfo>({ source: 'kw', sortId: '5', tagName: '', tagId: '' })

  useEffect(() => {
    void getSongListSetting().then(info => {
      const next: SonglistInfo = {
        source: info.source,
        sortId: info.sortId,
        tagName: info.tagName,
        tagId: info.tagId,
      }
      songlistInfo.current = next
      setFilterState(next)
      sourceSelectorRef.current?.setSource(info.source)
      openListRef.current?.setInfo(info.source)
      headerBarRef.current?.setSource(info.source, info.sortId, info.tagName, info.tagId)
      listRef.current?.loadList(info.source, info.sortId, info.tagId)
    })
  }, [])

  const handleSortChange: HeaderBarProps['onSortChange'] = (id) => {
    songlistInfo.current.sortId = id
    setFilterState(prev => ({ ...prev, sortId: id }))
    void saveSongListSetting({ sortId: id })
    listRef.current?.loadList(songlistInfo.current.source, id, songlistInfo.current.tagId)
  }

  const handleTagChange: HeaderBarProps['onTagChange'] = (name, id) => {
    songlistInfo.current.tagName = name
    songlistInfo.current.tagId = id
    setFilterState(prev => ({ ...prev, tagName: name, tagId: id }))
    void saveSongListSetting({ tagName: name, tagId: id })
    listRef.current?.loadList(songlistInfo.current.source, songlistInfo.current.sortId, id)
  }

  const handleSourceChange: SourceSelectorProps['onSourceChange'] = (source) => {
    const sortId = songlistState.sortList[source]![0].id
    const next: SonglistInfo = { source, sortId, tagName: '', tagId: '' }
    songlistInfo.current = next
    setFilterState(next)
    void saveSongListSetting({ sortId, source, tagId: '', tagName: '' })
    openListRef.current?.setInfo(source)
    headerBarRef.current?.setSource(source, sortId, '', '')
    listRef.current?.loadList(source, sortId, '')
  }

  const browseTabs = useMemo<Array<SegmentItem<BrowseTabId>>>(() => [
    { id: 'nav_songlist', label: t('nav_songlist') },
    { id: 'nav_top', label: t('nav_top') },
  ], [t])

  const filterBar = useMemo(() => (
    <HeaderBar
      ref={headerBarRef}
      source={filterState.source}
      sortId={filterState.sortId}
      tagName={filterState.tagName}
      tagId={filterState.tagId}
      onSortChange={handleSortChange}
      onTagChange={handleTagChange}
    />
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ), [filterState.source, filterState.sortId, filterState.tagName, filterState.tagId])

  const titleRight = (
    <View style={styles.titleRight}>
      <OpenList ref={openListRef} />
      <SourceSelector ref={sourceSelectorRef} onSourceChange={handleSourceChange} />
    </View>
  )

  return (
    <View style={styles.container}>
      <LargeTitle title={isWide ? t('nav_songlist') : t('apple_tab_browse')} right={titleRight} />
      {!isWide ? (
        <SegmentedControl
          items={browseTabs}
          value="nav_songlist"
          onChange={id => { setNavActiveId(id) }}
          style={styles.seg}
        />
      ) : null}
      <List ref={listRef} filterComponent={filterBar} />
    </View>
  )
}

const styles = createStyle({
  container: {
    position: 'relative',
    flex: 1,
  },
  titleRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  seg: {
    marginHorizontal: pagePadding,
    marginBottom: spacing.sm,
  },
})
