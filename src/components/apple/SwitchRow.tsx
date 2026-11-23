import { memo } from 'react'
import { View, Text, Switch, TouchableOpacity, StyleSheet, Platform } from 'react-native'
import { useApple, hairline, fontSize, spacing } from '@/theme/apple'

export interface SwitchRowProps {
  label: string
  description?: string
  value: boolean
  onValueChange: (val: boolean) => void
  disabled?: boolean
  showSeparator?: boolean
  /** 行内边距：grouped 卡片内用 16，紧凑弹层可传 0 或 12 */
  horizontalPadding?: number
}

/**
 * 标准 iOS 设置开关行：左侧文字（+可选副标题）+ 右侧原生绿色滑动 Switch + 左缩进细分割线
 */
export const SwitchRow = memo(({
  label,
  description,
  value,
  onValueChange,
  disabled = false,
  showSeparator = true,
  horizontalPadding = 16,
}: SwitchRowProps) => {
  const apple = useApple()

  return (
    <View style={disabled ? styles.disabled : undefined}>
      <TouchableOpacity
        activeOpacity={0.7}
        disabled={disabled}
        onPress={() => { onValueChange(!value) }}
        style={[styles.row, { paddingHorizontal: horizontalPadding }]}
      >
        <View style={styles.textCol}>
          <Text style={[styles.label, { color: apple.label }]} numberOfLines={2}>
            {label}
          </Text>
          {description ? (
            <Text style={[styles.desc, { color: apple.secondaryLabel }]} numberOfLines={3}>
              {description}
            </Text>
          ) : null}
        </View>
        <Switch
          value={value}
          onValueChange={onValueChange}
          disabled={disabled}
          trackColor={{ false: apple.switchOff, true: apple.switchOn }}
          thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
          ios_backgroundColor={apple.switchOff}
        />
      </TouchableOpacity>
      {showSeparator ? (
        <View style={[styles.sep, { backgroundColor: apple.separator, marginLeft: horizontalPadding }]} />
      ) : null}
    </View>
  )
})

const styles = StyleSheet.create({
  row: {
    minHeight: 46,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
  },
  textCol: {
    flex: 1,
  },
  label: {
    fontSize: fontSize.callout,
    lineHeight: 22,
  },
  desc: {
    fontSize: fontSize.footnote,
    lineHeight: 18,
    marginTop: 2,
  },
  sep: {
    height: hairline,
  },
  disabled: {
    opacity: 0.45,
  },
})
