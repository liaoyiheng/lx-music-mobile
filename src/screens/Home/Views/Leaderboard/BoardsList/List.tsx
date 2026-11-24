import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { FlatList, type FlatListProps } from 'react-native'

import { createStyle } from '@/utils/tools'
import { type Position } from './ListMenu'
import ListItem, { BOARD_ITEM_STEP, type ListItemProps } from './ListItem'
import { type BoardItem } from '@/store/leaderboard/state'
import { pagePadding } from '@/theme/apple'

type FlatListType = FlatListProps<BoardItem>

export interface ListProps {
  list?: BoardItem[]
  activeId?: string
  source?: LX.OnlineSource
  onBoundChange: (listId: string) => void
  onShowMenu: (info: { listId: string, name: string, index: number }, position: Position) => void
}
export interface ListType {
  setList: (list: BoardItem[], activeId: string) => void
  hideMenu: () => void
}

export default forwardRef<ListType, ListProps>(({
  list: propList,
  activeId: propActiveId,
  source,
  onBoundChange,
  onShowMenu,
}, ref) => {
  const [innerActiveId, setInnerActiveId] = useState('')
  const [longPressIndex, setLongPressIndex] = useState(-1)
  const [innerList, setInnerList] = useState<BoardItem[]>([])
  const flatListRef = useRef<FlatList<BoardItem>>(null)
  const lastScrolledKeyRef = useRef('')

  const list = propList ?? innerList
  const activeId = propActiveId ?? innerActiveId

  const scrollToActive = (targetList: BoardItem[], targetId: string) => {
    if (!targetList.length || !targetId) return
    const idx = targetList.findIndex(b => b.id == targetId)
    if (idx <= 0) {
      flatListRef.current?.scrollToOffset({ offset: 0, animated: false })
      return
    }
    flatListRef.current?.scrollToIndex({
      index: idx,
      animated: false,
      viewPosition: 0.15,
    })
  }

  useEffect(() => {
    if (!propList?.length || !propActiveId) return
    const key = `${source ?? ''}:${propList[0]?.id ?? ''}`
    if (lastScrolledKeyRef.current != key) {
      lastScrolledKeyRef.current = key
      requestAnimationFrame(() => {
        scrollToActive(propList, propActiveId)
      })
    }
  }, [propList, propActiveId, source])

  useImperativeHandle(ref, () => ({
    setList(nextList, nextActiveId) {
      setInnerList(nextList)
      setInnerActiveId(nextActiveId)
      requestAnimationFrame(() => {
        scrollToActive(nextList, nextActiveId)
      })
    },
    hideMenu() {
      setLongPressIndex(-1)
    },
  }), [])

  const handleBoundChange = (item: BoardItem) => {
    setInnerActiveId(item.id)
    onBoundChange(item.id)
  }

  const handleShowMenu: ListItemProps['onShowMenu'] = (listId, name, index, position: Position) => {
    setLongPressIndex(index)
    onShowMenu({ listId, name, index }, position)
  }

  const renderItem: FlatListType['renderItem'] = ({ item, index }) => (
    <ListItem
      item={item}
      index={index}
      sourceLabel={source}
      longPressIndex={longPressIndex}
      activeId={activeId}
      onShowMenu={handleShowMenu}
      onBoundChange={handleBoundChange}
    />
  )
  const getKey: FlatListType['keyExtractor'] = item => item.id
  const getItemLayout: FlatListType['getItemLayout'] = (_data, index) => ({
    length: BOARD_ITEM_STEP,
    offset: BOARD_ITEM_STEP * index,
    index,
  })

  return (
    <FlatList
      ref={flatListRef}
      horizontal
      data={list}
      renderItem={renderItem}
      keyExtractor={getKey}
      getItemLayout={getItemLayout}
      showsHorizontalScrollIndicator={false}
      keyboardShouldPersistTaps="always"
      contentContainerStyle={styles.content}
      onScrollToIndexFailed={() => {
        flatListRef.current?.scrollToOffset({ offset: 0, animated: false })
      }}
    />
  )
})

const styles = createStyle({
  content: {
    paddingLeft: pagePadding,
    paddingRight: pagePadding - 12,
    paddingVertical: 4,
    alignItems: 'flex-start',
  },
})
