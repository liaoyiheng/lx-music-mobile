import { memo } from 'react'
import { TouchableOpacity } from 'react-native'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface Props {
  icon: string
  label: string
  onPress: () => void
}

/**
 * Apple Music 风格的「播放 / 随机播放」按钮：灰色圆角底、强调色图标与文字
 * 放在横向容器里会平分宽度
 */
export default memo(({ icon, label, onPress }: Props) => {
  const apple = useApple()
  return (
    <TouchableOpacity style={{ ...styles.btn, backgroundColor: apple.fill }} activeOpacity={0.6} onPress={onPress}>
      <Icon name={icon} size={fontSize.subhead} color={apple.accent} />
      <Text style={styles.text} size={fontSize.callout} color={apple.accent} numberOfLines={1}>{label}</Text>
    </TouchableOpacity>
  )
})

const styles = createStyle({
  btn: {
    flex: 1,
    height: size.hitTarget,
    borderRadius: radius.medium,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    marginLeft: spacing.sm,
    fontWeight: fontWeight.semibold,
  },
})
