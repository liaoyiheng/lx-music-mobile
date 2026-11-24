import { useRef, forwardRef, useImperativeHandle, type ReactNode } from 'react'
import { type ListInfoItem } from '@/store/songlist/state'
import List, { type ListProps, type ListType, type Status } from './List'
import { navigations } from '@/navigation'
import commonState from '@/store/common/state'

export interface SonglistProps {
  onRefresh: ListProps['onRefresh']
  onLoadMore: ListProps['onLoadMore']
  enableEditorial?: boolean
  filterComponent?: ReactNode
  listHeaderTitle?: string
}
export interface SonglistType {
  setList: (list: ListInfoItem[], showSource?: boolean) => void
  setStatus: (val: Status) => void
}

export default forwardRef<SonglistType, SonglistProps>(({
  onRefresh,
  onLoadMore,
  enableEditorial,
  filterComponent,
  listHeaderTitle,
}, ref) => {
  const listRef = useRef<ListType>(null)

  useImperativeHandle(ref, () => ({
    setList(list, showSource) {
      listRef.current?.setList(list, showSource)
    },
    setStatus(val) {
      listRef.current?.setStatus(val)
    },
  }))

  const handleOpenDetail = (item: ListInfoItem, index: number) => {
    navigations.pushSonglistDetailScreen(commonState.componentIds.home!, item)
  }

  return (
    <List
      ref={listRef}
      onRefresh={onRefresh}
      onLoadMore={onLoadMore}
      onOpenDetail={handleOpenDetail}
      enableEditorial={enableEditorial}
      filterComponent={filterComponent}
      listHeaderTitle={listHeaderTitle}
    />
  )
})
