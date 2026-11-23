import { useCallback, useRef } from 'react'
import Text from '@/components/common/Text'
import Button, { type BtnType } from '@/components/common/Button'
import { createStyle } from '@/utils/tools'
import { type BoardItem } from '@/store/leaderboard/state'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

export interface ListItemProps {
  item: BoardItem
  index: number
  longPressIndex: number
  activeId: string
  onShowMenu: (id: string, name: string, index: number, position: { x: number, y: number, w: number, h: number }) => void
  onBoundChange: (item: BoardItem) => void
}

/**
 * 榜单胶囊：点击切换榜单，长按弹出菜单（播放 / 收藏）
 */
export default ({ item, activeId, index, longPressIndex, onBoundChange, onShowMenu }: ListItemProps) => {
  const apple = useApple()
  const buttonRef = useRef<BtnType>(null)

  const setPosition = useCallback(() => {
    if (buttonRef.current?.measure) {
      buttonRef.current.measure((fx, fy, width, height, px, py) => {
        onShowMenu(item.id, item.name, index, { x: Math.ceil(px), y: Math.ceil(py), w: Math.ceil(width), h: Math.ceil(height) })
      })
    }
  }, [index, item, onShowMenu])

  const active = activeId == item.id
  const pressed = index == longPressIndex

  return (
    <Button
      ref={buttonRef}
      style={{
        ...styles.chip,
        backgroundColor: active ? apple.accent : pressed ? apple.secondaryFill : apple.fill,
      }}
      onLongPress={setPosition}
      onPress={() => { if (!active) onBoundChange(item) }}
    >
      <Text
        size={fontSize.subhead}
        textBreakStrategy="simple"
        color={active ? apple.onAccent : apple.label}
        style={{ fontWeight: active ? fontWeight.semibold : fontWeight.regular }}
        numberOfLines={1}
      >{item.name}</Text>
    </Button>
  )
}

const styles = createStyle({
  chip: {
    height: 30,
    paddingHorizontal: 14,
    marginRight: spacing.sm,
    borderRadius: radius.pill,
    justifyContent: 'center',
  },
})
