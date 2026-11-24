import { useRef, useState, useMemo, forwardRef, useImperativeHandle, memo, type ReactNode } from 'react'
import { FlatList, View, RefreshControl, ScrollView, StyleSheet, TouchableOpacity, type FlatListProps } from 'react-native'

import ListItem, { CARD_GAP } from './ListItem'
import { type ListInfoItem } from '@/store/songlist/state'
import { useHorizontalMode, useLayout } from '@/utils/hooks'
import { useApple, fontSize, fontWeight, pagePadding, radius, size, spacing } from '@/theme/apple'
import { useI18n } from '@/lang'
import { scaleSizeW } from '@/utils/pixelRatio'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'

type FlatListType = FlatListProps<ListInfoItem>

const MIN_WIDTH = scaleSizeW(140)
const LIST_PADDING = pagePadding - CARD_GAP / 2
const EDITORIAL_GAP = 14
const EDITORIAL_COUNT = 4

export interface ListProps {
  onRefresh: () => void
  onLoadMore: () => void
  onOpenDetail: (item: ListInfoItem, index: number) => void
  enableEditorial?: boolean
  filterComponent?: ReactNode
  listHeaderTitle?: string
}
export type Status = 'loading' | 'refreshing' | 'end' | 'error' | 'idle'

export interface ListType {
  setList: (list: ListInfoItem[], showSource?: boolean) => void
  setStatus: (val: Status) => void
}

const SectionHead = memo(({ title }: { title: string }) => {
  const apple = useApple()
  return (
    <View style={styles.sectionHead}>
      <Text size={fontSize.title2} color={apple.label} style={styles.sectionTitle}>{title}</Text>
      <IonIcon name="chevron-forward" rawSize={18} color={apple.tertiaryLabel} style={styles.sectionChev} />
    </View>
  )
})

const EditorialCard = memo(({
  item,
  index,
  cardWidth,
  posterHeight,
  isLast,
  onPress,
}: {
  item: ListInfoItem
  index: number
  cardWidth: number
  posterHeight: number
  isLast: boolean
  onPress: (item: ListInfoItem, index: number) => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const subtitle = item.author
    ? (item.play_count ? `${item.author} · ${item.play_count}` : item.author)
    : (item.play_count ?? '')

  return (
    <TouchableOpacity
      activeOpacity={0.82}
      onPress={() => { onPress(item, index) }}
      style={{
        width: cardWidth,
        marginRight: isLast ? 0 : EDITORIAL_GAP,
      }}
    >
      <Text size={fontSize.caption2} color={apple.secondaryLabel} style={styles.edEyebrow} numberOfLines={1}>
        {`${t('apple_browse_featured')} · ${item.source.toUpperCase()}`}
      </Text>
      <Text size={19} color={apple.label} style={styles.edTitle} numberOfLines={1}>{item.name}</Text>
      <Text size={fontSize.subhead} color={apple.secondaryLabel} style={styles.edSub} numberOfLines={1}>
        {subtitle || ' '}
      </Text>
      <CoverArt
        uri={item.img}
        borderRadius={radius.alert}
        nativeID={`${NAV_SHEAR_NATIVE_IDS.songlistDetail_pic}_from_${item.id}`}
        style={{ width: cardWidth, height: posterHeight, ...styles.edPoster }}
      >
        <View pointerEvents="none" style={styles.edScrimWrap}>
          <View style={styles.edScrimTop} />
          <View style={styles.edScrimMid} />
          <View style={styles.edScrimBottom} />
        </View>
        <View style={styles.edFooter}>
          <View style={styles.edBadgeRow}>
            {item.play_count ? (
              <View style={styles.edBadge}>
                <IonIcon name="play" rawSize={10} color="#FFFFFF" />
                <Text size={10.5} color="#FFFFFF" style={styles.edBadgeText}>{item.play_count}</Text>
              </View>
            ) : null}
            {item.total ? (
              <View style={styles.edBadge}>
                <Text size={10.5} color="#FFFFFF" style={styles.edBadgeText}>{t('apple_library_songs_count', { num: item.total })}</Text>
              </View>
            ) : null}
          </View>
          {item.desc ? (
            <Text size={12.5} color="rgba(255,255,255,0.88)" style={styles.edDesc} numberOfLines={2}>
              {item.desc}
            </Text>
          ) : null}
        </View>
      </CoverArt>
    </TouchableOpacity>
  )
})

export default forwardRef<ListType, ListProps>(({
  onRefresh,
  onLoadMore,
  onOpenDetail,
  enableEditorial = false,
  filterComponent,
  listHeaderTitle,
}, ref) => {
  const flatListRef = useRef<FlatList>(null)
  const [currentList, setList] = useState<ListInfoItem[]>([])
  const [showSource, setShowSource] = useState(false)
  const [status, setStatus] = useState<Status>('idle')
  const { onLayout, width } = useLayout()
  const isWide = useHorizontalMode()
  const apple = useApple()
  const t = useI18n()

  useImperativeHandle(ref, () => ({
    setList(list, showSource = false) {
      setList(list)
      setShowSource(showSource)
    },
    setStatus(val) {
      setStatus(val)
    },
  }))

  const handleLoadMore = () => {
    if (status != 'idle') return
    onLoadMore()
  }

  const featuredList = useMemo(() => {
    return enableEditorial && currentList.length > 0 ? currentList.slice(0, EDITORIAL_COUNT) : []
  }, [enableEditorial, currentList])

  const gridSourceList = useMemo(() => {
    return enableEditorial && currentList.length > 0 ? currentList.slice(EDITORIAL_COUNT) : currentList
  }, [enableEditorial, currentList])

  const rowInfo = useMemo(() => {
    const available = width - LIST_PADDING * 2
    const num = Math.min(Math.max(Math.floor(available / (MIN_WIDTH + CARD_GAP)), 2), 10)
    return {
      num,
      width: available / num,
    }
  }, [width])

  const edCardWidth = useMemo(() => {
    if (!width) return 320
    return isWide ? 326 : Math.min(Math.max(width - pagePadding * 2 - 24, 260), 336)
  }, [width, isWide])
  const edPosterHeight = Math.round((edCardWidth * 10) / 16)

  const list = useMemo(() => {
    const items = [...gridSourceList]
    let whiteItemNum = items.length % rowInfo.num
    if (whiteItemNum > 0) whiteItemNum = rowInfo.num - whiteItemNum
    for (let i = 0; i < whiteItemNum; i++) {
      items.push({
        id: `white__${i}`,
        play_count: '',
        author: '',
        name: '',
        img: '',
        desc: '',
        // @ts-expect-error filler placeholder
        source: '',
      })
    }
    return items
  }, [gridSourceList, rowInfo.num])

  const renderItem: FlatListType['renderItem'] = ({ item, index }) => (
    <ListItem
      item={item}
      index={enableEditorial ? index + EDITORIAL_COUNT : index}
      width={rowInfo.width}
      showSource={showSource}
      onPress={onOpenDetail}
    />
  )
  const getkey: FlatListType['keyExtractor'] = item => item.id

  const refreshControl = useMemo(() => (
    <RefreshControl
      colors={[apple.accent]}
      refreshing={status == 'refreshing'}
      onRefresh={onRefresh}
    />
  ), [status, onRefresh, apple])

  const headerComponent = useMemo(() => {
    if (!featuredList.length && !filterComponent && !(listHeaderTitle && gridSourceList.length)) return null
    return (
      <View>
        {featuredList.length > 0 ? (
          <View>
            <SectionHead title={t('apple_browse_featured')} />
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              snapToInterval={edCardWidth + EDITORIAL_GAP}
              decelerationRate="fast"
              contentContainerStyle={styles.edScroller}
            >
              {featuredList.map((item, idx) => (
                <EditorialCard
                  key={item.id}
                  item={item}
                  index={idx}
                  cardWidth={edCardWidth}
                  posterHeight={edPosterHeight}
                  isLast={idx == featuredList.length - 1}
                  onPress={onOpenDetail}
                />
              ))}
            </ScrollView>
          </View>
        ) : null}

        {filterComponent ?? null}

        {gridSourceList.length > 0 && enableEditorial ? (
          <SectionHead title={t('apple_browse_hot_playlists')} />
        ) : null}

        {!enableEditorial && listHeaderTitle && gridSourceList.length > 0 ? (
          <SectionHead title={listHeaderTitle} />
        ) : null}
      </View>
    )
  }, [featuredList, filterComponent, listHeaderTitle, gridSourceList.length, enableEditorial, t, edCardWidth, edPosterHeight, onOpenDetail])

  const footerComponent = useMemo(() => {
    let label: FooterLabel
    switch (status) {
      case 'refreshing': return null
      case 'loading':
        label = 'list_loading'
        break
      case 'end':
        label = 'list_end'
        break
      case 'error':
        label = 'list_error'
        break
      case 'idle':
        label = null
        break
    }
    return (
      <View style={{ width: '100%' }}>
        <Footer label={label} onLoadMore={onLoadMore} />
      </View>
    )
  }, [onLoadMore, status])

  const bottomPadding = isWide ? size.miniPlayerClearance : size.dockClearance

  return (
    <View style={styles.container} onLayout={onLayout}>
      {
        width == 0
          ? null
          : (
              <FlatList
                key={String(rowInfo.num)}
                ref={flatListRef}
                style={styles.list}
                contentContainerStyle={{ paddingBottom: bottomPadding }}
                columnWrapperStyle={{ justifyContent: 'flex-start', paddingHorizontal: LIST_PADDING }}
                numColumns={rowInfo.num}
                data={list}
                maxToRenderPerBatch={4}
                windowSize={8}
                removeClippedSubviews={true}
                renderItem={renderItem}
                keyExtractor={getkey}
                onEndReachedThreshold={0.6}
                onEndReached={handleLoadMore}
                refreshControl={refreshControl}
                ListHeaderComponent={headerComponent}
                ListFooterComponent={footerComponent}
              />
            )
      }
    </View>
  )
})

type FooterLabel = 'list_loading' | 'list_end' | 'list_error' | null
const Footer = ({ label, onLoadMore }: {
  label: FooterLabel
  onLoadMore: () => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const handlePress = () => {
    if (label != 'list_error') return
    onLoadMore()
  }
  return (
    label
      ? (
          <View>
            <Text onPress={handlePress} style={styles.footer} color={apple.secondaryLabel}>{t(label)}</Text>
          </View>
        )
      : null
  )
}

const styles = createStyle({
  container: {
    flex: 1,
    overflow: 'hidden',
  },
  list: {
    flex: 1,
  },
  sectionHead: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.md + 2,
    paddingBottom: spacing.sm + 2,
    gap: 2,
  },
  sectionTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.35,
  },
  sectionChev: {
    marginTop: 1,
  },
  edScroller: {
    paddingHorizontal: pagePadding,
    paddingTop: 2,
    paddingBottom: spacing.xs,
  },
  edEyebrow: {
    fontWeight: fontWeight.bold,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginBottom: 2,
  },
  edTitle: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.3,
  },
  edSub: {
    marginBottom: spacing.sm,
  },
  edPoster: {
    shadowColor: '#000',
    shadowOpacity: 0.14,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 5,
  },
  edScrimWrap: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: 108,
  },
  edScrimTop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.06)',
  },
  edScrimMid: {
    flex: 1.2,
    backgroundColor: 'rgba(0, 0, 0, 0.36)',
  },
  edScrimBottom: {
    flex: 1.6,
    backgroundColor: 'rgba(0, 0, 0, 0.70)',
  },
  edFooter: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    paddingHorizontal: 14,
    paddingBottom: 12,
    gap: 6,
  },
  edBadgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  edBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.22)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.34)',
  },
  edBadgeText: {
    fontWeight: fontWeight.bold,
    letterSpacing: 0.2,
  },
  edDesc: {
    lineHeight: 17,
  },
  footer: {
    textAlign: 'center',
    padding: 12,
  },
})
