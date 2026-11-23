import { memo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'
import { LIST_ITEM_HEIGHT } from '@/config/constant'
import { IonIcon } from '@/components/common/Icon'
import { createStyle, type RowInfo } from '@/utils/tools'
import { useAssertApiSupport } from '@/store/common/hook'
import { scaleSizeH } from '@/utils/pixelRatio'
import Text from '@/components/common/Text'
import { useApple, hairline, spacing } from '@/theme/apple'

export const ITEM_HEIGHT = scaleSizeH(LIST_ITEM_HEIGHT)

const SN_WIDTH = 38

export default memo(({ item, index, activeIndex, onPress, onShowMenu, onLongPress, selectedList, rowInfo, isShowAlbumName, isShowInterval }: {
  item: LX.Music.MusicInfo
  index: number
  activeIndex: number
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

  const singer = `${item.singer}${isShowAlbumName && item.meta.albumName ? ` · ${item.meta.albumName}` : ''}`

  return (
    <View style={{
      ...styles.listItem,
      width: rowInfo.rowWidth,
      height: ITEM_HEIGHT,
      backgroundColor: isSelected ? apple.accentFill : apple.background,
      opacity: isSupported ? 1 : 0.5,
    }}>
      <TouchableOpacity activeOpacity={0.6} style={styles.listItemLeft} onPress={() => { onPress(item, index) }} onLongPress={() => { onLongPress(item, index) }}>
        {
          active
            ? (
                <View style={styles.snIcon}>
                  <IonIcon name="volume-high" rawSize={16} color={apple.accent} />
                </View>
              )
            : <Text style={styles.sn} size={13} color={apple.secondaryLabel}>{index + 1}</Text>
        }
        <View style={styles.itemInfo}>
          <Text size={16} color={active ? apple.accent : apple.label} numberOfLines={1}>{item.name}</Text>
          <View style={styles.listItemSingle}>
            <View style={[styles.tagPill, { backgroundColor: apple.fill }]}>
              <Text size={10} color={apple.secondaryLabel}>{item.source.toUpperCase()}</Text>
            </View>
            <Text style={styles.listItemSingleText} size={13} color={apple.secondaryLabel} numberOfLines={1}>
              {singer}
            </Text>
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
      <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
    </View>
  )
}, (prevProps, nextProps) => {
  return !!(prevProps.item === nextProps.item &&
    prevProps.index === nextProps.index &&
    prevProps.isShowAlbumName === nextProps.isShowAlbumName &&
    prevProps.isShowInterval === nextProps.isShowInterval &&
    prevProps.activeIndex != nextProps.index &&
    nextProps.activeIndex != nextProps.index &&
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
    width: SN_WIDTH,
    textAlign: 'center',
    paddingLeft: 4,
    paddingRight: 4,
  },
  snIcon: {
    width: SN_WIDTH,
    alignItems: 'center',
    justifyContent: 'center',
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
    left: SN_WIDTH,
    right: spacing.lg,
    bottom: 0,
    height: hairline,
  },
})
