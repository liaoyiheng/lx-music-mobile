import { memo } from 'react'
import { StyleSheet, View, TouchableOpacity } from 'react-native'
import { createStyle } from '@/utils/tools'
import { type ListInfoItem } from '@/store/songlist/state'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { useApple, fontWeight, radius, spacing } from '@/theme/apple'

/** 卡片左右间距（List 里计算列宽时也用到） */
export const CARD_GAP = 14

/**
 * 去卡片化热门歌单封面墙单项：
 * 1:1 圆角封面（内描边 + 右下角微型半透明播放角标）+ 裸露标题与副标题
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

  const subtitle = item.author
    ? (item.play_count ? `${item.author} · ${item.play_count}` : item.author)
    : (item.play_count ?? '')

  return (
    item.source
      ? (
          <TouchableOpacity activeOpacity={0.72} onPress={handlePress} style={{ ...styles.listItem, width: itemWidth }}>
            <CoverArt
              uri={item.img}
              size={itemWidth}
              borderRadius={radius.medium}
              nativeID={`${NAV_SHEAR_NATIVE_IDS.songlistDetail_pic}_from_${item.id}`}
            >
              {showSource ? <Text style={styles.sourceLabel} size={9.5} color="#FFFFFF">{item.source.toUpperCase()}</Text> : null}
              <View style={styles.playBadge}>
                <IonIcon name="play" rawSize={12} color="#FFFFFF" style={styles.playIcon} />
              </View>
            </CoverArt>
            <Text style={styles.title} size={14.5} color={apple.label} numberOfLines={2}>{item.name}</Text>
            {subtitle ? <Text style={styles.sub} size={12.5} color={apple.secondaryLabel} numberOfLines={1}>{subtitle}</Text> : null}
          </TouchableOpacity>
        )
      : <View style={{ ...styles.listItem, width: itemWidth }} />
  )
})

const styles = createStyle({
  listItem: {
    marginHorizontal: CARD_GAP / 2,
    marginBottom: spacing.lg + 2,
  },
  sourceLabel: {
    position: 'absolute',
    top: 7,
    right: 7,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 5,
    overflow: 'hidden',
    backgroundColor: 'rgba(20, 20, 24, 0.56)',
    fontWeight: fontWeight.bold,
  },
  playBadge: {
    position: 'absolute',
    right: 8,
    bottom: 8,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(24, 24, 28, 0.54)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.28)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  playIcon: {
    marginLeft: 1.5,
  },
  title: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.15,
    lineHeight: 19,
    marginTop: 7,
  },
  sub: {
    marginTop: 2,
  },
})
