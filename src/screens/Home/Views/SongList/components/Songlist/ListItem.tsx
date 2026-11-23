import { memo } from 'react'
import { View, TouchableOpacity } from 'react-native'
import { createStyle } from '@/utils/tools'
import { type ListInfoItem } from '@/store/songlist/state'
import Text from '@/components/common/Text'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import Image from '@/components/common/Image'
import { useApple, fontSize, radius, spacing } from '@/theme/apple'

/** 卡片左右间距（List 里计算列宽时也用到） */
export const CARD_GAP = 14

/**
 * Apple Music 风格歌单卡片：方形大圆角封面 + 下方两行标题
 */
export default memo(({ item, index, width, showSource, onPress }: {
  item: ListInfoItem
  index: number
  showSource: boolean
  width: number
  onPress: (item: ListInfoItem, index: number) => void
}) => {
  const apple = useApple()
  const itemWidth = width - CARD_GAP
  const handlePress = () => {
    onPress(item, index)
  }
  return (
    item.source
      ? (
          <TouchableOpacity activeOpacity={0.7} onPress={handlePress} style={{ ...styles.listItem, width: itemWidth }}>
            <View style={{ ...styles.artwork, backgroundColor: apple.fill }}>
              <Image
                url={item.img}
                nativeID={`${NAV_SHEAR_NATIVE_IDS.songlistDetail_pic}_from_${item.id}`}
                style={{ width: itemWidth, height: itemWidth, borderRadius: radius.medium }}
              />
              {showSource ? <Text style={styles.sourceLabel} size={9} color="#fff">{item.source.toUpperCase()}</Text> : null}
            </View>
            <Text style={styles.title} size={fontSize.footnote} color={apple.label} numberOfLines={2}>{item.name}</Text>
            {item.author ? <Text size={fontSize.caption1} color={apple.secondaryLabel} numberOfLines={1}>{item.author}</Text> : null}
          </TouchableOpacity>
        )
      : <View style={{ ...styles.listItem, width: itemWidth }} />
  )
})

const styles = createStyle({
  listItem: {
    marginHorizontal: CARD_GAP / 2,
    marginBottom: spacing.xl,
  },
  artwork: {
    borderRadius: radius.medium,
    overflow: 'hidden',
    marginBottom: spacing.sm - 2,
  },
  sourceLabel: {
    position: 'absolute',
    top: 6,
    right: 6,
    paddingHorizontal: 5,
    paddingVertical: 1,
    borderRadius: 4,
    overflow: 'hidden',
    backgroundColor: 'rgba(0, 0, 0, 0.45)',
  },
  title: {
    fontWeight: '500',
    marginBottom: 2,
  },
})
