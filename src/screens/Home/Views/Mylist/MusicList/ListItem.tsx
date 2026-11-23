import { memo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'
import { LIST_ITEM_HEIGHT } from '@/config/constant'
import { Icon } from '@/components/common/Icon'
import { createStyle, type RowInfo } from '@/utils/tools'
import { useAssertApiSupport } from '@/store/common/hook'
import { scaleSizeH } from '@/utils/pixelRatio'
import Text from '@/components/common/Text'
import Badge from '@/components/common/Badge'
import { useApple, fontSize, hairline } from '@/theme/apple'

export const ITEM_HEIGHT = scaleSizeH(LIST_ITEM_HEIGHT)

const SN_WIDTH = 44

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
      backgroundColor: isSelected ? apple.accentFill : 'transparent',
      opacity: isSupported ? 1 : 0.5,
    }}>
      <TouchableOpacity activeOpacity={0.6} style={styles.listItemLeft} onPress={() => { onPress(item, index) }} onLongPress={() => { onLongPress(item, index) }}>
        {
          active
            ? <Icon style={styles.sn} name="play-outline" size={13} color={apple.accent} />
            : <Text style={styles.sn} size={fontSize.subhead} color={apple.secondaryLabel}>{index + 1}</Text>
        }
        <View style={styles.itemInfo}>
          <Text size={16} color={active ? apple.accent : apple.label} numberOfLines={1}>{item.name}</Text>
          <View style={styles.listItemSingle}>
            <Badge>{item.source.toUpperCase()}</Badge>
            <Text style={styles.listItemSingleText} size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>
              {singer}
            </Text>
          </View>
        </View>
        {
          isShowInterval ? (
            <Text size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>{item.interval}</Text>
          ) : null
        }
      </TouchableOpacity>
      <TouchableOpacity onPress={handleShowMenu} ref={moreButtonRef} style={styles.moreButton}>
        <Icon name="dots-vertical" color={apple.secondaryLabel} size={14} />
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
    alignItems: 'center',
  },
  listItemLeft: {
    flex: 1,
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  sn: {
    width: SN_WIDTH,
    textAlign: 'center',
  },
  itemInfo: {
    flexGrow: 1,
    flexShrink: 1,
    paddingRight: 6,
  },
  listItemSingle: {
    paddingTop: 3,
    flexDirection: 'row',
  },
  listItemSingleText: {
    flexGrow: 0,
    flexShrink: 1,
  },
  moreButton: {
    height: '100%',
    width: 48,
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    position: 'absolute',
    left: SN_WIDTH,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
