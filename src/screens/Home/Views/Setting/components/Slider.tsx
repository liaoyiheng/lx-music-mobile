
import { memo } from 'react'

import Slider, { type SliderProps } from '@react-native-community/slider'
import { useApple } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export type {
  SliderProps,
}

export default memo(({ value, minimumValue, maximumValue, onSlidingStart, onSlidingComplete, onValueChange, step }: SliderProps) => {
  const apple = useApple()

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
      onValueChange={onValueChange}
      step={step}
    />
  )
})

const styles = createStyle({
  slider: {
    flexShrink: 0,
    flexGrow: 1,
    maxWidth: 300,
    height: 40,
    marginTop: -6,
  },
})
