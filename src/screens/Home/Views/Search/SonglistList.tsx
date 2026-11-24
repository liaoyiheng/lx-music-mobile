import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react'

import { search } from '@/core/search/songlist'
import Songlist, { type SonglistProps, type SonglistType } from '@/screens/Home/Views/SongList/components/Songlist'
import searchSonglistState, { type Source } from '@/store/search/songlist/state'
import { useI18n } from '@/lang'

export interface MusicListType {
  loadList: (text: string, source: Source) => void
}

/**
 * 歌单搜索结果列表：复用 SongList 封面墙（CoverArt 圆角封面、跳转详情、分页加载及底部 Dock 避让）
 */
export default forwardRef<MusicListType, {}>((props, ref) => {
  const t = useI18n()
  const listRef = useRef<SonglistType>(null)
  const searchInfoRef = useRef<{ text: string, source: Source }>({ text: '', source: 'kw' })
  const isUnmountedRef = useRef(false)

  useImperativeHandle(ref, () => ({
    async loadList(text, source) {
      listRef.current?.setList([], source == 'all')
      searchInfoRef.current.text = text
      searchInfoRef.current.source = source

      const cached = searchSonglistState.listInfos[source]
      if (searchSonglistState.searchText == text && searchSonglistState.source == source && cached?.list.length) {
        requestAnimationFrame(() => {
          listRef.current?.setList(cached.list, source == 'all')
        })
      } else {
        listRef.current?.setStatus('loading')
        const page = 1
        return search(text, page, source).then((list) => {
          if (isUnmountedRef.current) return
          requestAnimationFrame(() => {
            listRef.current?.setList(list, source == 'all')
            listRef.current?.setStatus(searchSonglistState.maxPages[searchSonglistState.source] == page ? 'end' : 'idle')
          })
        }).catch(() => {
          listRef.current?.setStatus('error')
        })
      }
    },
  }), [])

  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  const handleRefresh: SonglistProps['onRefresh'] = () => {
    const page = 1
    const activeSource = searchInfoRef.current.source
    listRef.current?.setStatus('refreshing')
    search(searchInfoRef.current.text, page, activeSource).then((list) => {
      if (isUnmountedRef.current) return
      listRef.current?.setList(list, activeSource == 'all')
      listRef.current?.setStatus(searchSonglistState.maxPages[searchSonglistState.source] == page ? 'end' : 'idle')
    }).catch(() => {
      listRef.current?.setStatus('error')
    })
  }

  const handleLoadMore: SonglistProps['onLoadMore'] = () => {
    listRef.current?.setStatus('loading')
    const activeSource = searchInfoRef.current.source
    const info = searchSonglistState.listInfos[activeSource]!
    const page = info.list.length ? info.page + 1 : 1
    search(searchInfoRef.current.text, page, activeSource).then((list) => {
      if (isUnmountedRef.current) return
      listRef.current?.setList(list, activeSource == 'all')
      listRef.current?.setStatus(searchSonglistState.maxPages[searchSonglistState.source] == page ? 'end' : 'idle')
    }).catch(() => {
      listRef.current?.setStatus('error')
    })
  }

  return (
    <Songlist
      ref={listRef}
      listHeaderTitle={t('apple_search_playlists')}
      onRefresh={handleRefresh}
      onLoadMore={handleLoadMore}
    />
  )
})
