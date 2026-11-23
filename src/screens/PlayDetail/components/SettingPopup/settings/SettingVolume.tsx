import { useState } from 'react'
import { View } from 'react-native'
import { fontSize, useApple } from '@/theme/apple'
import Text from '@/components/common/Text'
import { useSettingValue } from '@/store/setting/hook'
import Slider, { type SliderProps } from '@/components/common/Slider'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { setVolume } from '@/plugins/player'
import styles from './style'

const Volume = () => {
  const apple = useApple()
  const volume = Math.trunc(useSettingValue('player.volume') * 100)
  const [sliderSize, setSliderSize] = useState(volume)
  const [isSliding, setSliding] = useState(false)
  const t = useI18n()

  const handleSlidingStart: SliderProps['onSlidingStart'] = () => {
    setSliding(true)
  }
  const handleValueChange: SliderProps['onValueChange'] = value => {
    value = Math.trunc(value)
    setSliderSize(value)
    void setVolume(value / 100)
  }
  const handleSlidingComplete: SliderProps['onSlidingComplete'] = value => {
    setSliding(false)
    value = Math.trunc(value)
    if (volume == value) return
    updateSetting({ 'player.volume': value / 100 })
  }

  return (
    <View style={styles.section}>
      <Text size={fontSize.footnote} color={apple.secondaryLabel} style={styles.sectionTitle}>
        {t('play_detail_setting_volume')}
      </Text>
      <View style={{ ...styles.sliderCard, backgroundColor: apple.secondaryBackground }}>
        <View style={styles.content}>
          <Text size={fontSize.subhead} style={styles.label} color={apple.secondaryLabel}>{isSliding ? sliderSize : volume}</Text>
          <Slider
            minimumValue={0}
            maximumValue={100}
            onSlidingComplete={handleSlidingComplete}
            onValueChange={handleValueChange}
            onSlidingStart={handleSlidingStart}
            step={1}
            value={volume}
          />
        </View>
      </View>
    </View>
  )
}

export default Volume
