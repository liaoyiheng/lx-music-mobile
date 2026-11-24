import { memo, useMemo, useEffect, useRef, useState, useCallback } from 'react'
import { View } from 'react-native'
import PagerView, { type PagerViewOnPageSelectedEvent } from 'react-native-pager-view'
import Header from './components/Header'
import StateView from './components/StateView'
import CommentHot from './CommentHot'
import CommentNew from './CommentNew'
import { createStyle, toast } from '@/utils/tools'
import { useI18n } from '@/lang'
import { COMPONENT_IDS } from '@/config/constant'
import { setComponentId } from '@/core/common'
import PageContent from '@/components/PageContent'
import playerState from '@/store/player/state'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import { useWindowSize } from '@/utils/hooks'
import { useApple, pagePadding, size, spacing } from '@/theme/apple'

type ActiveId = 'hot' | 'new'

const HotCommentPage = memo(({ activeId, musicInfo, onUpdateTotal }: {
  activeId: ActiveId
  musicInfo: LX.Music.MusicInfoOnline
  onUpdateTotal: (total: number) => void
}) => {
  const initedRef = useRef(false)
  const comment = useMemo(() => <CommentHot musicInfo={musicInfo} onUpdateTotal={onUpdateTotal} />, [musicInfo, onUpdateTotal])
  switch (activeId) {
    case 'hot':
      if (!initedRef.current) initedRef.current = true
      return comment
    default:
      return initedRef.current ? comment : null
  }
})

const NewCommentPage = memo(({ activeId, musicInfo, onUpdateTotal }: {
  activeId: ActiveId
  musicInfo: LX.Music.MusicInfoOnline
  onUpdateTotal: (total: number) => void
}) => {
  const initedRef = useRef(false)
  const comment = useMemo(() => <CommentNew musicInfo={musicInfo} onUpdateTotal={onUpdateTotal} />, [musicInfo, onUpdateTotal])
  switch (activeId) {
    case 'new':
      if (!initedRef.current) initedRef.current = true
      return comment
    default:
      return initedRef.current ? comment : null
  }
})

const TABS = [
  'hot',
  'new',
] as const

const getMusicInfo = (musicInfo: LX.Player.PlayMusic | null) => {
  if (!musicInfo) return null
  return 'progress' in musicInfo ? musicInfo.metadata.musicInfo : musicInfo
}

export default memo(({ componentId }: {
  componentId: string
}) => {
  const pagerViewRef = useRef<PagerView>(null)
  const [activeId, setActiveId] = useState<ActiveId>('hot')
  const [musicInfo, setMusicInfo] = useState<LX.Music.MusicInfo | null>(getMusicInfo(playerState.playMusicInfo.musicInfo))
  const t = useI18n()
  const apple = useApple()
  const windowSize = useWindowSize()
  const isWide = windowSize.width >= size.wideBreakpoint
  const [total, setTotal] = useState({ hot: 0, new: 0 })

  useEffect(() => {
    setComponentId(COMPONENT_IDS.comment, componentId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const tabs = useMemo((): Array<SegmentItem<ActiveId>> => {
    return [
      { id: TABS[0], label: t('comment_tab_hot', { total: total.hot ? `(${total.hot})` : '' }).trim() },
      { id: TABS[1], label: t('comment_tab_new', { total: total.new ? `(${total.new})` : '' }).trim() },
    ]
  }, [total, t])

  const toggleTab = useCallback((id: ActiveId) => {
    setActiveId(id)
    pagerViewRef.current?.setPage(TABS.findIndex(tab => tab == id))
  }, [])

  const onPageSelected = useCallback(({ nativeEvent }: PagerViewOnPageSelectedEvent) => {
    setActiveId(TABS[nativeEvent.position])
  }, [])

  const refreshComment = useCallback(() => {
    if (!playerState.playMusicInfo.musicInfo) return
    let playerMusicInfo = playerState.playMusicInfo.musicInfo
    if ('progress' in playerMusicInfo) playerMusicInfo = playerMusicInfo.metadata.musicInfo

    if (musicInfo && musicInfo.id == playerMusicInfo.id) {
      toast(t('comment_refresh', { name: musicInfo.name }))
      return
    }
    setMusicInfo(playerMusicInfo)
  }, [musicInfo, t])

  const setHotTotal = useCallback((total: number) => {
    setTotal(totalInfo => ({ ...totalInfo, hot: total }))
  }, [])
  const setNewTotal = useCallback((total: number) => {
    setTotal(totalInfo => ({ ...totalInfo, new: total }))
  }, [])

  const commentComponent = useMemo(() => {
    return (
      <View style={styles.container}>
        <View style={styles.segment}>
          <SegmentedControl items={tabs} value={activeId} onChange={toggleTab} />
        </View>
        <PagerView
          ref={pagerViewRef}
          onPageSelected={onPageSelected}
          style={styles.pagerView}
        >
          <View collapsable={false} style={styles.pageStyle}>
            <HotCommentPage activeId={activeId} musicInfo={musicInfo as LX.Music.MusicInfoOnline} onUpdateTotal={setHotTotal} />
          </View>
          <View collapsable={false} style={styles.pageStyle}>
            <NewCommentPage activeId={activeId} musicInfo={musicInfo as LX.Music.MusicInfoOnline} onUpdateTotal={setNewTotal} />
          </View>
        </PagerView>
      </View>
    )
  }, [activeId, musicInfo, onPageSelected, setHotTotal, setNewTotal, tabs, toggleTab])

  const isLocal = musicInfo?.source == 'local'

  return (
    <PageContent>
      {
        musicInfo == null
          ? null
          : (
              <View style={{ ...styles.container, backgroundColor: apple.background }}>
                <Header
                  musicInfo={musicInfo}
                  totalCount={activeId == 'hot' ? total.hot : total.new}
                  onRefresh={isLocal ? undefined : refreshComment}
                />
                <View style={isWide ? styles.wideContent : styles.container}>
                  {
                    isLocal
                      ? <StateView icon="comment" title={t('comment_not support')} />
                      : commentComponent
                  }
                </View>
              </View>
            )
      }
    </PageContent>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
  },
  wideContent: {
    flex: 1,
    width: '100%',
    maxWidth: size.wideBreakpoint,
    alignSelf: 'center',
  },
  segment: {
    paddingHorizontal: pagePadding,
    paddingTop: spacing.sm,
    paddingBottom: spacing.sm,
  },
  pagerView: {
    flex: 1,
  },
  pageStyle: {
    overflow: 'hidden',
  },
})
