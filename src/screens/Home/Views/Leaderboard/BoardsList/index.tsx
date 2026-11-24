import { forwardRef, useImperativeHandle, useRef } from 'react'
import { StyleSheet, View } from 'react-native'
import List, { type ListType, type ListProps } from './List'
import ListMenu, { type ListMenuType, type Position } from './ListMenu'
import { type BoardItem } from '@/store/leaderboard/state'

export interface BoardsListProps {
  list?: BoardItem[]
  activeId?: string
  source?: LX.OnlineSource
  onBoundChange: (listId: string) => void
  onPlay: (listId: string) => void
  onCollect: (listId: string, name: string) => void
}
export interface BoardsListType {
  setList: (list: BoardItem[], activeId: string) => void
}

export default forwardRef<BoardsListType, BoardsListProps>(({
  list,
  activeId,
  source,
  onBoundChange,
  onPlay,
  onCollect,
}, ref) => {
  const listRef = useRef<ListType>(null)
  const listMenuRef = useRef<ListMenuType>(null)

  useImperativeHandle(ref, () => ({
    setList(nextList, listId) {
      listRef.current?.setList(nextList, listId)
    },
  }), [])

  const handleShowMenu: ListProps['onShowMenu'] = ({ listId, name, index }, position: Position) => {
    listMenuRef.current?.show({
      listId,
      index,
      name,
    }, position)
  }

  return (
    <View style={styles.container}>
      <List
        ref={listRef}
        list={list}
        activeId={activeId}
        source={source}
        onBoundChange={onBoundChange}
        onShowMenu={handleShowMenu}
      />
      <ListMenu
        ref={listMenuRef}
        onHideMenu={() => listRef.current?.hideMenu()}
        onPlay={({ listId }) => { onPlay(listId) }}
        onCollect={({ listId, name }) => { onCollect(listId, name) }}
      />
    </View>
  )
})

const styles = StyleSheet.create({
  container: {
    flexShrink: 0,
  },
})
