import { TouchableOpacity, View, type ViewStyle } from 'react-native'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight } from '@/theme/apple'
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
 * iOS 风格分段控件：灰色胶囊底 + 选中项白色（深色模式为灰色）滑块
 */
function SegmentedControl<T extends string>({ items, value, onChange, style }: Props<T>) {
  const apple = useApple()

  return (
    <View style={{ ...styles.container, backgroundColor: apple.fill, ...style }}>
      {items.map(item => {
        const active = item.id == value
        return (
          <TouchableOpacity
            key={item.id}
            activeOpacity={0.7}
            style={active
              ? { ...styles.item, ...styles.activeItem, backgroundColor: apple.isDark ? '#636366' : '#FFFFFF' }
              : styles.item}
            onPress={() => { if (!active) onChange(item.id) }}
          >
            <Text
              size={fontSize.footnote}
              color={apple.label}
              style={{ fontWeight: active ? fontWeight.semibold : fontWeight.medium }}
              numberOfLines={1}
            >{item.label}</Text>
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
    height: 32,
    padding: 2,
    borderRadius: 9,
  },
  item: {
    flex: 1,
    borderRadius: 7,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 8,
  },
  activeItem: {
    elevation: 2,
    shadowColor: '#000',
    shadowOpacity: 0.12,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
  },
})
