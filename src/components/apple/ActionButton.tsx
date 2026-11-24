import { memo } from 'react'
import { StyleSheet, TouchableOpacity } from 'react-native'
import { Icon, IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface Props {
  icon: string
  /** 若提供 ionIcon，优先直接渲染 Ionicons */
  ionIcon?: string
  label: string
  onPress: () => void
}

/**
 * Apple Music iOS 26 风格「播放 / 随机播放」等宽操作按钮：
 * 46dp 圆角 12 玻璃填充底 + 苹果红图标与粗体文字
 */
export default memo(({ icon, ionIcon, label, onPress }: Props) => {
  const apple = useApple()
  return (
    <TouchableOpacity
      style={{
        ...styles.btn,
        backgroundColor: apple.isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(120, 120, 128, 0.12)',
        borderColor: apple.isDark ? 'rgba(255, 255, 255, 0.14)' : 'rgba(255, 255, 255, 0.72)',
      }}
      activeOpacity={0.68}
      onPress={onPress}
    >
      {ionIcon
        ? <IonIcon name={ionIcon} rawSize={17} color={apple.accent} />
        : <Icon name={icon} rawSize={16} color={apple.accent} />}
      <Text style={styles.text} size={fontSize.callout} color={apple.accent} numberOfLines={1}>{label}</Text>
    </TouchableOpacity>
  )
})

const styles = createStyle({
  btn: {
    flex: 1,
    height: size.hitTarget,
    borderRadius: radius.large,
    borderWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    marginLeft: spacing.sm,
    fontWeight: fontWeight.semibold,
  },
})
