import { memo, useEffect, useRef, useState } from 'react'
import {
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  View,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
} from 'react-native'

import { IonIcon } from '@/components/common/Icon'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { useListFetching, useMyList } from '@/store/list/hook'
import { createStyle } from '@/utils/tools'
import { LIST_IDS, LIST_SCROLL_POSITION_KEY } from '@/config/constant'
import { getListPosition, saveListPosition } from '@/utils/data'
import { getListMusics, setActiveList } from '@/core/list'
import Text from '@/components/common/Text'
import { type Position } from './ListMenu'
import Loading from '@/components/common/Loading'
import { useI18n } from '@/lang'
import { useHorizontalMode, useLayout } from '@/utils/hooks'
import { useApple, fontSize, fontWeight, hairline, pagePadding, radius, size, spacing } from '@/theme/apple'

interface ListSummary {
  count: number
  coverUrl: string | null
}

const GRID_GAP = 14

const getListIcon = (id: string, outline = true) => {
  switch (id) {
    case LIST_IDS.LOVE: return outline ? 'heart-outline' : 'heart'
    case LIST_IDS.DEFAULT: return outline ? 'musical-notes-outline' : 'musical-notes'
    default: return outline ? 'albums-outline' : 'albums'
  }
}

const FALLBACK_PALETTES = [
  { bgLight: '#FFE9EE', bgDark: '#3A1922', accent: '#FA243C' },
  { bgLight: '#E8F0FF', bgDark: '#1A243B', accent: '#2D6BFF' },
  { bgLight: '#F3E9FF', bgDark: '#2A1D3D', accent: '#8E44EC' },
  { bgLight: '#E6F7EF', bgDark: '#162F24', accent: '#25B869' },
  { bgLight: '#FFF1E2', bgDark: '#382515', accent: '#F27D16' },
]

const getFallbackPalette = (id: string, index: number, isDark: boolean) => {
  if (id === LIST_IDS.LOVE) return isDark ? { bg: '#3C1823', tint: '#FF375F' } : { bg: '#FFE8EE', tint: '#FA243C' }
  if (id === LIST_IDS.DEFAULT) return isDark ? { bg: '#2A1E3A', tint: '#B87BFF' } : { bg: '#F2EAFF', tint: '#8C44EC' }
  const pal = FALLBACK_PALETTES[index % FALLBACK_PALETTES.length]
  return { bg: isDark ? pal.bgDark : pal.bgLight, tint: pal.accent }
}

const CategoryRow = memo(({ item, index, count, onPress, onShowMenu }: {
  item: LX.List.MyListInfo
  index: number
  count: number
  onPress: (item: LX.List.MyListInfo) => void
  onShowMenu: (item: LX.List.MyListInfo, index: number, position: Position) => void
}) => {
  const apple = useApple()
  const moreButtonRef = useRef<TouchableOpacity>(null)
  const fetching = useListFetching(item.id)

  const handleShowMenu = () => {
    if (moreButtonRef.current?.measure) {
      moreButtonRef.current.measure((fx, fy, width, height, px, py) => {
        onShowMenu(item, index, { x: Math.ceil(px), y: Math.ceil(py), w: Math.ceil(width), h: Math.ceil(height) })
      })
    }
  }

  return (
    <View style={styles.catRow}>
      <TouchableOpacity
        style={styles.catMain}
        activeOpacity={0.6}
        onPress={() => { onPress(item) }}
        onLongPress={handleShowMenu}
      >
        <View style={styles.catIconWrap}>
          <IonIcon name={getListIcon(item.id, true)} rawSize={22} color={apple.accent} />
        </View>
        <Text
          style={styles.catLabel}
          size={fontSize.title3}
          numberOfLines={1}
          color={apple.label}
        >
          {item.name}
        </Text>
        <View style={styles.catRight}>
          {fetching ? <Loading color={apple.secondaryLabel} style={styles.loading} /> : null}
          <Text size={15} color={apple.secondaryLabel} style={styles.catCount}>
            {count}
          </Text>
          <IonIcon name="chevron-forward" rawSize={15} color={apple.tertiaryLabel} />
        </View>
      </TouchableOpacity>
      <TouchableOpacity
        onPress={handleShowMenu}
        ref={moreButtonRef}
        style={styles.moreBtn}
        activeOpacity={0.6}
      >
        <IonIcon name="ellipsis-horizontal" color={apple.secondaryLabel} rawSize={17} />
      </TouchableOpacity>
      <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
    </View>
  )
})

const PlaylistCoverCard = memo(({ item, index, width, summary, songsLabel, onPress, onShowMenu }: {
  item: LX.List.MyListInfo
  index: number
  width: number
  summary?: ListSummary
  songsLabel: string
  onPress: (item: LX.List.MyListInfo) => void
  onShowMenu: (item: LX.List.MyListInfo, index: number, position: Position) => void
}) => {
  const apple = useApple()
  const cardRef = useRef<View>(null)
  const coverUrl = summary?.coverUrl ?? null
  const palette = getFallbackPalette(item.id, index, apple.isDark)

  const handleLongPress = () => {
    if (cardRef.current?.measure) {
      cardRef.current.measure((fx, fy, w, h, px, py) => {
        onShowMenu(item, index, { x: Math.ceil(px + w / 2), y: Math.ceil(py + 12), w: 24, h: 24 })
      })
    }
  }

  return (
    <View ref={cardRef} style={{ width }}>
      <TouchableOpacity
        activeOpacity={0.75}
        onPress={() => { onPress(item) }}
        onLongPress={handleLongPress}
      >
        <CoverArt
          uri={coverUrl}
          size={width}
          borderRadius={radius.medium}
        >
          {!coverUrl ? (
            <View style={[styles.coverFallback, { backgroundColor: palette.bg }]}>
              <View style={[styles.coverFallbackGlow, { backgroundColor: apple.isDark ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.55)' }]} />
              <IonIcon name={getListIcon(item.id, false)} rawSize={Math.max(28, Math.round(width * 0.3))} color={palette.tint} />
            </View>
          ) : null}
        </CoverArt>
        <Text
          style={styles.cardTitle}
          size={14.5}
          color={apple.label}
          numberOfLines={2}
        >
          {item.name}
        </Text>
        <Text
          style={styles.cardSub}
          size={fontSize.footnote}
          color={apple.secondaryLabel}
          numberOfLines={1}
        >
          {songsLabel}
        </Text>
      </TouchableOpacity>
    </View>
  )
})

export default ({ onShowMenu }: {
  onShowMenu: (info: { listInfo: LX.List.MyListInfo, index: number }, position: Position) => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const isWide = useHorizontalMode()
  const scrollViewRef = useRef<ScrollView>(null)
  const allList = useMyList()
  const [summaries, setSummaries] = useState<Record<string, ListSummary>>({})
  const { onLayout, width: containerWidth } = useLayout()

  useEffect(() => {
    let cancelled = false
    const loadSummaries = async(ids?: string[]) => {
      const targetLists = ids ? allList.filter(l => ids.includes(l.id)) : allList
      if (!targetLists.length) return
      const entries = await Promise.all(
        targetLists.map(async(list) => {
          const musics = await getListMusics(list.id)
          const coverUrl = musics.find(m => m.meta?.picUrl)?.meta.picUrl ?? null
          return [list.id, { count: musics.length, coverUrl }] as const
        }),
      )
      if (cancelled) return
      setSummaries((prev) => {
        const next = { ...prev }
        for (const [id, val] of entries) next[id] = val
        return next
      })
    }

    void loadSummaries()

    const handleMusicUpdate = (ids: string[]) => {
      void loadSummaries(ids)
    }
    global.app_event.on('myListMusicUpdate', handleMusicUpdate)
    return () => {
      cancelled = true
      global.app_event.off('myListMusicUpdate', handleMusicUpdate)
    }
  }, [allList])

  useEffect(() => {
    void getListPosition(LIST_SCROLL_POSITION_KEY).then((offset) => {
      scrollViewRef.current?.scrollTo({ y: offset, animated: false })
    })
  }, [])

  const handleToggleList = (item: LX.List.MyListInfo) => {
    global.app_event.changeLoveListVisible(false)
    requestAnimationFrame(() => {
      setActiveList(item.id)
    })
  }

  const handleScroll = ({ nativeEvent }: NativeSyntheticEvent<NativeScrollEvent>) => {
    void saveListPosition(LIST_SCROLL_POSITION_KEY, nativeEvent.contentOffset.y)
  }

  const showMenu = (listInfo: LX.List.MyListInfo, index: number, position: Position) => {
    onShowMenu({ listInfo, index }, position)
  }

  const innerWidth = containerWidth > 0 ? containerWidth - pagePadding * 2 : 300
  const cardWidth = Math.max(100, Math.floor((innerWidth - GRID_GAP) / 2))

  return (
    <ScrollView
      ref={scrollViewRef}
      onScroll={handleScroll}
      scrollEventThrottle={16}
      style={styles.container}
      contentContainerStyle={{
        paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance,
      }}
      onLayout={onLayout}
    >
      {/* Apple Music 分类入口列表 */}
      <View style={styles.catList}>
        {allList.map((item, index) => (
          <CategoryRow
            key={item.id}
            item={item}
            index={index}
            count={summaries[item.id]?.count ?? 0}
            onPress={handleToggleList}
            onShowMenu={showMenu}
          />
        ))}
      </View>

      {/* 「我的歌单」去卡片化双列封面墙 */}
      <View style={styles.sectionHead}>
        <Text size={fontSize.title2} color={apple.label} style={styles.sectionTitle}>
          {t('apple_library_recent')}
        </Text>
      </View>

      <View style={styles.grid}>
        {allList.map((item, index) => (
          <PlaylistCoverCard
            key={item.id}
            item={item}
            index={index}
            width={cardWidth}
            summary={summaries[item.id]}
            songsLabel={t('apple_library_songs_count', { num: summaries[item.id]?.count ?? 0 })}
            onPress={handleToggleList}
            onShowMenu={showMenu}
          />
        ))}
      </View>
    </ScrollView>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  catList: {
    marginTop: 2,
  },
  catRow: {
    height: 54,
    paddingLeft: pagePadding,
    paddingRight: 4,
    flexDirection: 'row',
    alignItems: 'center',
    position: 'relative',
  },
  catMain: {
    flex: 1,
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  catIconWrap: {
    width: 22,
    height: 22,
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  catLabel: {
    flex: 1,
    marginLeft: 16,
    fontWeight: fontWeight.regular,
    letterSpacing: -0.25,
  },
  catRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  catCount: {
    fontVariant: ['tabular-nums'],
  },
  loading: {
    marginRight: spacing.xs,
  },
  moreBtn: {
    height: '100%',
    paddingLeft: 10,
    paddingRight: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    position: 'absolute',
    left: 58,
    right: 0,
    bottom: 0,
    height: hairline,
  },
  sectionHead: {
    paddingHorizontal: pagePadding,
    paddingTop: 22,
    paddingBottom: 12,
  },
  sectionTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.35,
  },
  grid: {
    paddingHorizontal: pagePadding,
    flexDirection: 'row',
    flexWrap: 'wrap',
    columnGap: GRID_GAP,
    rowGap: 16,
  },
  coverFallback: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  coverFallbackGlow: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: '52%',
  },
  cardTitle: {
    marginTop: 7,
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.15,
    lineHeight: 18,
  },
  cardSub: {
    marginTop: 2,
    lineHeight: 16,
  },
})
