import { memo } from 'react'

import Slider, { type SliderProps as _SliderProps } from '@react-native-community/slider'
import { createStyle } from '@/utils/tools'
import { useApple } from '@/theme/apple'

export type SliderProps = Pick<_SliderProps,
'value'
| 'minimumValue'
| 'maximumValue'
| 'onSlidingStart'
| 'onSlidingComplete'
| 'onValueChange'
| 'step'
>

/**
 * Apple 风格滑块：已滑过部分是强调色，未滑过部分是浅灰填充色
 */
export default memo(({ value, minimumValue, maximumValue, onSlidingStart, onSlidingComplete, onValueChange, step }: SliderProps) => {
  const apple = useApple()

  const handleValueChange = (value: number) => {
    // 修复当value小于等于minimumValue时，首次调用会传入0的问题
    if (onValueChange && minimumValue != null) onValueChange(Math.max(value, minimumValue))
  }

  return (
    <Slider
      value={value}
      style={styles.slider}
      minimumValue={minimumValue}
      maximumValue={maximumValue}
      minimumTrackTintColor={apple.accent}
      maximumTrackTintColor={apple.tertiaryFill}
      thumbTintColor={apple.isDark ? apple.thumb : apple.accent}
      onSlidingStart={onSlidingStart}
      onSlidingComplete={onSlidingComplete}
      onValueChange={handleValueChange}
      step={step}
    />
  )
})

const styles = createStyle({
  slider: {
    flexShrink: 0,
    flexGrow: 1,
    height: 40,
  },
})
