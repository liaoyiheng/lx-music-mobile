import { memo, useRef } from 'react'
import { StyleSheet, View, TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { CoverArt, EqualizerBars } from '@/components/apple/LiquidGlass'
import { useI18n } from '@/lang'
import { useApple, fontSize, fontWeight, hairline, pagePadding, radius, size, spacing } from '@/theme/apple'
import { createStyle, type RowInfo } from '@/utils/tools'

export const ITEM_HEIGHT = size.songRow

const useQualityTag = (musicInfo: LX.Music.MusicInfoOnline) => {
  const t = useI18n()
  let text = ''
  if (musicInfo.meta._qualitys.flac24bit) {
    text = t('quality_lossless_24bit')
  } else if (musicInfo.meta._qualitys.flac ?? musicInfo.meta._qualitys.ape) {
    text = t('quality_lossless')
  } else if (musicInfo.meta._qualitys['320k']) {
    text = t('quality_high_quality')
  }

  return text
}

export default memo(({ item, index, showSource, active = false, isPlay = false, onPress, onLongPress, onShowMenu, selectedList, rowInfo, isShowAlbumName, isShowInterval }: {
  item: LX.Music.MusicInfoOnline
  index: number
  showSource?: boolean
  active?: boolean
  isPlay?: boolean
  onPress: (item: LX.Music.MusicInfoOnline, index: number) => void
  onLongPress: (item: LX.Music.MusicInfoOnline, index: number) => void
  onShowMenu: (item: LX.Music.MusicInfoOnline, index: number, position: { x: number, y: number, w: number, h: number }) => void
  selectedList: LX.Music.MusicInfoOnline[]
  rowInfo: RowInfo
  isShowAlbumName: boolean
  isShowInterval: boolean
}) => {
  const apple = useApple()

  const isSelected = selectedList.includes(item)

  const moreButtonRef = useRef<TouchableOpacity>(null)
  const handleShowMenu = () => {
    if (moreButtonRef.current?.measure) {
      moreButtonRef.current.measure((fx, fy, width, height, px, py) => {
        onShowMenu(item, index, { x: Math.ceil(px), y: Math.ceil(py), w: Math.ceil(width), h: Math.ceil(height) })
      })
    }
  }
  const tagText = useQualityTag(item)

  const singer = `${item.singer}${isShowAlbumName && item.meta.albumName ? ` · ${item.meta.albumName}` : ''}`

  return (
    <View
      style={{
        ...styles.listItem,
        width: rowInfo.rowWidth,
        height: ITEM_HEIGHT,
        backgroundColor: isSelected ? apple.accentFill : 'transparent',
      }}
    >
      <TouchableOpacity
        style={styles.listItemLeft}
        onPress={() => { onPress(item, index) }}
        onLongPress={() => { onLongPress(item, index) }}
        activeOpacity={0.6}
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
            <Text style={styles.listItemSingleText} size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>{singer}</Text>
            { tagText ? (
              <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
                <Text size={10} color={apple.secondaryLabel} style={styles.tagText}>{tagText}</Text>
              </View>
            ) : null }
            { showSource ? (
              <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
                <Text size={10} color={apple.secondaryLabel} style={styles.tagText}>{item.source.toUpperCase()}</Text>
              </View>
            ) : null }
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
      <View style={[styles.separator, { backgroundColor: apple.separator }]} pointerEvents="none" />
    </View>
  )
}, (prevProps, nextProps) => {
  return !!(prevProps.item === nextProps.item &&
    prevProps.index === nextProps.index &&
    prevProps.showSource === nextProps.showSource &&
    prevProps.active === nextProps.active &&
    prevProps.isPlay === nextProps.isPlay &&
    prevProps.isShowAlbumName === nextProps.isShowAlbumName &&
    prevProps.isShowInterval === nextProps.isShowInterval &&
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
