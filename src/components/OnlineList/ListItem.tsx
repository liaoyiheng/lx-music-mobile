import { memo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { useApple, hairline, spacing } from '@/theme/apple'
import { scaleSizeH } from '@/utils/pixelRatio'
import { LIST_ITEM_HEIGHT } from '@/config/constant'
import { createStyle, type RowInfo } from '@/utils/tools'

export const ITEM_HEIGHT = scaleSizeH(LIST_ITEM_HEIGHT)

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

export default memo(({ item, index, showSource, onPress, onLongPress, onShowMenu, selectedList, rowInfo, isShowAlbumName, isShowInterval }: {
  item: LX.Music.MusicInfoOnline
  index: number
  showSource?: boolean
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
        backgroundColor: isSelected ? apple.accentFill : apple.background,
      }}
    >
      <TouchableOpacity
        style={styles.listItemLeft}
        onPress={() => { onPress(item, index) }}
        onLongPress={() => { onLongPress(item, index) }}
        activeOpacity={0.6}
      >
        <Text style={styles.sn} size={13} color={apple.secondaryLabel}>{index + 1}</Text>
        <View style={styles.itemInfo}>
          <Text size={16} color={apple.label} numberOfLines={1}>{item.name}</Text>
          <View style={styles.listItemSingle}>
            { tagText ? (
              <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
                <Text size={10} color={apple.secondaryLabel}>{tagText}</Text>
              </View>
            ) : null }
            { showSource ? (
              <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
                <Text size={10} color={apple.secondaryLabel}>{item.source.toUpperCase()}</Text>
              </View>
            ) : null }
            <Text style={styles.listItemSingleText} size={13} color={apple.secondaryLabel} numberOfLines={1}>{singer}</Text>
          </View>
        </View>
        {
          isShowInterval ? (
            <Text style={styles.interval} size={12} color={apple.tertiaryLabel} numberOfLines={1}>{item.interval}</Text>
          ) : null
        }
      </TouchableOpacity>
      <TouchableOpacity onPress={handleShowMenu} ref={moreButtonRef} style={styles.moreButton} activeOpacity={0.6}>
        <IonIcon name="ellipsis-horizontal" rawSize={18} color={apple.secondaryLabel} />
      </TouchableOpacity>
      <View style={[styles.separator, { backgroundColor: apple.separator }]} pointerEvents="none" />
    </View>
  )
}, (prevProps, nextProps) => {
  return !!(prevProps.item === nextProps.item &&
    prevProps.index === nextProps.index &&
    prevProps.isShowAlbumName === nextProps.isShowAlbumName &&
    prevProps.isShowInterval === nextProps.isShowInterval &&
    nextProps.selectedList.includes(nextProps.item) == prevProps.selectedList.includes(nextProps.item)
  )
})

const styles = createStyle({
  listItem: {
    flexDirection: 'row',
    flexWrap: 'nowrap',
    paddingRight: 2,
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
  sn: {
    width: 38,
    textAlign: 'center',
    paddingLeft: 4,
    paddingRight: 4,
  },
  itemInfo: {
    flexGrow: 1,
    flexShrink: 1,
    paddingRight: spacing.xs,
  },
  listItemSingle: {
    marginTop: 2,
    flexDirection: 'row',
    alignItems: 'center',
  },
  tagPill: {
    borderRadius: 4,
    paddingHorizontal: 5,
    paddingVertical: 1,
    marginRight: 5,
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
    paddingLeft: 14,
    paddingRight: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    position: 'absolute',
    left: 38,
    right: spacing.lg,
    bottom: 0,
    height: hairline,
  },
})
