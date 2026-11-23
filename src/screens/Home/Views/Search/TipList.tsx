import { useRef, useImperativeHandle, forwardRef, useState, useEffect } from 'react'
import { TouchableHighlight, View } from 'react-native'
import SearchTipList, { type SearchTipListProps as _SearchTipListProps, type SearchTipListType as _SearchTipListType } from '@/components/SearchTipList'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { Icon } from '@/components/common/Icon'
import musicSdk from '@/utils/musicSdk'
import searchState, { type InitState as SearchState } from '@/store/search/state'
import { setSearchText, setTipList, setTipListInfo } from '@/core/search/search'
import { debounce } from '@/utils'
import { useApple, fontSize, hairline, pagePadding, size, spacing } from '@/theme/apple'

export const ITEM_HEIGHT = size.settingRow

export const debounceTipSearch = debounce((keyword: string, source: SearchState['temp_source'], callback: (list: string[]) => void) => {
  void musicSdk[source].tipSearch.search(keyword).then(callback)
}, 200)

export type SearchTipListProps = _SearchTipListProps<string>
export type SearchTipListType = _SearchTipListType<string>

interface TipListProps {
  onSearch: (keyword: string) => void
}
export interface TipListType {
  search: (keyword: string, height: number) => void
  show: (height: number) => void
  hide: () => void
}

export default forwardRef<TipListType, TipListProps>(({ onSearch }, ref) => {
  const searchTipListRef = useRef<SearchTipListType>(null)
  const [visible, setVisible] = useState(false)
  const visibleListRef = useRef(false)
  const isUnmountedRef = useRef(false)
  const apple = useApple()

  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  const handleSearch = (keyword: string, height: number) => {
    searchTipListRef.current?.setHeight(height)
    setSearchText(keyword)
    if (keyword) {
      setTipListInfo(keyword, searchState.temp_source)
      debounceTipSearch(keyword, searchState.temp_source, (list) => {
        if (keyword != searchState.tipListInfo.text) return
        setTipList(list)
        if (!visibleListRef.current || isUnmountedRef.current) return
        searchTipListRef.current?.setList(list)
      })
    } else {
      setTipListInfo(keyword, searchState.temp_source)
      setTipList([])
      searchTipListRef.current?.setList([])
    }
  }

  const handleShowList = (height: number) => {
    searchTipListRef.current?.setHeight(height)
    if (searchState.tipListInfo.list.length) {
      visibleListRef.current = true
      searchTipListRef.current?.setList([...searchState.tipListInfo.list])
    }
  }

  useImperativeHandle(ref, () => ({
    search(keyword, height) {
      if (visible) handleSearch(keyword, height)
      else {
        setVisible(true)
        requestAnimationFrame(() => {
          handleSearch(keyword, height)
        })
      }
    },
    show(height) {
      visibleListRef.current = true
      if (visible) handleShowList(height)
      else {
        setVisible(true)
        requestAnimationFrame(() => {
          handleShowList(height)
        })
      }
    },
    hide() {
      requestAnimationFrame(() => {
        visibleListRef.current = false
        searchTipListRef.current?.setList([])
      })
    },
  }), [visible])

  const renderItem: SearchTipListProps['renderItem'] = ({ item, index }) => {
    return (
      <TouchableHighlight underlayColor={apple.highlight} onPress={() => { onSearch(item) }} key={index}>
        <View style={styles.item}>
          <Icon name="search-2" size={14} color={apple.secondaryLabel} />
          <Text style={styles.text} size={fontSize.body} color={apple.label} numberOfLines={1}>{item}</Text>
          <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
        </View>
      </TouchableHighlight>
    )
  }
  const getkey: SearchTipListProps['keyExtractor'] = (item, index) => String(index)
  const getItemLayout: SearchTipListProps['getItemLayout'] = (data, index) => {
    return { length: ITEM_HEIGHT, offset: ITEM_HEIGHT * index, index }
  }

  return (
    visible
      ? <SearchTipList
          ref={searchTipListRef}
          renderItem={renderItem}
          onPressBg={() => searchTipListRef.current?.setList([])}
          keyExtractor={getkey}
          getItemLayout={getItemLayout}
        />
      : null
  )
})

const styles = createStyle({
  item: {
    height: ITEM_HEIGHT,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
  },
  text: {
    flex: 1,
    marginLeft: spacing.md,
  },
  separator: {
    position: 'absolute',
    left: pagePadding + 26,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
