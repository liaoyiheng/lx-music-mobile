import { forwardRef, useCallback, useEffect, useImperativeHandle, useMemo, useRef, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import OnlineList, { type OnlineListType, type OnlineListProps } from '@/components/OnlineList'
import ListMenu, { type ListMenuType } from '@/components/OnlineList/ListMenu'
import ListMusicAdd, { type MusicAddModalType as ListMusicAddType } from '@/components/MusicAddModal'
import {
  clearMusicUrl,
  handleDislikeMusic,
  handlePlay,
  handlePlayLater,
  handleShare,
  handleShowMusicSourceDetail,
} from '@/components/OnlineList/listAction'
import { CoverArt, GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { search } from '@/core/search/music'
import searchMusicState, { type Source } from '@/store/search/music/state'
import { useHorizontalMode } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, pagePadding, radius, size, spacing } from '@/theme/apple'

export interface MusicListType {
  loadList: (text: string, source: Source) => void
}

/**
 * 歌曲搜索结果列表：
 * 顶部「最佳匹配」液态玻璃大卡（60×60 CoverArt + 红色眉标 + 歌名/歌手 + 播放/菜单操作）
 * 下方复用 OnlineList 展示完整歌曲结果与分页加载
 */
export default forwardRef<MusicListType, {}>((props, ref) => {
  const apple = useApple()
  const t = useI18n()
  const isWide = useHorizontalMode()
  const listRef = useRef<OnlineListType>(null)
  const listMenuRef = useRef<ListMenuType>(null)
  const listMusicAddRef = useRef<ListMusicAddType>(null)
  const moreBtnRef = useRef<TouchableOpacity>(null)
  const searchInfoRef = useRef<{ text: string, source: Source }>({ text: '', source: 'kw' })
  const isUnmountedRef = useRef(false)
  const [bestMatch, setBestMatch] = useState<LX.Music.MusicInfoOnline | null>(null)
  const [showSourceTag, setShowSourceTag] = useState(false)

  useImperativeHandle(ref, () => ({
    async loadList(text, source) {
      listRef.current?.setList([], false, source == 'all')
      setBestMatch(null)
      setShowSourceTag(source == 'all')
      searchInfoRef.current.text = text
      searchInfoRef.current.source = source

      const cached = searchMusicState.listInfos[source]
      if (searchMusicState.searchText == text && searchMusicState.source == source && cached?.list.length) {
        requestAnimationFrame(() => {
          listRef.current?.setList(cached.list, false, source == 'all')
          setBestMatch(cached.list[0] ?? null)
        })
      } else {
        listRef.current?.setStatus('loading')
        const page = 1
        return search(text, page, source).then((list) => {
          if (isUnmountedRef.current) return
          requestAnimationFrame(() => {
            listRef.current?.setList(list, false, source == 'all')
            setBestMatch(list[0] ?? null)
            listRef.current?.setStatus(searchMusicState.listInfos[searchMusicState.source]!.maxPage <= page ? 'end' : 'idle')
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

  const handleRefresh: OnlineListProps['onRefresh'] = () => {
    const page = 1
    const activeSource = searchInfoRef.current.source
    listRef.current?.setStatus('refreshing')
    search(searchInfoRef.current.text, page, activeSource).then((list) => {
      if (isUnmountedRef.current) return
      listRef.current?.setList(list, false, activeSource == 'all')
      setBestMatch(list[0] ?? null)
      setShowSourceTag(activeSource == 'all')
      listRef.current?.setStatus(searchMusicState.listInfos[searchMusicState.source]!.maxPage <= page ? 'end' : 'idle')
    }).catch(() => {
      listRef.current?.setStatus('error')
    })
  }

  const handleLoadMore: OnlineListProps['onLoadMore'] = () => {
    listRef.current?.setStatus('loading')
    const activeSource = searchInfoRef.current.source
    const info = searchMusicState.listInfos[activeSource]!
    const page = info?.list.length ? info.page + 1 : 1
    search(searchInfoRef.current.text, page, activeSource).then((list) => {
      if (isUnmountedRef.current) return
      listRef.current?.setList(list, true, activeSource == 'all')
      setBestMatch(prev => prev ?? list[0] ?? null)
      listRef.current?.setStatus(info.maxPage <= page ? 'end' : 'idle')
    }).catch(() => {
      listRef.current?.setStatus('error')
    })
  }

  const handleOpenBestMenu = useCallback(() => {
    if (!bestMatch || !moreBtnRef.current?.measure) return
    moreBtnRef.current.measure((fx, fy, width, height, px, py) => {
      listMenuRef.current?.show({
        musicInfo: bestMatch,
        index: 0,
        single: true,
        selectedList: [],
      }, {
        x: Math.ceil(px),
        y: Math.ceil(py),
        w: Math.ceil(width),
        h: Math.ceil(height),
      })
    })
  }, [bestMatch])

  const listHeaderComponent = useMemo(() => {
    if (!bestMatch) return null
    const subParts: string[] = []
    if (bestMatch.singer) subParts.push(bestMatch.singer)
    if (bestMatch.meta.albumName) subParts.push(bestMatch.meta.albumName)
    if (showSourceTag) subParts.push(bestMatch.source.toUpperCase())
    const subtitle = subParts.join(' · ')

    return (
      <View style={styles.headerWrap}>
        <TouchableOpacity
          activeOpacity={0.82}
          onPress={() => { handlePlay(bestMatch) }}
        >
          <GlassSurface borderRadius={radius.alert} innerStyle={styles.bestMatchInner}>
            <CoverArt
              uri={bestMatch.meta.picUrl}
              size={60}
              borderRadius={radius.medium}
            />
            <View style={styles.bestMatchInfo}>
              <Text size={fontSize.caption2} color={apple.accent} style={styles.bestMatchEyebrow} numberOfLines={1}>
                {t('apple_search_best_match')}
              </Text>
              <Text size={fontSize.headline} color={apple.label} style={styles.bestMatchTitle} numberOfLines={1}>
                {bestMatch.name}
              </Text>
              <Text size={13.5} color={apple.secondaryLabel} style={styles.bestMatchSub} numberOfLines={1}>
                {subtitle}
              </Text>
            </View>
            <View style={styles.bestMatchActions}>
              <TouchableOpacity
                activeOpacity={0.7}
                onPress={() => { handlePlay(bestMatch) }}
                style={[styles.playBtn, { backgroundColor: apple.accentFill }]}
                hitSlop={{ top: 6, bottom: 6, left: 6, right: 4 }}
              >
                <IonIcon name="play" rawSize={15} color={apple.accent} style={styles.playIcon} />
              </TouchableOpacity>
              <TouchableOpacity
                ref={moreBtnRef}
                activeOpacity={0.6}
                onPress={handleOpenBestMenu}
                style={styles.moreBtn}
                hitSlop={{ top: 6, bottom: 6, left: 4, right: 6 }}
              >
                <IonIcon name="ellipsis-horizontal" rawSize={18} color={apple.secondaryLabel} />
              </TouchableOpacity>
            </View>
          </GlassSurface>
        </TouchableOpacity>

        <View style={styles.sectionHead}>
          <Text size={fontSize.title3} color={apple.label} style={styles.sectionTitle}>
            {t('apple_search_songs')}
          </Text>
        </View>
      </View>
    )
  }, [apple, bestMatch, handleOpenBestMenu, showSourceTag, t])

  return (
    <View style={{ flex: 1, paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance }}>
      <OnlineList
        ref={listRef}
        onRefresh={handleRefresh}
        onLoadMore={handleLoadMore}
        ListHeaderComponent={listHeaderComponent}
        checkHomePagerIdle
      />
      <ListMusicAdd ref={listMusicAddRef} onAdded={() => {}} />
      <ListMenu
        ref={listMenuRef}
        onPlay={info => { handlePlay(info.musicInfo) }}
        onPlayLater={info => { handlePlayLater(info.musicInfo, [], () => {}) }}
        onCopyName={info => { handleShare(info.musicInfo) }}
        onAdd={info => { listMusicAddRef.current?.show({ musicInfo: info.musicInfo, listId: '', isMove: false }) }}
        onMusicSourceDetail={info => { void handleShowMusicSourceDetail(info.musicInfo) }}
        onRemoveCache={info => { void clearMusicUrl(info.musicInfo) }}
        onDislikeMusic={info => { void handleDislikeMusic(info.musicInfo) }}
      />
    </View>
  )
})

const styles = createStyle({
  headerWrap: {
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xs,
  },
  bestMatchInner: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: spacing.md,
    gap: spacing.md,
  },
  bestMatchInfo: {
    flex: 1,
    minWidth: 0,
  },
  bestMatchEyebrow: {
    fontWeight: fontWeight.bold,
    letterSpacing: 0.3,
    marginBottom: 2,
  },
  bestMatchTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.25,
  },
  bestMatchSub: {
    marginTop: 2,
  },
  bestMatchActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    flexShrink: 0,
  },
  playBtn: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
  },
  playIcon: {
    marginLeft: 1.5,
  },
  moreBtn: {
    width: 30,
    height: 34,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sectionHead: {
    paddingTop: spacing.lg,
    paddingBottom: spacing.xs,
  },
  sectionTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.3,
  },
})
