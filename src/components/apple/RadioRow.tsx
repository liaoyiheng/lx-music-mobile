import { memo } from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { useApple, hairline, fontSize, spacing } from '@/theme/apple'

export interface RadioRowProps {
  label: string
  description?: string
  selected: boolean
  onPress: () => void
  disabled?: boolean
  showSeparator?: boolean
  horizontalPadding?: number
}

/**
 * 标准 iOS 单选行：左侧文字（+可选副标题）+ 右侧 Apple 红勾选 ✓ + 左缩进细分割线
 */
export const RadioRow = memo(({
  label,
  description,
  selected,
  onPress,
  disabled = false,
  showSeparator = true,
  horizontalPadding = 16,
}: RadioRowProps) => {
  const apple = useApple()

  return (
    <View style={disabled ? styles.disabled : undefined}>
      <TouchableOpacity
        activeOpacity={0.65}
        disabled={disabled}
        onPress={onPress}
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
        <View style={styles.checkWrap}>
          {selected ? <IonIcon name="checkmark" rawSize={20} color={apple.accent} /> : null}
        </View>
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
  checkWrap: {
    width: 24,
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  sep: {
    height: hairline,
  },
  disabled: {
    opacity: 0.45,
  },
})
