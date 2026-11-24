import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react'
import { View } from 'react-native'
import OnlineList, { type OnlineListType, type OnlineListProps } from '@/components/OnlineList'
import { clearListDetail, getListDetail, setListDetail, setListDetailInfo } from '@/core/leaderboard'
import boardState from '@/store/leaderboard/state'
import { useHorizontalMode } from '@/utils/hooks'
import { size } from '@/theme/apple'
import { handlePlay } from './listAction'

export interface MusicListProps {
  ListHeaderComponent?: OnlineListProps['ListHeaderComponent']
  onListLoaded?: (total: number) => void
}

export interface MusicListType {
  loadList: (source: LX.OnlineSource, listId: string) => void
}

export default forwardRef<MusicListType, MusicListProps>(({ ListHeaderComponent, onListLoaded }, ref) => {
  const listRef = useRef<OnlineListType>(null)
  const isUnmountedRef = useRef(false)
  const isWide = useHorizontalMode()

  useImperativeHandle(ref, () => ({
    async loadList(source, id) {
      const listDetailInfo = boardState.listDetailInfo
      listRef.current?.setList([])
      onListLoaded?.(0)
      if (listDetailInfo.id == id && listDetailInfo.source == source && listDetailInfo.list.length) {
        requestAnimationFrame(() => {
          listRef.current?.setList(listDetailInfo.list)
          onListLoaded?.(listDetailInfo.total || listDetailInfo.list.length)
        })
      } else {
        listRef.current?.setStatus('loading')
        const page = 1
        setListDetailInfo(id)
        return getListDetail(id, page).then((listDetail) => {
          const result = setListDetail(listDetail, id, page)
          if (isUnmountedRef.current) return
          requestAnimationFrame(() => {
            listRef.current?.setList(result.list)
            listRef.current?.setStatus(boardState.listDetailInfo.maxPage <= page ? 'end' : 'idle')
            onListLoaded?.(boardState.listDetailInfo.total || result.list.length)
          })
        }).catch(() => {
          if (boardState.listDetailInfo.list.length && page == 1) clearListDetail()
          listRef.current?.setStatus('error')
          onListLoaded?.(0)
        })
      }
    },
  }), [onListLoaded])

  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  const handlePlayList: OnlineListProps['onPlayList'] = (index) => {
    const listDetailInfo = boardState.listDetailInfo
    void handlePlay(listDetailInfo.id, listDetailInfo.list, index)
  }
  const handleRefresh: OnlineListProps['onRefresh'] = () => {
    const page = 1
    listRef.current?.setStatus('refreshing')
    getListDetail(boardState.listDetailInfo.id, page, true).then((listDetail) => {
      const result = setListDetail(listDetail, boardState.listDetailInfo.id, page)
      if (isUnmountedRef.current) return
      listRef.current?.setList(result.list)
      listRef.current?.setStatus(boardState.listDetailInfo.maxPage <= page ? 'end' : 'idle')
      onListLoaded?.(boardState.listDetailInfo.total || result.list.length)
    }).catch(() => {
      if (boardState.listDetailInfo.list.length && page == 1) clearListDetail()
      listRef.current?.setStatus('error')
    })
  }
  const handleLoadMore: OnlineListProps['onLoadMore'] = () => {
    listRef.current?.setStatus('loading')
    const page = boardState.listDetailInfo.list.length ? boardState.listDetailInfo.page + 1 : 1
    getListDetail(boardState.listDetailInfo.id, page).then((listDetail) => {
      const result = setListDetail(listDetail, boardState.listDetailInfo.id, page)
      if (isUnmountedRef.current) return
      listRef.current?.setList(result.list, true)
      listRef.current?.setStatus(boardState.listDetailInfo.maxPage <= page ? 'end' : 'idle')
      onListLoaded?.(boardState.listDetailInfo.total || boardState.listDetailInfo.list.length)
    }).catch(() => {
      if (boardState.listDetailInfo.list.length && page == 1) clearListDetail()
      listRef.current?.setStatus('error')
    })
  }

  return (
    <View style={{ flex: 1, paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance }}>
      <OnlineList
        ref={listRef}
        onPlayList={handlePlayList}
        onRefresh={handleRefresh}
        onLoadMore={handleLoadMore}
        ListHeaderComponent={ListHeaderComponent}
        checkHomePagerIdle
        rowType="medium"
      />
    </View>
  )
})
