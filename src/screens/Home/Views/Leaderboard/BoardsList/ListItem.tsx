import { memo, useCallback, useRef } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { createStyle } from '@/utils/tools'
import { type BoardItem } from '@/store/leaderboard/state'
import { useApple, fontWeight, radius } from '@/theme/apple'

export const BOARD_COVER_SIZE = 116
export const BOARD_CARD_GAP = 12
export const BOARD_ITEM_STEP = BOARD_COVER_SIZE + 6 + BOARD_CARD_GAP

interface BoardPalette {
  base: string
  glow1: string
  glow2: string
  icon: string
}

const BOARD_PALETTES: readonly BoardPalette[] = [
  { base: '#D91B3A', glow1: 'rgba(255, 138, 101, 0.55)', glow2: 'rgba(74, 14, 78, 0.52)', icon: 'flame' },
  { base: '#3B28CC', glow1: 'rgba(130, 144, 255, 0.55)', glow2: 'rgba(18, 12, 62, 0.52)', icon: 'trending-up' },
  { base: '#0B7A75', glow1: 'rgba(72, 219, 198, 0.48)', glow2: 'rgba(7, 46, 51, 0.56)', icon: 'pulse' },
  { base: '#E05A1F', glow1: 'rgba(255, 209, 102, 0.52)', glow2: 'rgba(92, 22, 16, 0.48)', icon: 'trophy' },
  { base: '#7B2CBF', glow1: 'rgba(224, 122, 255, 0.48)', glow2: 'rgba(36, 12, 68, 0.54)', icon: 'musical-notes' },
  { base: '#0E7490', glow1: 'rgba(103, 232, 249, 0.46)', glow2: 'rgba(12, 45, 72, 0.56)', icon: 'disc' },
  { base: '#BE185D', glow1: 'rgba(251, 146, 198, 0.48)', glow2: 'rgba(76, 5, 42, 0.52)', icon: 'sparkles' },
  { base: '#1E3A8A', glow1: 'rgba(96, 165, 250, 0.48)', glow2: 'rgba(15, 23, 42, 0.58)', icon: 'radio' },
]

export interface ListItemProps {
  item: BoardItem
  index: number
  sourceLabel?: string
  longPressIndex: number
  activeId: string
  onShowMenu: (id: string, name: string, index: number, position: { x: number, y: number, w: number, h: number }) => void
  onBoundChange: (item: BoardItem) => void
}

/**
 * 榜单封面视觉卡：116×116 圆角 14dp 多层配色梯度封面 + 选中高亮环 + 底部榜名
 * 保留点击切榜与长按弹出（播放 / 收藏）菜单
 */
export default memo(({
  item,
  sourceLabel,
  activeId,
  index,
  longPressIndex,
  onBoundChange,
  onShowMenu,
}: ListItemProps) => {
  const apple = useApple()
  const cardRef = useRef<View>(null)

  const setPosition = useCallback(() => {
    cardRef.current?.measure((fx, fy, width, height, px, py) => {
      onShowMenu(item.id, item.name, index, {
        x: Math.ceil(px),
        y: Math.ceil(py),
        w: Math.ceil(width),
        h: Math.ceil(height),
      })
    })
  }, [index, item, onShowMenu])

  const active = activeId == item.id
  const pressed = index == longPressIndex
  const palette = BOARD_PALETTES[index % BOARD_PALETTES.length]
  const badgeText = (sourceLabel ?? item.id.split('__')[0] ?? 'TOP').toUpperCase()

  return (
    <View
      ref={cardRef}
      collapsable={false}
      style={[
        styles.wrap,
        active && styles.wrapActive,
      ]}
    >
      <TouchableOpacity
        activeOpacity={0.78}
        onLongPress={setPosition}
        onPress={() => { if (!active) onBoundChange(item) }}
      >
        <View
          style={[
            styles.ring,
            {
              borderColor: active ? apple.accent : (pressed ? apple.secondaryLabel : 'transparent'),
            },
          ]}
        >
          <View style={[styles.cover, { backgroundColor: palette.base }]}>
            <View pointerEvents="none" style={[styles.glowTop, { backgroundColor: palette.glow1 }]} />
            <View pointerEvents="none" style={[styles.glowBottom, { backgroundColor: palette.glow2 }]} />
            <IonIcon
              name={palette.icon}
              rawSize={52}
              color="rgba(255, 255, 255, 0.22)"
              style={styles.decoIcon}
            />
            <View style={styles.badge}>
              <Text size={9.5} color="#FFFFFF" style={styles.badgeText}>{badgeText}</Text>
            </View>
            <Text size={13.5} color="#FFFFFF" style={styles.coverTitle} numberOfLines={2}>
              {item.name}
            </Text>
            <View pointerEvents="none" style={[styles.coverRing, { borderColor: apple.coverRing }]} />
          </View>
        </View>
        <Text
          size={13}
          color={active ? apple.accent : apple.label}
          style={styles.title}
          numberOfLines={1}
        >
          {item.name}
        </Text>
      </TouchableOpacity>
    </View>
  )
})

const styles = createStyle({
  wrap: {
    width: BOARD_COVER_SIZE + 6,
    marginRight: BOARD_CARD_GAP,
    transform: [{ scale: 0.97 }],
  },
  wrapActive: {
    transform: [{ scale: 1 }],
  },
  ring: {
    width: BOARD_COVER_SIZE + 6,
    height: BOARD_COVER_SIZE + 6,
    borderRadius: radius.alert + 3,
    borderWidth: 2,
    padding: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cover: {
    width: BOARD_COVER_SIZE,
    height: BOARD_COVER_SIZE,
    borderRadius: radius.alert,
    overflow: 'hidden',
    padding: 10,
    justifyContent: 'space-between',
    shadowColor: '#000',
    shadowOpacity: 0.16,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 6 },
    elevation: 4,
  },
  glowTop: {
    position: 'absolute',
    top: -32,
    right: -28,
    width: 96,
    height: 96,
    borderRadius: 48,
  },
  glowBottom: {
    position: 'absolute',
    bottom: -36,
    left: -24,
    width: 112,
    height: 112,
    borderRadius: 56,
  },
  decoIcon: {
    position: 'absolute',
    right: -4,
    top: 8,
  },
  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 5,
    backgroundColor: 'rgba(255, 255, 255, 0.22)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.34)',
  },
  badgeText: {
    fontWeight: fontWeight.bold,
    letterSpacing: 0.4,
  },
  coverTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.2,
    lineHeight: 17,
    textShadowColor: 'rgba(0, 0, 0, 0.28)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 4,
  },
  coverRing: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    borderRadius: radius.alert,
    borderWidth: StyleSheet.hairlineWidth,
  },
  title: {
    marginTop: 6,
    paddingHorizontal: 2,
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.1,
  },
})
