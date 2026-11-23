import * as React from 'react'
import {
  Animated,
  type GestureResponderEvent,
  View,
  Pressable,
} from 'react-native'

import { createStyle } from '@/utils/tools'
import { scaleSizeW } from '@/utils/pixelRatio'

export interface Props {
  /**
   * Status of checkbox.
   */
  status: 'checked' | 'unchecked' | 'indeterminate'
  /**
   * Whether checkbox is disabled.
   */
  disabled?: boolean
  /**
   * Function to execute on press.
   */
  onPress?: (e: GestureResponderEvent) => void

  size?: number

  /**
   * true：选中时的填充色；false：未选中时的描边色
   */
  tintColors: {
    true: string
    false: string
  }
  /**
   * 勾的颜色，默认白色
   */
  markColor?: string
}

const ANIMATION_DURATION = 180
const PADDING = scaleSizeW(4)

/**
 * iOS 风格的圆形勾选框：未选中是细圆圈，选中是实心圆 + 白色对勾
 */
const Checkbox = ({
  status,
  disabled,
  size = 1,
  onPress,
  tintColors,
  markColor = '#FFFFFF',
  ...rest
}: Props) => {
  const checked = status === 'checked'
  const indeterminate = status === 'indeterminate'
  const active = checked || indeterminate

  const { current: scaleAnim } = React.useRef<Animated.Value>(
    new Animated.Value(active ? 1 : 0),
  )

  const isFirstRendering = React.useRef<boolean>(true)

  React.useEffect(() => {
    if (isFirstRendering.current) {
      isFirstRendering.current = false
      return
    }

    Animated.spring(scaleAnim, {
      toValue: active ? 1 : 0,
      speed: 30,
      bounciness: 8,
      useNativeDriver: true,
    }).start()
  }, [active, scaleAnim])

  const diameter = scaleSizeW(22 * size)
  const markWidth = diameter * 0.26
  const markHeight = diameter * 0.48
  const markStroke = Math.max(1.5, diameter * 0.09)

  return (
    <Pressable
      {...rest}
      onPress={onPress}
      disabled={disabled}
      accessibilityRole="checkbox"
      accessibilityState={{ disabled, checked }}
      accessibilityLiveRegion="polite"
      style={({ pressed }) => ({ ...styles.container, padding: PADDING, marginLeft: -PADDING, opacity: pressed ? 0.6 : 1 })}
    >
      <View
        style={{
          width: diameter,
          height: diameter,
          borderRadius: diameter / 2,
          borderWidth: Math.max(1.2, diameter * 0.07),
          borderColor: tintColors.false,
        }}
      />
      <View style={styles.fillContainer} pointerEvents="none">
        <Animated.View
          style={{
            ...styles.fill,
            width: diameter,
            height: diameter,
            borderRadius: diameter / 2,
            backgroundColor: tintColors.true,
            opacity: scaleAnim,
            transform: [{ scale: scaleAnim }],
          }}
        >
          {
            indeterminate
              ? <View style={{ width: diameter * 0.46, height: markStroke, borderRadius: markStroke, backgroundColor: markColor }} />
              : (
                  <View
                    style={{
                      width: markWidth,
                      height: markHeight,
                      borderRightWidth: markStroke,
                      borderBottomWidth: markStroke,
                      borderColor: markColor,
                      marginTop: -diameter * 0.1,
                      transform: [{ rotate: '45deg' }],
                    }}
                  />
                )
          }
        </Animated.View>
      </View>
    </Pressable>
  )
}

Checkbox.displayName = 'Checkbox'

const styles = createStyle({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  fillContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fill: {
    alignItems: 'center',
    justifyContent: 'center',
  },
})

export default Checkbox
