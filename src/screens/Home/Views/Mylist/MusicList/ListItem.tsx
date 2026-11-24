import { memo, useRef } from 'react'
import { StyleSheet, View, TouchableOpacity } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { CoverArt, EqualizerBars } from '@/components/apple/LiquidGlass'
import { createStyle, type RowInfo } from '@/utils/tools'
import { useAssertApiSupport } from '@/store/common/hook'
import { useI18n } from '@/lang'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight, hairline, pagePadding, radius, size, spacing } from '@/theme/apple'

export const ITEM_HEIGHT = size.songRow

const useQualityTag = (item: LX.Music.MusicInfo) => {
  const t = useI18n()
  if (item.source == 'local') {
    return item.meta.ext ? item.meta.ext.toUpperCase() : ''
  }
  const qualitys = item.meta._qualitys
  if (qualitys.flac24bit) return t('quality_lossless_24bit')
  if (qualitys.flac ?? qualitys.ape) return t('quality_lossless')
  if (qualitys['320k']) return t('quality_high_quality')
  return ''
}

export default memo(({ item, index, activeIndex, isPlay = false, onPress, onShowMenu, onLongPress, selectedList, rowInfo, isShowAlbumName, isShowInterval }: {
  item: LX.Music.MusicInfo
  index: number
  activeIndex: number
  isPlay?: boolean
  onPress: (item: LX.Music.MusicInfo, index: number) => void
  onLongPress: (item: LX.Music.MusicInfo, index: number) => void
  onShowMenu: (item: LX.Music.MusicInfo, index: number, position: { x: number, y: number, w: number, h: number }) => void
  selectedList: LX.Music.MusicInfo[]
  rowInfo: RowInfo
  isShowAlbumName: boolean
  isShowInterval: boolean
}) => {
  const apple = useApple()

  const isSelected = selectedList.includes(item)
  const isSupported = useAssertApiSupport(item.source)
  const moreButtonRef = useRef<TouchableOpacity>(null)
  const handleShowMenu = () => {
    if (moreButtonRef.current?.measure) {
      moreButtonRef.current.measure((fx, fy, width, height, px, py) => {
        onShowMenu(item, index, { x: Math.ceil(px), y: Math.ceil(py), w: Math.ceil(width), h: Math.ceil(height) })
      })
    }
  }
  const active = activeIndex == index
  const qualityTag = useQualityTag(item)

  const singer = `${item.singer}${isShowAlbumName && item.meta.albumName ? ` · ${item.meta.albumName}` : ''}`

  return (
    <View style={{
      ...styles.listItem,
      width: rowInfo.rowWidth,
      height: ITEM_HEIGHT,
      backgroundColor: isSelected ? apple.accentFill : 'transparent',
      opacity: isSupported ? 1 : 0.5,
    }}>
      <TouchableOpacity
        activeOpacity={0.6}
        style={styles.listItemLeft}
        onPress={() => { onPress(item, index) }}
        onLongPress={() => { onLongPress(item, index) }}
      >
        <CoverArt
          uri={item.meta.picUrl}
          size={size.songArtwork}
          borderRadius={radius.xs}
        >
          {active ? (
            <View style={styles.eqOverlay}>
              <EqualizerBars playing={isPlay} size={14} />
            </View>
          ) : null}
        </CoverArt>
        <View style={styles.itemInfo}>
          <Text
            size={fontSize.callout}
            color={active ? apple.accent : apple.label}
            style={{ fontWeight: active ? fontWeight.semibold : fontWeight.medium, letterSpacing: -0.2 }}
            numberOfLines={1}
          >
            {item.name}
          </Text>
          <View style={styles.listItemSingle}>
            <Text style={styles.listItemSingleText} size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>
              {singer}
            </Text>
            {qualityTag ? (
              <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
                <Text size={10} color={apple.secondaryLabel} style={styles.tagText}>{qualityTag}</Text>
              </View>
            ) : null}
          </View>
        </View>
        {
          isShowInterval && item.interval ? (
            <Text style={styles.interval} size={12} color={apple.tertiaryLabel} numberOfLines={1}>{item.interval}</Text>
          ) : null
        }
      </TouchableOpacity>
      <TouchableOpacity onPress={handleShowMenu} ref={moreButtonRef} style={styles.moreButton} activeOpacity={0.6}>
        <IonIcon name="ellipsis-horizontal" rawSize={18} color={apple.tertiaryLabel} />
      </TouchableOpacity>
      <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
    </View>
  )
}, (prevProps, nextProps) => {
  const prevActive = prevProps.activeIndex == prevProps.index
  const nextActive = nextProps.activeIndex == nextProps.index
  return !!(prevProps.item === nextProps.item &&
    prevProps.index === nextProps.index &&
    prevProps.isShowAlbumName === nextProps.isShowAlbumName &&
    prevProps.isShowInterval === nextProps.isShowInterval &&
    !prevActive &&
    !nextActive &&
    nextProps.selectedList.includes(nextProps.item) == prevProps.selectedList.includes(nextProps.item)
  )
})

const styles = createStyle({
  listItem: {
    flexDirection: 'row',
    flexWrap: 'nowrap',
    paddingLeft: pagePadding,
    paddingRight: 4,
    alignItems: 'center',
  },
  listItemLeft: {
    flex: 1,
    flexGrow: 1,
    flexShrink: 1,
    flexDirection: 'row',
    alignItems: 'center',
    height: '100%',
  },
  eqOverlay: {
    ...StyleSheet.absoluteFillObject,
    borderRadius: radius.xs,
    backgroundColor: 'rgba(0, 0, 0, 0.36)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  itemInfo: {
    flexGrow: 1,
    flexShrink: 1,
    marginLeft: spacing.md,
    paddingRight: spacing.xs,
  },
  listItemSingle: {
    marginTop: 2,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  tagPill: {
    borderRadius: 4,
    paddingHorizontal: 5,
    paddingVertical: 1,
    flexShrink: 0,
  },
  tagText: {
    fontWeight: fontWeight.semibold,
  },
  listItemSingleText: {
    flexGrow: 0,
    flexShrink: 1,
  },
  interval: {
    marginLeft: spacing.sm,
  },
  moreButton: {
    height: '100%',
    paddingLeft: 10,
    paddingRight: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    position: 'absolute',
    left: pagePadding + size.songArtwork + spacing.md,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
