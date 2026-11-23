import { memo, useEffect, useRef } from 'react'
import { View, TouchableOpacity, FlatList, type NativeScrollEvent, type NativeSyntheticEvent, type FlatListProps } from 'react-native'

import { IonIcon } from '@/components/common/Icon'
import { useListFetching, useMyList } from '@/store/list/hook'
import { createStyle } from '@/utils/tools'
import { LIST_IDS, LIST_SCROLL_POSITION_KEY } from '@/config/constant'
import { getListPosition, saveListPosition } from '@/utils/data'
import { setActiveList } from '@/core/list'
import Text from '@/components/common/Text'
import { type Position } from './ListMenu'
import Loading from '@/components/common/Loading'
import { useApple, fontSize, hairline, pagePadding, radius, spacing } from '@/theme/apple'

type FlatListType = FlatListProps<LX.List.MyListInfo>

const ITEM_HEIGHT = 64
const ARTWORK_SIZE = 48

const getListIcon = (id: string) => {
  switch (id) {
    case LIST_IDS.LOVE: return 'heart'
    case LIST_IDS.DEFAULT: return 'musical-notes'
    default: return 'albums'
  }
}

const ListItem = memo(({ item, index, onPress, onShowMenu }: {
  onPress: (item: LX.List.MyListInfo) => void
  index: number
  item: LX.List.MyListInfo
  onShowMenu: (item: LX.List.MyListInfo, index: number, position: { x: number, y: number, w: number, h: number }) => void
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

  const handlePress = () => {
    onPress(item)
  }

  return (
    <View style={{ ...styles.listItem, height: ITEM_HEIGHT }}>
      <TouchableOpacity style={styles.main} activeOpacity={0.6} onPress={handlePress}>
        <View style={{ ...styles.artwork, backgroundColor: apple.fill }}>
          <IonIcon name={getListIcon(item.id)} rawSize={22} color={apple.accent} />
        </View>
        <Text style={styles.name} size={fontSize.callout} numberOfLines={1} color={apple.label}>{item.name}</Text>
        {fetching ? <Loading color={apple.secondaryLabel} style={styles.loading} /> : null}
      </TouchableOpacity>
      <TouchableOpacity onPress={handleShowMenu} ref={moreButtonRef} style={styles.moreBtn} activeOpacity={0.6}>
        <IonIcon name="ellipsis-horizontal" color={apple.secondaryLabel} rawSize={18} />
      </TouchableOpacity>
      <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
    </View>
  )
}, (prevProps, nextProps) => {
  return !!(prevProps.item === nextProps.item &&
    prevProps.index === nextProps.index &&
    prevProps.item.name == nextProps.item.name
  )
})

export default ({ onShowMenu }: {
  onShowMenu: (info: { listInfo: LX.List.MyListInfo, index: number }, position: Position) => void
}) => {
  const flatListRef = useRef<FlatList>(null)
  const allList = useMyList()

  const handleToggleList = (item: LX.List.MyListInfo) => {
    // 切到歌曲页
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

  useEffect(() => {
    void getListPosition(LIST_SCROLL_POSITION_KEY).then((offset) => {
      flatListRef.current?.scrollToOffset({ offset, animated: false })
    })
  }, [])

  const renderItem: FlatListType['renderItem'] = ({ item, index }) => (
    <ListItem
      key={item.id}
      item={item}
      index={index}
      onPress={handleToggleList}
      onShowMenu={showMenu}
    />
  )
  const getkey: FlatListType['keyExtractor'] = item => item.id
  const getItemLayout: FlatListType['getItemLayout'] = (data, index) => {
    return { length: ITEM_HEIGHT, offset: ITEM_HEIGHT * index, index }
  }

  return (
    <FlatList
      ref={flatListRef}
      onScroll={handleScroll}
      style={styles.container}
      data={allList}
      maxToRenderPerBatch={9}
      windowSize={9}
      removeClippedSubviews={true}
      initialNumToRender={14}
      renderItem={renderItem}
      keyExtractor={getkey}
      getItemLayout={getItemLayout}
    />
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  listItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: pagePadding,
  },
  main: {
    flex: 1,
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  artwork: {
    width: ARTWORK_SIZE,
    height: ARTWORK_SIZE,
    borderRadius: radius.small,
    alignItems: 'center',
    justifyContent: 'center',
  },
  name: {
    flex: 1,
    marginLeft: spacing.md,
  },
  loading: {
    marginLeft: spacing.sm,
  },
  moreBtn: {
    height: '100%',
    width: 48,
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    position: 'absolute',
    left: pagePadding + ARTWORK_SIZE + spacing.md,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
