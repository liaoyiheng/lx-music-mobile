import { StyleSheet, TouchableOpacity, View, type ViewStyle } from 'react-native'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight, radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export interface SegmentItem<T extends string> {
  id: T
  label: string
}

interface Props<T extends string> {
  items: Array<SegmentItem<T>>
  value: T
  onChange: (id: T) => void
  style?: ViewStyle
}

/**
 * iOS 26 液态玻璃胶囊分段控件：圆润全胶囊玻璃轨道 + 亮面折射滑块
 */
function SegmentedControl<T extends string>({ items, value, onChange, style }: Props<T>) {
  const apple = useApple()

  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor: apple.isDark ? 'rgba(120, 120, 128, 0.24)' : 'rgba(120, 120, 128, 0.13)',
          borderColor: apple.isDark ? 'rgba(255, 255, 255, 0.14)' : 'rgba(255, 255, 255, 0.85)',
        },
        style,
      ]}
    >
      {items.map(item => {
        const active = item.id == value
        return (
          <TouchableOpacity
            key={item.id}
            activeOpacity={0.72}
            style={[
              styles.item,
              active && {
                ...styles.activeItem,
                backgroundColor: apple.glassPillBg,
                borderColor: apple.glassPillBorder,
                shadowOpacity: apple.isDark ? 0.32 : 0.10,
              },
            ]}
            onPress={() => { if (!active) onChange(item.id) }}
          >
            <Text
              size={fontSize.footnote}
              color={active ? apple.label : apple.secondaryLabel}
              style={{ fontWeight: active ? fontWeight.semibold : fontWeight.medium }}
              numberOfLines={1}
            >
              {item.label}
            </Text>
          </TouchableOpacity>
        )
      })}
    </View>
  )
}

export default SegmentedControl

const styles = createStyle({
  container: {
    flexDirection: 'row',
    height: 36,
    padding: 3,
    borderRadius: radius.pill,
    borderWidth: StyleSheet.hairlineWidth,
  },
  item: {
    flex: 1,
    borderRadius: radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 10,
  },
  activeItem: {
    borderWidth: StyleSheet.hairlineWidth,
    elevation: 3,
    shadowColor: '#000',
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
  },
})
