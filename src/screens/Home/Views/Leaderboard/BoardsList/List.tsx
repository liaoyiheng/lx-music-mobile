import { forwardRef, useImperativeHandle, useRef, useState } from 'react'
import { ScrollView, View, type LayoutChangeEvent } from 'react-native'

import { createStyle } from '@/utils/tools'
import { type Position } from './ListMenu'
import ListItem, { type ListItemProps } from './ListItem'
import { type BoardItem } from '@/store/leaderboard/state'

export interface ListProps {
  onBoundChange: (listId: string) => void
  onShowMenu: (info: { listId: string, name: string, index: number }, position: Position) => void
}
export interface ListType {
  setList: (list: BoardItem[], activeId: string) => void
  hideMenu: () => void
}

export default forwardRef<ListType, ListProps>(({ onBoundChange, onShowMenu }, ref) => {
  const [activeId, setActiveId] = useState('')
  const [longPressIndex, setLongPressIndex] = useState(-1)
  const [list, setList] = useState<BoardItem[]>([])
  const scrollViewRef = useRef<ScrollView>(null)
  const needScrollRef = useRef(false)

  useImperativeHandle(ref, () => ({
    setList(list, activeId) {
      needScrollRef.current = true
      scrollViewRef.current?.scrollTo({ x: 0, animated: false })
      setList(list)
      setActiveId(activeId)
    },
    hideMenu() {
      setLongPressIndex(-1)
    },
  }), [])

  const handleBoundChange = (item: BoardItem) => {
    setActiveId(item.id)
    onBoundChange(item.id)
  }

  const handleShowMenu: ListItemProps['onShowMenu'] = (listId, name, index, position: Position) => {
    setLongPressIndex(index)
    onShowMenu({ listId, name, index }, position)
  }

  /** 首次显示时把当前榜单滚到可见位置 */
  const handleActiveLayout = (e: LayoutChangeEvent) => {
    if (!needScrollRef.current) return
    needScrollRef.current = false
    const x = Math.max(e.nativeEvent.layout.x - 16, 0)
    if (x > 0) {
      scrollViewRef.current?.scrollTo({ x, animated: false })
    }
  }

  return (
    <ScrollView
      ref={scrollViewRef}
      horizontal
      showsHorizontalScrollIndicator={false}
      keyboardShouldPersistTaps={'always'}
      contentContainerStyle={styles.content}
    >
      {
        list.map((item, index) => (
          <View key={item.id} onLayout={item.id == activeId ? handleActiveLayout : undefined}>
            <ListItem
              item={item}
              index={index}
              longPressIndex={longPressIndex}
              activeId={activeId}
              onShowMenu={handleShowMenu}
              onBoundChange={handleBoundChange}
            />
          </View>
        ))
      }
    </ScrollView>
  )
})

const styles = createStyle({
  content: {
    paddingLeft: 16,
    paddingRight: 8,
    alignItems: 'center',
  },
})
