import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { View } from 'react-native'
import { createStyle } from '@/utils/tools'

import LargeTitle from '@/components/apple/LargeTitle'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import { GlassCapsuleButton, GlassCircleButton } from '@/components/apple/LiquidGlass'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import MusicList, { type MusicListType } from './MusicList'
import BoardsList, { type BoardsListProps } from './BoardsList'
import SourceSelector, { type SourceSelectorType, type SourceSelectorProps } from './SourceSelector'
import { getLeaderboardSetting, saveLeaderboardSetting } from '@/utils/data'
import { getBoardsList } from '@/core/leaderboard'
import { setNavActiveId } from '@/core/common'
import { useI18n } from '@/lang'
import { useHorizontalMode } from '@/utils/hooks'
import { handleCollect, handlePlay } from './listAction'
import boardState, { type BoardItem } from '@/store/leaderboard/state'
import { useApple, fontSize, fontWeight, pagePadding, spacing } from '@/theme/apple'

type BrowseTabId = 'nav_songlist' | 'nav_top'

/**
 * 排行榜（Liquid Glass）
 * 顶部：LargeTitle（「浏览」/ 宽屏「排行榜」）+ 液态玻璃音源胶囊 + 窄屏分段控件
 * 列表头部：横向视觉榜单封面卡片行 + 当前榜单标题/数量/操作栏
 * 列表主体：复用 OnlineList（加载更多、多选、点击播放、长按菜单）
 */
export default () => {
  const apple = useApple()
  const t = useI18n()
  const isWide = useHorizontalMode()
  const musicListRef = useRef<MusicListType>(null)
  const sourceSelectorRef = useRef<SourceSelectorType>(null)
  const boundInfo = useRef<{ source: LX.OnlineSource, id: string | null }>({ source: 'kw', id: null })

  const [boards, setBoards] = useState<BoardItem[]>([])
  const [activeBoardId, setActiveBoardId] = useState('')
  const [activeSource, setActiveSource] = useState<LX.OnlineSource>('kw')
  const [songCount, setSongCount] = useState(0)

  const handleBoundChange = (source: LX.OnlineSource, id: string) => {
    setActiveBoardId(id)
    musicListRef.current?.loadList(source, id)
    void saveLeaderboardSetting({
      source,
      boardId: id,
    })
  }

  const onBoundChange: BoardsListProps['onBoundChange'] = useCallback((id) => {
    boundInfo.current.id = id
    handleBoundChange(boundInfo.current.source, id)
  }, [])

  const onPlay: BoardsListProps['onPlay'] = useCallback((id) => {
    boundInfo.current.id = id
    void handlePlay(id, boardState.listDetailInfo.list)
  }, [])

  const onCollect: BoardsListProps['onCollect'] = useCallback((id, name) => {
    boundInfo.current.id = id
    void handleCollect(id, name, boundInfo.current.source)
  }, [])

  const onSourceChange: SourceSelectorProps['onSourceChange'] = (source) => {
    boundInfo.current.source = source
    setActiveSource(source)
    void getBoardsList(source).then(list => {
      const id = list[0]?.id ?? ''
      requestAnimationFrame(() => {
        setBoards(list)
        if (id) {
          boundInfo.current.id = id
          handleBoundChange(source, id)
        }
      })
    })
  }

  useEffect(() => {
    void getLeaderboardSetting().then(({ source, boardId }) => {
      boundInfo.current.source = source
      boundInfo.current.id = boardId
      setActiveSource(source)
      setActiveBoardId(boardId)
      sourceSelectorRef.current?.setSource(source)
      void getBoardsList(source).then(list => {
        setBoards(list)
      })
      musicListRef.current?.loadList(source, boardId)
    })
  }, [])

  const browseTabs = useMemo<Array<SegmentItem<BrowseTabId>>>(() => [
    { id: 'nav_songlist', label: t('nav_songlist') },
    { id: 'nav_top', label: t('nav_top') },
  ], [t])

  const activeBoardName = useMemo(() => {
    return boards.find(b => b.id == activeBoardId)?.name ?? ''
  }, [boards, activeBoardId])

  const listHeaderComponent = useMemo(() => (
    <View style={styles.listHeader}>
      <View style={styles.sectionHead}>
        <Text size={fontSize.title2} color={apple.label} style={styles.sectionTitle}>
          {t('apple_browse_top_charts')}
        </Text>
        <IonIcon name="chevron-forward" rawSize={18} color={apple.tertiaryLabel} style={styles.sectionChev} />
      </View>

      <BoardsList
        list={boards}
        activeId={activeBoardId}
        source={activeSource}
        onBoundChange={onBoundChange}
        onPlay={onPlay}
        onCollect={onCollect}
      />

      <View style={styles.boardMetaBar}>
        <View style={styles.boardMetaText}>
          <Text size={fontSize.title2} color={apple.label} style={styles.sectionTitle} numberOfLines={1}>
            {activeBoardName || t('apple_browse_rank_songs')}
          </Text>
          <Text size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>
            {songCount > 0 ? t('apple_library_songs_count', { num: songCount }) : t('apple_browse_rank_songs')}
          </Text>
        </View>
        {activeBoardId ? (
          <View style={styles.boardActions}>
            <GlassCapsuleButton
              icon="play"
              label={t('apple_play')}
              onPress={() => { onPlay(activeBoardId) }}
            />
            <GlassCircleButton
              icon="add"
              size={32}
              onPress={() => { onCollect(activeBoardId, activeBoardName) }}
            />
          </View>
        ) : null}
      </View>
    </View>
  ), [apple, t, boards, activeBoardId, activeSource, activeBoardName, songCount, onBoundChange, onPlay, onCollect])

  return (
    <View style={styles.container}>
      <LargeTitle
        title={isWide ? t('nav_top') : t('apple_tab_browse')}
        right={<SourceSelector ref={sourceSelectorRef} onSourceChange={onSourceChange} />}
      />
      {!isWide ? (
        <SegmentedControl
          items={browseTabs}
          value="nav_top"
          onChange={id => { setNavActiveId(id) }}
          style={styles.seg}
        />
      ) : null}
      <MusicList
        ref={musicListRef}
        ListHeaderComponent={listHeaderComponent}
        onListLoaded={setSongCount}
      />
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
    flexDirection: 'column',
  },
  seg: {
    marginHorizontal: pagePadding,
    marginBottom: spacing.sm,
  },
  listHeader: {
    paddingBottom: spacing.xs,
  },
  sectionHead: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.sm,
    paddingBottom: spacing.sm,
    gap: 2,
  },
  sectionTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.35,
  },
  sectionChev: {
    marginTop: 1,
  },
  boardMetaBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.lg,
    paddingBottom: spacing.sm,
    gap: spacing.md,
  },
  boardMetaText: {
    flex: 1,
    gap: 2,
  },
  boardActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    flexShrink: 0,
  },
})
