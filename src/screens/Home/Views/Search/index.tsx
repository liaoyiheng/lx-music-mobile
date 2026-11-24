import { useCallback, useEffect, useRef, useState } from 'react'
import { type LayoutChangeEvent, View } from 'react-native'

import HeaderBar, { type HeaderBarProps, type HeaderBarType } from './HeaderBar'
import SearchTypeSelector from './SearchTypeSelector'
import { type SearchSource, type SearchSources } from './SourceSelector'
import searchState, { type SearchType } from '@/store/search/state'
import searchMusicState from '@/store/search/music/state'
import searchSonglistState from '@/store/search/songlist/state'
import { getSearchSetting, saveSearchSetting } from '@/utils/data'
import { createStyle } from '@/utils/tools'
import TipList, { type TipListType } from './TipList'
import List, { type ListType } from './List'
import { addHistoryWord, setSearchText, setTipList } from '@/core/search/search'

interface SearchInfo {
  temp_source: LX.OnlineSource
  source: SearchSource
  searchType: SearchType
}

export default () => {
  const headerBarRef = useRef<HeaderBarType>(null)
  const searchTipListRef = useRef<TipListType>(null)
  const listRef = useRef<ListType>(null)
  const layoutHeightRef = useRef<number>(0)
  const searchInfo = useRef<SearchInfo>({ temp_source: 'kw', source: 'kw', searchType: 'music' })
  const timeoutRef = useRef<NodeJS.Timeout | null>(null)

  const [isFocused, setIsFocused] = useState(false)
  const [activeQuery, setActiveQuery] = useState(searchState.searchText)
  const [searchType, setSearchType] = useState<SearchType>('music')
  const [sourceList, setSourceList] = useState<SearchSources>(searchMusicState.sources)
  const [source, setSource] = useState<SearchSource>('kw')

  useEffect(() => {
    void getSearchSetting().then(info => {
      searchInfo.current.temp_source = info.temp_source
      searchInfo.current.source = info.source
      searchInfo.current.searchType = info.type
      setSearchType(info.type)
      setSourceList(info.type === 'songlist' ? searchSonglistState.sources : searchMusicState.sources)
      setSource(info.source)
      setActiveQuery(searchState.searchText)
      headerBarRef.current?.setText(searchState.searchText)
      listRef.current?.loadList(searchState.searchText, info.source, info.type)
    })

    const handleTypeChange = (type: SearchType) => {
      searchInfo.current.searchType = type
      setSearchType(type)
      const nextSources: SearchSources = type === 'songlist' ? searchSonglistState.sources : searchMusicState.sources
      setSourceList(nextSources)
      if (!(nextSources as ReadonlyArray<SearchSource>).includes(searchInfo.current.source)) {
        const fallback = nextSources[0] ?? 'kw'
        searchInfo.current.source = fallback
        setSource(fallback)
        void saveSearchSetting({ type, source: fallback })
        listRef.current?.loadList(searchState.searchText, fallback, type)
      } else {
        void saveSearchSetting({ type })
        listRef.current?.loadList(searchState.searchText, searchInfo.current.source, type)
      }
    }
    global.app_event.on('searchTypeChanged', handleTypeChange)

    return () => {
      global.app_event.off('searchTypeChanged', handleTypeChange)
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
        timeoutRef.current = null
      }
    }
  }, [])

  const handleLayout = (e: LayoutChangeEvent) => {
    layoutHeightRef.current = e.nativeEvent.layout.height
  }

  const handleSourceChange = useCallback((nextSource: SearchSource) => {
    searchInfo.current.source = nextSource
    setSource(nextSource)
    void saveSearchSetting({ source: nextSource })
    listRef.current?.loadList(searchState.searchText, nextSource, searchInfo.current.searchType)
  }, [])

  const handleTipSearch: HeaderBarProps['onTipSearch'] = useCallback((text) => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current)
    timeoutRef.current = setTimeout(() => {
      searchTipListRef.current?.search(text, layoutHeightRef.current)
    }, 500)
  }, [])

  const handleHideTipList = useCallback(() => {
    setIsFocused(false)
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
      timeoutRef.current = null
    }
    searchTipListRef.current?.hide()
  }, [])

  const handleSearch: HeaderBarProps['onSearch'] = useCallback((text) => {
    const keyword = text.trim()
    handleHideTipList()
    setSearchText(keyword)
    setTipList([])
    headerBarRef.current?.setText(keyword)
    headerBarRef.current?.blur()
    setIsFocused(false)
    setActiveQuery(keyword)
    if (keyword) void addHistoryWord(keyword)
    listRef.current?.loadList(keyword, searchInfo.current.source, searchInfo.current.searchType)
  }, [handleHideTipList])

  const handleClear = useCallback(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
      timeoutRef.current = null
    }
    searchTipListRef.current?.hide()
    setSearchText('')
    setTipList([])
    setActiveQuery('')
    setIsFocused(true)
    listRef.current?.loadList('', searchInfo.current.source, searchInfo.current.searchType)
    headerBarRef.current?.focus()
  }, [])

  const handleCancel = useCallback(() => {
    handleHideTipList()
    setSearchText('')
    setTipList([])
    headerBarRef.current?.setText('')
    headerBarRef.current?.blur()
    setIsFocused(false)
    setActiveQuery('')
    listRef.current?.loadList('', searchInfo.current.source, searchInfo.current.searchType)
  }, [handleHideTipList])

  const handleFocus = useCallback(() => {
    setIsFocused(true)
  }, [])

  const handleShowTipList: HeaderBarProps['onShowTipList'] = useCallback(() => {
    setIsFocused(true)
    if (timeoutRef.current) clearTimeout(timeoutRef.current)
    timeoutRef.current = setTimeout(() => {
      searchTipListRef.current?.show(layoutHeightRef.current)
    }, 500)
  }, [])

  const isHeaderActive = isFocused || Boolean(activeQuery)

  return (
    <View style={styles.container}>
      <HeaderBar
        ref={headerBarRef}
        isActive={isHeaderActive}
        sourceList={sourceList}
        source={source}
        onSourceChange={handleSourceChange}
        onTipSearch={handleTipSearch}
        onSearch={handleSearch}
        onClear={handleClear}
        onCancel={handleCancel}
        onFocus={handleFocus}
        onHideTipList={handleHideTipList}
        onShowTipList={handleShowTipList}
      />
      {activeQuery ? (
        <SearchTypeSelector
          type={searchType}
          sourceList={sourceList}
          source={source}
          onSourceChange={handleSourceChange}
        />
      ) : null}
      <View style={styles.content} onLayout={handleLayout}>
        <TipList ref={searchTipListRef} onSearch={handleSearch} />
        <List ref={listRef} onSearch={handleSearch} />
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    width: '100%',
    flex: 1,
  },
  content: {
    flex: 1,
  },
})
